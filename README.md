<p align="center">
  <img src="BANNER.gif" alt="Precision Aim Banner" width="100%" style="border-radius: 12px;">
</p>

<h1 align="center">🎮 Game Launcher Pro V2.0 (v5.0.0 / versionCode 50) — Zero Touch Delay & Universal Game Spoofer Engine</h1>

<p align="center">
  <b>0ms Touch Delay, 1000Hz Digitizer Sampling Engine, Per-Game Hardware Identity Spoofer & 165Hz Game Patcher for Mobile eSports</b>
</p>

<p align="center">
  <a href="https://github.com/willygailo/Game-Launcher"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"></a>
  <a href="https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027"><img src="https://img.shields.io/badge/Facebook-1877F2?style=for-the-badge&logo=facebook&logoColor=white" alt="Facebook"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License"></a>
  <a href="android"><img src="https://img.shields.io/badge/Android-API_36-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"></a>
  <a href=".github/workflows/android-build.yml"><img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions" alt="Build Status"></a>
  <a href="https://github.com/willygailo/Game-Launcher/releases"><img src="https://img.shields.io/badge/Version-v5.0.0--PRO-emerald?style=for-the-badge" alt="Version v5.0.0"></a>
</p>

---

## 🚀 Overview

**Game Launcher Pro V2.0 (v5.0.0 / versionCode 50)** is an advanced, device-level performance utility, hardware identity spoofer, and game launcher engineered for competitive eSports gaming (*Mobile Legends, PUBG Mobile, COD Mobile, Honor of Kings, Genshin Impact, Wuthering Waves, Zenless Zone Zero, Delta Force, Free Fire, Wild Rift, Blood Strike, Warzone Mobile*).

By leveraging **Shizuku API (privileged ADB Binder IPC - uid 2000)**, it unlocks **120 FPS, 144 FPS, and 165 FPS**, enforces **0ms Zero Touch Delay with 1000Hz Digitizer Sampling**, applies **Per-Game Package Spoofer Profiles** (each game target receives its own custom hardware identity to eliminate conflicts), and controls **Transsion / Infinix / Tecno / ROG / Samsung Bypass Charging** directly.

> [!IMPORTANT]
> **SAFETY & COMPLIANCE GUARANTEE**:
> - ⚡ **Zero Executable Tampering**: Operates strictly using standard system properties (`setprop`, `resetprop`, `settings put`, `device_config`) and native game INI/JSON/XML configuration files.
> - ⚡ **Shizuku Legal System Bridge**: Uses ADB shell privileges (`uid 2000`) with automatic `.bak` backups and read-only file locks (`chmod 444`) to prevent game clients resetting graphics settings on startup.
> - ⚡ **100% Reversible**: All system property overrides are volatile and safely revert to device factory defaults upon reboot.

---

## ⚡ Key Modules & Real-Time Engines

### 1. Per-Game Hardware Identity Spoofer (`com.gamebooster.app.spoofer.games`)
Every game package receives its optimal hardware identity strategy without causing conflicts (*kanya-kanya files*):
- **Mobile Legends (MLBB)**: `MlbbSpooferStrategy` → ROG Phone 9 Pro Profile (Snapdragon 8 Elite, 165Hz Ultra Frame Rate).
- **PUBG Mobile / BGMI / New State**: `PubgSpooferStrategy` → REDMAGIC 10 Pro Profile (Snapdragon 8 Elite, ICE 14 Cooling, 165Hz Display).
- **Call of Duty Mobile (CODM)**: `CodmSpooferStrategy` → Black Shark 5 Pro Profile (Snapdragon 8 Gen 1, 144Hz Touch Boost).
- **Honor of Kings (HOK)**: `HokSpooferStrategy` → iQOO 15 Ultra Profile (Snapdragon 8 Elite, 165Hz Frame Rate).
- **Genshin Impact & Honkai: Star Rail**: `GenshinStarRailSpooferStrategy` → Galaxy S26 Ultra Profile (Snapdragon 8 Elite for Galaxy).
- **Free Fire / Free Fire Max**: `FreeFireSpooferStrategy` → OnePlus 12 Profile (Snapdragon 8 Gen 3, 120Hz Display).
- **League of Legends: Wild Rift**: `WildRiftSpooferStrategy` → ROG Phone 8 Pro Profile (165Hz Frame Rate).
- **Zenless Zone Zero & Wuthering Waves**: `ZzzWuWaSpooferStrategy` → Galaxy S26 Ultra Profile (Maximum Vulkan Shader Cache & Metal FX preset).

### 2. Flagship OEM Brand Profiles (12 Major Manufacturers)
Includes complete Android 13, 14, 15, and 16 fingerprints across 6 system property namespaces (`product`, `vendor`, `system`, `odm`, `product.product`, `system_ext`):
1. **Samsung**: Galaxy S26 Ultra (Android 16 `BP1A.260105.001`), Galaxy S25 Ultra, S24 Ultra.
2. **Vivo / iQOO**: iQOO 15 Ultra (Snapdragon 8 Elite, 165Hz), iQOO 12 Pro.
3. **ASUS ROG**: ROG Phone 9 Pro (165Hz, Snapdragon 8 Elite), ROG Phone 8 Pro.
4. **Nubia / REDMAGIC**: REDMAGIC 10 Pro (165Hz), REDMAGIC 9 Pro.
5. **Xiaomi / POCO**: Xiaomi 15 Ultra (HyperOS 2.0), Poco F6 Pro, Xiaomi 14 Ultra.
6. **Infinix**: GT 20 Pro 5G, GT 10 Pro 5G, Zero 30 5G (Dimensity 8200 Ultimate).
7. **Tecno**: Camon 30 Pro 5G, Pova 6 Pro 5G, Phantom V Fold.
8. **OnePlus / OPPO / Realme**: OnePlus 13 (OxygenOS 15), OnePlus 12, Find X7 Ultra.
9. **Sony**: Xperia 1 VI, Xperia 1 V.
10. **Google Pixel**: Pixel 9 Pro XL, Pixel 8 Pro.
11. **Black Shark**: Black Shark 5 Pro, Black Shark 4 Pro.

### 3. Modular OEM Bypass Charging Suite (`com.gamebooster.app.bypasscharging`)
Prevents device battery heating by routing charger power directly to the motherboard:
- **Transsion (Infinix & Tecno)**: Direct sysfs node writes (`/sys/class/power_supply/battery/bypass_mode`, `input_suspend`) + ADB settings overrides (`bypass_charge_enable=1`).
- **Samsung**: Direct Game Game Booster bypass charge injection (`cmd settings put global bypass_charge_enable 1`).
- **Xiaomi / POCO**: Smart Battery Manager bypass override (`setprop sys.battery.bypass 1`).
- **ASUS ROG & REDMAGIC**: Direct charging bypass control nodes.

### 4. Zero Touch Delay & 1000Hz Digitizer Sampling Engine
- **1000Hz Touch Sampling Frequency**: `setprop debug.input.max_events_per_sec 1000`
- **Zero Drag Deadzone**: `setprop view.touch_slop 0` & `settings put system touch_slop_reduction 1`
- **1:1 Linear Pointer Response**: `settings put system pointer_speed 7`
- **0ms Response Delay & Touch Rebound**: `setprop persist.sys.touch.response_time 0` & `setprop persist.sys.touch.sensitivity 10`
- **Predictive Touch Frame Synthesis**: `setprop persist.sys.touch_prediction 1` & `setprop persist.vendor.qti.input.touch_boost 1`

### 5. Universal Auto Game Scanner & 2025/2026 eSports Support
- Auto-detects 40+ AAA titles including: *Mobile Legends, PUBG Mobile, BGMI, CODM, Honor of Kings, Genshin Impact, Wuthering Waves, Zenless Zone Zero, Delta Force: Hawk Ops, Warzone Mobile, Blood Strike, Roblox, Free Fire, Wild Rift, Standoff 2, Farlight 84*.

---

## 🏗️ Project Architecture

```
Game-Launcher/
├── .github/
│   └── workflows/        # GitHub Actions CI build & release workflow
├── android/
│   ├── app/              # Android app source code (SDK 36, Java 17)
│   │   ├── src/main/assets/
│   │   │   ├── backgrounds/ # App background image assets
│   │   │   └── index.html   # Web Dashboard UI
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/  # Refresh rate locking & OEM thermal mitigation
│   │   │   ├── bypasscharging/ # Modular OEM Bypass Charging strategies (Infinix, Tecno, Samsung, Xiaomi, ROG, REDMAGIC)
│   │   │   ├── config/   # Game patchers (MLBB, PUBG, CODM, HOK, Genshin, ZZZ, WuWa, Delta Force)
│   │   │   ├── core/     # AppExecutors, PropertyResolver, GameBoosterJsInterface (JS Web Bridge)
│   │   │   ├── device/   # Snapdragon 8 Elite & Dimensity 9400/8200 detector, Sysfs CPU Temp Monitor
│   │   │   ├── engine/   # CommandExecutor & RefreshRateOverrideEngine
│   │   │   ├── games/    # Game library, GameManagerRepository, package detector & search engine
│   │   │   ├── gamespace/ # AutoGameMonitorService, DND Manager, GameCacheCleaner
│   │   │   ├── overlay/  # SurfaceFlinger Floating FPS HUD & Reticle/Crosshair overlay
│   │   │   ├── services/ # GameBoosterService & BootReceiver
│   │   │   ├── shizuku/  # ShizukuExecutor, Scoped Storage ShizukuFileBridge & AIDL UserService
│   │   │   ├── spoofer/  # Hardware Identity Spoofer Engine, 12 Brand Profiles, Per-Game Package Strategies (`spoofer/games/`)
│   │   │   ├── tweaks/   # System tweaks repository & providers (CpuGpu, TouchDisplay, NetworkAudio, SystemKernel)
│   │   │   └── ui/       # MainActivity & UI components
│   │   └── src/test/     # Unit tests
│   └── build.gradle      # App build config (versionCode 50, versionName "5.0.0")
├── BANNER.gif            # Project banner header
├── LICENSE               # Apache 2.0 Open Source License
├── SECURITY.md          # Security policy & ban-safety rules
└── README.md             # Project documentation
```

---

## 🛠️ Installation & Shizuku Setup

1. **Download APK**: Download `Game_Space_Debug.apk` or `Game_Space.apk` from [Latest Releases](https://github.com/willygailo/Game-Launcher/releases).
2. **Install & Launch Shizuku**: Install [Shizuku from Play Store or GitHub](https://shizuku.rikka.app/).
3. **Start Shizuku**: Start Shizuku service via **Wireless Debugging** or **ADB PC (`adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh`)**.
4. **Grant Privileges**: Open Game Launcher Pro V2.0 and tap **"Grant Shizuku Permission"**.
5. **Lock Frame Rate & Enjoy**: Select your target game, choose your device spoofer profile (e.g. S26 Ultra, iQOO 15 Ultra, ROG 9 Pro), enable 165Hz refresh rate lock, and launch your game!

---

## 📄 License

This project is open source under the **Apache License 2.0**.
