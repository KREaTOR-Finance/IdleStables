# MagicBlock VRF Integration (IdleStables)

Status: **spec + scaffold** (no deployment).

## Why MagicBlock VRF
- VRF follows **RFC 9381** (Ristretto/Curve25519 + Schnorr-style proofs).
- Randomness requests are queued on-chain and fulfilled by verified oracles.
- Proofs are **verifiable on-chain**; invalid proofs fail before game logic.
- Callback authorization uses the VRF program identity signer.

Refs:
- Docs (security): https://docs.magicblock.gg/pages/verifiable-randomness-functions-vrfs/introduction/security
- Repo: https://github.com/magicblock-labs/ephemeral-vrf

## Important notes / risk
- Repo README includes a general warning about audit status, but also links an audit report (Zenith) under `security_audits/`.
- We must design for **oracle withholding**: settlement should have timeout + retry behavior.

## Race lifecycle (VRF)
1) `enter_race_*`: entrants pay SKR, entrants list grows.
2) `lock_race`: freezes entrants list and requests VRF randomness.
3) VRF oracle fulfills request on-chain.
4) `settle_race`: reads VRF output and computes deterministic ranking.

## Deterministic ranking contract
Derive a per-entrant seed:
- `seed_i = H(vrf_output, race_id, horse_pubkey, horse_stats_hash, track_id)`

Then:
- `noise_i = f(seed_i)`
- `score_i = base_stats(track, horse) + noise_i`

Sort scores descending to produce ordered results.

## On-chain invariants
- entrants list is immutable after lock.
- settle uses the VRF output published for this request.
- payouts never exceed vault balances.
- fee splits sum to 100%.

## Integration approach (Anchor)
We will:
- Add VRF-related fields to `Race` account:
  - `vrf_request` / `vrf_hash_id` (identifier)
  - `vrf_fulfilled` bool
  - `vrf_output` (bytes) or stored hash
- Add instructions:
  - `lock_race_vrf` (requests randomness)
  - `settle_race_vrf` (settles using fulfilled randomness)

Implementation will use the MagicBlock `ephemeral-vrf` API/SDK crate(s) to:
- build CPI to request randomness
- validate callback signer identity

Next: read the official integration test in MagicBlock repo:
`program/tests/integration/use-randomness/...` and mirror the account wiring.
