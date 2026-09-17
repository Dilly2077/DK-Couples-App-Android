# Continue here — Pet Shop + Progression v0.1

Branch: `pet-shop-progression-v0.1`
Base: `playroom-play-v0.1`

## Completed in this chunk

- Added idempotent Pet Coin spending to the reward repository.
- Upgraded `evelune_rewards.db` to v2 with durable `coin_spends`.
- Added idempotent Kitchen inventory credits.
- Upgraded `evelune_feeding.db` to v2 with durable `inventory_credits`.
- Added `pets/shop/PetShopEngine.kt` and launch food pricing.
- Purchases feed directly into the existing Kitchen `food_inventory`.
- Added insufficient-funds behavior with no mutation.
- Added cross-database crash recovery using the same stable transaction id at both boundaries.
- Added Pet Shop UI and wired it into Pet World.
- Added persistent Evelune XP, Pet Bond XP and Pet Coin presentation.
- Added one-time level-up celebration when returning to Pet World after gaining a global or pet level.
- Added Pet Shop unit tests.
- Updated CI to validate this branch.

## Provisional launch prices

- Kibble: 8 Pet Coins
- Fish: 12
- Carrot: 6
- Berry bowl: 8
- Apple slices: 6
- Milk bowl: 8
- Biscuit: 5
- Salad bowl: 10
- Cupcake treat: 8
- Premium feast: 20

Keep balancing centralized in `PetShopCatalog`.

## Transaction contract

For purchase id `X`:

- coin spend id = `pet_shop:X`
- inventory credit id = `pet_shop:X`

Do not change this relationship casually. It is what makes a crash between the two local databases recoverable without double charging or duplicating stock.

## Progression UI

Pet World now shows:

- Evelune level and XP progress;
- first owned pet's Pet Bond level;
- Pet Coin balance;
- Shop entry;
- one-time level-up notice when a newly reached level has not yet been seen.

The Shop also shows Evelune and Pet Bond XP progress.

## Important visual constraint

The approved v0.3 PNG assets are still not physically present in the GitHub repository. Shop food icons remain emoji fallbacks, but all canonical food/economy asset keys and gameplay contracts are preserved.

## Validation requirement

Before calling this chunk finished, confirm the latest GitHub Actions run on `pet-shop-progression-v0.1` has:

- `Unit tests` = success
- `Verify app still compiles` (`assembleDebug`) = success

## Next recommended stage

The original seven gameplay chunks are now implemented. The next stage should be **Pet System Integration + Asset Wiring v0.1**:

1. physically import the approved v0.3 PNG asset pack into Android resources/assets;
2. replace Compose/emoji pet, room, food, toy, cleaning and economy fallbacks;
3. wire layered hot-tub and Kitchen foreground assets for depth;
4. verify all care loops visually on-device;
5. consolidate the stacked pet branches into the release integration branch;
6. produce a downloadable debug APK for hands-on testing;
7. then tune progression, prices, cooldowns and care values from real use.
