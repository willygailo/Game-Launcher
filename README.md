# 🚀 Game Launcher Pro & Precision Aim Tuner v2.1.1.1 🎯

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android SDK](https://img.shields.io/badge/Android-12%20to%2016%20(SDK%2036)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v2.1.1.1-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
[![Shizuku API](https://img.shields.io/badge/Shizuku-Privileged%20ADB-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#)
[![Facebook Profile](https://img.shields.io/badge/Facebook-Follow%20Me-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Star%20Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher)

**An ultra-high-performance Game Booster, Input Latency Tuner, Per-Game 120/144/165 FPS Unlocker, 5G/6G Latency Engine, and 2026 Hardware Device Spoofing for Android 12 to 16.**

---

### 📢 Share • Like • React • Star ⭐
If you find this project helpful, please consider **Starring ⭐ the Repository**, **Sharing**, and leaving a **Like & Reaction**!

</div>

---

## 📌 1. Introduction & v2.1.1.1 Highlights

**Game Launcher Pro (v2.1.1.1)** is a ban-safe, high-performance Android gaming utility engineered for competitive mobile esports across 7 major titles:
1. 🎮 **Mobile Legends: Bang Bang (MLBB)**
2. 🪂 **PUBG Mobile / BGMI**
3. 🎯 **Call of Duty: Mobile (CODM)**
4. 🔥 **Garena Free Fire & Free Fire MAX**
5. ⚔️ **Genshin Impact & HoYoverse Titles (Honkai: Star Rail, ZZZ)**
6. 👑 **Honor of Kings / Arena of Valor (HOK/AoV)**
7. 🧱 **Roblox**

---

### 🌟 What's New in v2.1.1.1

- ⚡ **Strict 120 / 144 / 165Hz Lock (Zero Fallback)**: Multi-layer Shizuku engine (`MaxHzForceChannel`) executing 17+ commands across AOSP, Android Game Mode API, SurfaceFlinger (1035/1036), setprop, and vendor OEM keys without capability throttling.
- 🛠️ **Dedicated Per-Application CFG Patchers**:
  - `MlbbConfigPatcher`: 120/144/165 FPS, super-fast touch, damage script asset configs.
  - `PubgConfigPatcher`: 120/144/165 FPS, UE4 CVar injection, aim assist & recoil control.
  - `CodmConfigPatcher`: JSON/XML/INI graphics unlocks, 165Hz touch boost.
  - `FreeFireConfigPatcher`: HighFPS=1, MaxFPS=120/144/165, aim precision assist.
  - `GenshinConfigPatcher`: GameSettings.json & hardware model overrides, Vulkan pipeline unlock.
  - `HokConfigPatcher`: SGameSettings.ini 120/144/165 FPS & UltraFrameRate unlock.
  - `RobloxConfigPatcher`: FastFlags `DFIntTaskSchedulerTargetFps=165` & Vulkan rendering optimizations.
- 🎯 **Targeted Game Driver & Google ANGLE Vulkan Opt-In**: Explicitly opt-in only the 7 supported games per application (`game_driver_opt_in_apps` & `angle_gl_driver_selection_pkgs`) instead of affecting unrelated system apps.
- 📡 **5G / 6G & Dual Data / Wi-Fi Latency Acceleration**: Low-latency Wi-Fi mode (`cmd wifi force-low-latency-mode`), seamless zero-drop dual data (`mobile_data_always_on 1`), and 5G/6G NR TCP buffer tuning.
- 📱 **2025/2026 Gaming Device Spoofing Powerhouses**:
  - **Samsung Galaxy S26 Ultra** (`SM-S948B` / Snapdragon 8 Elite Gen 5 / Adreno 840)
  - **ASUS ROG Phone 9 Pro** (185Hz / Adreno 830)
  - **REDMAGIC 10 Pro** (Snapdragon 8 Elite / Red Core R3 / Adreno 830)
  - **Xiaomi 15 Ultra** (Snapdragon 8 Elite / Adreno 830)
- 🎯 **Precision Aim & HUD Overlay**: Live reticle presets, crosshair overlay, and interactive DPI/Gyro sensitivity calculator.

---

## 📄 2. Technical Architecture & Directory Structure

```
Game-Launcher-PRO/
├── android/
│   ├── app/
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/          # MaxHzForceChannel, GpuTweaksChannel, NetworkOptimizer
│   │   │   ├── config/           # Per-Game Config Patchers (MLBB, PUBGM, CODM, FF, Genshin, HOK, Roblox)
│   │   │   ├── device/           # DisplayCapabilitiesDetector & DeviceInfoChannel
│   │   │   ├── engine/           # CommandExecutor & RefreshRateOverrideEngine
│   │   │   ├── games/            # GameLauncherHelper & HomeGameScanner
│   │   │   ├── overlay/          # CrosshairOverlayService & FloatingOverlayService
│   │   │   ├── shizuku/          # ShizukuExecutor & ShizukuFileManager
│   │   │   ├── spoofer/          # DeviceSpooferEngine & 2026 Brand Profiles (S26 Ultra, ROG 9 Pro, etc.)
│   │   │   ├── tweaks/           # TweakManagerRepository
│   │   │   └── ui/
│   │   │       ├── screens/      # HomeFragment, SettingsFragment, MainActivity
│   │   │       └── sensitivity/  # SensitivityCalculator & SensitivityModel
│   │   └── src/main/res/
│   │       └── drawable/         # Custom Cyberpunk UI & Glassmorphism Assets
│   └── build.gradle              # versionCode 2111, versionName "2.1.1.1", targetSdk 36
├── Game_Space.apk
└── README.md
```

---

## 📥 3. Download & Setup Instructions

### Download Release APK
Get the latest compiled binary from GitHub Releases:
👉 **[Download Latest Version (v2.1.1.1 APK)](https://github.com/willygailo/Game-Launcher/releases)**

### Setup Steps (Shizuku Privileged Access)

1. Install **Shizuku** from Google Play Store or GitHub.
2. Enable **Developer Options** and **Wireless Debugging** in system settings.
3. Start Shizuku via Wireless Debugging or connect your device to PC via ADB:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
4. Open **Game Launcher Pro** and tap **Grant Shizuku Permission**.
5. Select your target game, apply 120/144/165 FPS CFG overrides, and launch with instant auto-boost!

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
