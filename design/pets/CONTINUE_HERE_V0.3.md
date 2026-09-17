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
- exact reproducible generation prompts;
- Step E: v0.3 food sheet generated;
- Step E: cleaning/toy/economy sheet generated;
- Step E: Mocha care-state sheet generated;
- Step E: Lumi care-state sheet generated;
- Step E: thought-bubble sheet generated;
- Step F: all sheets split into individual transparent PNG runtime assets;
- Step F: approved Playroom, Kitchen, Garden and Outdoor Spa exported as runtime WebP backgrounds;
- Step F: Kitchen feeding seat/station/drop-glow interaction assets prepared;
- Step F: hot-tub rear rim, water, bubbles and front-rim compositing overlays prepared;
- final runtime filenames mapped in `asset_manifest_v0.3.json`;
- export structure and archive SHA-256 hashes recorded in `ASSET_EXPORT_V0.3.md`.

The generated runtime pack contains 60 files / 8,809,119 bytes.

## Remaining repository-only item

The exact production binaries exist in the completed runtime export, but the current GitHub connector does not expose a mounted-file/binary-upload argument. Therefore the binary archive itself has not been pushed into GitHub from this chat. Do not regenerate or redesign the pack just because the binary import is pending. Use the filenames and hashes in `ASSET_EXPORT_V0.3.md` to verify the exact export when a binary-capable upload path is available.

Runtime archive SHA-256:
`9459fbe279812513a1072e72f4dce24cb73aedadecf7e02fb4dfefe5375f4309`

Full source+runtime archive SHA-256:
`9bbcf7ea7e379a1a7e233a54174cd9810ba18d985c1ecc11198f16495fc9c00b`

## Important warning for future sessions

Earlier food/prop/Mocha/Lumi sheets were generated in a more plastic-looking 3D style before the user re-locked the art direction. They should **not** silently become production assets. The v0.3 generation described in `ASSET_EXPORT_V0.3.md` supersedes those drafts.

## Development sequence after Chunk 1

The art-production work for Chunk 1 is complete. The next engineering work is:

1. Reward/XP/Coin engine.
2. Universal pet drag/drop + target highlighting.
3. Kitchen feeding + food inventory.
4. Outdoor hot-tub cleaning mini-game.
5. Play interactions.
6. Shop + progression polish.
