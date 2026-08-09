# Contributing to Precision Aim – Input Tuner 🎯

Thank you for your interest in contributing to **Precision Aim**! We welcome contributions from developers, testers, and gaming enthusiasts.

---

## 🛠️ Development & Building Setup

### Requirements
- **JDK 17**
- **Android SDK 36** (minSdk 24, targetSdk 36)
- **Gradle 8.7+**

### Local Build Commands
```bash
# Clone the repository
git clone https://github.com/willygailo/Game-Launcher.git
cd Game-Launcher/android

# Run unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug
```

---

## 📐 Coding Standards & Guidelines

1. **Architecture**: Clean Android architecture with separated domain/service layers (`core`, `shizuku`, `booster`, `overlay`, `ui`).
2. **Safety First**: Every system property tweaked must be reversible and backed up in `SettingsManager`.
3. **No Game Tampering**: Features must operate strictly at the Android OS/digitizer level. Never modify game memory, APK binaries, or internal config files.
4. **Testing**: Add JUnit unit tests for new manager classes or logic changes in `android/app/src/test/`.

---

## 🔀 Pull Request Process

1. Fork the repository and create a feature branch (`git checkout -b feature/awesome-knob`).
2. Ensure `./gradlew test` and `./gradlew assembleDebug` pass without errors.
3. Keep commits clean and descriptive.
4. Open a Pull Request referencing relevant issues.
