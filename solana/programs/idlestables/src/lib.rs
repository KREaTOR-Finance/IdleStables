use anchor_lang::prelude::*;

// ============================================================
// IdleStables (MVP)
// - Tracks (2 launch tracks, treasury-owned track shares initially)
// - Scheduled races (:00/:30), entry closes T-60
// - Manual vs Auto(Circuit) entry fees
// - Mega Cup once per track race day: 18 entrants, 2x fee
// - Fee split: 55/25/10/10
// - Payout: top3 60/25/15
// - 4 traits: speed, stamina, focus, temperament (racing+breeding only)
// - Season lock horses can be auto-entered (backend steward initially)
// ============================================================

declare_id!("IdLeStAbLeS1111111111111111111111111111111");

#[program]
pub mod idlestables {
    use super::*;

    // ---------- CONFIG / TRACKS ----------

    pub fn initialize(ctx: Context<Initialize>, args: InitializeArgs) -> Result<()> {
        let cfg = &mut ctx.accounts.config;
        cfg.admin = args.admin;
        cfg.treasury = args.treasury;
        cfg.purse_mint = args.purse_mint;
        cfg.bump = ctx.bumps.config;
        Ok(())
    }

    pub fn create_track(ctx: Context<CreateTrack>, args: CreateTrackArgs) -> Result<()> {
        // MVP assumption: admin creates 2 tracks; track shares minted separately.
        let t = &mut ctx.accounts.track;
        t.config = ctx.accounts.config.key();
        t.track_id = args.track_id;
        // args.name should be a 32-byte, zero-padded UTF-8 string
        t.name = args.name;
        t.distance = args.distance;
        t.cadence_minutes = args.cadence_minutes;
        t.base_entry_fee = args.base_entry_fee;
        t.purse_vault_bps = 5500;
        t.track_vault_bps = 2500;
        t.treasury_bps = 1000;
        t.burn_bps = 1000;
        t.bump = ctx.bumps.track;
        Ok(())
    }

    // ---------- RACE DAY / SCHEDULING ----------

    pub fn open_race(ctx: Context<OpenRace>, args: OpenRaceArgs) -> Result<()> {
        // Steward opens scheduled races. Program enforces schedule window.
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
        race.purse_pool_manual = 0;
        race.purse_pool_auto = 0;
        race.bump = ctx.bumps.race;
        Ok(())
    }

    pub fn enter_race_manual(
        ctx: Context<EnterRaceManual>,
        _args: EnterRaceManualArgs,
    ) -> Result<()> {
        // TODO: charge F (or 2F if mega), update pools and entrant list.
        Ok(())
    }

    pub fn close_and_fill(ctx: Context<CloseAndFill>, _args: CloseAndFillArgs) -> Result<()> {
        // Called at entry_close_ts.
        // 1) Close manual entries
        // 2) Fill remaining from circuit pool (season-locked horses), charging 0.35F (or 0.70F mega)
        // 3) Fill remaining from house horses
        Ok(())
    }

    pub fn resolve_race(ctx: Context<ResolveRace>, _args: ResolveRaceArgs) -> Result<()> {
        // Called at scheduled_ts.
        // Uses VRF (future) or seed + traits modifiers to rank entrants.
        // Pays top3 from manual_pool if exists else auto_pool.
        Ok(())
    }

    // ---------- HORSES / BREEDING / TRAINING ----------

    pub fn mint_horse(ctx: Context<MintHorse>, args: MintHorseArgs) -> Result<()> {
        // MVP: registry horse seed/traits/layers. Actual NFT minting can be integrated later.
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
        // One session per trait per horse; +4 to chosen trait.
        let h = &mut ctx.accounts.horse;
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
        // TODO: charge trainer fee and route (assume treasury-heavy split).
        Ok(())
    }

    pub fn breed(ctx: Context<Breed>, _args: BreedArgs) -> Result<()> {
        // TODO: charge breeding fee, apply parent decay, mint offspring seed derived from parents.
        Ok(())
    }
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

    // pools (denominated in PURSE smallest units)
    pub purse_pool_manual: u64,
    pub purse_pool_auto: u64,

    pub bump: u8,
}

#[account]
pub struct Horse {
    pub owner: Pubkey,
    pub seed: u64,
    pub tier: HorseTier,

    // traits (0..255 for compactness in MVP)
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
pub struct CreateTrack<'info> {
    #[account(mut, seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(init, payer=payer, space=8 + 32 + 4 + 32 + 1 + 2 + 8 + 2+2+2+2 + 1, seeds=[b"track", &args.track_id.to_le_bytes()], bump)]
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub payer: Signer<'info>,
    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
pub struct OpenRace<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(seeds=[b"track", &track.track_id.to_le_bytes()], bump=track.bump)]
    pub track: Account<'info, Track>,
    #[account(init, payer=payer, space=8 + 32 + 8 + 8 + 1 + 1 + 1 + 1 + 1 + 1 + 8 + 8 + 1,
      seeds=[b"race", track.key().as_ref(), &args.scheduled_ts.to_le_bytes(), &[args.is_mega_cup as u8]], bump)]
    pub race: Account<'info, Race>,
    #[account(mut)]
    pub payer: Signer<'info>,
    pub system_program: Program<'info, System>,
}

// Stubs for MVP; will be expanded as we wire token accounts + entrants.
#[derive(Accounts)]
pub struct EnterRaceManual<'info> {
    pub config: Account<'info, Config>,
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub race: Account<'info, Race>,
    pub signer: Signer<'info>,
}

#[derive(Accounts)]
pub struct CloseAndFill<'info> {
    pub config: Account<'info, Config>,
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub race: Account<'info, Race>,
    pub signer: Signer<'info>,
}

#[derive(Accounts)]
pub struct ResolveRace<'info> {
    pub config: Account<'info, Config>,
    pub track: Account<'info, Track>,
    #[account(mut)]
    pub race: Account<'info, Race>,
    pub signer: Signer<'info>,
}

#[derive(Accounts)]
pub struct MintHorse<'info> {
    #[account(seeds=[b"cfg"], bump=config.bump)]
    pub config: Account<'info, Config>,
    #[account(init, payer=payer, space=8 + 32 + 8 + 1 + 4 + 4 + 1,
      seeds=[b"horse", args.owner.as_ref(), &args.seed.to_le_bytes()], bump)]
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
pub struct CloseAndFillArgs {}

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
    #[msg("Trait already trained for this horse")]
    TraitAlreadyTrained,
}
