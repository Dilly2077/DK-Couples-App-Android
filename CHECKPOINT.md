# CHECKPOINT — DK Together V0.3

## Product focus

V0.3 deliberately narrows the app back to the Paired-style couples core. The earlier pet and Fruit Merge prototypes were too rough to justify keeping in the main experience. Shared pets and arcade games are deferred until they can be rebuilt with proper art/animation/physics.

## Implemented

- High-contrast cream/lilac Compose design with clearer reading hierarchy and less clutter.
- Five core destinations: Home, Explore, Discuss, Dates and Us.
- Onboarding for both names and relationship start date.
- Home-screen sticky-note flow with a real Android widget.
- Separate Android daily-question widget.
- Latest note from the other partner shown prominently on Home.
- Daily private question with two-sided answer locking: neither partner's answer is revealed until both have replied.
- Three deterministic daily conversation cards using the same lock/reveal protocol.
- Five-round `Guess Me` game: each partner chooses an A/B preference and predicts the other partner's choice; scores reveal only after both complete all rounds.
- Searchable Explore library for Questions, Cards and Games.
- Large original question/content bank spanning everyday life, humour, memories, values, future, communication, conflict, support, money, home, family/friends, trust/boundaries, affection, intimacy, consensual adult sex/pleasure, growth, long distance and commitment.
- Mature content includes opt-in-style prompts around desire, foreplay, orgasm/climaxing, sexual feedback, boundaries, aftercare and performance pressure. Content is original and not copied from Paired.
- Discuss screen shows only mutually unlocked question/card answers, note history and completed game results.
- Dates screen starts empty and only contains dates explicitly added by a user.
- Removed fake/pre-filled status/mood/note/currency values from active UI.
- Local two-side testing mode for exercising partner flows on one device.
- Room interaction database and DataStore profile/preferences.
- Adaptive/round/themed launcher icon and branded Android splash.
- GitHub Actions runs unit tests, assembles a debug APK and uploads `DKTogether-v0.3-debug-apk`.

## Intentionally removed/deferred from active product

- Pet need bars and pet rooms.
- Pet home-screen widget.
- Prototype Fruit Merge game.
- Hearts/reward economy in the active UI.
- Mood/status prototype.
- Broad fake activity/timeline entries.

The model/store still retains some pet-compatible data structures so a later migration can be easier, but no pet experience is exposed in V0.3.

## Still not implemented

- Real two-device accounts/pairing.
- Remote synchronisation / encrypted relationship space.
- Push notifications.
- Partner device widget updates over the network.
- Photo/doodle widgets.
- Production-grade shared pets with illustrated sprites, real environments, animation/state machines, inventory and room interactions.
- Physics-based arcade games such as a proper falling/merging fruit game.
- Production release signing / Play Store distribution.

## Important locations

- `app/src/main/java/com/dk/together/ui/screens/HomeScreen.kt` — sticky note + daily question + cards + Guess Me.
- `app/src/main/java/com/dk/together/ui/screens/ExploreScreen.kt` — searchable activity library.
- `app/src/main/java/com/dk/together/ui/screens/TogetherScreen.kt` — Discuss/reveal history.
- `app/src/main/java/com/dk/together/ui/screens/TimelineScreen.kt` — empty-by-default Dates screen.
- `app/src/main/java/com/dk/together/model/ContentBanks.kt` — original expanded questions/cards/games.
- `app/src/main/java/com/dk/together/widget/RelationshipWidgetProvider.kt` — sticky-note widget.
- `app/src/main/java/com/dk/together/widget/DailyQuestionWidgetProvider.kt` — daily-question widget.
- `.github/workflows/android.yml` — CI APK build.

## Build

Use JDK 17, Gradle 9.6.0, Android platform 37.0 and build-tools 37.0.0.

`gradle :app:testDebugUnitTest :app:assembleDebug`

Expected APK: `app/build/outputs/apk/debug/app-debug.apk`

## Recommended next engineering step

Implement authenticated pairing plus a `SyncRepository`/remote relationship space so notes, answer locks/reveals, dates and game picks genuinely synchronise between two phones. Keep the pet/arcade layer deferred until that shared-state foundation is reliable.
