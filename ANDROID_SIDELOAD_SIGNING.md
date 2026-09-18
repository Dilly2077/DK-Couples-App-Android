# Evelune Android sideload update signing

The sideload/development APK line uses a stable non-production signing identity so Android can update an installed Evelune build in place.

Rules for every future sideload APK:

1. Keep `applicationId = "com.dk.evelune"`.
2. Keep the `eveluneDevUpdate` signing config and `app/signing/evelune-dev-update.jks` unchanged.
3. Increment `versionCode` for every APK handed to the user.
4. Never regenerate or replace the development update key for this sideload line.
5. A Play Store production release should use a separate production/Play App Signing identity.

The stable development certificate SHA-256 is:

`6D:15:55:42:BC:47:F6:F8:CB:A9:DB:97:91:1F:5D:F8:04:AC:BF:F5:42:D6:31:AA:FE:C3:9A:78:F8:E0:B6:81`

Historical v0.8 and older CI APKs were produced with ephemeral GitHub runner debug keys, so they cannot be updated in-place by a newly stable-signed APK. v0.9.0 is the migration point: after installing it once, all later sideload builds can update over it when these rules are preserved.
