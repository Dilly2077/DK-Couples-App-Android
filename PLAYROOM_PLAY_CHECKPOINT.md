# Continue here — Playroom Play v0.1

Branch: `playroom-play-v0.1`
Base: `garden-cleaning-v0.1`

## Completed in this chunk

- Added `pets/play/PlayEngine.kt`.
- Added four launch toys: Ball, Rope, Plush and Frisbee.
- Added deterministic toy-specific progress tracking.
- Added hidden 30-minute per-pet reward cooldown.
- Added `pets/play/AndroidPlayRepository.kt` with durable SQLite receipts and cooldown state.
- Added `PetEngine.playOnce(...)` using persistent `pet_care_events`.
- Added `PersistentPlayroomScreen` and wired the Pet World Play action.
- Reused universal press/hold, drag, hover and drop targets.
- Added happy completion state and PET_PLAY reward emission.
- Added unit tests for toy inputs, cooldown, rewards, retry safety and room validation.
- Updated CI workflow to validate this branch.

## Canonical PET_PLAY reward

Current policy remains:

- 8 Evelune XP
- 12 Pet Bond XP
- 5 Pet Coins

Do not duplicate these values in new gameplay code; use `RewardHooks.petPlayedWith(...)`.

## Anti-farming rule

`PlayEngine.DEFAULT_COOLDOWN_MS = 30 minutes`.

This is provisional balancing. The cooldown is hidden; no boredom/play meter should be shown.

## Important visual constraint

The generated v0.3 PNG pack is still not physically committed to this GitHub repository. Current Playroom visuals are temporary Compose/emoji fallbacks. Preserve the canonical keys (`toy_ball`, `toy_rope`, `toy_plush`, `toy_frisbee`) so final art can be swapped in without changing gameplay.

## Next development chunk

Build **Pet Shop + Inventory + Progression Polish v0.1** on top of this branch:

1. persistent shop catalog;
2. Pet Coin purchases;
3. purchase transaction/duplicate safety;
4. write purchased food into the same Kitchen `food_inventory` used by feeding;
5. insufficient-coin handling;
6. shop UI in the Pet section;
7. Evelune XP / Pet Bond XP / Pet Coin presentation and level-up effects;
8. keep hunger/dirtiness hidden — do not add care bars.

Before claiming this chunk is fully validated, check the latest GitHub Actions run on `playroom-play-v0.1` and confirm both unit tests and `assembleDebug` succeed.
