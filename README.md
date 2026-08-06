# 🚀 Game Launcher Pro & Precision Aim Tuner v5.0.0-PRO 🎯

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android SDK](https://img.shields.io/badge/Android-12%20to%2016%20(SDK%2036)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v5.0.0--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
[![Shizuku API](https://img.shields.io/badge/Shizuku-Privileged%20ADB-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#)
[![Facebook Profile](https://img.shields.io/badge/Facebook-Follow%20Me-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Star%20Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher)

**An ultra-high-performance Game Booster, Input Latency Tuner, Per-Game 120/144/165 FPS Unlocker, and Precision HUD Overlay for Android 12 to 16.**

---

### 📢 Share • Like • React • Star ⭐
If you find this project helpful, please consider **Starring ⭐ the Repository**, **Sharing**, and leaving a **Like & Reaction**!

</div>

---

## 📌 1. Introduction & v5.0.0-PRO Highlights

**Game Launcher Pro (Precision Aim Input Tuner v5.0.0-PRO)** is a ban-safe, high-performance Android utility app engineered for competitive mobile gamers across titles such as *Mobile Legends: Bang Bang (MLBB)*, *PUBG Mobile / BGMI*, *Call of Duty Mobile (CODM)*, and *Free Fire*.

It optimizes device-level touch sampling frequency, digitizer response rates, gyroscope polling, display refresh rate overrides (120/144/165Hz), and game driver configurations using **Shizuku (ADB privileged execution)** — with **ZERO game memory tampering** or executable modification.

### 🌟 What's New in v5.0.0-PRO

- ⚡ **Targeted GameDriver Opt-In**: GameDriver and ANGLE drivers are now explicitly targeted **ONLY** to MLBB, PUBGM/BGMI, and CODM (`game_driver_opt_in_apps`) instead of forcing all background apps.
- 🎮 **Per-Game 120/144/165 FPS Unlocks**: Dedicated Shizuku Game Mode interventions (`cmd game set --fps 165`, `cmd window set-app-refresh-rate`, `device_config put game_overlay`) tailored for MLBB, PUBGM, and CODM.
- 🎯 **Precision Aim & Gyro Tuner Fixes**:
  - **Sleek HUD Crosshair**: Tuned size to 60px with clean view detachment on toggle OFF.
  - **Live Presets**: Reticle presets (`Dot`, `Tactical Cross`, `Scope Ring`, `Sniper Cross`) persist and render live.
  - **Interactive Recoil & Gyro Calculator**: Select your game profile, target DPI, and recoil dampening mode (`Balanced`, `Low Recoil / Precision Aim`, `Pro Gyro 400%`).
- 🖼️ **Omni Dashboard Theme**: Integrated `omni.jpeg` as the settings dashboard background with a sleek glass scrim overlay.
- 📱 **Full Android 12 to 16 (API 31–36) Compatibility**: Full support for Android 12, 13, 14, 15, and 16 with Android 14+ FGS subtype declarations.

> [!IMPORTANT]
> **SAFETY & COMPLIANCE GUARANTEE**:
> - ❌ **Zero Game Memory Tampering**: Never reads, injects into, or modifies game binaries, RAM, or APK data.
> - ❌ **No Anti-Cheat Violation**: Avoids prohibited UE4 console variable injections (`r.AimSensitivityScale`, `r.TouchDeadZone`) detected by Tencent ACE / anti-cheat solutions.
> - ❌ **No Automation or Macros**: Strictly zero aimbots, auto-fire, recoil macros, or automated inputs.
> - ✅ **100% Reversible**: Every system property modification is backed up and automatically restored to system defaults upon app termination.

---

## 📄 2. Technical Architecture & Directory Structure

```
Game_Launcher_Pro/
├── android/
│   ├── app/
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/          # GpuTweaksChannel, PerformanceChannel, NetworkOptimizer
│   │   │   ├── device/           # DisplayCapabilitiesDetector & DeviceInfoChannel
│   │   │   ├── engine/           # CommandExecutor & Hardware Engine Modes
│   │   │   ├── games/            # GameLauncherHelper & Zero-Delay Auto Scanner
│   │   │   ├── overlay/          # CrosshairOverlayService, FloatingOverlayService & CrosshairPreset
│   │   │   ├── shizuku/          # ShizukuExecutor, ShizukuChannel & ShizukuProvider
│   │   │   ├── tweaks/           # TweakManagerRepository & Per-Game 120/165 FPS Unlocks
│   │   │   └── ui/
│   │   │       ├── screens/      # HomeFragment, SettingsFragment, MainActivity
│   │   │       └── sensitivity/  # SensitivityCalculator & SensitivityModel (Gyro Recoil Tuner)
│   │   └── src/main/res/
│   │       └── drawable/         # Custom UI Assets (omni.jpeg, hero_banner.gif, home_bg_new.jpg)
│   └── build.gradle              # versionCode 50, versionName "5.0.0-PRO", targetSdk 36
├── Game_Space_v5.0.0-PRO.apk
└── README.md
```

### ⚡ System Property & Shizuku Tuning Matrix

| System Setting / Command | Target Scope | Value | Description |
| :--- | :--- | :--- | :--- |
| `game_driver_opt_in_apps` | MLBB, PUBGM, CODM | **Target Package Names** | Assigns GameDriver explicitly to target games without affecting system apps |
| `cmd game set --fps 165` | Target Package | **165 FPS** | Overrides system frame rate caps via Android Game Mode API |
| `cmd window set-app-refresh-rate` | Target Package | **165 Hz** | Pins SurfaceFlinger refresh rate to maximum hardware display Hz |
| `debug.input.max_events_per_sec` | System-wide | **1000 Hz** | Maximizes digitizer event dispatch rate for instant touch input |
| `view.touch_slop` | System-wide | **0 px** | Eliminates drag deadzone for immediate aim response |
| `debug.sensor.gyro.rate` | System-wide | **1000 Hz** | Unlocks max sensor polling for smooth scope tracking |

---

## 📥 3. Download & Setup Instructions

### Download Release APK
Get the latest compiled binary from GitHub Releases:
👉 **[Download Latest Version (v5.0.0-PRO APK)](https://github.com/willygailo/Game-Launcher/releases)**

### Setup Steps (Shizuku Privileged Access)

1. Install **Shizuku** from Google Play Store or GitHub.
2. Enable **Developer Options** and **Wireless Debugging** in system settings.
3. Start Shizuku via Wireless Debugging or connect your device to PC via ADB:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
4. Open **Game Launcher Pro** and grant Shizuku permission when prompted.
5. Select your game profile and apply 120/144/165 FPS overrides!

---

## 🙏 4. Acknowledgments & Special Thanks

- ❤️ **Shizuku Developer Team (Rikka)** — For providing the privileged ADB binder bridge API without requiring full device root access.
- ❤️ **Android Open Source Project (AOSP)** — For system developer properties and flexible OS framework capabilities.
- ❤️ **Glide Image Loading Library (Bumptech)** — For high-performance animated GIF and image rendering.
- ❤️ **Community Testers & Gamers** — For continuous feedback, performance logs, and testing across different Android 12-16 devices.

---

## 👤 Developer Profiles & Connect

<div align="center">

| Profile | Link |
| :--- | :--- |
| 🌐 **Facebook Profile** | [Willy Jr Carnasa Gailo](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027) |
| 🐙 **GitHub Repository** | [Willy Gailo / Game-Launcher](https://github.com/willygailo/Game-Launcher) |
| 📦 **GitHub Releases** | [Game Launcher Releases](https://github.com/willygailo/Game-Launcher/releases) |

---

### 👍 Don't Forget to Share, Like, React & Star! 👍

**Thank you for your support! Enjoy ultra-low input latency and maximum frame stability! 🎮🔥**

</div>
