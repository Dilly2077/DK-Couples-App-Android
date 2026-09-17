# Evelune Pet Shop + Progression v0.1

## Purpose

This chunk closes the first pet-care loop by giving Pet Coins a real use and making Evelune progression visible without exposing hidden hunger or dirtiness meters.

## Pet Shop flow

1. Open Pet World.
2. The progression card shows Evelune level, Pet Bond level and shared Pet Coins.
3. Tap the card to open the Pet Shop.
4. Choose food from the launch catalog.
5. Pet Coins are spent exactly once.
6. Purchased food is credited exactly once into the same persistent `food_inventory` used by Kitchen Feeding.
7. The shop refreshes the wallet and owned quantity immediately.
8. If funds are insufficient, no coins or inventory are changed.

## Launch prices

Prices are provisional balancing data and live only in `PetShopCatalog`.

| Food | Pet Coins | Quantity |
| --- | ---: | ---: |
| Kibble | 8 | 1 |
| Fish | 12 | 1 |
| Carrot | 6 | 1 |
| Berry bowl | 8 | 1 |
| Apple slices | 6 | 1 |
| Milk bowl | 8 | 1 |
| Biscuit | 5 | 1 |
| Salad bowl | 10 | 1 |
| Cupcake treat | 8 | 1 |
| Premium feast | 20 | 1 |

## Transaction safety

The reward and feeding stores are separate local SQLite databases. A purchase therefore uses one stable transaction id at both boundaries:

- `RewardRepository.spendCoins(...)` stores the coin spend in `coin_spends` and debits the wallet in one transaction.
- `FeedingRepository.creditOnce(...)` stores the inventory credit in `inventory_credits` and adds the food in one transaction.

If the app stops after the coin debit but before the inventory credit, retrying the same purchase sees a duplicate coin spend and safely applies only the missing inventory credit. A fully completed retry neither spends coins nor adds food twice.

Insufficient-funds attempts are deliberately not persisted, so that purchase id can succeed later after more Pet Coins are earned.

## Database upgrades

- `evelune_rewards.db`: schema v2 adds `coin_spends`.
- `evelune_feeding.db`: schema v2 adds `inventory_credits`.

Both upgrades preserve existing wallet, reward, food and feeding data.

## Starter inventory

The Pet Shop calls the same one-time starter inventory initialisation used by the Kitchen before allowing purchases. This prevents opening the Shop before the Kitchen from suppressing the original starter food pack.

## Progression presentation

Pet World now presents:

- Evelune level;
- Evelune XP progress to the next level;
- Pet Bond level for the first owned pet;
- shared Pet Coin balance;
- direct entry into the Pet Shop.

The Shop shows both Evelune XP and Pet Bond XP progress bars plus the current Pet Coin balance.

## Level-up effect

Pet World stores the last level the user has already seen. When the user returns after an action that raised Evelune level and/or Pet Bond level, a one-time level-up celebration card appears. It records the new seen level afterward, so normal recomposition does not repeatedly replay the effect.

## Care-meter rule

Hunger and dirtiness remain hidden simulation values. This chunk adds no hunger, cleanliness or boredom bars.

## Visual assets

The UI preserves the approved v0.3 food asset keys and shop/economy contracts. The generated PNG binaries are still not physically committed through the GitHub connector, so food icons currently use temporary emoji fallbacks. Replacing those fallbacks with the approved art does not require changes to the shop engine or purchase persistence.

## Tests

`PetShopEngineTest.kt` covers:

- all launch foods appear once in the catalog;
- successful coin spend and Kitchen inventory credit;
- duplicate-purchase protection;
- insufficient-funds behavior;
- retry after later earning more coins;
- recovery when coins were spent before inventory credit;
- starter inventory initialization before Kitchen is opened.

CI continues to run all reward, interaction, feeding, cleaning and play tests plus `assembleDebug`.
