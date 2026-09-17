# Evelune Pet Interaction Layer v0.1

## Purpose

This chunk implements the universal physical interaction foundation for Evelune pets.

The intended player flow is:

1. press and hold a pet;
2. pet enters the carried state;
3. drag the pet around the room;
4. valid interaction zones react when the pet is hovered over them;
5. releasing over a valid zone snaps the pet into the target position;
6. an interaction transition is emitted;
7. the destination feeding, cleaning or play flow takes over;
8. the reward engine is called only after that destination flow completes successfully.

The interaction layer does **not** grant XP or Pet Coins on drop. This prevents farming by repeatedly dragging a pet onto an object without completing the actual care interaction.

## Core files

### Pure Kotlin state engine

`app/src/main/java/com/dk/together/pets/interaction/`

- `PetInteractionModels.kt`
- `PetInteractionEngine.kt`

The engine is independent of Compose and Android UI. It owns:

- carried/hovering/idle/transition states;
- normalized room coordinates;
- room-scoped target detection;
- valid/invalid drop handling;
- snap positions;
- transition intents for feed, clean and play.

### Compose layer

`app/src/main/java/com/dk/together/ui/PetDragDropLayer.kt`

Reusable UI behaviour:

- long-press to pick up;
- drag movement;
- carried visual scaling;
- target hover detection;
- target highlight slot;
- valid drop snapping;
- transition callback;
- reusable pet-content slot so the v0.3 carried sprite can replace the current placeholder art later without changing interaction logic.

## Default interaction targets

### Kitchen

- `KITCHEN_CHAIR_1` -> Feed
- `KITCHEN_CHAIR_2` -> Feed

The chair targets snap the pet into a seated position. The Kitchen feeding chunk will then present food inventory and the feeding interaction.

### Garden

- `GARDEN_HOT_TUB` -> Clean
- `PLAY_BALL` -> Play

The hot tub target is the primary cleaning entrance. Dropping a pet there should transition to the zoomed bath/sponge interaction in the next cleaning chunk.

### Playroom

- `PLAY_BALL` -> Play
- `PLAY_ROPE` -> Play
- `PLAY_PLUSH` -> Play
- `PLAY_FRISBEE` -> Play

The first production play chunk only needs two polished toy interactions even though the target contract supports more.

### Bathroom

No launch target is defined. The bathroom remains reserved for later expansion because the outdoor hot tub is the launch cleaning mechanic.

## Coordinate system

All hitboxes and positions are normalized from `0.0..1.0` rather than pixel-based.

This makes interaction geometry independent of device resolution and lets the same targets scale across phones/tablets.

Example:

```kotlin
NormalizedRect(
    left = 0.58,
    top = 0.32,
    right = 0.97,
    bottom = 0.78,
)
```

represents the Garden hot-tub interaction zone as a percentage of the room surface.

The final bounds should be tuned once the production v0.3 environment binaries are wired into the runtime UI.

## Reward boundary

A successful **drop is not a completed care action**.

The transition emitted by the drag/drop layer should open the appropriate activity:

- `FEED` -> Kitchen feeding flow;
- `CLEAN` -> hot-tub cleaning flow;
- `PLAY` -> selected toy interaction.

Only successful completion of that activity should call the existing reward hooks:

- `petFed(...)`
- `petCleaned(...)`
- `petPlayedWith(...)`

This preserves the anti-farming design from `REWARD_ENGINE.md`.

## Visual state mapping

During long-press/drag the parent pet renderer receives `carried = true` and should render the v0.3 `*_carried.png` state when production assets are imported.

After a valid Kitchen drop, the next screen should render `*_seated.png` before feeding.

Cleaning/play destinations later switch to `*_bathing.png` and `*_playing.png` respectively.

## Tests

`PetInteractionEngineTest.kt` covers:

- press/hold -> carried state;
- hover entry;
- hover exit;
- Kitchen feed transition;
- invalid/free drop;
- Garden hot-tub clean transition;
- Playroom play transition;
- room scoping;
- transition completion returning to idle.

## Next chunk

Build the first complete care loop: **Kitchen feeding**.

That chunk should:

1. place the production Kitchen background;
2. use the existing chair/drop-zone interaction contract;
3. snap the pet into the seated state;
4. show persistent food inventory;
5. let the user choose/drag food to the pet;
6. play the eating state/reaction;
7. reduce hunger only after successful feeding;
8. consume one inventory item;
9. call `RewardHooks.petFed(...)` once;
10. return the pet to the room.
