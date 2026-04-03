# IdleStables UI (React)

This folder is a **UI spec that runs**.

Goal: build the key IdleStables screens as React components (fast iteration), then port 1:1 to Kotlin/Compose.

## Run
```bash
npm install
npm run dev
```

## Screens to build first (MVP)
- DashboardResultsFeed
- MyStableGrid
- TrackSchedule (races at :00 / :30; closes 60s prior)
- EnterRaceSheet
- RaceResultPhotoFinish
- SilksEditor

## Data model (front-end)
The app will render horses from deterministic metadata:
- seed
- layer IDs (pose/coat/markings/mane/background)
- silks profile

No dependence on AI-generated unique images for the main collection.
