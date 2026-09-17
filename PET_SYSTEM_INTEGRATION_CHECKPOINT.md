# Continue here — Evelune Pet System Integration v0.1

Branch: `pet-system-integration-v0.1`
Base: `pet-shop-progression-v0.1`
Target integration base: `main`
Version: `0.5.0` / versionCode 5

## What is integrated

This branch contains the complete stacked pet feature chain through Chunk 7:

- pet engine + hatch persistence;
- v0.2/v0.3 asset contracts and art handoff docs;
- Reward Engine;
- universal drag/drop interaction layer;
- Kitchen Feeding;
- Garden Hot-Tub Cleaning;
- Playroom Play;
- Pet Shop + Inventory + Progression.

The current Pet World routes to Feed, Clean, Play, Hatch and Shop. Shop purchases write to the same persistent food inventory used by Kitchen feeding. All care loops use the central reward system and duplicate/retry protections.

## Validation contract

Before calling integration complete, the latest GitHub Actions run for `pet-system-integration-v0.1` must show:

1. `:app:testDebugUnitTest` success;
2. `:app:assembleDebug` success;
3. `evelune-pet-system-debug` artifact uploaded.

Once green, open/maintain an integration PR from this branch to `main`; merge only after the final head SHA is the one that passed CI.

## Art handoff

The generated v0.3 binary art was intentionally not reconstructed from incomplete base64 fragments. Partial transport files created during integration were removed. Continue to use the approved source/style contracts under `design/pets/`; when a connector path supporting binary file upload is available, import the complete 60-asset pack and replace Compose/emoji fallbacks without changing gameplay contracts.

## Recommended next work after merge

Real-device QA of the complete pet loop, then balancing/polish: animation timing, drag hitboxes, scrub sensitivity, toy interaction feel, shop prices, XP thresholds and final production-art replacement.
