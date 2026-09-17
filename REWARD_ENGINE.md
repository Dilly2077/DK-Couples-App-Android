# Evelune Reward Engine v0.1

## Purpose

The reward engine gives meaningful Evelune activity a persistent progression result while keeping balancing values separate from app screens.

Three progression outputs are supported:

- **Evelune XP** — shared/global app progression.
- **Pet Bond XP** — progression for the specific pet directly cared for or played with.
- **Pet Coins** — pet-economy currency, primarily earned from direct pet interaction and intended for the pet shop.

## Architecture

Core package:

`app/src/main/java/com/dk/together/rewards/`

Files:

- `RewardModels.kt` — event, grant, balance, level and ledger models.
- `RewardPolicy.kt` — all provisional balancing values in one place.
- `RewardRepository.kt` — persistence interface plus in-memory test implementation.
- `RewardEngine.kt` — idempotency, daily-cap checks, level calculations and reward application.
- `AndroidRewardRepository.kt` — SQLite-backed persistent wallet, pet Bond XP and immutable event ledger.
- `RewardHooks.kt` — canonical feature-facing methods and stable event IDs.

## Event model

Supported event types:

- question answer;
- question-chat message;
- daily card completion;
- daily challenge completion;
- daily game completion;
- sticky note sent;
- memory created;
- generic conversation message;
- pet feed;
- pet clean;
- pet play;
- pet hatch;
- generic meaningful pet interaction.

Every event has a stable event ID. Recording is idempotent: submitting or retrying the same underlying action cannot grant rewards twice.

## Meaningful-action rule

Rewards are issued after an action succeeds, not when a control is merely tapped.

Examples:

- first submission of a question can reward;
- editing that existing answer does not reward again;
- sending a new chat message can reward;
- reprocessing the same message ID cannot reward again;
- future pet care should mark an action meaningful only when the interaction really occurred (for example, a valid completed feed/clean/play loop rather than repeatedly touching the target).

`RewardEvent.meaningful = false` records the attempted event with zero reward so it cannot later be replayed for credit.

## Anti-farming

Two protections exist in v0.1:

1. stable event IDs prevent duplicate rewards;
2. repeatable low-cost social actions can have per-day grant caps.

The provisional policy currently caps question-chat messages, generic conversation messages, sticky notes and memory creation. Pet-care spam control should primarily be enforced by the future interaction/cooldown/need logic so legitimate care is still rewarded.

## Provisional balance values

These are intentionally not final and live only in `RewardPolicy.default()`.

| Action | Evelune XP | Pet Bond XP | Pet Coins |
| --- | ---: | ---: | ---: |
| Question answer | 25 | 0 | 0 |
| Question chat message | 3 | 0 | 0 |
| Daily card | 25 | 0 | 0 |
| Daily challenge | 30 | 0 | 0 |
| Daily game | 20 | 0 | 0 |
| Sticky note | 5 | 0 | 0 |
| Memory created | 20 | 0 | 0 |
| Conversation message | 2 | 0 | 0 |
| Feed pet | 8 | 12 | 5 |
| Clean pet | 10 | 15 | 6 |
| Play with pet | 8 | 12 | 5 |
| Hatch pet | 20 | 5 | 3 |
| Generic pet interaction | 4 | 5 | 2 |

Change these values later in the policy only; no database migration is needed.

## Level curve

Both global Evelune progression and individual Pet Bond progression use the same provisional curve:

- Level 1 -> 2 requires 100 XP.
- Each following level requires 25 more XP than the previous level.

The curve is centralized in `RewardProgression` and can be replaced/tuned later.

## Persistence

`AndroidRewardRepository` stores:

- one global reward wallet (`evelune_xp`, `pet_coins`);
- per-pet `bond_xp`;
- an immutable reward-event ledger containing decisions and grants.

Database: `evelune_rewards.db`.

The ledger includes zero-grant capped/non-meaningful events, which preserves idempotency and gives future debugging/analytics a reliable history without collecting remote telemetry.

## Existing feature integrations

Already wired on `reward-engine-v0.1`:

- first question answer submission -> Evelune XP;
- first daily-card submission -> Evelune XP;
- first daily-challenge submission -> Evelune XP;
- continued question-chat messages -> Evelune XP, with anti-spam cap;
- generic stored conversation messages -> Evelune XP, with anti-spam cap;
- memory creation through both current local timeline stores -> Evelune XP.

Updates to an already submitted question/card/challenge do not issue another reward.

## Future feature hooks already available

No redesign is needed when these features become interactive:

- `stickyNoteSent(...)`
- `dailyGameCompleted(...)`
- `petFed(...)`
- `petCleaned(...)`
- `petPlayedWith(...)`
- `petHatched(...)`
- `petInteraction(...)`

The upcoming pet drag/drop and care flows should call these only after their interaction completes successfully.

## Tests

`RewardEngineTest.kt` covers:

- first grant vs duplicate event;
- global XP + Pet Bond XP + Pet Coins from pet care;
- independent pet levelling;
- non-meaningful zero-grant events;
- daily cap behaviour and capped-event idempotency;
- increasing level thresholds.

## Next development chunk

After this reward engine passes CI, proceed to the universal pet interaction layer:

1. press/hold pet;
2. drag pet around room;
3. detect valid interaction targets;
4. highlight target while hovering;
5. snap/drop pet onto target;
6. transition to feeding/cleaning/play interaction;
7. emit the appropriate reward hook only after successful completion.
