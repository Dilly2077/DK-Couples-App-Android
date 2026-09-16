# DK Together

An Android-first private couples app combining relationship activities, home-screen connection and a shared virtual-pet loop in one dark, playful interface.

This repository is an original implementation. It does not contain Paired or Widgetable source code, branding, artwork, or proprietary content.

## V0.1 features

- Five-section UI: Home, Explore, Together, Timeline and Us.
- Local onboarding with relationship start date.
- 100 original daily relationship questions.
- 50 original date ideas and a date roulette.
- Quick This-or-That game and repeatable challenges.
- Mood and status sharing with local Partner A / Partner B demo mode.
- Shared virtual pet with hunger, happiness, cleanliness, energy and affection.
- Hearts reward currency.
- Widget-note history and a real Android home-screen relationship widget.
- Persistent interaction history using Room.
- Persistent preferences using DataStore.
- Memories and relationship timeline.
- Relationship duration and clearly-labelled heartbeat estimate.
- Descriptive local relationship insights.
- GitHub Actions APK build.

## Build

Requirements:

- JDK 17
- Android SDK platform 37
- Android build tools 36.0.0
- Gradle 9.6.0

From the repository root:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

The debug APK is generated at:

`app/build/outputs/apk/debug/app-debug.apk`

A GitHub Actions run also uploads `DKTogether-debug-apk` as a downloadable build artifact.

## Architecture

- Kotlin + Jetpack Compose + Material 3
- Room for interaction history
- Preferences DataStore for profile, pet and lightweight state
- MVVM-style `AppViewModel`
- Repository boundary around persistence
- Classic Android `RemoteViews` widget for broad launcher compatibility

See `ARCHITECTURE.md` and `CHECKPOINT.md`.

## Privacy

V0.1 is local-first and does not include analytics, ad SDKs, telemetry, contact scraping or location collection. Real partner-to-partner sync is not enabled yet. See `PRIVACY.md`.

## Notes

The current package name is `com.dk.together` and the temporary product name is **DK Together**. Both can be changed before Play Store publication.
