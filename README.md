<div align="center">

# 🎮 GAME BOOSTER PRO & LAUNCHER

<p align="center">
  <img src="https://img.shields.io/badge/Release-v4.7.0-00F0FF?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v4.7.0" />
  <img src="https://img.shields.io/badge/Platform-Android%2015%20(SDK%2036)-00FF66?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android SDK 36" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Shizuku%20ADB%20Temporary%20Root-FF9900?style=for-the-badge&logo=linux&logoColor=white" alt="100% Shizuku ADB Temporary Root" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Pure Native Android Gaming Launcher & System Tuner with 100% Shizuku API Temporary Root Privilege Execution, Per-Brand 44-Device Hardware Identity Spoofing Engine, Home Card Competitive CFGs & Unthrottled 120/144/165 FPS HUD* ⚡

---

[📦 Download v4.7.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.7.0) • [📖 Features](#-master-features) • [🏛️ Project Architecture](#-project-architecture) • [🚀 Build Guide](#-building-the-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 What's New in v4.7.0

> [!IMPORTANT]
> **GAME BOOSTER PRO v4.7.0** introduces **Per-Brand Hardware Device Identity Spoofing** with **44 flagship profiles** across 10 brand classes (Samsung, Realme, ROG, Xiaomi, OnePlus, OPPO, Vivo, Apple, Nubia, Black Shark), a dedicated **Hardware Device Spoofing Settings Card** with master ON/OFF toggle, **Integrated Competitive Game CFG Profiles** directly on Home Screen game cards, and full **Shizuku Temporary Root Access** property enforcement.

* 🎭 **Per-Brand Hardware Device Identity Spoofing Engine**: Modular design split into 10 brand profile classes (`SamsungProfiles`, `RealmeProfiles`, `AsusRogProfiles`, `XiaomiProfiles`, `OnePlusProfiles`, `OppoProfiles`, `VivoProfiles`, `AppleProfiles`, `NubiaProfiles`, `BlackSharkProfiles`) featuring **44 real-world flagship device profiles** (Galaxy S25 Ultra, ROG Phone 8 Pro, REDMAGIC 9S Pro, iPad Pro M4, Xiaomi 14 Ultra, etc.).
* 🔒 **Shizuku Temporary Full Root 6-Namespace Property Overrides**: Overrides all 6 Android system property namespaces (`ro.product.model`, `ro.product.vendor.model`, `ro.product.system.model`, `ro.product.odm.model`, `ro.product.product.model`, `ro.product.system_ext.model`) plus `fingerprint`, `board`, `chipname`, `displayId`, and `glRenderer`.
* ⚙️ **Dedicated Hardware Device Spoofing Settings Card**: Features a master ON/OFF switch, active profile status badge, and scrollable 44-device profile list. Toggling OFF resets spoofing properties.
* 🎮 **Home Screen Game Card Competitive CFG Integration**: Per-game competitive profiles (165Hz max refresh rate, 165 FPS limit, Super Fast Touch 165Hz boost) configured directly inside Home screen game cards and auto-applied via Shizuku on game launch.
* 🧹 **Cleaned & Deduplicated Tweaks Engine**: Removed legacy hardcoded model spoof items from Advanced System Tweaks Engine to establish Hardware Device Spoofing as the single source of truth.

---

## ⚡ Master Features

### 🔒 1. 100% Shizuku API Temporary Root Execution
* **Automated 28-Permission Shizuku ADB Combo**: Automatically grants `WRITE_SECURE_SETTINGS`, `SYSTEM_ALERT_WINDOW`, `MANAGE_GAME_MODE`, `FORCE_STOP_PACKAGES`, `CLEAR_APP_CACHE`, `SET_PROCESS_LIMIT`, `POST_NOTIFICATIONS`, and `FOREGROUND_SERVICE_SPECIAL_USE` via Shizuku binder IPC.
* **Dual Execution Engine (`Shizuku` vs `System Settings`)**: Seamless fallback execution if Shizuku binder is disconnected.

### 🎭 2. Per-Brand 44-Device Hardware Identity Spoofing Engine
* **Multi-Namespace Property Overrides via Shizuku ADB**: Overrides system identity properties (`ro.product.model`, `ro.product.brand`, `ro.product.manufacturer`, `ro.vendor.product.model`, `ro.product.name`, `ro.hardware`, `ro.board.platform`, `ro.soc.model`, `ro.build.fingerprint`) and Android 12+ Game Mode overlay settings (`device_config put game_overlay`).
* **Supported Brand Categories (44 Total Profiles)**:
  - 🟢 **Samsung** (6 profiles): Galaxy S25 Ultra (Snapdragon 8 Elite), S24 Ultra, S23 Ultra, Z Fold 5, Tab S9 Ultra, A55 5G.
  - 🟡 **Realme** (4 profiles): GT 5 Pro, GT Neo 5, GT 3, Narzo 60 Pro.
  - 🔴 **ASUS ROG** (4 profiles): ROG Phone 8 Pro (165Hz), ROG Phone 7 Ultimate, ROG Phone 6, ROG Phone 5s.
  - 🟠 **Xiaomi / POCO / Redmi** (5 profiles): Xiaomi 14 Ultra, 14 Pro, POCO F6 Pro, Redmi K70 Pro, Redmi Note 13 Pro+.
  - 🔴 **OnePlus** (4 profiles): OnePlus 12, 11, Ace 3 Pro, Nord 4.
  - 🟢 **OPPO** (3 profiles): Find X7 Ultra, Find X6 Pro, Reno 12 Pro.
  - 🔵 **Vivo / iQOO** (4 profiles): X100 Ultra, iQOO 12, iQOO Neo 9 Pro, V30 Pro.
  - ⚪ **Apple** (4 profiles): iPad Pro M4 (Tablet FOV), iPad Pro M2, iPhone 16 Pro Max, iPhone 15 Pro Max.
  - 🔴 **Nubia / REDMAGIC** (3 profiles): REDMAGIC 9S Pro (165Hz), 9 Pro, 8 Pro.
  - ⬛ **Black Shark** (3 profiles): Black Shark 5 Pro, 4 Pro, 5.

### 🎯 3. Hardware Refresh Rate (Hz) & FPS Lock
* Detects hardware display modes and locks refresh rates to **60Hz, 90Hz, 120Hz, 144Hz, or 165Hz**.
* Integrates Android 12+ Game Mode API (`cmd game mode performance <package>`) and per-app refresh rate overrides (`cmd window set-app-refresh-rate`).

### 🎮 4. Home Screen Game Card Competitive CFG Profiles
* **Direct Per-Game Configuration**: Click "⚡ COMP CFG" on any game card to configure FPS limits (up to 165 FPS), refresh rates (up to 165Hz), and Super Fast Touch latency boost.
* **Auto-Apply on Launch**: Game launcher auto-detects saved competitive profiles and applies Shizuku boost commands prior to launching games (MLBB, PUBGM, CODM, Wild Rift, Genshin, etc.).

### 🎨 5. Graphics & GPU Engine Optimization
* **Google ANGLE Vulkan GLES Driver Layer** (`settings put global angle_gl_driver_all_angle 1`).
* **Updatable System Game Driver** (`settings put global game_driver_all_apps 1`).
* **Vulkan HWUI Graphics Renderer** (`setprop debug.hwui.renderer vulkan`).
* **SurfaceFlinger Unsignaled Latching** (`setprop debug.sf.latch_unsignaled 1`).
* **Force 4x MSAA Anti-Aliasing** (`setprop debug.egl.force_msaa 1`).
* **High-Speed 16-Bit Alpha Textures** (`setprop persist.sys.use_16bpp_alpha 1`).

### 👆 6. Touch Latency & Digitizer Sensitivity
* **Super Fast Touch 165Hz Competitive Mode** (`setprop view.touch_slop 0`, `setprop debug.input.max_events_per_sec 1000`).
* **Touch Pressure Scale Reduction** (`setprop persist.sys.touch.pressure.scale 0.001`).
* **Touch Slop Gesture Sensitivity** (`settings put system touch_slop_reduction 1`).
* **Zero Scroll Cache Compression** (`setprop persist.sys.scrollingcache 3`).

### 🌐 7. Network & Location Acceleration
* **1-Tap Gaming DNS Booster**: Cloudflare (`1.1.1.1`), Google (`8.8.8.8`), and Default System DNS presets.
* **Tethering Hardware Offload Acceleration**: Enables hardware tethering offload (`settings put global tether_offload_disabled 0`).
* **Force Full GNSS Raw Measurements**: Forces raw GPS/GNSS measurements (`settings put global force_gnss_raw_measurements 1`).

### 🔍 8. Multi-Platform Deep Search Engine (Android 11 to 16)
* **Unrestricted Multi-User Package Scanner**: Uses Shizuku ADB (`pm list packages -3 -u -a`) to bypass Android 11–16 package visibility restrictions, discovering games installed in secondary user profiles, Work Profiles, Dual Apps, Parallel Space, Island, and Shelter.
* **Third-Party Platform Store Detection**: Automatically scans and indexes games downloaded from **Google Play Store**, **TapTap**, **Garena App Store**, **Samsung Galaxy Store**, **APKPure**, **QooApp**, and **Amazon Appstore**.

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
| **v4.7.0 (Latest)** | Per-Brand 44-Device Hardware Spoofing Engine, Dedicated Settings ON/OFF Card, Home Card Competitive CFGs & Shizuku Temporary Root Overrides | [Download v4.7.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.7.0) |
| **v4.6.0** | Full Device Identity Spoofing Engine, Multi-Platform Deep Search, Unthrottled 120/144/165 FPS HUD & Game Config Patchers | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.6.0) |
| **v4.4.0** | 100% Shizuku API Temporary Root, Redesigned Gaming Launcher, Full Boost System Tweaks & Internal Data Config Patcher | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.4.0) |
| **v4.3.0** | Guaranteed Online Games Library, App Picker Catalog Injection & Play Store Fallback | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.3.0) |
| **v4.2.0** | Manifest Package Visibility Queries, Shizuku Package Scanner & Add Game App Picker | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.2.0) |
| **v4.1.0** | Dual-Scan Game Detection Engine & Game Storage Config Auto-Patcher | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.1.0) |

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
