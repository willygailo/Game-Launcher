<p align="center">
  <img src="BANNER.gif" alt="Precision Aim Banner" width="100%" style="border-radius: 12px;">
</p>

<h1 align="center">🎯 Precision Aim – Input Tuner</h1>

<p align="center">
  <b>Ban-Safe Device Input & Touch Sampling Frequency Optimizer for Mobile eSports</b>
</p>

<p align="center">
  <a href="https://github.com/willygailo/Game-Launcher"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"></a>
  <a href="https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027"><img src="https://img.shields.io/badge/Facebook-1877F2?style=for-the-badge&logo=facebook&logoColor=white" alt="Facebook"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License"></a>
  <a href="android"><img src="https://img.shields.io/badge/Android-API_36-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"></a>
  <a href=".github/workflows/android-build.yml"><img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions" alt="Build Status"></a>
</p>

---

## 🚀 Overview

**Precision Aim – Input Tuner** is an open-source, device-level input tuning utility designed for competitive mobile eSports titles (*PUBG Mobile, COD Mobile, Free Fire, Mobile Legends*). 

By leveraging **Shizuku** (privileged ADB shell access), it tunes low-level Android digitizer sampling rates, gyroscope polling frequency, pointer acceleration curves, and touch slop deadzones directly at the OS kernel/framework level.

> [!IMPORTANT]
> **SAFETY & COMPLIANCE GUARANTEE**:
> - ⚡ **Zero Game Memory Tampering**: Never reads, modifies, or interacts with game RAM, executable code, or game data files.
> - ⚡ **No Anti-Cheat Violation**: Does not inject UE4 console variables (`r.TouchDeadZone`, `r.AimSensitivityScale`, etc.) which trigger anti-cheat bans.
> - ⚡ **No Scripting or Automation**: No aimbot, recoil reduction, macros, or automated touch inputs.
> - ⚡ **100% Reversible**: Automatically backs up original system state and restores factory defaults on profile exit or app close.

---

## ⚡ Key Tuned System Knobs (Device-Level)

All optimizations modify standard Android developer options and digitizer system properties via ADB shell privileges (`setprop` / `settings put`):

| System Property / Setting | Default Range | Tuned Value | Description |
| :--- | :--- | :--- | :--- |
| `debug.input.max_events_per_sec` | 60 - 240 Hz | **1000 Hz** | Increases touch digitizer event dispatch frequency |
| `view.touch_slop` | 8 - 24 px | **0 px** | Eliminates initial drag deadzone for instant aim response |
| `touch_slop_reduction` | 0 | **1** | Reduces touch slop scaling factor across views |
| `debug.sensor.gyro.rate` | 100 - 200 Hz | **1000 Hz** | Maxes out gyroscope sampling rate for smooth scope aim |
| `pointer_speed` | 0 (scaled) | **7** | Applies 1:1 linear pointer acceleration curve |
| `persist.sys.touch.pressure.scale` | 1.0 | **0.0001** | Minimizes touch pressure calculation overhead |

---

## 🏗️ Project Architecture & Modules

```
Precision-Aim/
├── .github/
│   ├── workflows/        # GitHub Actions CI build & test automation
│   └── ISSUE_TEMPLATE/   # Bug report & feature request templates
├── android/
│   ├── app/              # Android app source code (SDK 36, Java 17)
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── core/     # SettingsManager & ProfileManager engines
│   │   │   ├── shizuku/  # Shizuku binder IPC & ADB executor
│   │   │   ├── booster/  # Thermal, FPS & GPU tweaking channels
│   │   │   └── ui/       # Activities, Fragments & sensitivity calculator
│   │   └── src/test/     # JUnit & Mockito unit testing suite
├── BANNER.gif            # Project banner header
├── CONTRIBUTING.md       # Open-source developer contribution guide
├── LICENSE               # Apache 2.0 Open Source License
├── SECURITY.md          # Security policy & ban-safety rules
└── README.md             # Documentation & setup guide
```

---

## 🛠️ Shizuku Setup Guide

Precision Aim requires **Shizuku** to execute ADB shell commands without requiring root access.

### Setup Steps:
1. Download & Install [Shizuku from Google Play](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) or GitHub.
2. Enable **Developer Options** and **Wireless Debugging** on your Android device.
3. Start Shizuku via Wireless Debugging inside the Shizuku app, or connect your device to a PC and run:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
4. Open **Precision Aim** and grant Shizuku permission when prompted.

---

## 🎯 Key Features

### 1. Dynamic Game Detection & Auto-Tuning
- Automatically detects foreground game launches (*PUBG Mobile, COD Mobile, Mobile Legends, Free Fire*).
- Applies per-game input profiles instantly upon game focus.
- Reverts all system properties back to factory defaults as soon as the game session ends.

### 2. Sensitivity & Gyro Setup Helper
- Interactive calculator mapping device **DPI**, **Screen Size**, and **Gyro Preference** to recommended in-game scope sensitivities.
- Includes manual calibration guides for PUBG Mobile and COD Mobile.

### 3. Hardware-Accelerated Crosshair Overlay
- Optional floating HUD canvas overlay.
- 4 Visual Presets: **Dot**, **Tactical Cross**, **Scope Ring**, **Sniper Cross**.
- Full customization for size, stroke width, opacity, color, and position offsets.
- Touch pass-through enabled (`FLAG_NOT_TOUCHABLE`).

---

## ⚙️ Building & Testing

### Prerequisites
- JDK 17
- Android SDK 36 (minSdk 24, targetSdk 36)

### Gradle Commands
```bash
cd android

# Run unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug
```

---

## 🌐 Community & Social Links

Connect with the developer and contribute to the project:

- **GitHub Repository**: [github.com/willygailo/Game-Launcher](https://github.com/willygailo/Game-Launcher)
- **Facebook Profile**: [facebook.com/willy.jr.carnasa.gailo](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
- **Contribution Guide**: [CONTRIBUTING.md](CONTRIBUTING.md)
- **License**: [Apache License 2.0](LICENSE)

---

## 📜 Legal Disclaimer

```
DISCLAIMER: Precision Aim is an independent device input tuning utility. It operates strictly at the Android OS level to adjust touch digitizer polling rates and display properties via standard ADB permissions. Precision Aim does not modify, inject into, or tamper with any game files, game memory, or third-party applications.
```
