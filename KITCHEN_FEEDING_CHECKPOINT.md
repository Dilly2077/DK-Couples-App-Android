# Evelune Kitchen Feeding Checkpoint

Branch: `kitchen-feeding-v0.1`

Base: `pet-interaction-v0.1`

## Completed in this chunk

- Added the 10-item food catalog with canonical v0.3 asset keys and provisional satiation values.
- Added persistent food inventory and retry-safe feeding receipts.
- Added a one-time starter food pack so feeding can work before the Pet Coin shop is implemented.
- Added food-specific hidden hunger reduction.
- Upgraded the pet repository with idempotent `pet_care_events` and `PetEngine.feedOnce(...)`.
- Added `FeedingEngine` to coordinate Kitchen validation, food reservation, pet mutation and reward emission.
- Added anti-farming: a new feeding requires hidden hunger >= 15/100.
- Added the persistent Kitchen feeding Compose route.
- Wired the Pet World **Feed** care action to the Kitchen.
- Added chair drag/drop using the universal `PetDragDropLayer`.
- Added seated/eating/celebration UI phases and persistent Food Shelf quantities.
- Feeding completion calls `RewardHooks.petFed(...)` with a stable interaction id and therefore grants global Evelune XP, Pet Bond XP and Pet Coins exactly once.
- Added unit coverage for inventory seeding, consumption, hunger change, reward grant, duplicate retry safety, room validation, anti-farming, stock failure and RESERVED-interaction recovery.

## Important implementation detail

There are three independent idempotency boundaries:

1. `feeding_receipts` ensures an interaction consumes at most one inventory item;
2. `pet_care_events` ensures it changes the pet at most once;
3. the reward ledger ensures it grants XP/coins at most once.

This is intentional so a process interruption can retry the same interaction id safely.

## Visual status

The approved Kitchen and pet action art exists in the v0.3 asset production pack, but the generated PNG binaries still are not physically stored in GitHub because the connector could not directly transport the previous local binary archive. `KitchenFeedingScreen.kt` therefore uses temporary Compose/emoji fallbacks while preserving the correct layout/interaction contract. Replace those fallbacks with the canonical v0.3 assets when the binaries are available; do not rewrite the feeding engine.

## Next chunk

**Garden Hot-Tub Cleaning v0.1**.

Build:

1. Garden hot-tub drop target -> cleaning transition;
2. close-up pet cleaning scene;
3. finger-driven sponge;
4. visual dirt overlays + invisible coverage mask;
5. completion threshold around 85–90%;
6. idempotent dirtiness reduction;
7. `RewardHooks.petCleaned(...)` exactly once;
8. clean/happy reaction and return to Garden.

Reuse the same care-event retry pattern introduced by Kitchen Feeding.
