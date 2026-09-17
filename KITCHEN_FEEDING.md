# Evelune Kitchen Feeding v0.1

## Purpose

This chunk turns the Kitchen feeding concept into a persistent gameplay loop built on top of the universal pet interaction layer and reward engine.

## Player flow

1. Open **Feed** from Pet World.
2. The first owned pet is moved into `PetRoom.KITCHEN`.
3. Press and hold the pet to pick it up.
4. Drag it over either Kitchen chair.
5. The chair interaction target highlights.
6. Release the pet to snap it into the seated feeding state.
7. The Food Shelf opens.
8. Choose an owned food item.
9. The pet enters its eating/reaction state.
10. One food item is consumed.
11. Hidden hunger is reduced by that food's satiation value.
12. `RewardHooks.petFed(...)` is emitted exactly once.
13. Evelune XP, Pet Bond XP and Pet Coins are granted according to the reward policy.
14. The user can return the pet to free Kitchen movement.

No hunger/status bar is shown. A pet communicates need through its expression/thought bubble; the feeding engine uses the hidden hunger value only for simulation and anti-farming.

## Food catalog

Launch catalog:

| Food | Hidden hunger reduction | Asset key |
| --- | ---: | --- |
| Kibble | 35 | `food_kibble` |
| Fish | 45 | `food_fish` |
| Carrot | 25 | `food_carrot` |
| Berry bowl | 30 | `food_berry_bowl` |
| Apple slices | 25 | `food_apple_slices` |
| Milk bowl | 30 | `food_milk_bowl` |
| Biscuit | 20 | `food_biscuit` |
| Salad bowl | 35 | `food_salad_bowl` |
| Cupcake treat | 20 | `food_cupcake_treat` |
| Premium feast | 70 | `food_premium_feast_tray` |

These values are provisional balancing data.

## Starter inventory

Until the Pet Coin shop chunk lands, a local starter pack is seeded once when the feeding system is first opened:

- Kibble ×3
- Fish ×1
- Carrot ×2
- Berry bowl ×1
- Biscuit ×2

The later Shop writes into the same persistent `food_inventory` table, so no feeding redesign is required.

## Persistence

`AndroidFeedingRepository` uses `evelune_feeding.db` with:

- `food_inventory`
- `feeding_receipts`

A feeding receipt has a stable `interactionId` and is first recorded as `RESERVED`. Reserving food and decrementing inventory happen in one SQLite transaction. The receipt then becomes `COMPLETED` after the pet mutation and reward hook succeed.

The pet database was upgraded to v2 with `pet_care_events`. `PetEngine.feedOnce(...)` atomically writes the new pet state and the interaction id. This prevents the same feeding from reducing hunger twice after a retry/process interruption.

The reward engine already has its own idempotent event ledger, so replaying the same `interactionId` cannot grant XP or Pet Coins twice either.

Together the flow is retry-safe at three boundaries:

1. food reservation;
2. pet mutation;
3. reward emission.

## Anti-farming

A new feeding only becomes meaningful when hidden hunger is at least 15/100. Trying to feed a nearly full pet:

- consumes no food;
- changes no pet state;
- grants no XP;
- grants no Pet Coins.

If an interaction was already RESERVED before an interruption, it is allowed to resume even if the pet's hunger changed meanwhile.

## UI

`PersistentKitchenFeedingScreen` creates the real local repositories and engines and is now reachable from the **Feed** care action in `PetSectionScreen`.

The room uses the universal `PetDragDropLayer`; a valid Kitchen chair drop changes the UI to the seated phase and opens the Food Shelf.

The current room/pet visuals are temporary Compose/emoji fallbacks because the generated v0.3 PNG binaries still are not physically committed through the GitHub connector. The screen is deliberately structured so those production images can replace the fallbacks without changing feeding logic.

## Tests

`FeedingEngineTest.kt` covers:

- starter inventory is seeded once;
- food consumption;
- food-specific hunger reduction;
- XP/Bond XP/Pet Coin reward emission;
- complete-interaction retries;
- Kitchen-only feeding;
- full-pet anti-farming;
- out-of-stock handling;
- recovery from an already RESERVED interaction.

## Next chunk

**Garden Hot-Tub Cleaning v0.1**:

- drag pet into hot tub;
- transition to close-up cleaning mode;
- sponge gesture over visible dirt overlays;
- track cleaned coverage without a visible status bar;
- complete above a configured cleaning threshold;
- reduce hidden dirtiness once;
- emit `RewardHooks.petCleaned(...)` once;
- return to Garden with a happy/clean reaction.
