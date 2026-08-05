# Precision Aim – Input Tuner 🎯

**Precision Aim – Input Tuner** is a legitimate, ban-safe device input tuning utility for mobile eSports players (PUBG Mobile, COD Mobile, Free Fire, etc.). 

It optimizes device-level touch sampling rates, gyroscope polling frequency, pointer speed, and touch deadzones using standard Android developer system properties via **Shizuku** (ADB shell privileges).

> [!IMPORTANT]
> **SAFETY & COMPLIANCE GUARANTEE**:
> - **Zero Game Tampering**: Precision Aim **NEVER** reads, modifies, or interacts with game memory, processes, APK files, or internal game data.
> - **No UE4 Console Variable Injection**: No `r.AimSensitivityScale`, `r.TouchDeadZone`, or `r.MobileTouchBoostRate` modifications (which are detected by Tencent ACE / anti-cheat engines).
> - **No Scripting or Automation**: No aimbot, auto-aim, recoil compensation, macros, or automated inputs.
> - **100% Reversible**: Every system property change is backed up before modification and automatically restored to original defaults on app exit, profile change, or uninstall.

---

## 🏗️ Project Architecture & Modules

```
Precision-Aim/
├── app/                  # UI screens, navigation, onboarding, sensitivity calculator
├── shizuku/              # ShizukuClient, binder lifecycle, ADB setup wizard
├── core/
│   ├── settings/        # SettingsManager (system prop backup & restore engine)
│   └── profile/         # ProfileManager (PUBG Mobile & COD Mobile profiles, JSON import/export)
├── overlay/              # CrosshairOverlayService & custom floating canvas (4 presets)
├── game-detector/        # ForegroundAppDetector (UsageStatsManager auto-profile switcher)
└── README.md             # Documentation, compliance, and setup guide
```

---

## ⚡ Key Tuned System Knobs (Device-Level)

All optimizations are standard Android developer options modified via ADB/Shizuku shell commands (`setprop` / `settings put`):

| System Property / Setting | Default Range | Tuned Value | Description |
| :--- | :--- | :--- | :--- |
| `debug.input.max_events_per_sec` | 60 - 240 Hz | **1000 Hz** | Increases touch digitizer event dispatch frequency |
| `view.touch_slop` | 8 - 24 px | **0 px** | Eliminates initial drag deadzone for instant aim response |
| `touch_slop_reduction` | 0 | **1** | Reduces touch slop scaling factor across views |
| `debug.sensor.gyro.rate` | 100 - 200 Hz | **1000 Hz** | Maxes out gyroscope sampling rate for smooth scope aim |
| `pointer_speed` | 0 (scaled) | **7** | Applies 1:1 linear pointer acceleration curve |
| `persist.sys.touch.pressure.scale` | 1.0 | **0.0001** | Minimizes touch pressure calculation overhead |

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

## 🎯 Features

### 1. Dynamic Game Detection & Auto-Tuning
- Automatically detects when **PUBG Mobile** (`com.tencent.ig`, `com.pubg.imobile`, etc.) or **COD Mobile** (`com.activision.callofduty.shooter`) enters the foreground.
- Applies per-game input profiles instantly.
- Reverts all system properties back to factory defaults as soon as the game closes.

### 2. Sensitivity & Gyro Setup Helper
- Interactive calculator that maps your device's **DPI**, **Screen Size**, and **Gyro Preference** to optimal in-game sensitivity values.
- Includes a step-by-step calibration guide for manual entry in the game's official settings menu.

### 3. Optional Hardware-Accelerated Crosshair Overlay
- Disabled by default.
- 4 Visual Presets: **Dot**, **Tactical Cross**, **Scope Ring**, **Sniper Cross**.
- Full customization for size, stroke width, opacity, color, and position offsets.
- Touch pass-through enabled (`FLAG_NOT_TOUCHABLE`).

---

## 📋 Play Store Policy & Safety Compliance

When publishing or listing this application on the Google Play Store or third-party stores:
- **Title**: Precision Aim – Input Tuner (or Device Input Latency Tuner)
- **Category**: Tools / Utilities
- **Description Rules**:
  - Describe as a *"device touch responsiveness and input latency tuning utility"*.
  - **Do NOT** use prohibited terms such as *"cheat"*, *"hack"*, *"aimbot"*, *"recoil reducer"*, *"anti-cheat bypass"*, or *"100% ban-safe"*.
  - **Do NOT** make unverifiable claims like *"60% better headshots"* or *"guaranteed victory"*.

---

## 📜 Disclaimer Template

```
DISCLAIMER: Precision Aim is an independent system input tuning utility. It operates strictly at the Android OS level to adjust touch digitizer polling rates and display settings via ADB permissions. Precision Aim does not modify, inject into, or tamper with any game files, game memory, or third-party applications. Some game developers may restrict third-party visual overlays; use of the optional crosshair overlay feature is at the user's discretion.
```

---

## ⚙️ Building the Application

### Prerequisites:
- Android Studio Ladybug / Jellyfish or latest stable
- JDK 17
- Android SDK 36 (minSdk 24, targetSdk 36)

### Build Command:
```bash
./gradlew assembleDebug
```
The compiled APK will be output to `android/app/build/outputs/apk/debug/`.
