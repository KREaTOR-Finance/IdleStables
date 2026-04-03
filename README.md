# IdleStables

Rust + Kotlin Android idle racing protocol/game on Solana.

## Repo layout
- `solana/` — on-chain programs (Anchor / Rust)
- `steward/` — Race Steward backend (Railway) that runs the race schedule
- `ui-web/` — running UI spec (React) used as design reference for Kotlin/Compose
- `android/` — Kotlin/Compose app (skeleton TBD)

## Locked MVP rules
See:
- `SPEC_MVP.md`
- `TOKENOMICS_RACING_v0.1.md`

## Core MVP constants
- Manual entry fee = **F**
- Auto (Circuit fill) entry fee = **0.35F**
- Mega Cup entry fee = **2F** (auto mega = **0.70F**)
- Fee split: **55% purse / 25% track / 10% treasury / 10% burn**
- Top 3 payout: **60% / 25% / 15%**
- Schedule: resolves at **:00 and :30**, closes **T-60s**
- Field size: **12** (Mega Cup **18** once per track race day)

## Run (dev)

### UI spec (web)
```bash
cd ui-web
npm install
npm run dev
```

### Steward (Railway/local)
```bash
cd steward
npm install
cp .env.example .env
npm run dev
```

### Solana program (local build)
```bash
cd solana
cargo fmt
cargo build
```

**Windows note:** if `cargo build` fails with `Access is denied (os error 5)` while linking/copying build scripts, it’s typically antivirus/locking on `.exe` in the target dir. Workarounds:
- add an AV exclusion for the repo/target dir
- build from WSL2
- or set `CARGO_TARGET_DIR` to a different path and retry

## Next
- Wire SPL token accounts + real fee transfers
- Add steward Anchor client integration
- Add Android app scaffold
