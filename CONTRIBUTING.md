# Contributing to Game Launcher PRO

Thank you for your interest in contributing to **Game Launcher PRO** (`willygailo/Game-Launcher`)! Every bug report, device compatibility test, game tuning preset, documentation improvement, and pull request helps make Android gaming smoother and more reliable for the community.

> **Looking for the Tagalog build SOP?** See [CLEAN_BUILD.md](CLEAN_BUILD.md) for the full 6-stage Tagalog build and verification checklist.

---

## Ways to Contribute

- **Report a Bug:** Open an issue using the [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.yml) with your device model, Android version (14–16), privilege mode (Non-Root / Shizuku / Root), and relevant `adb logcat` output.
- **Request a Feature:** Use the [Feature Request template](.github/ISSUE_TEMPLATE/feature_request.yml) to propose new performance channels, HUD improvements, or OEM profiles.
- **Submit Game Compatibility Reports:** Use the [Game Compatibility template](.github/ISSUE_TEMPLATE/game_compatibility.yml) to share frame-rate behavior, device specs, or config issues for specific titles.
- **Contribute Code, Presets, or Docs:** Fork the repository, build and verify your changes locally, and submit a Pull Request using our [Pull Request template](.github/PULL_REQUEST_TEMPLATE.md).
- **Report Security Vulnerabilities:** **Do not** open a public issue for security flaws. Follow the responsible disclosure process in [SECURITY.md](SECURITY.md).

---

## Development Environment Setup

### Prerequisites
- **JDK:** OpenJDK 17 or Eclipse Temurin 17
- **Android SDK:** `compileSdk 36`, `targetSdk 36`, `minSdk 34` (Android 14–16)
- **Android NDK:** `27.0.12077973` (supports 16 KB ELF page-size alignment)
- **Build Tool:** Gradle Wrapper included inside `android/` (`./gradlew`)
- **ADB:** Android Platform Tools (for live device testing)

### 1. Fork & Clone
```bash
git clone https://github.com/<your-username>/Game-Launcher.git
cd Game-Launcher
git remote add upstream https://github.com/willygailo/Game-Launcher.git
```

### 2. Configure Local SDK Paths
Create `android/local.properties` (git-ignored) pointing to your local Android SDK and NDK installations:
```properties
sdk.dir=/path/to/Android/Sdk
ndk.dir=/path/to/Android/Sdk/ndk/27.0.12077973
```

---

## Build & Verification Pipeline

Before opening a Pull Request, run the verification pipeline from inside `android/`:

```bash
cd android

# 1. Compile Java sources & AIDL interfaces
./gradlew compileDebugJavaWithJavac

# 2. Run Android Lint static analysis
./gradlew lintDebug

# 3. Run local unit tests
./gradlew testDebugUnitTest

# 4. Assemble debug APK
./gradlew assembleDebug
```

Or run the full validation chain in one command:
```bash
cd android && ./gradlew compileDebugJavaWithJavac lintDebug testDebugUnitTest assembleDebug
```

### Native C++ (`android/app/src/main/cpp/`) Changes
If your PR touches native C++ code or `CMakeLists.txt`, verify native compilation across ABIs (`arm64-v8a`, `armeabi-v7a`, `x86_64`):
```bash
cd android
./gradlew buildCMakeDebug[arm64-v8a]
# Or run the standalone multi-ABI script:
./build_native_libs.sh
```

---

## Shizuku & Physical Device Testing

When testing changes that touch refresh-rate enforcement, touch latency, config patching, or Shizuku IPC on a physical device:

1. Follow the [Shizuku setup guide in README.md](README.md#-step-by-step-guide-how-to-use-shizuku-with-game-space) to start Shizuku via Wireless Debugging and authorize `com.gamebooster.app`.
2. Install and smoke-test the debug APK over ADB:
   ```bash
   # Install debug build
   adb install -r android/app/build/outputs/apk/debug/app-debug.apk

   # Launch MainActivity
   adb shell am start -n com.gamebooster.app/.ui.activities.MainActivity

   # Confirm Shizuku server remains alive
   adb shell ps -A | grep -i shizuku

   # Inspect logs for crashes or Shizuku diagnostics
   adb logcat -d -s AndroidRuntime:E GameBooster:V ShizukuDiag:V | tail -n 40
   ```
3. Verify that your feature degrades cleanly when Shizuku is **stopped** (Non-Root fallback mode).

---

## Engineering & Safety Guidelines

All contributions must follow the project's core reliability and safety standards (see [CLEAN_BUILD.md](CLEAN_BUILD.md) and [SECURITY.md](SECURITY.md)):

1. **Dual-Engine Fallback:** Any feature interacting with privileged Android APIs or shell execution must gracefully fall back to standard Android SDK APIs when Shizuku or Root is unavailable.
2. **Shizuku IPC Stability:**
   - Never run `setprop service.adb.tcp.port` or `settings put global adb_wifi_enabled 1` inside background pulses — doing so restarts `adbd` and terminates `shizuku_server`.
   - Always pass `remove = false` to `Shizuku.unbindUserService()` so background services survive network switches.
   - Always call `remoteProcess.destroy()` inside a `finally` block to prevent file descriptor leaks.
3. **Game Account & Config Safety:**
   - Preserve sensitive session/account files (`Active.sav`, `LoginToken.dat`, `guest_uuid.dat`, `login_state.json`) and never overwrite full user account state.
   - Use `ConfigBackupManager` / `SnapshotSystem` so modified settings can be safely rolled back.
4. **Android 14–16 (`minSdk 34` / `targetSdk 36`) Cleanliness:**
   - Avoid obsolete `Build.VERSION.SDK_INT` checks for API levels below 34.
   - Document any required `@SuppressLint` annotations with a clear inline rationale.
5. **No Secrets Committed:** Never commit signing keystores (`keystore.properties`, `*.jks`), API keys, or local `.env` files.

---

## Pull Request Workflow

1. Create a descriptive branch from the latest `main`:
   ```bash
   git checkout main
   git pull upstream main
   git checkout -b feat/your-feature-name
   ```
   Use clear branch prefixes such as `feat/`, `fix/`, `perf/`, `docs/`, or `refactor/`.
2. Keep commits focused, atomic, and descriptive (Conventional Commits style preferred, e.g., `fix(shizuku): guard null binder during reconnect`).
3. Push your branch and open a Pull Request against `main`.
4. Fill out every applicable section of the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md), including device test details when relevant.

### Co-Authored Commits & Pair Programming

Collaborated with another contributor on a bug fix, device profile, or review? Credit all contributors directly in your commit message using GitHub's standard `Co-authored-by:` trailer (after at least one blank line at the bottom of the commit message):

```text
fix(display): stabilize 120Hz lock on Android 16 panels

Ensures SurfaceFlinger refresh-rate fallback is preserved when VRR toggles.

Co-authored-by: Partner Name <partner-email@users.noreply.github.com>
```

GitHub automatically links co-authored commits to every contributor's profile once the PR is merged into `main`.

---

## License

By contributing to **Game Launcher PRO**, you agree that your contributions will be licensed under the project's [MIT License](LICENSE).