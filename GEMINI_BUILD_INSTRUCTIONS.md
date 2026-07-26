# 🚀 GEMINI BUILD INSTRUCTIONS — GAME SPACE

This guide provides instructions for **Gemini AI Assistants** and developers to build, compile, debug, and deploy **GAME SPACE** as a **100% Pure Native Java Android Application (JDK 17)**.

---

## 📋 Prerequisites & Environment Setup

1. **Java Development Kit (JDK)**:
   - Must use **JDK 17** (`/usr/lib/jvm/java-17-openjdk-amd64`).
   - Export environment variable before building:
     ```bash
     export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
     ```

2. **Android SDK**:
   - `compileSdk`: **35**
   - `targetSdk`: **35**
   - `minSdk`: **24**
   - Essential SDK Components: `platform-tools`, `platforms;android-36`, `build-tools;28.0.3` (or latest).

---

## 🛠️ Release APK Build Procedure

Navigate to the `android/` directory and execute the Gradle wrapper:

```bash
cd /home/willygailo/Documents/Game_Launcher_Pro/android
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew assembleRelease
```

### Output Location:
The output APK is generated at:
`android/app/build/outputs/apk/release/app-release.apk`

---

## 📱 Device Installation & Testing

Install the built APK to a connected Android phone via ADB:

```bash
adb install android/app/build/outputs/apk/release/app-release.apk
```

---

## ⚡ Shizuku Non-Rooted Setup Verification

1. Ensure the **Shizuku app** is installed and running on the target device via Wireless ADB or USB ADB.
2. Launch **GAME SPACE**, navigate to **Permissions**, and tap **GRANT SHIZUKU PERMISSION**.
3. Verify that `ShizukuExecutor.java` automatically grants `WRITE_SECURE_SETTINGS`, `WRITE_SETTINGS`, and `PACKAGE_USAGE_STATS`.
