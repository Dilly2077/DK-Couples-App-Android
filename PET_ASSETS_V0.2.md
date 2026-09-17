# Evelune Pet Assets v0.2 — Care + Economy

## Purpose

Chunk 1 establishes the complete asset contract for the first production pet-care loop: drag pets around their environments, drop them onto context-sensitive interaction zones, then enter focused feeding, cleaning or play interactions.

No visible hunger/cleanliness/status bars are used. Needs are communicated by pet expression, behaviour and thought bubbles.

## Audit result

Two earlier generated packs are known from the project history:

- `Evelune-pet-assets-v0.1.zip`
  - Mocha/Puppy and Lumi/Kitten with 8 PNG states each
  - 8 egg hatch-stage PNGs
  - props/thought bubbles, previews, `manifest.json`, `animation_presets.json`, `README.md`
  - `room_playroom.png`
  - `kitchen_feeding_room.png`
  - `bathroom_cleaning_room.png`
  - `garden_outdoor_room.png`
- `Evelune-pet-expansion-v0.1.zip`
  - Bunny, Duckling, Hedgehog, Ferret, Otter, Fox, Red Panda, Axolotl, Tiger and Dragon
  - 8 transparent PNG states per species
  - animated GIF preview, per-species manifest, rarity/hatch metadata

Those packs were generated locally but were never committed to this repository. The current `main` and `pet-engine-v0.1` trees contain no PNG binaries. Current visible pets/environments in the app are Compose-drawn placeholders; `PetAssetContract.kt` contains logical state keys only.

The earlier environment concepts therefore count as **existing designs requiring binary recovery/re-import**, not as GitHub-available runtime assets.

## World layout

### Evelune Home

#### Kitchen
Primary feeding location.

Required layers:

- `kitchen_bg`
- `kitchen_back_furniture`
- `kitchen_chair_1`
- `kitchen_chair_2`
- `kitchen_table_front`
- `kitchen_foreground`

Interaction:

1. User presses and holds a pet.
2. Pet enters carried state and can be dragged.
3. A valid chair subtly highlights as the pet approaches.
4. Releasing over the chair snaps the pet into a seated pose.
5. Food inventory appears.
6. Food is dragged/tapped into the feeding target.
7. Eating animation/reaction runs.
8. Reward event is emitted.

### Evelune Garden

#### Meadow
General free-roam area.

#### Spa / Hot Tub
Primary cleaning location.

Required layers:

- `garden_bg`
- `spa_back`
- `hot_tub_back_rim`
- `water_overlay`
- `bubble_overlay`
- `hot_tub_front_rim`
- `garden_foreground`

Interaction:

1. Pet is dragged over the hot tub.
2. Tub/water reacts to hover.
3. Dropping the pet opens a close-up bath scene.
4. User drags a sponge over the pet.
5. Sponge strokes clear an invisible dirt-mask coverage map and visible dirt overlays.
6. At completion threshold, the pet enters clean/happy reaction.
7. Reward event is emitted.

The Bathroom remains a valid logical room for future use, but the Garden hot tub is the launch cleaning mechanic.

### Playroom / Garden play area

Initial interaction props:

- ball
- rope toy
- plush toy
- frisbee
- toy basket

The first implementation only needs two polished playable interactions; the asset contract reserves all five.

## Required pet visual states

Existing v0.1 logical states:

- `idle_front`
- `blink`
- `walk_left`
- `walk_right`
- `happy`
- `hungry`
- `dirty`
- `sleepy`

New v0.2 care states:

- `carried`
- `seated`
- `eating`
- `bathing`
- `playing`
- `level_up`

The carried state must work visually while the pet is lifted from the floor and dragged. The seated state must align with the Kitchen chair anchor. Bathing should be composable with water/bubble overlays rather than requiring a unique full-scene render for every pet.

## Food launch set

The initial shop/inventory art set is:

1. kibble
2. fish
3. carrot
4. berry bowl
5. apple slices
6. milk bowl
7. biscuit
8. salad bowl
9. cupcake treat
10. premium feast tray

Food behaviour, cost, satiation and reward scaling are data values and must not be baked into the art.

## Cleaning assets

- sponge
- soap bottle
- bubbles
- splash effect
- clean sparkle effect
- `dirt_overlay_01`
- `dirt_overlay_02`

Dirt should be layered independently from the pet base sprite so cleaning progress can remove it gradually.

## Economy assets

- Pet Coin icon
- XP burst
- floating XP reward
- floating Pet Coin reward
- level-up badge/effect
- shop tile
- inventory slot
- valid-drop highlight/glow

## Reward architecture dependency

Chunk 2 will distinguish:

- **Evelune XP** — shared/global progression; awarded for meaningful actions anywhere in the app, including daily questions, sticky notes, cards/games, date/memory actions and pet care.
- **Pet Bond XP** — progression for the specific pet directly interacted with.
- **Pet Coins** — earned through meaningful pet interactions and later selected app activities; spent in the pet shop.

Repeated taps or no-op interactions must not farm rewards. Reward events are tied to completed meaningful actions.

## Source and runtime paths

Source-art organization:

```text
design/pets/source/
  environments/
    kitchen/
    garden_spa/
    playroom/
    bathroom/
  pets/
  props/
    food/
    cleaning/
    toys/
  economy/
  ui/
```

Runtime Android drawables will eventually live under:

```text
app/src/main/res/drawable-nodpi/pets/
```

Actual Android resource filenames should be flattened to Android-safe snake_case names when imported.

## Export requirements

- PNG
- transparent background for pets, props, overlays and foreground layers
- environment source artwork at least 1440 px wide
- props generally 512–1024 px source size
- consistent perspective and light direction
- no baked UI text
- consistent pet baseline/anchor metadata
- layered scenes rather than single flattened scenes

## Chunk 1 completion criteria

Chunk 1 is complete when:

- previous generated binaries are recovered/recreated and committed;
- Kitchen, Garden Spa and Playroom are available as layered runtime-ready art;
- the launch food, cleaning, toy and economy props are committed;
- all supported pets have the care-state additions required by the first playable flows;
- every binary maps to a canonical logical key in the manifest;
- previews show correct layer ordering and anchor placement.
