<div align="center">

# 🎮 GAME BOOSTER PRO & LAUNCHER

<p align="center">
  <img src="https://img.shields.io/badge/Release-v4.7.0-00F0FF?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v4.7.0" />
  <img src="https://img.shields.io/badge/Platform-Android%2016%20(SDK%2036)-00FF66?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android SDK 36" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Shizuku%20ADB%20Temporary%20Root-FF9900?style=for-the-badge&logo=linux&logoColor=white" alt="100% Shizuku ADB Temporary Root" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Pure Native Android Gaming Launcher & System Tuner with 100% Shizuku API Temporary Root Privilege Execution, Modular 44-Profile Device Identity Spoofing Engine, Per-Game Competitive CFG Cards & Unthrottled 165 FPS HUD* ⚡

---

[📦 Download v4.7.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.7.0) • [📖 Features](#-master-features) • [🏛️ Project Structure](#%EF%B8%8F-project-architecture) • [🚀 Build Guide](#-building-the-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 What's New in v4.7.0

> [!IMPORTANT]
> **GAME BOOSTER PRO v4.7.0** introduces a **Modular Per-Brand Hardware Device Identity Spoofing Engine** (44 real-world profiles across 10 brand classes), a **Dedicated Hardware Device Spoofing Settings Card** (master ON/OFF switch & active badge), and **Per-Game Competitive CFG Cards** directly in the Home screen with automatic Shizuku execution upon launch.

* 🎭 **Modular Per-Brand Device Identity Spoofing Engine**: Restructured into 10 separate brand profile modules (**Samsung**, **Realme**, **ASUS ROG**, **Xiaomi**, **OnePlus**, **OPPO**, **Vivo**, **Apple**, **Nubia / REDMAGIC**, **Black Shark**) featuring **44 real-world flagship profiles**.
* ⚙️ **Dedicated Hardware Device Spoofing Settings Card**: Features a master ON/OFF toggle, real-time active profile status badge, and scrollable catalog directly inside the Settings tab.
* ⚡ **Full 6-Namespace System Property Override**: Overrides `ro.product.model`, `ro.product.brand`, `ro.product.manufacturer`, `ro.product.device`, `ro.product.name`, `ro.hardware`, `ro.soc.model`, `ro.build.fingerprint`, `ro.build.display.id`, `ro.product.board`, `ro.hardware.chipname` via Shizuku temporary root access across all 6 Android property namespaces.
* 🎮 **Per-Game Competitive CFG Integration**: Directly configure 165Hz/165FPS Max Refresh & Touch Boost profiles inside Home game cards, auto-applying upon game launch via Shizuku.
* 🧹 **Cleaned System Tweaks Engine**: Centralized spoofing into the dedicated Settings card while deduplicating system tweaks for maximum stability.

---

## ⚡ Master Features

### 🔒 1. 100% Shizuku API Temporary Root Execution
* **Automated 28-Permission Shizuku ADB Combo**: Automatically grants `WRITE_SECURE_SETTINGS`, `SYSTEM_ALERT_WINDOW`, `MANAGE_GAME_MODE`, `FORCE_STOP_PACKAGES`, `CLEAR_APP_CACHE`, `SET_PROCESS_LIMIT`, `POST_NOTIFICATIONS`, and `FOREGROUND_SERVICE_SPECIAL_USE` via Shizuku binder IPC.
* **Dual Execution Engine (`Shizuku` vs `System Settings`)**: Seamless fallback execution if Shizuku binder is disconnected.

### 🎯 2. Hardware Refresh Rate (Hz) & FPS Lock
* Detects hardware display modes and locks refresh rates to **60Hz, 90Hz, 120Hz, 144Hz, or 165Hz**.
* Integrates Android 12+ Game Mode API (`cmd game mode performance <package>`) and per-app refresh rate overrides (`cmd window set-app-refresh-rate`).

### 🎨 3. Graphics & GPU Engine Optimization
* **Google ANGLE Vulkan GLES Driver Layer** (`settings put global angle_gl_driver_all_angle 1`).
* **Updatable System Game Driver** (`settings put global game_driver_all_apps 1`).
* **Vulkan HWUI Graphics Renderer** (`setprop debug.hwui.renderer vulkan`).
* **SurfaceFlinger Unsignaled Latching** (`setprop debug.sf.latch_unsignaled 1`).
* **Force 4x MSAA Anti-Aliasing** (`setprop debug.egl.force_msaa 1`).
* **High-Speed 16-Bit Alpha Textures** (`setprop persist.sys.use_16bpp_alpha 1`).

### 👆 4. Touch Latency & Digitizer Sensitivity
* **Touch Pressure Scale Reduction** (`setprop persist.sys.touch.pressure.scale 0.001`).
* **Touch Slop Gesture Sensitivity** (`settings put system touch_slop_reduction 1`).
* **Zero Scroll Cache Compression** (`setprop persist.sys.scrollingcache 3`).

### 🌐 5. Network & Location Acceleration
* **1-Tap Gaming DNS Booster**: Cloudflare (`1.1.1.1`), Google (`8.8.8.8`), and Default System DNS presets.
* **Tethering Hardware Offload Acceleration**: Enables hardware tethering offload (`settings put global tether_offload_disabled 0`).
* **Force Full GNSS Raw Measurements**: Forces raw GPS/GNSS measurements (`settings put global force_gnss_raw_measurements 1`).

### ⚙️ 6. Game Storage INI/Config Auto-Patcher & Modifier
* **Internal Data Config Auto-Patcher**: Creates and modifies game INI configuration files in `/sdcard/Android/data/<package_name>/files/` AND internal app storage `/data/data/<package_name>/files/` to force high FPS modes (`HighFPSMode=1`, `FrameRateLevel=9`, `MaxFrameRate=120`, `FPS=120`/`144`/`165`) before launching games.
* **Supported Popular Online Games**:
  - **Mobile Legends (MLBB)**: `com.mobile.legends`, `com.mobile.legends.vng`, `com.mobile.legends.kr`, `com.mobile.legends.jp`
  - **Call of Duty: Mobile (CODM)**: `com.activision.callofduty.shooter`, `com.garena.game.codm`, `com.tencent.tmgp.kr.codm`, `com.tencent.tmgp.cod`
  - **PUBG Mobile & BGMI**: `com.tencent.ig`, `com.pubg.imobile`, `com.pubg.krmobile`, `com.vng.pubgmobile`, `com.tencent.iglite`, `com.pubg.newstate`
  - **Garena Free Fire & MAX**: `com.dts.freefireth`, `com.dts.freefiremax`
  - **League of Legends: Wild Rift**: `com.riotgames.league.wildrift`, `com.riotgames.league.wildrifttw`, `com.riotgames.league.wildriftvn`
  - **Honor of Kings (HOK) & AoV**: `com.levelinfinite.sgameGlobal`, `com.tencent.tmgp.sgame`, `com.garena.game.kgtw`
  - **Genshin Impact & HoYoverse**: `com.cognosphere.GenshinImpact`, `com.miHoYo.GenshinImpact`, `com.HoYoverse.hkrpgoversea`
  - **Roblox & Shooters**: `com.roblox.client`, `com.axlebolt.standoff2`, `com.netease.bloodstrike`, `com.miracle.farlight84`

### 🎭 7. Full System Device Identity Spoofing Engine (44 Profiles)
* **Multi-Tier Property Overrides via Shizuku ADB**: Overrides system identity properties across 6 namespaces (`ro.product.model`, `ro.product.brand`, `ro.product.manufacturer`, `ro.product.device`, `ro.product.name`, `ro.hardware`, `ro.board.platform`, `ro.soc.model`, `ro.build.fingerprint`, `ro.build.display.id`, `ro.product.board`, `ro.hardware.chipname`).
* **10 Brand Profile Collections**:
  - **Samsung**: Galaxy S25 Ultra, S24 Ultra, S23 Ultra, Z Fold 5, Tab S9 Ultra, A55 5G.
  - **Realme**: GT 5 Pro, GT Neo 5, GT 3, Narzo 60 Pro.
  - **ASUS ROG**: ROG Phone 8 Pro, 7 Ultimate, 6, 5s (144/165Hz Extreme).
  - **Xiaomi**: 14 Ultra, 14 Pro, POCO F6 Pro, Redmi K70 Pro, Redmi Note 13 Pro+.
  - **OnePlus**: 12, 11, Ace 3 Pro, Nord 4.
  - **OPPO**: Find X7 Ultra, Find X6 Pro, Reno 12 Pro.
  - **Vivo**: X100 Ultra, iQOO 12, iQOO Neo 9 Pro, V30 Pro.
  - **Apple**: iPad Pro M4, iPad Pro M2, iPhone 16 Pro Max, iPhone 15 Pro Max (Tablet FOV & Ultra Graphics).
  - **Nubia / REDMAGIC**: REDMAGIC 9S Pro, 9 Pro, 8 Pro (165Hz Extreme).
  - **Black Shark**: 5 Pro, 4 Pro, 5.

### 🔍 8. Multi-Platform Deep Search Engine (Android 11 to 16)
* **Unrestricted Multi-User Package Scanner**: Uses Shizuku ADB (`pm list packages -3 -u -a`) to bypass Android 11–16 package visibility restrictions, discovering games installed in secondary user profiles, Work Profiles, Dual Apps, Parallel Space, Island, and Shelter.
* **Third-Party Platform Store Detection**: Automatically scans and indexes games downloaded from **Google Play Store**, **TapTap**, **Garena App Store**, **Samsung Galaxy Store**, **APKPure**, **QooApp**, and **Amazon Appstore**.
* **Deep Storage Directory Scanner**: Inspects `/sdcard/Android/data/`, `/sdcard/Android/obb/`, and `/data/data/` for game data signatures and regional patches.

---

## 🚀 Building the APK

To build the debug APK, run:

```bash
cd android
./gradlew clean assembleDebug
```

### 📦 Output APK Location:
```
Game_Space_Debug.apk
android/app/build/outputs/apk/debug/Game_Space_Debug.apk
```

---

## 🌐 Latest Releases & Downloads

| Release Tag | Title | Link |
| :--- | :--- | :--- |
| **v4.7.0 (Latest)** | Modular 44-Profile Device Identity Spoofing Engine, Dedicated Settings Spoof Card & Per-Game Competitive CFG | [Download v4.7.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.7.0) |
| **v4.6.0** | Full Device Identity Spoofing Engine, Multi-Platform Deep Search, Unthrottled 120/144/165 FPS HUD & Game Config Patchers | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.6.0) |
| **v4.4.0** | 100% Shizuku API Temporary Root, Redesigned Gaming Launcher, Full Boost System Tweaks & Internal Data Config Patcher | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.4.0) |
| **v4.3.0** | Guaranteed Online Games Library, App Picker Catalog Injection & Play Store Fallback | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.3.0) |
| **v4.2.0** | Manifest Package Visibility Queries, Shizuku Package Scanner & Add Game App Picker | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.2.0) |
| **v4.1.0** | Dual-Scan Game Detection Engine & Game Storage Config Auto-Patcher | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.1.0) |
| **v4.0.0** | Floating Overlay HUD Fix, Online Games Library, 144Hz/165Hz FPS Unlock & Auto Shizuku Permissions | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.0.0) |

---

## 👤 Developer & Contact

<table align="center">
  <tr>
    <td align="center" width="150">
      <img src="https://github.com/willygailo.png" width="100px;" alt="Willy Gailo"/><br />
      <sub><b>Willy Gailo</b></sub>
    </td>
    <td>
      <b>Connect with the Developer & Downloads:</b>
      <br/><br/>
      📦 <b>APK Releases:</b> <a href="https://github.com/willygailo/Game-Launcher/releases">willygailo/Game-Launcher/releases</a><br/>
      🐱 <b>GitHub:</b> <a href="https://github.com/willygailo">@willygailo</a><br/>
      📘 <b>Facebook:</b> <a href="https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027">Willy Gailo</a><br/>
      📂 <b>Repository:</b> <a href="https://github.com/willygailo/Game-Launcher">willygailo/Game-Launcher</a>
    </td>
  </tr>
</table>

---

<div align="center">
  <sub>Built with ❤️ and Pure Java 17 for Non-Rooted Android Gamers worldwide.</sub>
</div>
