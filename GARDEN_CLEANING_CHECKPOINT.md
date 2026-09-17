# Garden Hot-Tub Cleaning v0.1 — Continuation Checkpoint

Branch: `garden-cleaning-v0.1`
Base checkpoint: Kitchen Feeding v0.1 (`3d311d34c9477026091c48e5d81f7e6772e5283c`)

## Implemented

- `CleaningCoverageTracker`
  - 12 × 16 normalized scrub grid by default;
  - local brush-radius coverage;
  - deterministic cleaned-cell set;
  - no user-facing hidden dirtiness value.

- `CleaningEngine`
  - requires Garden room;
  - requires >= 85% scrub coverage;
  - requires >= 15/100 hidden dirtiness for a new interaction;
  - uses `PetEngine.cleanOnce(...)`;
  - emits `RewardHooks.petCleaned(...)`;
  - supports care-applied/reward-missing retry recovery;
  - duplicate completed interactions grant nothing.

- `PetEngine`
  - added `cleanOnce(...)` using persistent `pet_care_events`;
  - added `careWasApplied(...)` for coordinator retry recovery.

- `PersistentGardenCleaningScreen`
  - moves owned pet to `PetRoom.GARDEN`;
  - uses universal long-press drag/drop layer;
  - existing `GARDEN_HOT_TUB` target starts cleaning;
  - close-up sponge gesture removes local dirt flecks;
  - auto-completes at 85% coverage;
  - shows happy/clean completion state;
  - no status bars for hunger/dirtiness.

- Pet World
  - Clean care tile now opens Garden cleaning route;
  - Feed still opens Kitchen feeding route;
  - Play remains reserved for the next chunk.

- Tests
  - coverage accumulation;
  - threshold validation;
  - reward values;
  - hidden dirtiness mutation;
  - duplicate safety;
  - interrupted reward recovery;
  - wrong-room validation;
  - anti-farming when already clean.

## Persistence / safety contract

A stable cleaning interaction id is generated when a hot-tub cleaning session starts.

`pet_care_events` prevents dirtiness from being reduced twice. The reward ledger prevents `PET_CLEAN` rewards from being awarded twice. `CleaningEngine` intentionally allows replay of a care event when the reward entry is missing, making the care -> reward boundary recoverable.

## Visual contract

The approved v0.3 pet/Garden PNG assets are still not physically present in GitHub. Current Garden and pet visuals are Compose/emoji fallbacks. Do not redesign the gameplay flow when production assets arrive; replace only rendering.

## Validation

Workflow: `.github/workflows/pet-engine.yml`

Expected validation for this branch:

- `:app:testDebugUnitTest`
- `:app:assembleDebug`

If CI fails, inspect the newest `garden-cleaning-v0.1` workflow run and fix the concrete compiler/test failure before starting the next chunk.

## Next chunk

Create `playroom-play-v0.1` from the final green Garden branch.

Implement Playroom Play v0.1 on the existing interaction targets:

- `PLAY_BALL`
- `PLAY_ROPE`
- `PLAY_PLUSH`
- `PLAY_FRISBEE`

Keep completion/reward idempotent and add a hidden anti-farming cooldown/need rule before enabling repeated play rewards.
