# IdleStables (Android)

Kotlin + Jetpack Compose starter app with Solana **Mobile Wallet Adapter (MWA)** wired.

## Open
Open this folder in Android Studio:
`Desktop/IdleStables/android/IdleStables`

## What works
- Launches a Compose UI
- "Connect Wallet" uses Solana Mobile Wallet Adapter `connect()`

## Next steps
- Persist `authToken` (DataStore)
- Add bottom navigation (Dashboard/Stable/Tracks/Breed/Silks)
- Add RPC client + devnet/localnet switch
- Transaction building + signing

## Dependencies
- `com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.8`

Docs: https://docs.solanamobile.com/android-native/using_mobile_wallet_adapter
