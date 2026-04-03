# IdleStables — Racing + Circuit Tokenomics (v0.1)

**Decisions locked (Money):**
- Auto entry fee factor **α = 0.35x**
- Fee split (of each entry fee): **55/25/10/10**
  - 55% → Purse Pool (race prizes)
  - 25% → Track Revenue Vault
  - 10% → Treasury
  - 10% → Burn

## Entry modes
### Manual Entry
- Fee paid: `F`
- Eligible for full prizes.

### Auto Entry (Circuit / Season-Locked)
- Fee paid: `Fa = α * F = 0.35F`
- Used to fill empty race slots after entry closes.
- Fee is fronted from the horse’s escrow (see escrow rules).

## Split accounting per entrant
For a given fee `X` (either `F` or `Fa`):
- `purse_pool += 0.55 * X`
- `track_vault += 0.25 * X`
- `treasury += 0.10 * X`
- `burn += 0.10 * X`

All entrants (including auto/circuit and treasury/house) are “charged” fees for consistent accounting.

## Purse pools: recommended MVP implementation
Use **two pools** for clarity and tunability:
- `manual_pool`: sum of purse allocations from manual entrants
- `auto_pool`: sum of purse allocations from auto entrants

At payout time:
1) If there is at least 1 manual entrant, pay **Top 3** primarily from `manual_pool`.
2) If `manual_pool` is insufficient (edge cases), optionally top-up from `auto_pool`.
3) If there are **zero** manual entrants, pay Top 3 from `auto_pool`.

This preserves the intuition: bigger payouts come from full-fee entries.

### Top 3 payout curve (MVP)
- **60% / 25% / 15%**

## Circuit / Season-Locked horses (auto-racing pool)
Season lock makes horses eligible for auto-entry.

### Escrow funding rule for auto fees
When steward attempts auto-entry for horse `H`:
1) Deduct `Fa` from `H.escrow_emissions` if available.
2) Else deduct from `H.escrow_winnings`.
3) Else skip horse and select a different circuit horse.

### Anti-abuse
- A horse can be auto-entered at most **once per cadence window**.
- Auto entrants are only used to fill remaining slots after manual entrants.

## Race timing (MVP)
- Races resolve at **:00 and :30**.
- Entry closes at `resolve_ts - 60s`.
- Field size: **12**.
- Each track race day includes one **Mega Cup** featured race:
  - Field size **18**
  - Entry fee multiplier **2x** (relative to the track’s base fee)
  - Auto fee still applies: `0.35 * (2x base fee)`

## Track ownership NFTs (MVP)
- Track #1: 100 TrackShare NFTs
- Track #2: 500 TrackShare NFTs
- Treasury initially holds all shares; later sold.
- Track vault accrues fees; claims distribute by share ownership at claim time.

## Stable Upgrades (wallet-wide)
Introduce **Stable Upgrade NFTs** that apply wallet-wide modifiers.

Recommended MVP-safe effects (bounded, non-stackable):
- `decay_multiplier_bps` (e.g., -10% to -30% decay)
- optional: `season_emission_bonus_bps` (small)

Rules:
- At most **one** active stable upgrade per wallet.
- Benefits are capped to avoid permanent emission machines.

## Training (sink)
- Each horse may be trained **once per trait** (Speed once, Stamina once).
- Each training consumes a **trainer fee** in PURSE and permanently increases that trait by **+4**.

## Notes / Open items
- Randomness source (MVP vs VRF) not finalized here.
- House horses: if treasury/house fills beyond circuit horses, fees are still booked but are internal transfers.
