# IdleStables UI (React)

This folder is a **UI spec that runs**.

Goal: build the key IdleStables screens as React components (fast iteration), then port 1:1 to Kotlin/Compose.

## Run
```bash
npm install
npm run dev
```

## Pages (MVP UI spec)
- /dashboard
- /stable
- /tracks
- /tracks/:id
- /race/:id
- /breed
- /silks

## Data model (front-end)
The app will render horses from deterministic metadata:
- seed
- layer IDs (pose/coat/markings/mane/background)
- silks profile

No dependence on AI-generated unique images for the main collection.
