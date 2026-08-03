<div align="center">

# 🎮 GAME BOOSTER PRO & LAUNCHER

<p align="center">
  <img src="https://img.shields.io/badge/Release-v4.5.0-00F0FF?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v4.5.0" />
  <img src="https://img.shields.io/badge/Platform-Android%2015%20(SDK%2035)-00FF66?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android SDK 35" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Shizuku%20ADB%20Temporary%20Root-FF9900?style=for-the-badge&logo=linux&logoColor=white" alt="100% Shizuku ADB Temporary Root" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Pure Native Android Gaming Launcher & System Tuner with 100% Shizuku API Temporary Root Privilege Execution, ANGLE Vulkan Driver, Updatable Game Driver & Internal Data Config Patcher* ⚡

---

[📦 Download v4.5.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.5.0) • [📖 Features](#-master-features) • [🏛️ Project Structure](#%EF%B8%8F-project-architecture) • [🚀 Build Guide](#-building-the-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 What's New in v4.5.0

> [!IMPORTANT]
> **GAME BOOSTER PRO** is built **100% in Native Java (JDK 17)** specifically designed for **Non-Rooted Android Devices (Target SDK 35 / Android 15)**. It communicates via **Shizuku ADB Binder IPC (`rikka.shizuku.ShizukuProvider`)** to gain **temporary root privileges** (UID 2000 / UID 0 ADB Shell rights) with zero permanent system modifications.

* 🔒 **100% Shizuku API Temporary Root Integration**: Auto-starts AIDL `ShizukuUserServiceConnector` binder on launch and features a 1-tap auto-grant engine for protected system permissions (`WRITE_SECURE_SETTINGS`, `PACKAGE_USAGE_STATS`, `MANAGE_GAME_MODE`, `SYSTEM_ALERT_WINDOW`, `FORCE_STOP_PACKAGES`, `CLEAR_APP_CACHE`).
* 🎯 **120Hz / 144Hz / 165Hz Display & FPS Lock**: SurfaceFlinger binder IPC (`service call SurfaceFlinger 1035 i32 <hz>`), swap interval cap removal (`setprop debug.gr.swapinterval 0`), and per-app rate overrides (`cmd window set-app-refresh-rate <pkg> <fps>`).
* ⚡ **Floating Performance HUD Real FPS Engine**: Unthrottled overlay window compositor with `params.preferredRefreshRate = (float) caps.maxRefreshRate` (90Hz, 120Hz, 144Hz, 165Hz) and dynamic frame rate calculation.
* 🛡️ **Permanent Performance Retention (Zero Auto-Off)**: Preserves active high refresh rates, DND mode, ANGLE Mode, System Game Driver, GPU Vulkan 3D, and CPU Extreme Performance Governor permanently across game exits and app restarts.
* 🛠️ **39 Legitimate System Tweaks**: PowerHAL Sustained Performance Boost, Thermal Throttling Bypass Override, GPU Maximum Clocks, and SurfaceFlinger Zero VSync Phase Offsets.

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
| **v4.4.0 (Latest)** | 100% Shizuku API Temporary Root, Redesigned Gaming Launcher, Full Boost System Tweaks & Internal Data Config Patcher | [Download v4.4.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.4.0) |
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
