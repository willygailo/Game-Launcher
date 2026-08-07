# 🚀 Game Launcher Pro & Precision Aim Tuner v5.5.0-PRO 🎯

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android SDK](https://img.shields.io/badge/Android-12%20to%2016%20(SDK%2036)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v5.5.0--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
[![Tag](https://img.shields.io/badge/Tag-v5.5.0--PRO-FF5722?style=for-the-badge&logo=git&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases/tag/v5.5.0-PRO)
[![Shizuku API](https://img.shields.io/badge/Shizuku-Privileged%20ADB-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#)
[![Theme](https://img.shields.io/badge/UI-Cyberpunk%20Glass-FF007F?style=for-the-badge&logo=materialdesign&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#)
[![Facebook Profile](https://img.shields.io/badge/Facebook-Follow%20Me-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Star%20Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher)

**🔥 An ultra-high-performance Game Booster, Cyberpunk Glassmorphism UI, Input Latency Tuner, Per-Game 120/144/165 FPS Unlocker, 24/7 Background Auto-Game Detector, Full Hardware Identity Spoofer, Esports Network Tuner, and Precision HUD Overlay for Android 12 to 16. 🎮**

---

### 📢 Share • Like • React • Star ⭐
If you find this project helpful, please consider **Starring ⭐ the Repository**, **Sharing**, and leaving a **Like & Reaction**!

</div>

---

## 📌 1. 🌟 Executive Summary & Highlights

**Game Launcher Pro (v5.5.0-PRO)** is a ban-safe, high-performance Android gaming utility engineered for competitive mobile esports across titles such as *Mobile Legends: Bang Bang (MLBB)*, *PUBG Mobile / BGMI*, *Call of Duty Mobile (CODM)*, *Free Fire*, *Honor of Kings*, *Blood Strike*, *Wild Rift*, *Genshin Impact*, and *Roblox*.

It optimizes device-level touch sampling frequency, digitizer response rates, gyroscope polling, display refresh rate overrides (120/144/165Hz), full-system hardware identity spoofing (concealing host CPU, GPU, Build Fingerprint, display ID across 6 property namespaces), background 24/7 game launch detection without opening the main app UI, and low-latency esports network tuning using **Shizuku (ADB privileged execution)** — with **ZERO game memory tampering** or executable modification.

---

## 🎨 2. 💎 Cyberpunk Glassmorphism UI & Layout Design

```
+-----------------------------------------------------------------------------------+
| ⚡ GAME BOOSTER PRO                                                    ⚙️ SETTINGS |
| Mobile Legends • PUBG Mobile • CODM • HOK • Blood Strike                          |
+-----------------------------------------------------------------------------------+
| ⚡ SHIZUKU BINDER ACTIVE                                        🧠 RAM: 4.2 / 8 GB  |
+-----------------------------------------------------------------------------------+
| 🚀 EXTREME HARDWARE SPOOFER                          [ 🟢 24/7 AUTO BOOST ACTIVE ] |
| 165Hz Display • ROG 8 Pro Profile • Esports Low-Latency DNS                       |
+-----------------------------------------------------------------------------------+
| 🎮 INSTALLED TARGET GAMES (3 DETECTED)                                            |
|  [ 🗡️ Mobile Legends: Bang Bang ]  ▶ LAUNCH  ⚙️ CFG  |  165 FPS • Ultra Graphics    |
|  [ 🪖 PUBG Mobile               ]  ▶ LAUNCH  ⚙️ CFG  |  165 FPS • 90Hz Extreme     |
|  [ 🎯 Call of Duty: Mobile      ]  ▶ LAUNCH  ⚙️ CFG  |  120 FPS • Ultra Frame      |
+-----------------------------------------------------------------------------------+
```

### 🖼️ Visual System Features

- 🌌 **Neon Glassmorphism Cards (`card_neon_glow.xml`)**: Multi-stop dark slate gradient background (`#1E0F172A` $\rightarrow$ `#2D1E293B`) encased in a glowing neon cyan border (`#8800F0FF`).
- 💎 **Translucent Glass Badges (`hero_glass_badge.xml`)**: High-visibility status indicator tags rendered in vivid neon green (`#D900FF66`).
- 🌈 **Vibrant Cyberpunk Palette (`colors.xml`)**:
  - `accent_cyan` (`#00F0FF`) — Neon Cyan Headings & Glows
  - `accent_neon_green` (`#00FF66`) — Active Statuses & High Frame Rate Indicators
  - `accent_purple` (`#9D4EDD`) — Shizuku Binder Badges
  - `bg_dark` (`#0B0E14`) — OLED Pitch-Black Background
- 📊 **Dynamic Glass Floating HUD (`FloatingOverlayService.java`)**: Real-time Choreographer frame rate counter, battery temperature, power current, and RAM usage monitor.

---

## ⚡ 3. 🚀 Key Technical Features in v5.5.0-PRO

- ⚡ **24/7 Background Auto-Game Launch Detection (`AutoGameMonitorService`)**: Automatically intercepts game launches directly from the Android home screen or app drawer without opening Game Launcher Pro first.
- 🛠️ **Shizuku ADB Dumpsys Fallback**: Uses `dumpsys window visible-apps` fallback via Shizuku binder IPC for 100% reliable background game detection even if UsageStats permission is not granted.
- 🌐 **Esports Low-Latency Network & DNS Tuner (`EsportsNetworkTuner`)**: Automatically applies Cloudflare (`1.1.1.1`) and Google (`8.8.8.8`) DNS rules, optimizes TCP window scale buffers (`net.tcp.buffersize`), and forces Wi-Fi low-latency mode on game launch.
- 🎭 **Full Hardware & System Identity Hiding (`DeviceSpooferEngine`)**: Overrides 6 system property namespaces (`ro.product.model`, `ro.product.brand`, `ro.product.manufacturer`, `ro.product.device`, `ro.product.name`, `ro.hardware`, `ro.soc.model`, `ro.build.fingerprint`) via Shizuku ADB binder IPC.
- ⚡ **Direct 120/144/165Hz Display & FPS Enforcement (`MaxHzForceChannel`)**: Intercepts SurfaceFlinger binder calls (1035/1036) and forces 120Hz/144Hz/165Hz display refresh rate caps alongside Android Game Mode API FPS rules.
- 🎮 **New 120 FPS Config Patchers**: Added native configuration patchers for *Honor of Kings* (`HonorOfKingsConfigPatcher`) and *Blood Strike* (`BloodStrikeConfigPatcher`).
- 📱 **Expanded OEM Device Profiles (`SpoofProfileRegistry`)**: Includes full real-world profiles for ASUS ROG 8 Pro, REDMAGIC 9 Pro, Galaxy S25 Ultra, Black Shark 5 Pro, Xiaomi 14 Ultra, and 10+ major brand suites.
- 📱 **Full Android 12 to 16 (API 31–36) Compatibility**: Target SDK upgraded to SDK 36 with full FGS subtype declarations.

---

## 📄 4. 🛠️ System Architecture & Execution Flow

```
+-----------------------------------------------------------------------------------+
|               User Launches Game (from Home Screen, Drawer, or App)              |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                        AutoGameMonitorService (24/7 Loop)                         |
|  1. BOOT_COMPLETED Broadcast  --> Auto-Starts Background Monitor Service           |
|  2. UsageStatsManager Query   --> Polled every 1.2s for Instant Interception       |
|  3. Shizuku ADB Activity Log  --> Fallback: `dumpsys window visible-apps`          |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                     Shizuku Executor / Privileged ADB Binder                      |
+-----------------------------------------------------------------------------------+
       |                                   |                                  |
       v                                   v                                  v
[ System Properties ]             [ Display & Frame Rates ]           [ Esports Network ]
Build.MODEL, Build.FINGERPRINT    SurfaceFlinger (1035/1036)          DNS (1.1.1.1 / 8.8.8.8)
6 Product Namespaces              120Hz / 144Hz / 165Hz Lock          TCP Window Buffer Tuning
       |                                   |                                  |
       +-----------------------------------+----------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                      Target Game Runs at Max Graphics & 165 FPS                   |
+-----------------------------------------------------------------------------------+
```

---

## 📄 5. 📂 Directory Layout & Codebase Structure

```
Game_Launcher_Pro/
├── android/
│   ├── app/
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/          # EsportsNetworkTuner, MaxHzForceChannel, HzFpsChannel, PerformanceChannel
│   │   │   ├── config/           # HonorOfKingsConfigPatcher, BloodStrikeConfigPatcher, PubgConfigPatcher, CodmConfigPatcher, MlbbConfigPatcher
│   │   │   ├── core/             # PropertyResolver, SettingsManager, SettingsStateRestorer, GameBoosterJsInterface
│   │   │   ├── device/           # DisplayCapabilitiesDetector, UniversalDeviceAdapter, DeviceInfoChannel
│   │   │   ├── engine/           # CommandExecutor, ShellExecutor & Hardware Engine Modes
│   │   │   ├── games/            # GamePackageRegistry, GameLauncherHelper, GameManagerRepository, HomeGameScanner
│   │   │   ├── gamespace/        # AutoGameMonitorService, GameCacheCleaner, GameSpaceDndManager
│   │   │   ├── overlay/          # CrosshairOverlayService, FloatingOverlayService & CrosshairPreset
│   │   │   ├── service/          # BootReceiver, GameBoosterService
│   │   │   ├── shizuku/          # ShizukuExecutor, ShizukuChannel & ShizukuProvider
│   │   │   ├── spoofer/          # DeviceSpooferEngine, SpoofProfileRegistry & OEM Profiles (Samsung, ROG, Xiaomi, Apple, etc.)
│   │   │   ├── tweaks/           # TweakManagerRepository & Per-Game 120/165 FPS Unlocks
│   │   │   └── ui/               # HomeFragment, SettingsFragment, ProfilesFragment, MainActivity
│   │   └── src/main/res/
│   │       ├── drawable/         # Custom UI Assets (card_neon_glow.xml, hero_glass_badge.xml, hero_banner.gif, omni.jpeg)
│   │       └── values/           # colors.xml, strings.xml
│   └── build.gradle              # versionCode 55, versionName "5.5.0-PRO", targetSdk 36
├── Game_Space_v5.5.0-PRO.apk     # Release APK Binary Output
└── README.md
```

---

## ⚡ 6. 🔧 System Property & Shizuku Tuning Matrix

| System Setting / Command | Target Scope | Value | Description |
| :--- | :--- | :--- | :--- |
| `resetprop ro.product.model` | 6 Namespaces | **ASUS_AI2401_A** | Spoofs host model to ROG Phone 8 Pro across system, vendor, odm, product |
| `service call SurfaceFlinger 1035` | Display Server | **165** | Direct SurfaceFlinger binder override forcing 165Hz refresh rate |
| `cmd game set --fps 165` | Target Package | **165 FPS** | Overrides system frame rate caps via Android Game Mode API |
| `cmd window set-app-refresh-rate` | Target Package | **165 Hz** | Pins SurfaceFlinger refresh rate to maximum hardware display Hz |
| `settings put global net.dns1` | System Network | **1.1.1.1** | Sets high-priority Cloudflare DNS for minimum game server ping |
| `net.tcp.buffersize.wifi` | Kernel Network | **524288,...** | Optimizes TCP buffer scaling for zero packet loss during online matches |
| `debug.input.max_events_per_sec` | System-wide | **1000 Hz** | Maximizes digitizer event dispatch rate for instant touch input |
| `view.touch_slop` | System-wide | **0 px** | Eliminates drag deadzone for immediate aim response |

---

## 📱 7. 📱 Supported Hardware Profiles & Preset Matrix

| Brand Preset | Model ID | Manufacturer | Target SoC / GPU | Max Refresh Rate |
| :--- | :--- | :--- | :--- | :--- |
| **ASUS ROG Phone 8 Pro** | `ASUS_AI2401_A` | `asus` | Snapdragon 8 Gen 3 / Adreno 750 | **165 Hz** 🚀 |
| **REDMAGIC 9 Pro** | `NX769J` | `NUBIA` | Snapdragon 8 Gen 3 / Adreno 750 | **165 Hz** 🔥 |
| **Galaxy S25 Ultra** | `SM-S938B` | `samsung` | Snapdragon 8 Elite / Adreno 830 | **120 Hz** ✨ |
| **Black Shark 5 Pro** | `KTUS-A0` | `blackshark` | Snapdragon 8 Gen 1 / Adreno 730 | **144 Hz** ⚡ |
| **Xiaomi 14 Ultra** | `24030PN60G` | `Xiaomi` | Snapdragon 8 Gen 3 / Adreno 750 | **120 Hz** 💎 |
| **iPad Pro 12.9** | `iPad13,8` | `Apple` | Apple M2 / Apple GPU | **120 Hz** 📱 |

---

## 📥 8. 📦 Download & Setup Instructions

### Download Release APK
Get the latest compiled binary directly from GitHub Releases:
👉 **[Download Latest Release (v5.5.0-PRO APK)](https://github.com/willygailo/Game-Launcher/releases)** 📲

### Setup Steps (Shizuku Privileged Access)

1. 📲 Install **Shizuku** from Google Play Store or GitHub.
2. ⚙️ Enable **Developer Options** and **Wireless Debugging** in system settings.
3. ⚡ Start Shizuku via Wireless Debugging or connect your device to PC via ADB:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
4. 🎮 Open **Game Launcher Pro** and grant Shizuku permission when prompted.
5. 🚀 Select your game profile or simply launch games from your home screen — **auto-detection will handle the rest!**

---

## 🙏 9. ❤️ Acknowledgments & Special Thanks

- ❤️ **Shizuku Developer Team (Rikka)** — For providing the privileged ADB binder bridge API without requiring full device root access.
- ❤️ **Android Open Source Project (AOSP)** — For system developer properties and flexible OS framework capabilities.
- ❤️ **Glide Image Loading Library (Bumptech)** — For high-performance animated GIF and image rendering.
- ❤️ **Community Testers & Gamers** — For continuous feedback, performance logs, and testing across different Android 12-16 devices.

---

## 👤 10. 🌐 Developer Profiles & Connect

<div align="center">

| Profile | Link |
| :--- | :--- |
| 🌐 **Facebook Profile** | [Willy Jr Carnasa Gailo](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027) |
| 🐙 **GitHub Repository** | [Willy Gailo / Game-Launcher](https://github.com/willygailo/Game-Launcher) |
| 📦 **GitHub Releases** | [Game Launcher Releases](https://github.com/willygailo/Game-Launcher/releases) |

---

### 👍 Don't Forget to Share, Like, React & Star! ⭐

**Thank you for your support! Enjoy ultra-low input latency and maximum frame stability! 🎮🔥**

</div>
