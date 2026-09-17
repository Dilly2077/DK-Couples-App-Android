# Evelune Pet Engine v0.1

This branch contains the pet simulation engine only. It intentionally does **not** wire the pet asset packs into the current app UI yet.

## Current hatch table

| Pet | Tier | Base chance |
|---|---|---:|
| Puppy | Common | 12.5% |
| Kitten | Common | 12.5% |
| Bunny | Common | 12.5% |
| Duckling | Common | 12.5% |
| Hedgehog | Uncommon | 10% |
| Ferret | Uncommon | 10% |
| Otter | Uncommon | 10% |
| Fox | Rare | 5% |
| Red Panda | Rare | 5% |
| Axolotl | Rare | 5% |
| Tiger | Legendary | 4% |
| Dragon | Mythic | 1% |

Total: 100%.

## Hatch protection

- First 3 claimed hatches are forced to be unique where unowned species remain.
- After 3 duplicate hatches in succession, the next hatch is forced to an unowned species where one remains.
- 20 hatches without Legendary-or-better triggers a Tiger/Dragon-only hatch pool.
- 100 hatches without Dragon guarantees Dragon.
- Pity and duplicate counters update only when a completed egg is claimed.

## Hatch lifecycle

The engine stores absolute start/end timestamps. It does not require a timer service to stay alive while the app is closed.

Stages are derived from elapsed progress:

- 0-30%: pristine
- 30-50%: wobble
- 50-65%: small crack
- 65-78%: medium crack
- 78-88%: heavy crack
- 88-96%: split shell
- 96-100%: hatch flash
- complete: ready to claim / open shell

`HatchDurationProvider` is injectable. All pets currently default to four hours until species-specific timings are decided.

## Needs

Hunger and dirtiness exist as hidden internal simulation values only. They are not intended for visible status bars.

The UI contract is:

- hunger threshold -> hungry expression + food thought bubble
- dirt threshold -> dirty expression + bath thought bubble
- feed/clean action -> happy state

Thresholds and rates are configurable through `NeedTuning`.

## Movement

Pet positions use normalized coordinates from 0 to 1, independent of screen resolution.

`PetMovementEngine` generates autonomous room-local destinations, direction, and move duration. Compose can later animate the pet between the returned start/end points.

Rooms currently defined:

1. Playroom
2. Kitchen
3. Bathroom
4. Garden

V0.1 plans movement inside one room. Automatic room-to-room travel can be layered on later.

## Persistence

`PetEngineRepository` defines storage operations for:

- hatch history and pity counters
- current active egg
- owned pet instances

Two implementations are included:

- `InMemoryPetEngineRepository` for unit tests and isolated development.
- `AndroidPetEngineRepository`, a durable local SQLite implementation ready for later app integration.

The Android repository stores the active egg using absolute start/end timestamps, so incubation survives app closure, process death, and device restart. It also persists each individual pet's room, position, hidden hunger/dirt values, current visual behaviour, hatch date, and pity/duplicate history.

## Asset binding

`PetAssetContract.kt` defines logical visual states without importing any drawable resources yet:

- idle
- blink
- walking left/right
- happy
- hungry
- dirty
- sleepy
- food/bath thought bubbles
- all eight egg hatch stages

The generated PNG asset packs can therefore be assigned later without changing the engine rules.

## Integration target

For the later Evelune update:

1. Import the pet/environment asset packs into Android resources.
2. Instantiate `AndroidPetEngineRepository` and `PetEngine` from the app layer.
3. Bind `PetAssetKey` / hatch visual states to concrete resources.
4. Connect the existing Pets UI to `PetEngine`.
5. Animate `MovementPlan` results in Compose.
6. Add feed/clean/play interactions and richer multi-frame walk animations.
