# DK Together

An Android-first private couples app focused on a simple daily relationship loop: leave each other home-screen notes, answer a question, play conversation cards and complete a short partner game.

This repository is an original implementation. It does not contain Paired or Widgetable source code, branding, artwork or proprietary question/game content.

## V0.3 core

- High-contrast cream/lilac Material 3 interface.
- Home screen centred on the latest sticky note and today's activities.
- Sticky-note Android home-screen widget.
- Daily-question Android home-screen widget.
- One daily private couple question. Each answer stays hidden until both sides respond.
- Three daily conversation cards with the same two-sided reveal flow.
- Five-round `Guess Me` game: choose your own A/B answer and predict your partner's; results unlock after both finish.
- Large original content library generated from curated relationship topics, including communication, memories, values, money, conflict, affection, intimacy and consensual adult sexual topics.
- Searchable Explore library for questions, cards and game dilemmas.
- Discuss screen containing only mutually unlocked answers, sticky-note history and completed game results.
- Dates screen starts genuinely empty and contains only special dates entered by the couple.
- Local two-side testing mode so the answer-lock/reveal protocol can be exercised before remote partner sync is added.
- Room persistence, DataStore preferences and GitHub Actions APK builds.
- Existing launcher icon and branded splash screen retained.

## Deliberately deferred

The earlier pet room, need bars, pet widget and prototype Fruit Merge game have been removed from the active product flow. They were not polished enough. The shared-pet/Widgetable-style layer will be rebuilt later with proper art, animation, interactions and game physics instead of shipping placeholder mechanics.

Real two-device pairing/sync is also not implemented yet. V0.3 simulates the two partners locally; it does not claim to synchronise two phones.

## Build

Requirements:

- JDK 17
- Android SDK platform 37.0
- Android build tools 37.0.0
- Gradle 9.6.0

From the repository root:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

The debug APK is generated at:

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions uploads `DKTogether-v0.3-debug-apk` as a build artifact.

## Architecture

- Kotlin + Jetpack Compose + Material 3
- Room for activity/date/note response persistence
- Preferences DataStore for lightweight profile state
- MVVM-style `AppViewModel`
- Repository boundary around persistence and widget snapshots
- Android `RemoteViews` widgets for sticky notes and the daily question

See `ARCHITECTURE.md` and `CHECKPOINT.md`.

## Privacy

This build is local-first and contains no analytics, ad SDKs, telemetry, contact scraping or location collection. See `PRIVACY.md`.

The package name is `com.dk.together` and the working product name is **DK Together**.
