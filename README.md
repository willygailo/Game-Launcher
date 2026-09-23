# Game Launcher

An Android game launcher for Android 14 and newer. It finds launchable games,
stores a per-game display preference, and starts games through Android's normal
launcher intent.

## What it does

- Lists launchable games that Android makes visible to the app.
- Reads the display modes reported by Android and only offers those modes.
- Stores a per-game display preference; a game remains in control of its own
  graphics settings, frame rate, account, and files.
- Provides optional, visible overlay and session-monitoring features when the
  relevant Android permissions have been granted.
- Includes arm64-v8a, armeabi-v7a, and x86_64 native libraries built with
  16 KB ELF segment alignment.

## Requirements

- Android 14 (API 34) through Android 16 (API 36)
- A device-reported display mode for any explicit refresh-rate preference
- Optional: notification, overlay, usage-access, or modify-system-settings
  access for the features that request them

The app does not invent display modes, modify another app's files, force a
game's FPS, spoof device identity, or use hidden shell commands.

## Build

```bash
cd android
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The debug APK is written to:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

To build a release, create `android/keystore.properties` with `storeFile`,
`storePassword`, `keyAlias`, and `keyPassword`. Release builds intentionally
do not fall back to the public debug signing key.

## Validation

```bash
cd android
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
zipalign -c -p -v 4 app/build/outputs/apk/debug/app-debug.apk
```

For device testing, install the debug APK on an Android 14, 15, and 16 device
or emulator, then confirm that the launcher discovers a game, accurately shows
its display modes, and launches it normally.

## License

MIT
