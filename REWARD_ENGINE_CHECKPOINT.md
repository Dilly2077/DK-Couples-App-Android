# Evelune Reward Engine — checkpoint

Last updated: 17 September 2026

Working branch: `reward-engine-v0.1`
Stacked on: `pet-assets-v0.3`
Draft PR: #4

## Completed in this chunk

- Added persistent global Evelune XP.
- Added persistent Pet Coin wallet.
- Added per-pet Bond XP and calculated pet levels.
- Added increasing provisional level threshold curve.
- Added immutable reward event ledger.
- Added idempotent event IDs so updates/retries cannot award twice.
- Added daily anti-spam caps for repeatable low-cost social actions.
- Added `meaningful` gating for future no-op/cooldown pet interactions.
- Added canonical reward hooks for questions, chats, cards, challenges, games, sticky notes, memories, pet feed, pet clean, pet play, pet hatch and generic pet interaction.
- Wired existing question answer, card, challenge, question-chat, stored chat and memory flows into rewards.
- Added pure JVM unit tests.
- Added CI coverage for `reward-engine-v0.1`.

## Important product rules preserved

- Meaningful app activity earns Evelune XP.
- Direct pet care additionally earns Pet Bond XP and Pet Coins.
- Pet Coins are intended for the pet shop.
- Editing an already completed answer/card/challenge does not farm XP.
- Future feed/clean/play flows should call reward hooks only after a real interaction completes.
- Balancing numbers are provisional and centralized in `RewardPolicy.kt` for later tuning.

## Next chunk after CI

Universal pet interaction layer:

1. press and hold a pet;
2. lift/carried visual state;
3. drag around room;
4. hover detection against interaction targets;
5. target highlight using `valid_drop_glow`;
6. drop/snap into target anchor;
7. launch feed/clean/play focused interaction;
8. emit reward hook only after successful completion.

The Kitchen feeding seat and Outdoor Hot-Tub Spa are the first production interaction targets.
