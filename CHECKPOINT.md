# CHECKPOINT — DK Together V0.1

## Implemented

- Buildable Android project structure targeting modern Android.
- Original dark-purple / lavender UI with five main sections.
- Onboarding for two names and relationship start date.
- 100 bundled original questions.
- 50 bundled original date ideas.
- Daily question answer persistence and rewards.
- This-or-That quick game and daily challenge loop.
- Local demo-partner switch so both sides can be exercised on one phone.
- Mood and status updates.
- Shared virtual pet with five needs and five care actions.
- Hearts currency.
- Widget note composer/history.
- Android home-screen widget showing couple names, days together and latest note.
- Memory creation and complete chronological interaction timeline.
- Room interaction database.
- DataStore profile/pet/preferences storage.
- Relationship time breakdown and 70 BPM heartbeat estimate labelled as an estimate.
- Local descriptive insights.
- Unit tests for relationship calculations.
- GitHub Actions workflow that tests, builds and uploads the debug APK.

## Not implemented yet

- Real two-device accounts or pairing.
- Remote sync / E2EE.
- Push notifications.
- Photo uploads and doodles.
- Rich multi-size widget configuration.
- Full quizzes and answer reveal protocol.
- Pet inventory, multiple species and room decoration.
- Guided journeys and achievements.
- Shared calendar.
- Astrology input/content beyond the reserved entertainment section.

## Important locations

- `app/src/main/java/com/dk/together/ui/` — Compose UI and ViewModel.
- `app/src/main/java/com/dk/together/data/` — Room/DataStore/repository.
- `app/src/main/java/com/dk/together/widget/` — Android widget.
- `app/src/main/assets/questions.json` — original prompt library.
- `app/src/main/assets/date_ideas.json` — date idea library.
- `.github/workflows/android.yml` — CI APK build.

## Build

Use JDK 17, Gradle 9.6.0, Android platform 37 and build-tools 36.0.0.

`gradle :app:testDebugUnitTest :app:assembleDebug`

Expected APK: `app/build/outputs/apk/debug/app-debug.apk`

## Recommended next step

Add a `SyncRepository` abstraction plus authenticated pairing before expanding content further. After sync works, build widget photo/doodle/status payloads on top of the same encrypted relationship-space model.
