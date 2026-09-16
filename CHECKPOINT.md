# CHECKPOINT — DK Together V0.2

## Implemented

- Buildable Android project targeting modern Android.
- Reworked higher-contrast purple/lilac design system with brighter text, clearer card separation and less visual clutter.
- Cleaner Home, Explore and Together screen hierarchy plus polished bottom navigation.
- Original adaptive launcher icon with monochrome themed-icon layer.
- Android 12+ branded launch splash using the new icon.
- Onboarding for two names and relationship start date.
- 100 bundled original questions.
- 50 bundled original date ideas.
- Daily question answer persistence and rewards.
- This-or-That quick game and daily challenge loop.
- New playable Fruit Merge mini-game with stacking/merging, scoring and shared-history score banking.
- Local demo-partner switch so both sides can be exercised on one phone.
- Mood and status updates.
- Expanded shared virtual pet world with five needs, gradual need decay and contextual thought bubbles.
- Animated in-app pet movement.
- Four pet environments: Living room, Kitchen, Bathroom and Outdoors.
- Context-sensitive pet care actions including feeding, treats, washing, splashing, playing, cuddling, naps and exploring.
- Pet room changes and care actions saved into the shared activity history.
- Hearts currency.
- Couple widget note composer/history.
- Android couple home-screen widget showing names, days together and latest note.
- New dedicated shared-pet home-screen widget with pet sprite, room, thought bubble and need summary.
- Pet widget supports direct Feed, Wash and Play actions which also persist back into app state.
- Memory creation and complete chronological interaction timeline.
- Room interaction database.
- DataStore profile/pet/preferences storage.
- Relationship time breakdown and 70 BPM heartbeat estimate labelled as an estimate.
- Local descriptive insights.
- Unit tests for relationship calculations plus pet decay/thought logic.
- GitHub Actions workflow that tests, builds and uploads the debug APK.

## Platform limitation to remember

Android apps cannot force Pixel Launcher to place a newly sideloaded app icon onto the Home screen or automatically navigate the launcher to its location after installation. That behaviour belongs to the launcher/install source and user settings. The app now correctly provides a launcher activity, adaptive/round/themed icons and a proper splash screen, which is the app-controlled part of the install/launch experience.

## Not implemented yet

- Real two-device accounts or pairing.
- Remote sync / E2EE. The current Partner Mode is local simulation only.
- Push notifications.
- Photo uploads and doodles.
- Rich configurable photo/status widgets.
- Continuous animated movement inside Android home-screen widgets (standard RemoteViews widgets are snapshot/action surfaces rather than a continuously rendered game scene).
- Full quizzes and two-device answer reveal protocol.
- Multiple pet species, growth stages, inventory, clothing and room decoration.
- More arcade/competitive mini-games and leaderboards.
- Guided journeys and achievements.
- Shared calendar.
- Astrology input/content beyond the reserved entertainment section.

## Important locations

- `app/src/main/java/com/dk/together/ui/` — Compose UI and ViewModel.
- `app/src/main/java/com/dk/together/ui/screens/TogetherScreen.kt` — animated pet world and care UI.
- `app/src/main/java/com/dk/together/ui/screens/ExploreScreen.kt` — Fruit Merge and couple activities.
- `app/src/main/java/com/dk/together/data/` — Room/DataStore/repository.
- `app/src/main/java/com/dk/together/widget/` — couple and shared-pet widgets.
- `app/src/main/res/layout/pet_widget.xml` — interactive pet widget layout.
- `app/src/main/res/drawable/ic_launcher_foreground.xml` — launcher artwork.
- `app/src/main/assets/questions.json` — original prompt library.
- `app/src/main/assets/date_ideas.json` — date idea library.
- `.github/workflows/android.yml` — CI APK build.

## Build

Use JDK 17, Gradle 9.6.0, Android platform 37.0 and build-tools 37.0.0.

`gradle :app:testDebugUnitTest :app:assembleDebug`

Expected APK: `app/build/outputs/apk/debug/app-debug.apk`

## Recommended next step

The next major engineering step should be real partner sync. Add a `SyncRepository` implementation backed by an authenticated remote relationship space, then make pet state, widget payloads, questions and messages synchronise between two devices. Only after that should the pet system be expanded into multiple species, growth stages, furniture/room decoration and additional multiplayer mini-games.
