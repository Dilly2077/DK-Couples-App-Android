# Evelune Pet Interaction Checkpoint

Branch: `pet-interaction-v0.1`

Base: `reward-engine-v0.1`

## Completed in this chunk

- Added a pure Kotlin interaction state machine for pet press/hold, carrying, hover detection, valid/invalid drop handling, snapping and transition intents.
- Added normalized interaction rectangles so the same geometry scales across devices.
- Added room-scoped default targets for Kitchen feeding chairs, Garden hot tub, Garden play, and Playroom toys.
- Added a reusable Compose `PetDragDropLayer` with long-press pickup, carried-state scaling, pointer dragging, hover highlighting, snap/drop behavior and transition callbacks.
- Added unit coverage for the universal interaction flow.
- Preserved the reward boundary: drag/drop itself never awards XP or Pet Coins. Feeding/cleaning/play rewards happen only after the destination care flow successfully completes.

## Important architecture

`PetInteractionEngine` is UI-independent. `PetDragDropLayer` is a generic renderer/controller bridge and takes pet/target content slots, so production v0.3 assets can replace placeholders without rewriting the interaction logic.

A successful valid drop emits `PetInteractionTransition` with one of:

- `FEED`
- `CLEAN`
- `PLAY`

and includes the target plus snap location.

## Production art dependency

The exact interaction rectangles are provisional until the v0.3 environment binaries are imported into Android runtime resources. Once imported, tune the normalized target rectangles against the approved Kitchen, Garden Spa and Playroom artwork. Do not redesign the engine for this; only adjust `DefaultPetInteractionTargets` values.

## Next chunk

Kitchen Feeding v0.1:

1. wire the approved Kitchen runtime environment;
2. enter Kitchen via room navigation;
3. drag pet to a feeding chair;
4. snap to seated state;
5. open food inventory;
6. select/drag food to pet;
7. play eating/reaction state;
8. update hunger;
9. consume inventory item;
10. call `RewardHooks.petFed(...)` exactly once after completion;
11. return to Kitchen world state.

After Kitchen Feeding: Hot-Tub Cleaning, then Play interactions, then Shop/Inventory expansion.
