# Evelune pet assets — continue here

Last updated: 17 September 2026

Current working branch: `pet-assets-v0.3`

## What is locked

- Art style: pastel pink/lavender illustrated storybook look. See `ASSET_PACK_V0.3.md`.
- Official room set:
  1. Living Room / Playroom
  2. Kitchen
  3. Outdoor Garden
  4. Outdoor Hot-Tub Spa
- Hot tub is outside and is the launch cleaning location.
- Pets are intended to be press-held, dragged around the room and dropped over interaction targets.
- Kitchen feeding flow: drag pet to feeding seat -> seated state -> food inventory -> feed -> reward.
- Cleaning flow: drag pet to outdoor hot tub -> transition to close-up -> scrub with sponge -> dirt visibly clears -> reward.
- Play uses draggable/interactive toys.
- No visible hunger/cleanliness status bars; expressions/thought bubbles communicate needs.

## Progression model to build after art pack

- Evelune XP: global/shared progression awarded for meaningful actions anywhere in the app, including daily question answers, sticky notes and other substantive couple interactions.
- Pet Bond XP: individual progression for the specific pet receiving direct care/play.
- Pet Coins: pet-economy currency, earned from meaningful pet interactions and selected app activities; used in the pet shop.
- Anti-farming: no reward for redundant no-op taps/repeated actions.

## Current Chunk 1 state

Completed:
- repo audit;
- v0.2 logical asset contract;
- v0.3 style lock;
- room list and gameplay art requirements;
- canonical asset names;
- exact reproducible generation prompts.

Still required before Chunk 1 is fully complete:
- save/import final approved environment binaries into durable repository storage;
- generate the food sheet in v0.3 style;
- generate cleaning/toy/economy sheet in v0.3 style;
- generate Mocha care-state sheet in v0.3 style;
- generate Lumi care-state sheet in v0.3 style;
- generate thought-bubble sheet;
- extract individual transparent PNGs from those sheets;
- make hot-tub layered assets and Kitchen interaction layers;
- map the final filenames in `asset_manifest_v0.3.json`.

## Important warning for future sessions

Earlier food/prop/Mocha/Lumi sheets were generated in a more plastic-looking 3D style before the user re-locked the art direction. They should **not** silently become production assets. Regenerate them using `GENERATION_PROMPTS_V0.3.md` unless the user explicitly approves the old drafts.

## Development sequence after Chunk 1

1. Reward/XP/Coin engine.
2. Universal pet drag/drop + target highlighting.
3. Kitchen feeding + food inventory.
4. Outdoor hot-tub cleaning mini-game.
5. Play interactions.
6. Shop + progression polish.
