<p align="center">
  <img src="BANNER.gif" alt="Precision Aim Banner" width="100%" style="border-radius: 12px;">
</p>

<h1 align="center">🎮 Game Launcher Pro V2.0 (v10.0.0-PRO / versionCode 51) — Zero Touch Delay & Universal Game Spoofer Engine</h1>

<p align="center">
  <b>0ms Touch Delay, 1000Hz Digitizer Sampling Engine, Per-Game Hardware Identity Spoofer & 165Hz Game Patcher for Mobile eSports</b>
</p>

<p align="center">
  <a href="https://github.com/willygailo/Game-Launcher"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"></a>
  <a href="https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027"><img src="https://img.shields.io/badge/Facebook-1877F2?style=for-the-badge&logo=facebook&logoColor=white" alt="Facebook"></a>
  <a href="https://github.com/willygailo/Game-Launcher/releases"><img src="https://img.shields.io/badge/Releases-Download_APK-FF6C37?style=for-the-badge&logo=android&logoColor=white" alt="Download APK"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License"></a>
  <a href="android"><img src="https://img.shields.io/badge/Android-API_36-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"></a>
  <a href=".github/workflows/android-build.yml"><img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions" alt="Build Status"></a>
  <a href="https://github.com/willygailo/Game-Launcher/releases"><img src="https://img.shields.io/badge/Version-v10.0.0--PRO-emerald?style=for-the-badge" alt="Version v10.0.0-PRO"></a>
</p>

---

## 👨‍💻 Lead Developer & Author Information

<div align="center">

| 👤 Developer | 🌐 Official Facebook | 🐙 Official GitHub | 📦 Latest Downloads / APK |
| :--- | :--- | :--- | :--- |
| **WILLY JR CARNASA GAILO** | [![Facebook](https://img.shields.io/badge/Facebook-WILLY_JR_CARNASA_GAILO-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027) | [![GitHub](https://img.shields.io/badge/GitHub-willygailo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo) | [![Releases](https://img.shields.io/badge/Releases-v10.0.0_PRO_APK_Downloads-22c55e?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases) |

</div>

---

## 🚀 Overview

**Game Launcher Pro V2.0 (v10.0.0-PRO / versionCode 51)** is an advanced, device-level performance utility, hardware identity spoofer, and game launcher engineered by **WILLY JR CARNASA GAILO** for competitive eSports gaming (*Mobile Legends, PUBG Mobile, COD Mobile, Honor of Kings, Genshin Impact, Wuthering Waves, Zenless Zone Zero, Delta Force, Free Fire, Wild Rift, Blood Strike, Warzone Mobile*).

By leveraging **Shizuku API (privileged ADB Binder IPC - uid 2000)**, it unlocks **120 FPS, 144 FPS, and 165 FPS**, enforces **0ms Zero Touch Delay with 1000Hz Digitizer Sampling**, applies **Per-Game Package Spoofer Profiles** (each game target receives its own custom hardware identity to eliminate conflicts), and controls **Transsion / Infinix / Tecno / ROG / Samsung Bypass Charging** directly.

> [!IMPORTANT]
> **SAFETY & COMPLIANCE GUARANTEE**:
> - ⚡ **Zero Executable Tampering**: Operates strictly using standard system properties (`setprop`, `resetprop`, `settings put`, `device_config`) and native game INI/JSON/XML configuration files.
> - ⚡ **Shizuku Legal System Bridge**: Uses ADB shell privileges (`uid 2000`) with automatic `.bak` backups and read-only file locks (`chmod 444`) to prevent game clients resetting graphics settings on startup.
> - ⚡ **100% Reversible**: All system property overrides are volatile and safely revert to device factory defaults upon reboot.

---

## ⚡ Key Modules & Real-Time Engines

### 1. 🛡️ Per-Game Hardware Identity Spoofer (`com.gamebooster.app.spoofer.games`)
Every game package receives its optimal hardware identity strategy without causing conflicts (*kanya-kanya files*):
- 👑 **Mobile Legends (MLBB)**: `MlbbSpooferStrategy` → ROG Phone 9 Pro Profile (Snapdragon 8 Elite, 165Hz Ultra Frame Rate).
- 🔫 **PUBG Mobile / BGMI / New State**: `PubgSpooferStrategy` → REDMAGIC 10 Pro Profile (Snapdragon 8 Elite, ICE 14 Cooling, 165Hz Display).
- 💣 **Call of Duty Mobile (CODM)**: `CodmSpooferStrategy` → Black Shark 5 Pro Profile (Snapdragon 8 Gen 1, 144Hz Touch Boost).
- ⚔️ **Honor of Kings (HOK)**: `HokSpooferStrategy` → iQOO 15 Ultra Profile (Snapdragon 8 Elite, 165Hz Frame Rate).
- 🔮 **Genshin Impact & Honkai: Star Rail**: `GenshinStarRailSpooferStrategy` → Galaxy S26 Ultra Profile (Snapdragon 8 Elite for Galaxy).
- 🎯 **Free Fire / Free Fire Max**: `FreeFireSpooferStrategy` → OnePlus 12 Profile (Snapdragon 8 Gen 3, 120Hz Display).
- 🗡️ **League of Legends: Wild Rift**: `WildRiftSpooferStrategy` → ROG Phone 8 Pro Profile (165Hz Frame Rate).
- 🌀 **Zenless Zone Zero & Wuthering Waves**: `ZzzWuWaSpooferStrategy` → Galaxy S26 Ultra Profile (Maximum Vulkan Shader Cache & Metal FX preset).

### 2. 📱 Flagship OEM Brand Profiles (12 Major Manufacturers)

#### 📊 Flagship Gaming Hardware Comparison Matrix
| Phone | Chipset | GPU | Display | RAM / Storage | Battery & Thermal |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Galaxy S26 Ultra** | Snapdragon 8 Elite Gen 5 for Galaxy (3nm, Oryon V3) | Adreno 840 | 6.9" LTPO AMOLED, 120Hz / 165Hz | 12/256GB – 24/1TB, LPDDR5X | 5000mAh, 45W wired |
| **Galaxy S25 Ultra** | Snapdragon 8 Elite for Galaxy (3nm, Oryon Gen2) | Adreno 830 | 6.9" LTPO AMOLED, 120Hz | 12/256GB – 16/1TB, LPDDR5X | 5000mAh, 45W wired |
| **Galaxy S24 Ultra** | Snapdragon 8 Gen 3 for Galaxy (4nm) | Adreno 750 | 6.8" LTPO AMOLED, 120Hz | 12/256GB – 12/1TB, LPDDR5X | 5000mAh, 45W wired |
| **iQOO 15 Ultra** | Snapdragon 8 Elite Gen 5 (3nm) + Active Fan | Adreno 840 (@1300MHz) | 6.85" LTPO AMOLED, 144Hz/165Hz 2K, 2600 nits | up to 24GB/1TB, LPDDR5X | 7400mAh, 100W/40W |
| **iQOO 15 (Vanilla)** | Snapdragon 8 Elite Gen 5 (3nm) + Q3 Gaming Chip | Adreno 840 | 6.85" LTPO AMOLED, 144Hz 2K | up to 16GB/1TB, LPDDR5X | 7000mAh, 100W/40W |
| **iQOO 13** | Snapdragon 8 Elite (3nm) + Q2 Gaming Chip | Adreno 830 | 6.82" LTPO AMOLED, 144Hz 2K | up to 16GB/512GB, LPDDR5X | 6150mAh, 120W wired |

Includes complete Android 13, 14, 15, and 16 fingerprints across 6 system property namespaces (`product`, `vendor`, `system`, `odm`, `product.product`, `system_ext`):
1. 🌌 **Samsung**: Galaxy S26 Ultra (Android 16 `BP1A.260105.001`), Galaxy S25 Ultra, S24 Ultra.
2. ⚡ **Vivo / iQOO**: iQOO 15 Ultra (Snapdragon 8 Elite, 165Hz), iQOO 12 Pro.
3. 👹 **ASUS ROG**: ROG Phone 9 Pro (165Hz, Snapdragon 8 Elite), ROG Phone 8 Pro.
4. 🔴 **Nubia / REDMAGIC**: REDMAGIC 10 Pro (165Hz), REDMAGIC 9 Pro.
5. 🐲 **Xiaomi / POCO**: Xiaomi 15 Ultra (HyperOS 2.0), Poco F6 Pro, Xiaomi 14 Ultra.
6. 🚀 **Infinix**: GT 20 Pro 5G, GT 10 Pro 5G, Zero 30 5G (Dimensity 8200 Ultimate).
7. 💎 **Tecno**: Camon 30 Pro 5G, Pova 6 Pro 5G, Phantom V Fold.
8. 🔴 **OnePlus / OPPO / Realme**: OnePlus 13 (OxygenOS 15), OnePlus 12, Find X7 Ultra.
9. 📷 **Sony**: Xperia 1 VI, Xperia 1 V.
10. 🎯 **Google Pixel**: Pixel 9 Pro XL, Pixel 8 Pro.
11. 🦈 **Black Shark**: Black Shark 5 Pro, Black Shark 4 Pro.

### 3. 🔌 Modular OEM Bypass Charging Suite (`com.gamebooster.app.bypasscharging`)
Prevents device battery heating by routing charger power directly to the motherboard:
- ⚡ **Transsion (Infinix & Tecno)**: Direct sysfs node writes (`/sys/class/power_supply/battery/bypass_mode`, `input_suspend`) + ADB settings overrides (`bypass_charge_enable=1`).
- ⚡ **Samsung**: Direct Game Booster bypass charge injection (`cmd settings put global bypass_charge_enable 1`).
- ⚡ **Xiaomi / POCO**: Smart Battery Manager bypass override (`setprop sys.battery.bypass 1`).
- ⚡ **ASUS ROG & REDMAGIC**: Direct charging bypass control nodes.

### 4. 🎯 Zero Touch Delay & 1000Hz Digitizer Sampling Engine
- ⚡ **1000Hz Touch Sampling Frequency**: `setprop debug.input.max_events_per_sec 1000`
- ⚡ **Zero Drag Deadzone**: `setprop view.touch_slop 0` & `settings put system touch_slop_reduction 1`
- ⚡ **1:1 Linear Pointer Response**: `settings put system pointer_speed 7`
- ⚡ **0ms Response Delay & Touch Rebound**: `setprop persist.sys.touch.response_time 0` & `setprop persist.sys.touch.sensitivity 10`
- ⚡ **Predictive Touch Frame Synthesis**: `setprop persist.sys.touch_prediction 1` & `setprop persist.vendor.qti.input.touch_boost 1`

### 5. 🎮 Universal Auto Game Scanner & 2025/2026 eSports Support
- Auto-detects 40+ AAA titles including: *Mobile Legends, PUBG Mobile, BGMI, CODM, Honor of Kings, Genshin Impact, Wuthering Waves, Zenless Zone Zero, Delta Force: Hawk Ops, Warzone Mobile, Blood Strike, Roblox, Free Fire, Wild Rift, Standoff 2, Farlight 84*.

### 6. ⚡ Unified Target Game Registry & Shizuku Auto-Grant Engine
- 🔑 **Canonical Target Game Registry (`TargetGameRegistry.java`)**: Centralized single source of truth for 35+ eSports packages across permission grants, spoofer strategies, and manifest declarations.
- 🔑 **Automated Shizuku System Permission Grant**: Automatically executes `pm grant` & `appops set` batch sequence upon Shizuku binder connection and boot. Grants `WRITE_SECURE_SETTINGS`, `WRITE_SETTINGS`, `PACKAGE_USAGE_STATS`, `MANAGE_EXTERNAL_STORAGE`, `SCHEDULE_EXACT_ALARM`, and unrestricted background operations without root.
- 🔑 **Zero-Delay Background Boot Pipeline**: `BootReceiver` and `GameBoosterService` trigger asynchronous background tasks to register binder listeners, apply low-latency touch/GPU tweaks, and launch `AutoGameMonitorService` cleanly on boot.

---


---

## 🛠️ Installation & Shizuku Setup

1. **📥 Download APK**: Download `Game_Space_Debug.apk` or `Game_Space.apk` from [Official Releases](https://github.com/willygailo/Game-Launcher/releases).
2. **🔌 Install & Launch Shizuku**: Install [Shizuku from Play Store or GitHub](https://shizuku.rikka.app/).
3. **⚡ Start Shizuku**: Start Shizuku service via **Wireless Debugging** or **ADB PC (`adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh`)**.
4. **🔑 Grant Privileges**: Open Game Launcher Pro V2.0 and tap **"Grant Shizuku Permission"**.
5. **🔥 Lock Frame Rate & Enjoy**: Select your target game, choose your device spoofer profile (e.g. S26 Ultra, iQOO 15 Ultra, ROG 9 Pro), enable 165Hz refresh rate lock, and launch your game!

---

## 🔄 How to Revert Back to Stock Normal Phone (Full Reset Guide)

To safely revert all settings, ANGLE graphics drivers, display refresh rate overrides, touch optimizations, and hardware identity spoofer back to factory stock defaults:

### Method 1: ⚡ 1-Tap Reset via Game Launcher App
Open **Game Launcher Pro V2.0** → Navigate to **Settings** → Tap **RESET TO FACTORY DEFAULTS**, then **Reboot your Phone**.

---

### Method 2: 💻 Complete ADB Command Sequence (PC / Wireless Debugging)

Run these ADB commands directly on your PC, LADB, or Shizuku Terminal:

#### 1. Reset ANGLE Graphics Driver & Game Driver Settings
```bash
adb shell settings delete global angle_gl_driver_all_angle
adb shell settings delete global angle_gl_driver_selection_pkgs
adb shell settings delete global angle_gl_driver_selection_values
adb shell settings delete global show_angle_in_use_dialog_box
adb shell settings delete global game_driver_all_apps
adb shell settings delete global game_driver_opt_in_apps
adb shell setprop debug.angle.backend ""
```

#### 2. Reset CPU Governor & Realtime Performance Modes
```bash
adb shell cmd power set-mode 0 0
adb shell cmd power set-mode 2 0
adb shell setprop persist.sys.cpu.governor ""
adb shell setprop sys.io.scheduler ""
adb shell setprop sys.use_fifo ""
```

#### 3. Reset GPU Hardware Pipeline & HWUI Composition
```bash
adb shell setprop debug.hwui.renderer ""
adb shell setprop debug.sf.hw ""
adb shell setprop debug.egl.hw ""
adb shell setprop debug.egl.hw_renderer ""
```

#### 4. Reset Display Refresh Rate Settings
```bash
adb shell settings delete system peak_refresh_rate
adb shell settings delete system min_refresh_rate
adb shell settings delete system user_refresh_rate
adb shell settings delete global peak_refresh_rate
adb shell settings delete global min_refresh_rate
adb shell settings delete secure user_refresh_rate
adb shell settings delete secure refresh_rate_mode
```

#### 5. Reset Game Mode API & Overlays
```bash
adb shell cmd game mode standard global
adb shell cmd window reset-app-refresh-rate global
adb shell device_config delete game_overlay global
```

#### 6. Reset Touch Latency & Digitizer Slop
```bash
adb shell setprop debug.input.max_events_per_sec ""
adb shell setprop view.touch_slop ""
adb shell settings delete system touch_slop_reduction
adb shell settings delete system pointer_speed
adb shell setprop persist.sys.touch.response_time ""
adb shell setprop persist.sys.touch.sensitivity ""
adb shell setprop persist.sys.touch_prediction ""
adb shell setprop persist.vendor.qti.input.touch_boost ""
```

#### 7. Reset SurfaceFlinger & SwapInterval Overrides
```bash
adb shell setprop debug.sf.fps_limit ""
adb shell setprop persist.sys.NV_FPSLIMIT ""
adb shell setprop persist.sys.NV_POWERMODE ""
adb shell setprop debug.gr.swapinterval ""
adb shell setprop debug.egl.swapinterval ""
adb shell setprop debug.sf.latch_unsignaled ""
adb shell setprop debug.sf.disable_backpressure ""
```

#### 8. Reset Hardware Identity Spoofer & Reboot Phone
```bash
adb shell setprop persist.sys.game.boost.profile 0
adb reboot
```

---

## 🌐 Contact & Connect with Developer

- 👤 **Developer**: **WILLY JR CARNASA GAILO**
- 🔵 **Facebook**: [https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
- 🐙 **GitHub**: [https://github.com/willygailo](https://github.com/willygailo)
- 📦 **Releases & APK Downloads**: [https://github.com/willygailo/Game-Launcher/releases](https://github.com/willygailo/Game-Launcher/releases)

---

## 📄 License

This project is open source under the **Apache License 2.0**. Developed with ❤️ by **WILLY JR CARNASA GAILO**.
