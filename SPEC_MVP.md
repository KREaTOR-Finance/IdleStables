# IdleStables — MVP Spec (Rust + Kotlin)

## Hard constraints (from Money)
- 12 horses per race.
- One **Mega Cup** featured race per track race day: **18 entrants**.
- Mega Cup has **2x entry fee** (fees are low anyway) and therefore a larger purse.
- Races resolve on a fixed schedule: **:00 and :30**.
- Entry closes **60 seconds** before resolve.
- Payouts: **Top 3 only** (for now).
- Races must always run, even without players.
- “House” horses fill empty slots and are charged fees as entrants.
- Tracks limited at first; track ownership initially treasury-owned.
- Track ownership NFTs must exist in MVP:
  - Track #1: **100** TrackShare NFTs
  - Track #2: **500** TrackShare NFTs
  - Sold later; treasury owns them in MVP.
- Photo finish is a **static image**; winner horse must match actual winner (player horse if it wins).
- Need **silks** for jockey + horse gear (saddlecloth) in a clean system.
- Breeding is required; yield decays over time and with breeding; supports Yearling → Racehorse → Graded → Legendary.
- Start with **100 horses** (test batch) and ensure breeding can generate new ones.

## Product split
1) **On-chain (Solana) programs in Rust (Anchor)**
2) **Android app (Kotlin + Jetpack Compose)**
3) **Race Steward (keeper)** service to guarantee scheduled races are created/filled/resolved.

## Key design decision: deterministic horses (seed + layers)
We do **not** rely on generating 10,000 unique AI images.

Each Horse is primarily:
- a deterministic **seed** (u64)
- a set of **layer IDs** (pose/coat/markings/mane/background)
- stats + lifecycle fields

Android renders the horse portrait + silks **on-device** from a controlled PNG layer kit and caches results.

This makes:
- consistency high
- breeding images trivial (offspring = new seed/layer mix)
- “100 now → 10,000 later” feasible

## Programs (Rust / Anchor)
### idlestables_tracks
- Track accounts
- Track revenue vault accounting
- TrackShare NFT recognition (ownership at claim time)

### idlestables_racing
- Scheduled races per track
- Entry (horse escrow/registration) + fees
- House fill
- Resolution (randomness strategy pluggable)
- Payout recording + claim

### idlestables_horses
- Horse registry (seed/layers/stats)
- Lifecycle + breeding + decay

## Scheduling rules
- Every track has a repeating schedule producing Race slots.
- Entry window closes at `resolve_ts - 60s`.
- At close: fill remaining slots with house horses.
- Resolve at `resolve_ts`.

## Track distance affects Speed/Stamina weighting (MVP)
- Track A distance: **6f** (Sprint)
- Track B distance: **1.5 miles** (Distance)
- Race scoring uses the same 4 traits, but Speed/Stamina weights shift by track distance.
- No additional stats beyond Speed/Stamina/Focus/Temperament.

## Payouts (MVP)
- Top 3 only.
- Top 3 split: **60% / 25% / 15%**
- Two entry modes:
  - Manual fee = F
  - Auto (circuit fill) fee = 0.35F
- Fee split (of each entrant fee):
  - 55% purse pool
  - 25% track revenue vault
  - 10% treasury
  - 10% burn

## Silks (MVP)
- Owner-selected silks profile:
  - patternId, primaryColor, secondaryColor
- Rendered as overlay layers on jockey + saddlecloth.

## Owner Stable Upgrades (wallet-wide)
- A wallet can have an active **Stable Upgrade NFT** that applies modifiers wallet-wide.
- Primary purpose: slow decay / preserve emissions via bounded modifiers.

## Traits (MVP)
- Each horse has **4 traits**:
  - Speed
  - Stamina
  - Focus
  - Temperament
- Traits impact **racing and breeding only** (not emissions).

## Training (one-time per trait per horse)
- Each horse can run **one training session per trait** (Speed/Stamina/Focus/Temperament).
- Each training session gives **+4** to that trait and charges a trainer fee.
- One-time operation (cannot repeat for that trait on that horse).

## Breeding (MVP)
- Inputs: sire horse, dam horse + fee.
- Output: new Yearling (new seed).
- Parents: decrement breeding counters; apply yield decay step.

## Next deliverables (this repo)
- Web UI component library (React) as visual spec.
- Kotlin Compose versions of the same components.
- Anchor workspace scaffold.
- Race Steward scaffold.
