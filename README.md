<div align="center">

# 🎮 Game Launcher & Performance Booster

[![Android API](https://img.shields.io/badge/Android_10--16_API_29--36-3DDC84?style=for-the-badge&logo=android&logoColor=white)]()
[![Kotlin](https://img.shields.io/badge/Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose_2024.06-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)]()
[![Hilt](https://img.shields.io/badge/Hilt_2.52_(KSP)-FF4088?style=for-the-badge&logo=dagger&logoColor=white)]()
[![TensorFlow Lite](https://img.shields.io/badge/TFLite_2.17-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)]()
[![Version](https://img.shields.io/badge/Version_1.7.2-00C853?style=for-the-badge)]()

> **Ang pinaka-powerful na Android performance booster para sa mobile gaming — designed for non-root devices!**

---

## 👤 Developer

| | |
|---|---|
| ![Willy Gailo](https://github.com/willygailo.png?size=120) | **Willy Gailo** |
| ![Facebook](assets/facebook_profile.jpg) | [GitHub](https://github.com/willygailo) • [Facebook](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027) |

---

## Screenshots

*<!-- TODO: Add screenshots here -->*

| Dashboard | FPS Overlay | Games Library | Settings |
|---|---|---|---|
| `screenshot_1.png` | `screenshot_2.png` | `screenshot_3.png` | `screenshot_4.png` |

---

## ✨ Features

| Feature | Description | Status |
|---|---|---|
| **Real-time Monitoring** | CPU, RAM, GPU, battery, network, display stats | ✅ |
| **Floating FPS Counter** | Live FPS overlay habang naglalaro | ✅ |
| **Live Boost Notification** | Real-time FPS & Hz sa notification habang naglalaro | ✅ |
| **Game Library** | Auto-detect ng lahat ng installed games | ✅ |
| **Per-Game Boosts** | Custom FPS (30-165), WiFi lock per game | ✅ |
| **Immersive Controls** | DND mode (INTERRUPTION_FILTER_NONE), max brightness automation | ✅ |
| **Home Widget** | Quick Boost at Open buttons sa home screen | ✅ |
| **Quick Settings Tile** | Toggle booster sa notification panel | ✅ |
| **Game Detector** | Auto-boost pag nag-open ng game (UsageStatsManager) | ✅ |
| **Stats History** | Track ng play time per session (manual + auto) | ✅ |
| **RAM Optimizer** | Kill background apps instantly (non-root via ActivityManager) | ✅ |
| **Storage Optimizer** | Run fstrim para sa speed boost | ✅ |
| **Network Manager** | WiFi low-latency lock, signal strength, link speed | ✅ |
| **ML Game Detection** | TensorFlow Lite 2.17 auto-classify games | ✅ |
| **Background Tasks** | WorkManager scheduled optimization | ✅ |
| **Permission UI** | Individual permission cards na may status indicator (incl. battery exemption) | ✅ |
| **Game Search & Filter** | Search bar + category filter chips (MOBA, FPS, RPG, etc.) | ✅ |
| **Gaming Session Recording** | Auto-record play time, FPS, battery drain, RAM per session | ✅ |
| **Session Stats Dashboard** | Total sessions & play time cards sa Dashboard | ✅ |
| **Game Details Screen** | Dedicated screen with session history, avg FPS, stats per game | ✅ |
| **Per-Game Graphics Mode** | Performance/Balanced/Battery Saver/Custom mode per game | ✅ |
| **Draggable FPS Overlay** | Drag the FPS counter anywhere on screen | ✅ |
| **Brightness Slider** | Adjustable brightness level (10%-100%) | ✅ |
| **CPU Core Control** | Toggle individual CPU cores on/off (root) | ✅ |
| **Benchmark Mode** | CPU/GPU/Memory benchmark with overall score | ✅ |
| **Dark/Light Theme** | Toggle between dark and light theme | ✅ |
| **Profile Import/Export** | Backup & restore per-game settings as JSON | ✅ |
| **Onboarding Walkthrough** | Guided 4-step intro sa unang open ng app | ✅ |

### v1.7.2: Live Monitoring, DND Fix, Benchmark & Session Overhaul

| Feature | Description | Status |
|---|---|---|
| **Live FPS/Hz Notification** | GameBoosterService shows real-time FPS & Hz every 2s | 🆕 |
| **DND Fix** | `DndManager` now uses `INTERRUPTION_FILTER_NONE` (totally block notifs) | 🔧 |
| **Benchmark Update** | Real GPU test via Bitmap, CPU via sort/primes, memory via 512KB arrays | 🔧 |
| **Manual Session Recording** | Games tab now records GamingSession on PLAY | 🆕 |
| **Session Update Fix** | Uses session ID directly, not fragile `getLastSessionForGame` | 🔧 |
| **FpsMonitor Rewrite** | StateFlow instead of callbackFlow for reliable FPS | 🔧 |
| **Display & FPS Fix** | Real FPS & Hz now properly display on Dashboard | 🔧 |
| **Battery Exemption UI** | Shows "Granted" status, direct app settings fallback | 🔧 |
| **Non-Root GPU Force** | `forceGpuRendering()` + `setHighPerformanceMode()` | 🆕 |
| **Universal Chipset Detection** | Rockchip, Allwinner, Amlogic, Broadcom support + device fallback | 🆕 |

### v1.7.1: Performance Overhaul & Bug Fixes

| Feature | Description | Status |
|---|---|---|
| **Namespace Fix** | `com.gamelauncher.app` → `com.gamelauncher` (fixes ClassNotFoundException) | 🔧 |
| **Foreground Service Fix** | `dataSync` → `specialUse` with proper declaration (crash fix on Android 15+) | 🔧 |
| **Kapt → KSP Migration** | Hilt compiler via KSP, no more "falling back to 1.9" warning | 🔧 |
| **Wake Lock Support** | PowerManager partial wake lock for non-root devices | 🆕 |
| **Non-Root Optimizer** | `optimizeNonRoot()` — wake lock + thread priority + animations off | 🆕 |
| **Non-Root App Killer** | killBackgroundApps now uses ActivityManager for non-root | 🆕 |
| **All Deprecation Warnings Fixed** | NetworkManager, Theme, aaptOptions | 🔧 |
| **lockRefreshRate Cleanup** | Removed broken API 36 dead code | 🔧 |

### v2.0.0: Enhanced Gaming Experience

| Feature | Description | Status |
|---|---|---|
| **Game Session Recording** | Auto-record start/end time, FPS, battery drain, RAM per session | ✅ |
| **Game Search & Filter** | Real-time search bar + category filter chips | ✅ |
| **Draggable FPS Overlay** | Touch and drag the FPS counter anywhere on screen | ✅ |
| **Onboarding Walkthrough** | 4-page guided intro for first-time users | ✅ |
| **Per-Game Graphics Mode** | Choose Performance/Balanced/Battery Saver/Custom per game | ✅ |
| **Brightness Slider** | Adjustable 10%-100% brightness slider | ✅ |
| **Session Stats Dashboard** | Total sessions & play time tracking cards | ✅ |
| **Game Details Screen** | Session history, average FPS, per-game statistics | ✅ |
| **CPU Core Control** | Toggle individual cores on/off (root required) | ✅ |
| **Benchmark Mode** | Run CPU/GPU/Memory benchmark, compare scores | ✅ |
| **Dark/Light Theme Toggle** | Switch between dark and light theme in Settings | ✅ |
| **Profile Import/Export** | Backup/restore per-game settings as JSON files | ✅ |

</div>

---

<div align="center">

### Supported Games (165+ Titles)

</div>

| Game | Package | Max FPS |
|---|---|---|
| **PUBG Mobile** | `com.tencent.ig` | 165 |
| **PUBG Mobile (Regional)** | `com.pubg.krmobile`, `com.pubg.imobile`, `com.pubg.mobile` | 165 |
| **PUBG NEW STATE** | `com.krafton.gamepubg` | 165 |
| **Call of Duty Mobile** | `com.activision.callofduty.shooter` | 165 |
| **Call of Duty Mobile (Garena)** | `com.garena.game.codm` | 165 |
| **Call of Duty Warzone Mobile** | `com.activision.callofduty.warzone` | 165 |
| **Free Fire / Free Fire Max** | `com.garena.game.freefire` / `com.garena.game.freefiremobile` | 165 |
| **Mobile Legends: Bang Bang** | `com.mobile.legends` | 165 |
| **Genshin Impact** | `com.miHoYo.GenshinImpact` / `com.HoYoverse.GenshinImpact` | 165 |
| **Honkai: Star Rail** | `com.miHoYo.hsr` / `com.HoYoverse.hkrpg` | 165 |
| **Zenless Zone Zero** | `com.HoYoverse.zzz` | 165 |
| **Wuthering Waves** | `com.kuro.wutheringwaves` | 165 |
| **League of Legends: Wild Rift** | `com.riotgames.leagueofwildrift` | 165 |
| **Valorant Mobile** | `com.riotgames.valorant` | 165 |
| **Honor of Kings** | `com.tencent.tmgp.sgame` | 165 |
| **Fortnite** | `com.epicgames.fortnite` / `com.epicgames.fortnitemobile` | 165 |
| **Apex Legends Mobile** | `com.ea.gp.apexlegendsmobilefps` | 165 |
| **Roblox** | `com.roblox.client` | 165 |
| **Minecraft PE** | `com.mojang.minecraftpe` | 165 |
| **Squad Busters** | `com.supercell.squad` | 165 |
| **Marvel Snap** | `com.levelinfinite.marvelsnap` | 165 |
| **World of Tanks Blitz** | `com.wargaming.wot.blitz` | 165 |
| **Monster Hunter Now** | `com.capcom.monsterhunter` | 165 |
| **Dragon Ball Legends** | `com.bandainamco.dragonballlegends` | 165 |
| **eFootball PES** | `com.konami.pes` | 165 |
| **GTA: Definitive Edition** | `com.rockstargames.gtade` | 165 |

*And 135+ more titles — including regional variants and ML-detected games.*

---

<div align="center">

## 🛠️ Tech Stack

</div>

| Category | Technology |
|---|---|
| **UI Framework** | Jetpack Compose BOM 2024.06 + Material Design 3 |
| **Dependency Injection** | Hilt 2.52 via KSP (no Kapt) |
| **Local Storage** | Room 2.6.1 + DataStore Preferences |
| **Architecture** | MVVM + Repository Pattern |
| **Build System** | Gradle 8.9 + AGP 8.5.2 + Kotlin DSL |
| **Language** | Kotlin 2.0.21 |
| **Annotation Processing** | KSP (Room + Hilt compiler) |
| **ML** | TensorFlow Lite 2.17 (game classification) |
| **Background Tasks** | WorkManager 2.10 |
| **Performance API** | ADPF v2 (Android 15+) + ADPF v1 (Android 12+) |
| **Frame Analysis** | Choreographer StateFlow-based FPS + FrameMetrics jank detection |
| **Wake Lock** | PowerManager partial wake lock (non-root) |
| **Benchmark** | CPU (sort/primes), GPU (Bitmap operations), Memory (allocation) |
| **Chipset Detection** | Proc/cpuinfo + sysfs + Build.SOC_MODEL (Snapdragon/MTK/Exynos/Kirin/Unisoc/Tensor + others) |

---

<div align="center">

## 🚀 Quick Start

</div>

### Requirements

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 36 (Android 16)
- Test device: Android 10-16 (API 29-36)
- Gradle 8.9 (wrapper included)

### Build Instructions

```bash
# Clone
git clone https://github.com/willygailo/Game-Launcher.git
cd "Pro Game Launcher.apk"

# Build debug APK (clean)
./gradlew clean assembleDebug

# Or incremental build
./gradlew assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Install sa Device

```bash
# Enable USB Debugging:
# Settings > About Phone > Tap "Build Number" 7 times
# Settings > System > Developer Options > Enable USB Debugging

adb install app/build/outputs/apk/debug/app-debug.apk
```

### Run Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumentation tests (naka connect na device/emulator)
./gradlew connectedDebugAndroidTest
```

### Build Troubleshooting

| Issue | Fix |
|---|---|
| `Kapt doesn't support language version 2.0+` | Already fixed — Hilt uses KSP now |
| `ForegroundServiceTypeMismatch` | Already fixed — all services use `specialUse` with declarations |
| `ClassNotFoundException` on launch | Already fixed — namespace matches actual packages |

---

<div align="center">

## 🔐 Permissions Needed

</div>

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | FPS overlay display sa top ng games |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | Background boosting services (game perf, overlay, game detection) |
| `WRITE_SETTINGS` | Automatic brightness at animation control |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep service alive during sessions |
| `ACCESS_WIFI_STATE` / `CHANGE_WIFI_STATE` | WiFi low-latency lock |
| `ACCESS_NOTIFICATIONS` | DND control during gaming |
| `PACKAGE_USAGE_STATS` | Auto game detection (UsageStatsManager) |
| `QUERY_ALL_PACKAGES` | Scan installed apps for games |
| `POST_NOTIFICATIONS` | Booster status notifications (Android 13+) |
| `MODIFY_AUDIO_SETTINGS` | Volume optimization during gaming |

---

<div align="center">

## 📱 Supported Chipsets

</div>

| Manufacturer | Series | Status |
|---|---|---|
| **Qualcomm** | Snapdragon 8 Elite, 8 Gen 1/2/3/4, 7+ Gen 2/3, 7s/7 Gen 3, 6 Gen 1, 4 Gen 2, all SD models | ✅ |
| **MediaTek** | Dimensity 9400/9300/9200/9000/8300/8200/8100/8025/7350/7300/7250/7200/7050, Helio G99/G96/G95/G91/G88/G85/G80/G70/G36/G25 | ✅ |
| **Samsung** | Exynos 2400/2200/2100/1480/1380/1280/1080, all Exynos models | ✅ |
| **Huawei** | Kirin 9010/9000S/9000/8000/990, all Kirin models | ✅ |
| **Unisoc** | T820/T770/T765/T760/T7250/T620/T618/T616/T610/T606 | ✅ |
| **Google** | Tensor G5/G4/G3/G2/G1 | ✅ |

*Smart auto-performance scaling — NOT permanent max lock to prevent overheating.*

### Android Versions

[![API 29](https://img.shields.io/badge/Android_10-API_29-3DDC84?style=flat)]()
[![API 30](https://img.shields.io/badge/Android_11-API_30-3DDC84?style=flat)]()
[![API 31](https://img.shields.io/badge/Android_12-API_31-3DDC84?style=flat)]()
[![API 33](https://img.shields.io/badge/Android_13-API_33-3DDC84?style=flat)]()
[![API 34](https://img.shields.io/badge/Android_14-API_34-3DDC84?style=flat)]()
[![API 35](https://img.shields.io/badge/Android_15-API_35-3DDC84?style=flat)]()
[![API 36](https://img.shields.io/badge/Android_16-API_36-3DDC84?style=flat)]()

---

<div align="center">

## 🔓 Root vs Non-Root Features

> The app is **fully designed for non-root devices**. 80% of features work without root.
> Root users get additional low-level optimizations, but the core gaming boost experience is complete without it.

</div>

| Feature | Without Root | With Root |
|---|---|---|
| ADPF Performance Session (Android 12+) | ✅ | ✅ |
| Wake Lock (PowerManager) | ✅ | ✅ |
| Thread Priority Boost | ✅ | ✅ |
| FPS/Hz Monitoring | ✅ | ✅ |
| Performance Benchmark | ✅ | ✅ |
| Jank Detection & Frame Timing | ✅ ✅ | ✅ ✅ |
| Refresh Rate Lock | ✅ (via Settings) | ✅ (via sysfs) |
| Animation Speed Control | ✅ (via Settings) | ✅ (via sysfs) |
| Memory Cleanup | ✅ (killBackgroundProcesses) | ✅ (kill + drop caches) |
| Touch Optimization | ✅ (pointer speed) | ✅ (20+ sysfs/props) |
| Storage Optimization | ✅ (cache cleanup) | ✅ (FSTRIM) |
| Game Session Recording | ✅ (manual + auto) | ✅ (manual + auto) |
| GPU Force Render | ✅ (via Settings) | ✅ (via sysfs) |
| High Performance Mode | ✅ (via PowerManager) | ✅ (via kernel) |
| CPU Governor Control | ❌ | ✅ |
| CPU Core Control | ❌ | ✅ |
| GPU Governor Control | ❌ | ✅ |
| Thermal Throttling Disable | ❌ | ✅ |
| Hidden Power Unlock | ❌ | ✅ |
| SoC-Specific Tweaks | ❌ | ✅ |
| FPS/Hz System Prop Lock | ❌ | ✅ |

*Root is optional — the app is designed to work great on non-root devices!*

---

<div align="center">

## 🔧 Troubleshooting

</div>

<details>
<summary><strong>Why are some features marked as "unavailable" in the notification?</strong></summary>

This is normal on non-root devices. The app shows a count of optimizations that require root access (CPU Governor, GPU tuning, etc.). All non-root features (FPS Boost, ADPF, Thread Priority, Animation Control, Memory Cleanup, Touch Optimization, Refresh Rate Lock) are still fully active.

</details>

<details>
<summary><strong>Max Brightness Not Working?</strong></summary>

1. Buksan ang **Settings** tab sa app
2. I-tap ang **Grant System Settings**
3. I-enable ang **Modify system settings** para sa Game Launcher

</details>

<details>
<summary><strong>FPS Overlay Not Showing?</strong></summary>

1. Go to **Settings → Apps → Game Launcher → Permissions**
2. I-enable ang **Display over other apps**
3. Restart ang app

</details>

<details>
<summary><strong>DND Not Working?</strong></summary>

1. Go to **Settings → Apps → Game Launcher → Notifications**
2. I-enable ang **Do Not Disturb access**
3. Grant permission sa popup dialog

</details>

<details>
<summary><strong>Booster Not Starting Automatically?</strong></summary>

1. Go to **Settings → Apps → Game Launcher**
2. Battery → **Unrestricted** (para di mapatay ng system)
3. Enable **Auto-Start** kung available sa device

</details>

---

<div align="center">

## 📂 Project Structure

</div>

```
app/src/main/java/com/gamelauncher/
├── core/              # Performance, Device, FPS/Jank, SoC, DND, Network, Touch, Thermals
│   ├── BenchmarkManager.kt    # CPU/GPU/Memory benchmark
│   ├── ProfileManager.kt      # Game profile import/export
│   └── ...
├── data/              # Database, Models, Repository
│   ├── local/         # Room Database & DAOs
│   ├── model/         # Data classes (GameModel, GamingSession, DeviceSpecs)
│   ├── preference/    # DataStore settings (theme, onboarding, toggles)
│   └── repository/    # Data layer
├── di/                # Hilt Dependency Injection
├── ml/                # TensorFlow Lite Game Classifier
├── receivers/         # Broadcast Receivers (Boot, Package Changes)
├── services/          # Background Services
│   ├── GameBoosterService
│   ├── GameDetectorService (UsageStatsManager-based, session recording)
│   ├── OverlayService (draggable FPS counter)
│   └── GameBoosterTileService
├── ui/                # UI Components (Compose)
│   ├── dashboard/     # Dashboard w/ Device Tier, Network, FPS, Benchmark, CPU Core
│   ├── games/         # Games Library w/ search, filter, game details screen
│   ├── onboarding/    # 4-step onboarding walkthrough
│   ├── settings/      # Settings w/ permissions, theme toggle, import/export
│   └── theme/         # App Theme & Colors (dark/light)
└── widgets/           # Home Screen Widget (Boost + Open buttons)
```

---

<div align="center">

## 🤝 Contributing

Contributions are welcome! Here's how:

1. Fork the repo
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Follow existing code style (MVVM, Compose, Hilt patterns)
4. Make sure tests pass: `./gradlew testDebugUnitTest`
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

---

## 📥 Download

- [Latest Release: v1.7.2](https://github.com/willygailo/Game-Launcher/releases/tag/v1.7.2)
- Or build from source using the instructions above

---

## 📜 License

MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🌟 Showcase

*Built with ❤️ for Filipino mobile gamers!*
*📍 Philippines 🇵🇭*

---

*Optimizing gaming performance one device at a time ⚡*

[![GitHub stars](https://img.shields.io/github/stars/willygailo/Game-Launcher?style=social)](https://github.com/willygailo/Game-Launcher/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/willygailo/Game-Launcher?style=social)](https://github.com/willygailo/Game-Launcher/network/members)

</div>