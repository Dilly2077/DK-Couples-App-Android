# Evelune Android sideload update signing

The sideload/development APK line uses a stable non-production signing identity so Android can update an installed Evelune build in place.

Rules for every future sideload APK:

1. Keep `applicationId = "com.dk.evelune"`.
2. Keep the `eveluneDevUpdate` signing config and `app/signing/evelune-dev-update.jks` unchanged.
3. Increment `versionCode` for every APK handed to the user.
4. Never regenerate or replace the development update key for this sideload line.
5. A Play Store production release should use a separate production/Play App Signing identity.

The stable development certificate SHA-256 is:

`63:46:43:CA:8C:44:4E:7C:58:DF:E4:E7:B8:60:8A:16:60:73:A5:0C:A4:7B:1E:72:68:43:A9:41:40:97:EF:CE`

Historical v0.8 and older CI APKs were produced with ephemeral GitHub runner debug keys, so they cannot be updated in-place by a newly stable-signed APK. v0.9.0 is the migration point: after installing it once, all later sideload builds can update over it when these rules are preserved.
