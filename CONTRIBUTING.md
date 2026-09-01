# Contributing to Game Launcher PRO

Thank you for your interest in contributing to **Game Launcher PRO**! We welcome bug fixes, architecture improvements, new game configuration presets, translations, and feature proposals from the open-source community.

Please take a few moments to review this guide to ensure a smooth contribution process.

---

## 📜 Table of Contents
1. [Core Project Philosophy & Ban-Safety Guarantee](#-core-project-philosophy--ban-safety-guarantee)
2. [Development Environment Setup](#-development-environment-setup)
3. [Building & Testing](#-building--testing)
4. [Architecture & Coding Guidelines](#-architecture--coding-guidelines)
5. [Submitting a Pull Request](#-submitting-a-pull-request)
6. [Community & Support](#-community--support)

---

## 🛡️ Core Project Philosophy & Ban-Safety Guarantee

Game Launcher PRO operates strictly as an **external system optimizer and hardware accelerator**. To protect all users from anti-cheat flags and account bans:

- ❌ **NEVER inject into game memory** (no ptrace, no memory reading/writing of game processes, no DLL/SO injection).
- ❌ **NEVER patch or modify game executable binaries** (no APK modification, no `.so` hex editing).
- ✅ **ONLY use official Android SDK APIs** (`GameManager`, `DisplayManager`, `PowerManager`, ADPF, `WifiManager`).
- ✅ **ONLY use elevated OS-level shell commands via Shizuku / ADB** (`SurfaceFlinger`, `cmd game`, `cmd power`, `setprop`, `sysctl`, CPU/GPU governors).
- ✅ **ONLY modify standard user-accessible configuration files** (`UserCustom.ini`, `playerprefs.xml`, `Active.sav`) via legal file I/O or Shizuku elevated file permissions.

---

## 💻 Development Environment Setup

### Prerequisites
- **JDK:** OpenJDK 17 or Eclipse Temurin 17 (`java -version` should report 17.x).
- **Android SDK:**
  - `compileSdkVersion`: **36** (Android 16 / Vanilla Ice Cream / Baklava preview)
  - `targetSdkVersion`: **36**
  - `minSdkVersion`: **33** (Android 13)
- **Build Tooling:** Gradle 8.11.1+ (included via Gradle Wrapper `./gradlew`).
- **IDE:** Android Studio (Ladybug / Iguana / Koala / Meerkat) or VS Code with Java/Android extensions.
- **Physical Test Device / Emulator:**
  - Android 13, 14, 15, or 16.
  - [Shizuku](https://shizuku.rikka.app/) installed and running via Wireless Debugging for elevated tier testing.

### Cloning & Setup
```bash
git clone https://github.com/willygailo/Game-Launcher.git
cd Game-Launcher/android
```

---

## 🛠️ Building & Testing

Always verify your changes before submitting a PR:

### 1. Run All Unit Tests
```bash
cd android
./gradlew testDebugUnitTest
```
Ensure all test suites pass with 0 failures.

### 2. Generate Code Coverage Report (JaCoCo)
```bash
./gradlew testDebugUnitTest jacocoTestReport
```
Coverage HTML reports are generated at `android/app/build/reports/jacoco/jacocoTestReport/html/index.html`.

### 3. Run Android Lint Checks
```bash
./gradlew lint
```

### 4. Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK will be located at `android/app/build/outputs/apk/debug/app-debug.apk`.

---

## 📐 Architecture & Coding Guidelines

### 1. Dual-Engine Fallback Principle
Every feature interacting with the Android system MUST support dual-engine execution:
- **Tier 1 (Privileged Tier):** Elevated execution via `ShizukuUserServiceConnector` / `ShizukuExecutor` / Rish Shell.
- **Tier 2 (Standard Tier):** 100% legal fallback via `NativeFrameworkBridge`, `AdpfPerformanceEngine`, and Android Framework managers.

### 2. Concurrency & Thread Safety
- **No Long Operations on Main Thread:** Background commands and disk I/O must run asynchronously via `AppExecutors.getInstance().executeCommand(...)` or `diskIO()`.
- **UI Updates:** Post UI interactions back to the main thread via `AppExecutors.getInstance().postToMainThread(...)`.

### 3. Adding New Game Profiles
When adding support for a new game:
1. Register package IDs in [`com.gamebooster.app.games.GamePackageRegistry`](file:///home/willygailo/Documents/Game-Launcher/android/app/src/main/java/com/gamebooster/app/games/GamePackageRegistry.java).
2. Define storage paths in [`com.gamebooster.app.config.GameConfigPathResolver`](file:///home/willygailo/Documents/Game-Launcher/android/app/src/main/java/com/gamebooster/app/config/GameConfigPathResolver.java).
3. Add FPS / graphics preset rules in [`com.gamebooster.app.config.GameProfileAutoConfigurator`](file:///home/willygailo/Documents/Game-Launcher/android/app/src/main/java/com/gamebooster/app/config/GameProfileAutoConfigurator.java).
4. Add corresponding unit test cases in [`GamePackageRegistryTest.java`](file:///home/willygailo/Documents/Game-Launcher/android/app/src/test/java/com/gamebooster/app/games/GamePackageRegistryTest.java) and [`ConfigPatcherTest.java`](file:///home/willygailo/Documents/Game-Launcher/android/app/src/test/java/com/gamebooster/app/config/ConfigPatcherTest.java).

---

## 🚀 Submitting a Pull Request

1. **Fork the Repository:** Create your own fork of `willygailo/Game-Launcher`.
2. **Create a Feature Branch:**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```
3. **Commit Your Changes:**
   Write clear, descriptive commit messages:
   ```bash
   git commit -m "feat(overlay): add customizable RGB crosshair styles"
   git commit -m "fix(shizuku): handle AIDL dead object exception on service kill"
   ```
4. **Push to Your Fork:**
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Open a Pull Request:**
   Submit your PR against the `main` branch. Fill out the PR template completely with test verification results.

---

## 💬 Community & Support

- **Bug Reports & Feature Requests:** [GitHub Issues](https://github.com/willygailo/Game-Launcher/issues)
- **Developer:** [@willygailo](https://github.com/willygailo) • [Willy Jr Carnasa Gailo](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
- **License:** [MIT License](LICENSE)
