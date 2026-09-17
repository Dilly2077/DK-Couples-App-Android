# Evelune Pet System Integration v0.1

Branch: `pet-system-integration-v0.1`
App version: `0.5.0` (`versionCode 5`)

This integration branch is built directly on `pet-shop-progression-v0.1`, which already contains the full stacked pet-development chain. It therefore brings the following systems into one Android build:

- persistent pet/hatching engine;
- global Evelune XP, per-pet Bond XP and Pet Coins;
- press/hold, drag, hover, drop and snap interaction layer;
- Kitchen feeding and persistent food inventory;
- Garden hot-tub cleaning mini-game;
- Playroom toy mini-games;
- Pet Shop purchasing into the same Kitchen inventory;
- global and per-pet progression presentation;
- level-up celebration state;
- retry/duplicate protection across care actions, rewards, purchases and inventory credits.

## Integrated player loop

1. Enter Pet World.
2. View shared Evelune level, Pet Bond level and Pet Coin wallet.
3. Feed: open Kitchen, drag pet to feeding seat, choose owned food, complete feeding and receive rewards when meaningful.
4. Clean: open Garden, drag pet to hot tub, scrub to the completion threshold and receive rewards when meaningful.
5. Play: open Playroom, drag pet to Ball/Rope/Plush/Frisbee and complete the toy-specific interaction.
6. Shop: spend Pet Coins on food. Purchased items are written into the exact `food_inventory` used by Kitchen feeding.
7. Return to Pet World and see progression/level-up presentation.

Hidden care state remains intentional: hunger, dirtiness and play cooldowns are not displayed as status bars. The pet communicates needs through expression, behaviour and thought-bubble contracts.

## Persistence boundaries

The existing local SQLite stores remain separate by responsibility:

- pet engine state and care receipts;
- reward wallet, reward ledger, Bond XP and coin-spend receipts;
- food inventory and inventory-credit receipts;
- cleaning receipts;
- play receipts/cooldown state.

The transaction IDs joining these stores are stable and idempotent so a process interruption cannot legitimately charge/spend/reward the same completed action twice.

## Art status

The approved v0.3 style specification, manifests, source references, canonical asset keys and generated-asset handoff remain under `design/pets/`. The generated binary art pack exists as a completed 60-asset production set from the asset-production stage, but those binaries are not yet stored in this repository. This integration build therefore deliberately retains the current Compose/emoji fallbacks instead of committing a partial/corrupted binary transport.

Do not redesign the art or change the canonical asset keys when the final binaries are imported. The gameplay code is already written around the v0.3 contracts.

## Validation and APK

`.github/workflows/pet-engine.yml` validates this branch by running all debug unit tests, compiling `:app:assembleDebug`, and uploading the resulting debug APK as the `evelune-pet-system-debug` workflow artifact.
