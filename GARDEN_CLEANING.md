# Evelune Garden Hot-Tub Cleaning v0.1

## Purpose

This chunk adds the second complete pet-care loop: cleaning. It builds directly on the universal pet drag/drop layer, hidden needs simulation and reward ledger already used by Kitchen feeding.

## Player flow

1. Open **Clean** from Pet World.
2. The first owned pet is moved into `PetRoom.GARDEN`.
3. Press and hold the pet.
4. Drag it over the Garden hot tub.
5. The hot-tub target highlights.
6. Release to snap the pet into the cleaning interaction.
7. The app opens a close-up bath view.
8. Drag the sponge across the pet.
9. Dirt flecks disappear locally as scrubbed grid cells are covered.
10. The flow completes automatically at 85% scrub coverage.
11. Hidden dirtiness is reduced by the existing clean amount (85 points).
12. `RewardHooks.petCleaned(...)` is emitted with the stable interaction id.
13. The reward policy grants Evelune XP, Pet Bond XP and Pet Coins.
14. The pet shows a clean/happy completion state and can return to the Garden.

No hidden dirtiness meter is shown. The only feedback is the visible dirt disappearing plus qualitative text such as “Much better” and “Almost sparkling”.

## Scrub coverage

`CleaningCoverageTracker` uses a deterministic 12 × 16 normalized grid. Sponge movement marks cells within a 1.35-cell brush radius as cleaned.

The UI receives only:

- the set of cleaned grid cells;
- a normalized 0..1 scrub-coverage fraction.

The 85% threshold is deliberately based on the user's physical scrub interaction, not the pet's hidden dirtiness simulation value.

## Hidden needs and anti-farming

The pet's hidden `dirtiness` value remains the simulation source of truth.

A brand-new cleaning interaction only grants care/rewards when dirtiness is at least 15/100. An already-clean pet therefore cannot be repeatedly scrubbed for XP or Pet Coins.

Successful cleaning uses the existing `PetNeedsEngine.clean(...)` rule, which currently removes 85 dirtiness points and sets the pet's behaviour to `HAPPY`.

## Retry safety

`PetEngine.cleanOnce(...)` reuses the persistent `pet_care_events` table introduced by Kitchen feeding.

The stable cleaning `interactionId` protects two boundaries:

1. pet state mutation — `cleanOnce` cannot reduce dirtiness twice;
2. reward emission — `RewardHooks.petCleaned(...)` maps to the idempotent reward ledger.

`CleaningEngine` also handles the interruption case where the care mutation was written but reward emission did not complete. Replaying the same interaction skips a second clean but retries the reward hook. Once the reward exists, further retries return `AlreadyCompleted` and grant nothing.

## Reward policy

The existing `PET_CLEAN` rule currently grants:

- 10 Evelune XP;
- 15 Pet Bond XP;
- 6 Pet Coins.

These values remain provisional balancing data.

## UI

`PersistentGardenCleaningScreen` is reachable from the **Clean** care action in `PetSectionScreen`.

The Garden room uses `PetDragDropLayer` and the existing `GARDEN_HOT_TUB` interaction target. A successful clean target drop enters the sponge close-up.

The close-up uses local scrub geometry to remove dirt flecks in the touched areas. The current pet/Garden rendering is a Compose/emoji fallback because the approved v0.3 PNG binaries are still not physically present in the repository. The gameplay contract is independent of those assets.

## Tests

`CleaningEngineTest.kt` covers:

- deterministic scrub coverage accumulation;
- completion at the 85% threshold;
- hidden dirtiness reduction;
- PET_CLEAN reward values;
- duplicate interaction safety;
- reward recovery after an interrupted care mutation;
- Garden-only validation;
- incomplete scrub rejection;
- clean-pet anti-farming.

## Next chunk

**Playroom Play v0.1**:

- enter the Playroom from Pet World;
- drag the pet onto one of the existing toy targets;
- toy-specific short interactions for ball, rope, plush and frisbee;
- meaningful completion only after the interaction finishes;
- emit `RewardHooks.petPlayedWith(...)` once;
- add hidden play/cooldown rules to prevent reward farming;
- return the pet to free room movement with a happy reaction.
