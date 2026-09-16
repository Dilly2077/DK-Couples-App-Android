# Architecture

## Layers

`ui/` contains Compose screens, reusable UI and the app-level ViewModel.

`model/` contains pure relationship and pet models plus deterministic relationship calculations.

`data/` contains Room, DataStore and `CoupleRepository`. UI code never talks directly to Room or DataStore.

`widget/` contains the Android home-screen widget provider. A deliberately small SharedPreferences snapshot is mirrored from repository writes because `RemoteViews` updates need a synchronous lightweight data source.

## Persistence

Room stores chronological `InteractionEntity` records for questions, widget notes, moods, statuses, pet events, challenges and memories.

Preferences DataStore stores the profile, relationship start date, current mood/status, pet state, Hearts balance and demo-partner state.

## Future sync

The current repository is local-only. Before multi-device release, introduce a `SyncRepository` interface with local and remote implementations and keep UI/ViewModel APIs unchanged. Private messages, images, doodles and answers should be designed for end-to-end encryption where practical.
