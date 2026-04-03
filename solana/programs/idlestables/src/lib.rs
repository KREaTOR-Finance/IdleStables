use anchor_lang::prelude::*;

// ============================================================
// IdleStables (MVP)
// - 2 launch tracks: Vedauwoo Park (6F) + Canoli Downs (1.5M)
// - Scheduled races resolve :00/:30; entry closes T-60s
// - 12 entrants per normal race
// - Mega Cup featured race: 18 entrants, 2x entry fee
// - Entry modes:
//   - Manual: fee = F
//   - Auto (circuit fill / house fill): fee = 0.35F
// - Fee split (bps): 55/25/10/10
// - Payout top3: 60/25/15 (paid from manual pool if any manual entrants, else auto pool)
// - Traits (racing+breeding only): Speed, Stamina, Focus, Temperament
// - Training: +4 once per trait per horse
// ============================================================

// NOTE: Replace with real program id after `anchor keys list` / deploy.
declare_id!("11111111111111111111111111111111");

const BPS_DENOM: u64 = 10_000;

// Entrant kind for race slots
const KIND_EMPTY: u8 = 0;
const KIND_MANUAL: u8 = 1;
const KIND_AUTO: u8 = 2;
const KIND_HOUSE: u8 = 3;

#[program]
pub mod idlestables {
    use super::*;

    // ---------------- CONFIG / TRACKS ----------------

    pub fn initialize(ctx: Context<Initialize>, args: InitializeArgs) -> Result<()> {
        let cfg = &mut ctx.accounts.config;
        cfg.admin = args.admin;
        cfg.treasury = args.treasury;
        cfg.purse_mint = args.purse_mint;
        cfg.bump = ctx.bumps.config;
        Ok(())
    }

    pub fn create_track(ctx: Context<CreateTrack>, args: CreateTrackArgs) -> Result<()> {
        let t = &mut ctx.accounts.track;
        t.config = ctx.accounts.config.key();
        t.track_id = args.track_id;
        // args.name should be a 32-byte, zero-padded UTF-8 string
        t.name = args.name;
        t.distance = args.distance;
        t.cadence_minutes = args.cadence_minutes;
        t.base_entry_fee = args.base_entry_fee;

        // locked MVP split
        t.purse_vault_bps = 5500;
        t.track_vault_bps = 2500;
        t.treasury_bps = 1000;
        t.burn_bps = 1000;

        t.bump = ctx.bumps.track;
        Ok(())
    }

    // ---------------- RACE DAY / SCHEDULING ----------------

    pub fn open_race(ctx: Context<OpenRace>, args: OpenRaceArgs) -> Result<()> {
        let race = &mut ctx.accounts.race;
        let track = &ctx.accounts.track;

        race.track = track.key();
        race.scheduled_ts = args.scheduled_ts;
        race.entry_closes_ts = args.scheduled_ts.saturating_sub(60);
        race.field_size = if args.is_mega_cup { 18 } else { 12 };
        race.is_mega_cup = args.is_mega_cup;
        race.status = RaceStatus::Open;

        race.manual_count = 0;
        race.auto_count = 0;
        race.entrant_count = 0;
        race.entrants = [Pubkey::default(); 18];
        race.entrant_kinds = [KIND_EMPTY; 18];

        race.purse_pool_manual = 0;
        race.purse_pool_auto = 0;
        race.track_accrued = 0;
        race.treasury_accrued = 0;
        race.burn_accrued = 0;

        race.resolved = false;
        race.top3 = [Pubkey::default(); 3];
        race.top3_prizes = [0u64; 3];

        race.bump = ctx.bumps.race;
        Ok(())
    }

    /// Manual entry: app/user submits a horse.
    /// MVP: only accounting + entrant recording (no SPL token transfer yet).
    pub fn enter_race_manual(
        ctx: Context<EnterRaceManual>,
        _args: EnterRaceManualArgs,
    ) -> Result<()> {
        let clock = Clock::get()?;
        let track = &ctx.accounts.track;
        let race = &mut ctx.accounts.race;
        let horse = &ctx.accounts.horse;

        require!(race.status == RaceStatus::Open, IdleErr::RaceNotOpen);
        require!(
            clock.unix_timestamp < race.entry_closes_ts,
            IdleErr::EntryClosed
        );
        require!(race.entrant_count < race.field_size, IdleErr::RaceFull);
        require!(
            horse.owner == ctx.accounts.signer.key(),
            IdleErr::NotHorseOwner
        );

        // Fee = base F, or 2F for mega cup
        let fee = track
            .base_entry_fee
            .saturating_mul(if race.is_mega_cup { 2 } else { 1 });

        // add entrant
        let idx = race.entrant_count as usize;
        race.entrants[idx] = ctx.accounts.horse.key();
        race.entrant_kinds[idx] = KIND_MANUAL;
        race.entrant_count = race.entrant_count.saturating_add(1);
        race.manual_count = race.manual_count.saturating_add(1);

        // split accounting
        let (purse, track_cut, treasury, burn) = split_fee(fee, track);
        race.purse_pool_manual = race.purse_pool_manual.saturating_add(purse);
        race.track_accrued = race.track_accrued.saturating_add(track_cut);
        race.treasury_accrued = race.treasury_accrued.saturating_add(treasury);
        race.burn_accrued = race.burn_accrued.saturating_add(burn);

        Ok(())
    }

    /// Close and fill: called at entry close. Steward supplies auto-fill horses.
    /// Auto-fill fee = 0.35F (or 0.70F in mega).
    pub fn close_and_fill(ctx: Context<CloseAndFill>, args: CloseAndFillArgs) -> Result<()> {
        let clock = Clock::get()?;
        let track = &ctx.accounts.track;
        let race = &mut ctx.accounts.race;

        require!(race.status == RaceStatus::Open, IdleErr::RaceNotOpen);
        require!(
            clock.unix_timestamp >= race.entry_closes_ts,
            IdleErr::TooEarlyToClose
        );
        require!(!race.resolved, IdleErr::AlreadyResolved);

        race.status = RaceStatus::Closed;

        let base_fee = track
            .base_entry_fee
            .saturating_mul(if race.is_mega_cup { 2 } else { 1 });

        // auto fee = 0.35x
        let auto_fee = (base_fee.saturating_mul(35)).saturating_div(100);

        // Fill remaining slots: circuit autos first
        for pk in args.auto_horses.iter() {
            if race.entrant_count >= race.field_size {
                break;
            }
            add_auto_entrant(race, *pk, KIND_AUTO, auto_fee, track)?;
        }

        // Then fill with house horses
        for pk in args.house_horses.iter() {
            if race.entrant_count >= race.field_size {
                break;
            }
            add_auto_entrant(race, *pk, KIND_HOUSE, auto_fee, track)?;
        }

        Ok(())
    }

    /// Resolve race: deterministic placeholder (no VRF yet).
    /// Provide remaining accounts containing Horse accounts (in any order).
    pub fn resolve_race<'a>(
        ctx: Context<'a, ResolveRace<'a>>,
        _args: ResolveRaceArgs,
    ) -> Result<()> {
        let clock = Clock::get()?;
        let track = &ctx.accounts.track;
        let race = &mut ctx.accounts.race;

        require!(!race.resolved, IdleErr::AlreadyResolved);
        require!(
            clock.unix_timestamp >= race.scheduled_ts,
            IdleErr::TooEarlyToResolve
        );
        require!(
            race.entrant_count == race.field_size,
            IdleErr::RaceNotFilled
        );

        // Build a map of horse pubkey -> Horse data from remaining accounts
        // (MVP simplicity; not optimized)
        use std::collections::BTreeMap;
        let mut map: BTreeMap<Pubkey, Horse> = BTreeMap::new();
        for acc in ctx.remaining_accounts.iter() {
            // Only parse accounts that deserialize as Horse
            if let Ok(h) = Account::<Horse>::try_from(acc) {
                map.insert(acc.key(), h.into_inner());
            }
        }

        let mut scores: Vec<(usize, i64)> = Vec::with_capacity(race.field_size as usize);
        for i in 0..(race.field_size as usize) {
            let horse_pk = race.entrants[i];
            require!(horse_pk != Pubkey::default(), IdleErr::BadEntrant);
            let horse = map.get(&horse_pk).ok_or(IdleErr::MissingHorseAccount)?;
            let s = compute_score(&race.key(), track.distance, horse);
            scores.push((i, s));
        }

        // Sort by score desc
        scores.sort_by(|a, b| b.1.cmp(&a.1));

        let top1 = race.entrants[scores[0].0];
        let top2 = race.entrants[scores[1].0];
        let top3 = race.entrants[scores[2].0];

        // Determine payout pool
        let pool = if race.manual_count > 0 {
            race.purse_pool_manual
        } else {
            race.purse_pool_auto
        };

        let p1 = pool.saturating_mul(60).saturating_div(100);
        let p2 = pool.saturating_mul(25).saturating_div(100);
        let p3 = pool.saturating_sub(p1).saturating_sub(p2); // remainder

        race.top3 = [top1, top2, top3];
        race.top3_prizes = [p1, p2, p3];
        race.resolved = true;
        race.status = RaceStatus::Resolved;

        Ok(())
    }

    // ---------------- HORSES / BREEDING / TRAINING ----------------

    pub fn mint_horse(ctx: Context<MintHorse>, args: MintHorseArgs) -> Result<()> {
        let h = &mut ctx.accounts.horse;
        h.owner = args.owner;
        h.seed = args.seed;
        h.tier = args.tier;
        h.speed = args.speed;
        h.stamina = args.stamina;
        h.focus = args.focus;
        h.temperament = args.temperament;
        h.speed_trained = false;
        h.stamina_trained = false;
        h.focus_trained = false;
        h.temperament_trained = false;
        h.bump = ctx.bumps.horse;
        Ok(())
    }

    pub fn train_horse(ctx: Context<TrainHorse>, args: TrainHorseArgs) -> Result<()> {
        let h = &mut ctx.accounts.horse;
        require!(h.owner == ctx.accounts.owner.key(), IdleErr::NotHorseOwner);

        match args.trait_type {
            TraitType::Speed => {
                require!(!h.speed_trained, IdleErr::TraitAlreadyTrained);
                h.speed = h.speed.saturating_add(4);
                h.speed_trained = true;
            }
            TraitType::Stamina => {
                require!(!h.stamina_trained, IdleErr::TraitAlreadyTrained);
                h.stamina = h.stamina.saturating_add(4);
                h.stamina_trained = true;
            }
            TraitType::Focus => {
                require!(!h.focus_trained, IdleErr::TraitAlreadyTrained);
                h.focus = h.focus.saturating_add(4);
                h.focus_trained = true;
            }
            TraitType::Temperament => {
                require!(!h.temperament_trained, IdleErr::TraitAlreadyTrained);
                h.temperament = h.temperament.saturating_add(4);
                h.temperament_trained = true;
            }
        }

        // NOTE: trainer fee routing not implemented in MVP stub.
        Ok(())
    }

    pub fn breed(_ctx: Context<Breed>, _args: BreedArgs) -> Result<()> {
        // TODO: charge breeding fee, apply decay, derive child seed from parents.
        Ok(())
    }
}

// ======================== Helper logic =======================

fn split_fee(fee: u64, track: &Track) -> (u64, u64, u64, u64) {
    let purse = fee.saturating_mul(track.purse_vault_bps as u64) / BPS_DENOM;
    let track_cut = fee.saturating_mul(track.track_vault_bps as u64) / BPS_DENOM;
    let treasury = fee.saturating_mul(track.treasury_bps as u64) / BPS_DENOM;
    let burn = fee
        .saturating_sub(purse)
        .saturating_sub(track_cut)
        .saturating_sub(treasury);
    (purse, track_cut, treasury, burn)
}

fn add_auto_entrant(
    race: &mut Race,
    horse: Pubkey,
    kind: u8,
    fee: u64,
    track: &Track,
) -> Result<()> {
    require!(race.entrant_count < race.field_size, IdleErr::RaceFull);

    let idx = race.entrant_count as usize;
    race.entrants[idx] = horse;
    race.entrant_kinds[idx] = kind;
    race.entrant_count = race.entrant_count.saturating_add(1);
    race.auto_count = race.auto_count.saturating_add(1);

    let (purse, track_cut, treasury, burn) = split_fee(fee, track);
    race.purse_pool_auto = race.purse_pool_auto.saturating_add(purse);
    race.track_accrued = race.track_accrued.saturating_add(track_cut);
    race.treasury_accrued = race.treasury_accrued.saturating_add(treasury);
    race.burn_accrued = race.burn_accrued.saturating_add(burn);

    Ok(())
}

/// Deterministic score placeholder.
/// VRF will replace the "random" component later.
fn compute_score(race_key: &Pubkey, distance: TrackDistance, h: &Horse) -> i64 {
    // distance weights
    let (w_speed, w_stamina) = match distance {
        TrackDistance::Sprint6F => (70i64, 30i64),
        TrackDistance::Distance1_5M => (30i64, 70i64),
    };

    // mild EV edge
    let edge = (w_speed * h.speed as i64 + w_stamina * h.stamina as i64) / 10; // keep small

    // deterministic pseudo-random term based on race + seed
    let mut data: Vec<u8> = Vec::with_capacity(32 + 8);
    data.extend_from_slice(race_key.as_ref());
    data.extend_from_slice(&h.seed.to_le_bytes());
    let hash = anchor_lang::solana_program::hash::hash(&data);
    let n = u64::from_le_bytes(hash.to_bytes()[0..8].try_into().unwrap());

    // Map to signed range roughly [-1000..1000]
    let mut r = (n % 2001) as i64 - 1000;

    // Focus reduces variance (high focus => smaller random swings)
    // focus 0..255 -> scale 100..50
    let focus = h.focus as i64;
    let scale = 100 - (focus / 5); // up to -51
    r = (r * scale) / 100;

    // Temperament anti-choke: if very negative, push upward
    let temp = h.temperament as i64;
    if r < -700 {
        r += temp / 2;
    }

    edge + r
}

// ======================== Accounts ==========================

#[account]
pub struct Config {
    pub admin: Pubkey,
    pub treasury: Pubkey,
    pub purse_mint: Pubkey,
    pub bump: u8,
}

#[account]
pub struct Track {
    pub config: Pubkey,
    pub track_id: u32,
    pub name: [u8; 32],
    pub distance: TrackDistance,
    pub cadence_minutes: u16,
    pub base_entry_fee: u64,

    // split bps (sum 10000)
    pub purse_vault_bps: u16, // 5500
    pub track_vault_bps: u16, // 2500
    pub treasury_bps: u16,    // 1000
    pub burn_bps: u16,        // 1000

    pub bump: u8,
}

#[account]
pub struct Race {
    pub track: Pubkey,
    pub scheduled_ts: i64,
    pub entry_closes_ts: i64,
    pub field_size: u8, // 12 or 18
    pub is_mega_cup: bool,
    pub status: RaceStatus,

    pub manual_count: u8,
    pub auto_count: u8,
    pub entrant_count: u8,

    pub entrants: [Pubkey; 18],
    pub entrant_kinds: [u8; 18],

    // pools (denominated in PURSE smallest units)
    pub purse_pool_manual: u64,
    pub purse_pool_auto: u64,

    // accrued cuts (not yet claimable in MVP stub)
    pub track_accrued: u64,
    pub treasury_accrued: u64,
    pub burn_accrued: u64,

    pub resolved: bool,
    pub top3: [Pubkey; 3],
    pub top3_prizes: [u64; 3],

    pub bump: u8,
}

#[account]
pub struct Horse {
    pub owner: Pubkey,
    pub seed: u64,
    pub tier: HorseTier,

    // traits
    pub speed: u8,
    pub stamina: u8,
    pub focus: u8,
    pub temperament: u8,

    // one-time training flags
    pub speed_trained: bool,
    pub stamina_trained: bool,
    pub focus_trained: bool,
    pub temperament_trained: bool,

    pub bump: u8,
}

// ======================== Contexts ==========================

#[derive(Accounts)]
pub struct Initialize<'info> {
    #[account(init, payer = payer, space = 8 + 32 + 32 + 32 + 1, seeds=[b"cfg"], bump)]
    pub config: Account<'info, Config>,
    #[account(mut)]
    pub payer: Signer<'info>,
    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
#[instruction(args: CreateTrackArgs)]
pub struct CreateTrack<'info> {
    #[account(mut, seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(
        init,
        payer=payer,
        space=8 + 32 + 4 + 32 + 1 + 2 + 8 + 2+2+2+2 + 1,
        seeds=[b"track".as_ref(), &args.track_id.to_le_bytes()],
        bump
    )]
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub payer: Signer<'info>,
    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
#[instruction(args: OpenRaceArgs)]
pub struct OpenRace<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(seeds=[b"track".as_ref(), &track.track_id.to_le_bytes()], bump=track.bump)]
    pub track: Account<'info, Track>,
    #[account(
        init,
        payer=payer,
        space=8 + 900,
        seeds=[b"race", track.key().as_ref(), &args.scheduled_ts.to_le_bytes(), &[args.is_mega_cup as u8]],
        bump
    )]
    pub race: Account<'info, Race>,
    #[account(mut)]
    pub payer: Signer<'info>,
    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
pub struct EnterRaceManual<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(seeds=[b"track".as_ref(), &track.track_id.to_le_bytes()], bump=track.bump)]
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub race: Account<'info, Race>,
    pub horse: Account<'info, Horse>,
    pub signer: Signer<'info>,
}

#[derive(Accounts)]
pub struct CloseAndFill<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(seeds=[b"track".as_ref(), &track.track_id.to_le_bytes()], bump=track.bump)]
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub race: Account<'info, Race>,
    pub signer: Signer<'info>,
}

#[derive(Accounts)]
pub struct ResolveRace<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(seeds=[b"track".as_ref(), &track.track_id.to_le_bytes()], bump=track.bump)]
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub race: Account<'info, Race>,
    pub signer: Signer<'info>,
}

#[derive(Accounts)]
#[instruction(args: MintHorseArgs)]
pub struct MintHorse<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(
        init,
        payer=payer,
        space=8 + 32 + 8 + 1 + 4 + 4 + 1,
        seeds=[b"horse", args.owner.as_ref(), &args.seed.to_le_bytes()],
        bump
    )]
    pub horse: Account<'info, Horse>,
    #[account(mut)]
    pub payer: Signer<'info>,
    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
pub struct TrainHorse<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(mut)]
    pub horse: Account<'info, Horse>,
    pub owner: Signer<'info>,
}

#[derive(Accounts)]
pub struct Breed<'info> {
    pub config: Account<'info, Config>,
    #[account(mut)]
    pub sire: Account<'info, Horse>,
    #[account(mut)]
    pub dam: Account<'info, Horse>,
    pub owner: Signer<'info>,
}

// ========================= Args =============================

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct InitializeArgs {
    pub admin: Pubkey,
    pub treasury: Pubkey,
    pub purse_mint: Pubkey,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct CreateTrackArgs {
    pub track_id: u32,
    pub name: [u8; 32],
    pub distance: TrackDistance,
    pub cadence_minutes: u16,
    pub base_entry_fee: u64,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct OpenRaceArgs {
    pub scheduled_ts: i64,
    pub is_mega_cup: bool,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct EnterRaceManualArgs {}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct CloseAndFillArgs {
    pub auto_horses: Vec<Pubkey>,
    pub house_horses: Vec<Pubkey>,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct ResolveRaceArgs {}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct MintHorseArgs {
    pub owner: Pubkey,
    pub seed: u64,
    pub tier: HorseTier,
    pub speed: u8,
    pub stamina: u8,
    pub focus: u8,
    pub temperament: u8,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct TrainHorseArgs {
    pub trait_type: TraitType,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone)]
pub struct BreedArgs {}

// ========================= Enums ============================

#[derive(AnchorSerialize, AnchorDeserialize, Clone, Copy, PartialEq, Eq)]
pub enum RaceStatus {
    Open,
    Closed,
    Resolved,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone, Copy, PartialEq, Eq)]
pub enum TrackDistance {
    Sprint6F,
    Distance1_5M,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone, Copy, PartialEq, Eq)]
pub enum HorseTier {
    Yearling,
    Racehorse,
    Graded,
    Legendary,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone, Copy, PartialEq, Eq)]
pub enum TraitType {
    Speed,
    Stamina,
    Focus,
    Temperament,
}

// ========================= Errors ===========================

#[error_code]
pub enum IdleErr {
    #[msg("Race is not open")]
    RaceNotOpen,
    #[msg("Entry window is closed")]
    EntryClosed,
    #[msg("Race is full")]
    RaceFull,
    #[msg("Too early to close")]
    TooEarlyToClose,
    #[msg("Too early to resolve")]
    TooEarlyToResolve,
    #[msg("Race not filled")]
    RaceNotFilled,
    #[msg("Missing horse account in remaining accounts")]
    MissingHorseAccount,
    #[msg("Bad entrant")]
    BadEntrant,
    #[msg("Already resolved")]
    AlreadyResolved,
    #[msg("Not horse owner")]
    NotHorseOwner,
    #[msg("Trait already trained for this horse")]
    TraitAlreadyTrained,
}
