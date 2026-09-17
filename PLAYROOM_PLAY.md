# Evelune Playroom Play v0.1

## Purpose

This chunk turns the Play care action into a persistent gameplay loop on top of the universal pet drag/drop system and reward engine.

## Player flow

1. Open **Play** from Pet World.
2. The first owned pet is moved into `PetRoom.PLAYROOM`.
3. Press and hold the pet to carry it.
4. Drag it onto one of four toy targets: Ball, Rope, Plush or Frisbee.
5. The toy target highlights while hovered.
6. Release to start that toy's mini-game.
7. Complete the toy-specific interaction.
8. `PetEngine.playOnce(...)` records the play care event exactly once and puts the pet into a happy reaction state.
9. `RewardHooks.petPlayedWith(...)` grants the canonical PET_PLAY reward exactly once.
10. The play repository records completion and starts the per-pet cooldown.
11. Return to the Playroom or Pet World.

## Toy mini-games

The launch set is deliberately simple but mechanically distinct:

| Toy | Input | Required successful actions | Asset key |
| --- | --- | ---: | --- |
| Ball | Tap/bounce | 5 | `toy_ball` |
| Rope | Alternating left/right swipes | 6 | `toy_rope` |
| Plush | Tap/cuddle | 4 | `toy_plush` |
| Frisbee | Upward flick | 4 | `toy_frisbee` |

The mini-game logic is kept separate from rendering so the approved v0.3 sprites/animations can replace the temporary emoji/Compose presentation without changing progression or persistence.

## Anti-farming

A successful play starts a hidden **30-minute per-pet cooldown**. This value is provisional and centralized as `PlayEngine.DEFAULT_COOLDOWN_MS`.

During cooldown:

- the pet can still be moved around the room;
- starting another rewarded play session is blocked;
- no XP, Pet Bond XP or Pet Coins are granted;
- no visible need/status bar is shown.

The room communicates cooldown with plain language only when the user tries to start another play session.

## Rewards

A completed meaningful play uses the existing PET_PLAY policy:

- Evelune XP: 8
- Pet Bond XP: 12
- Pet Coins: 5

These remain provisional balancing values in `RewardPolicy`.

## Persistence and retry safety

`AndroidPlayRepository` uses `evelune_play.db` with:

- `play_receipts` — stable interaction IDs and toy/status timestamps;
- `pet_play_state` — the last completed rewarded play time for each pet.

The pet care database already has `pet_care_events`, so `PetEngine.playOnce(...)` cannot apply the same play reaction twice. The reward ledger separately prevents duplicate rewards.

This allows a started interaction to recover safely if the app/process is interrupted between pet mutation, reward emission and play-receipt completion.

## UI

`PersistentPlayroomScreen` is reachable from the **Play** action in `PetSectionScreen`.

It uses the existing `PetDragDropLayer` and existing `PLAY_BALL`, `PLAY_ROPE`, `PLAY_PLUSH` and `PLAY_FRISBEE` drop targets. Each target renders a temporary toy fallback and launches the correct mini-game.

The approved v0.3 PNG binaries still are not physically committed through the GitHub connector, so current pet/toy/room visuals are temporary Compose/emoji fallbacks. The gameplay contracts already use canonical asset keys.

## Tests

`PlayEngineTest.kt` covers:

- all four toy progress trackers;
- rope alternation rules;
- successful PET_PLAY reward values;
- happy reaction state;
- incomplete-activity rejection;
- cooldown blocking;
- cooldown expiry;
- duplicate completion protection;
- interruption recovery;
- Playroom-only validation.

## Next chunk

**Pet Shop + Inventory + Progression Polish v0.1**:

- spend Pet Coins on food;
- persistent shop catalog and pricing;
- inventory quantity updates shared with Kitchen feeding;
- insufficient-funds handling;
- reward/level-up presentation polish;
- expose Evelune XP, Pet Bond XP and Pet Coins in appropriate pet UI without adding pet need bars.
