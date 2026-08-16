# 🚀 Game Launcher Pro & Precision Aim Tuner v2.2.1.1 🎯

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android SDK](https://img.shields.io/badge/Android-12%20to%2016%20(SDK%2036)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v2.2.1.1-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
[![Shizuku API](https://img.shields.io/badge/Shizuku-Privileged%20ADB-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#)
[![Facebook Profile](https://img.shields.io/badge/Facebook-Follow%20Me-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Star%20Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher)

**An ultra-high-performance Game Booster, Precision Latency Tuner, Per-Game 120/144/165 FPS Unlocker, 5G/6G Latency Engine, and 2026 Hardware Device Spoofing for Android 12 to 16.**

---

### 📢 Share • Like • React • Star ⭐
If you find this project helpful, please consider **Starring ⭐ the Repository**, **Sharing**, and leaving a **Like & Reaction**!

</div>

---

## 🎨 UI & Layout Design System

Game Launcher Pro v2.2.1.1 features a state-of-the-art **Cyberpunk Glassmorphic** UI architecture crafted for seamless ergonomics and ultra-responsive responsiveness.

```
┌──────────────────────────────────────────────────────────────┐
│  🎮 GAME SPACE PRO                  [Shizuku: ACTIVE 🟢]     │
├──────────────────────────────────────────────────────────────┤
│  ⚡ QUICK BOOST STATUS                                       │
│  [ CPU: 100% Performance ]  [ GPU: ANGLE Vulkan ]            │
│  [ Refresh: 165Hz Lock   ]  [ Latency: Sub-1ms Touch ]       │
├──────────────────────────────────────────────────────────────┤
│  🎯 7 SUPPORTED ESPORTS TITLES                               │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐          │
│  │     MLBB     │ │    PUBGM     │ │     CODM     │  ...     │
│  │ 165 FPS CFG  │ │ Recoil + CVar│ │ Ultra Frame  │          │
│  └──────────────┘ └──────────────┘ └──────────────┘          │
├──────────────────────────────────────────────────────────────┤
│  🛠️ HARDWARE PROFILE SPOOFER                                 │
│  Selected: Samsung Galaxy S26 Ultra (Snapdragon 8 Elite)     │
│  [ ROG Phone 9 Pro ]  [ REDMAGIC 10 Pro ]  [ Xiaomi 15 Ultra]│
├──────────────────────────────────────────────────────────────┤
│  ⚙️ FLOATING HUD & SENSITIVITY CALCULATOR                     │
│  [ Crosshair Overlay ]  [ Gyro 1000Hz ]  [ Touch Boost ]     │
├──────────────────────────────────────────────────────────────┤
│  [ 🏠 Home ] [ 🎮 Games ] [ ⚡ Tweaks ] [ 🎯 Profiles ] [ ⚙️ Settings ]
└──────────────────────────────────────────────────────────────┘
```

### ✨ Visual & Aesthetic Highlights:
- 🌌 **Deep Cyber Dark Mode**: Rich `#070A0F` backdrop accented with neon cyan (`#00F0FF`) and hyper-green (`#00FF88`) highlights.
- 💎 **Glassmorphic Surface Cards**: Custom translucent surfaces with high-contrast borders (`card_glass_shape.xml`).
- 🔘 **Interactive Tab Dock**: Floating pill navigation bar with dynamic active-state transitions.
- ⚡ **Real-Time Visual Status**: Instant live indicators for Shizuku Binder status, current FPS/Hz refresh modes, and DNS acceleration.

---

## 📌 1. Introduction & Highlights in v2.2.1.1

**Game Launcher Pro (v2.2.1.1)** is engineered specifically for competitive mobile esports across 7 major titles:
1. 🎮 **Mobile Legends: Bang Bang (MLBB)** — 120/144/165 FPS, instant response scripts, battle performance injection.
2. 🪂 **PUBG Mobile / BGMI** — 120/144/165 FPS, UE4 CVar injection, zero-delay gyroscope, and recoil steadiness tuning.
3. 🎯 **Call of Duty: Mobile (CODM)** — JSON/XML/INI graphics unlocks, 165Hz touch boost, max shader pre-warming.
4. 🔥 **Garena Free Fire & Free Fire MAX** — HighFPS=1, MaxFPS=120/144/165, aim precision assist, fast drag headshot calculation.
5. ⚔️ **Genshin Impact & HoYoverse Titles (Honkai: Star Rail, ZZZ)** — GameSettings.json hardware model overrides, Vulkan pipeline unlock.
6. 👑 **Honor of Kings / Arena of Valor (HOK/AoV)** — SGameSettings.ini 120/144/165 FPS & UltraFrameRate unlock.
7. 🧱 **Roblox** — FastFlags `DFIntTaskSchedulerTargetFps=165` & Vulkan rendering optimizations.

---

### 🌟 Features Breakdown:

- ⚡ **Strict 120 / 144 / 165Hz Lock (Zero Fallback)**: Multi-layer Shizuku engine (`MaxHzForceChannel`) executing 17+ commands across AOSP, Android Game Mode API, SurfaceFlinger (1035/1036), setprop, and vendor OEM keys without capability throttling.
- 🛠️ **Dedicated Per-Application CFG Patchers**: Custom file injection directly into game data folders safely via Shizuku privileged access.
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
│   │   │   ├── shizuku/          # ShizukuExecutor & ShizukuFileManager & RishManager
│   │   │   ├── spoofer/          # DeviceSpooferEngine & 2026 Brand Profiles (S26 Ultra, ROG 9 Pro, etc.)
│   │   │   ├── tweaks/           # TweakManagerRepository
│   │   │   └── ui/
│   │   │       ├── screens/      # HomeFragment, GamesFragment, TweaksFragment, SettingsFragment, MainActivity
│   │   │       └── sensitivity/  # SensitivityCalculator & SensitivityModel
│   │   └── src/main/res/
│   │       ├── drawable/         # Custom Cyberpunk UI, Glass Cards & Neon Vector Assets
│   │       └── layout/           # Clean Glassmorphism Fragment & Activity Layouts
│   └── build.gradle              # versionCode 2211, versionName "2.2.1.1", targetSdk 36
├── Game_Space.apk
└── README.md
```

---

## 📥 3. Download & Setup Instructions

### Download Release APK
Get the latest compiled binary directly from GitHub Releases:
👉 **[Download Latest Version (v2.2.1.1 APK)](https://github.com/willygailo/Game-Launcher/releases)**

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
