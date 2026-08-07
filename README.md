# 🚀 Game Launcher Pro & Precision Aim Tuner v5.4.0-PRO 🎯

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android SDK](https://img.shields.io/badge/Android-12%20to%2016%20(SDK%2036)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v5.4.0--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
[![Tag](https://img.shields.io/badge/Tag-v5.4.0--PRO-FF5722?style=for-the-badge&logo=git&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases/tag/v5.4.0-PRO)
[![Shizuku API](https://img.shields.io/badge/Shizuku-Privileged%20ADB-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#)
[![Facebook Profile](https://img.shields.io/badge/Facebook-Follow%20Me-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Star%20Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher)

**An ultra-high-performance Game Booster, Input Latency Tuner, Per-Game 120/144/165 FPS Unlocker, Background Auto-Game Detector, Full Hardware Identity Spoofer, and Precision HUD Overlay for Android 12 to 16.**

---

### 📢 Share • Like • React • Star ⭐
If you find this project helpful, please consider **Starring ⭐ the Repository**, **Sharing**, and leaving a **Like & Reaction**!

</div>

---

## 📌 1. Introduction & v5.4.0-PRO Highlights

**Game Launcher Pro (v5.4.0-PRO / Release Tag v5.4.0-PRO)** is a ban-safe, high-performance Android utility app engineered for competitive mobile gamers across titles such as *Mobile Legends: Bang Bang (MLBB)*, *PUBG Mobile / BGMI*, *Call of Duty Mobile (CODM)*, *Free Fire*, *Honor of Kings*, *Blood Strike*, and *Wild Rift*.

It optimizes device-level touch sampling frequency, digitizer response rates, gyroscope polling, display refresh rate overrides (120/144/165Hz), full-system hardware identity spoofing (concealing host CPU, GPU, Build Fingerprint, display ID across 6 property namespaces), background 24/7 game detection without opening the main app UI, and low-latency esports network tuning using **Shizuku (ADB privileged execution)** — with **ZERO game memory tampering** or executable modification.

### 🌟 What's New in v5.4.0-PRO

- ⚡ **Background 24/7 Auto Game Launch Detection**: Automatically intercepts game launches directly from the Android home screen or app drawer without opening Game Launcher Pro first.
- 🛠️ **Shizuku ADB Dumpsys Fallback**: Uses `dumpsys window visible-apps` fallback via Shizuku binder IPC for 100% reliable background game detection even if UsageStats permission is not granted.
- 🌐 **Esports Low-Latency Network & DNS Tuner (`EsportsNetworkTuner`)**: Automatically applies Cloudflare (`1.1.1.1`) and Google (`8.8.8.8`) DNS rules, optimizes TCP window scale buffers (`net.tcp.buffersize`), and forces Wi-Fi low-latency mode on game launch.
- 🎮 **New 120 FPS Config Patchers**: Added native configuration patchers for *Honor of Kings* (`HonorOfKingsConfigPatcher`) and *Blood Strike* (`BloodStrikeConfigPatcher`).
- 🎭 **Full Hardware & System Identity Hiding**: Overrides 6 system property namespaces (`ro.product.model`, `ro.product.brand`, `ro.product.manufacturer`, `ro.product.device`, `ro.product.name`, `ro.hardware`, `ro.soc.model`, `ro.build.fingerprint`) via Shizuku ADB binder IPC.
- ⚡ **Direct 120/144/165Hz Display & FPS Enforcement**: Intercepts SurfaceFlinger binder calls (1035/1036) and forces 120Hz/144Hz/165Hz display refresh rate caps alongside Android Game Mode API FPS rules.

---

## 📄 2. Technical Architecture & Directory Structure

```
Game_Launcher_Pro/
├── android/
│   ├── app/
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/          # GpuTweaksChannel, PerformanceChannel, TouchLatencyChannel, NetworkOptimizer
│   │   │   ├── config/           # PubgConfigPatcher, CodmConfigPatcher, FreeFireConfigPatcher, MlbbConfigPatcher
│   │   │   ├── core/             # PropertyResolver, SettingsManager, SettingsStateRestorer, GameBoosterJsInterface
│   │   │   ├── device/           # DisplayCapabilitiesDetector, UniversalDeviceAdapter, DeviceInfoChannel
│   │   │   ├── engine/           # CommandExecutor, ShellExecutor & Hardware Engine Modes
│   │   │   ├── games/            # GamePackageRegistry, GameLauncherHelper, GameManagerRepository
│   │   │   ├── gamespace/        # AutoGameMonitorService, GameCacheCleaner, GameSpaceDndManager
│   │   │   ├── overlay/          # CrosshairOverlayService, FloatingOverlayService & CrosshairPreset
│   │   │   ├── service/          # BootReceiver, GameBoosterService
│   │   │   ├── shizuku/          # ShizukuExecutor, ShizukuChannel & ShizukuProvider
│   │   │   ├── spoofer/          # SpoofProfileRegistry & OEM Brand Profiles (Samsung, ROG, Xiaomi, Apple, etc.)
│   │   │   ├── tweaks/           # TweakManagerRepository & Per-Game 120/165 FPS Unlocks
│   │   │   └── ui/
│   │   │       ├── screens/      # HomeFragment, SettingsFragment, ProfilesFragment, MainActivity
│   │   │       └── sensitivity/  # SensitivityCalculator & SensitivityModel (Gyro Recoil Tuner)
│   │   └── src/main/res/
│   │       └── drawable/         # Custom UI Assets (omni.jpeg, hero_banner.gif, home_bg_new.jpg)
│   └── build.gradle              # versionCode 51, versionName "5.1.0-PRO", targetSdk 36
├── Game_Space.apk                # Release APK Output
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
👉 **[Download Latest Version (v5.1.0-PRO / v2.4.0 APK)](https://github.com/willygailo/Game-Launcher/releases)**

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
