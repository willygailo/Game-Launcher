<div align="center">

# 🎮 GAME BOOSTER PRO & LAUNCHER

<p align="center">
  <img src="https://img.shields.io/badge/Release-v4.1.0-00F0FF?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v4.1.0" />
  <img src="https://img.shields.io/badge/Platform-Android%2015%20(SDK%2035)-00FF66?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android SDK 35" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Non--Rooted%20Shizuku%20ADB-FF9900?style=for-the-badge&logo=linux&logoColor=white" alt="100% Non-Rooted Shizuku ADB" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Pure Native Non-Rooted Android Gaming Optimizer, Hardware FPS/Hz Locker, Vulkan HWUI Engine & Shizuku ADB System Tuner* ⚡

---

[📦 Download v4.1.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.1.0) • [📖 Features](#-master-features) • [🏛️ Project Structure](#%EF%B8%8F-project-architecture) • [🚀 Build Guide](#-building-the-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 What's New in v4.1.0

> [!IMPORTANT]
> **GAME BOOSTER PRO** is built **100% in Native Java (JDK 17)** specifically designed for **Non-Rooted Android Devices (Target SDK 35 / Android 15)**. It communicates via **Shizuku ADB Binder IPC (`rikka.shizuku.ShizukuProvider`)**, **System Settings**, and **Android 12+ Game Mode API** with zero root requirement.

* ⚙️ **Game Storage INI/Config Auto-Patcher & Modifier**: Automatically creates and patches game configuration files inside app data folders (`dragon2017/assets/Com/MobileLegendsSettings.ini`, `ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini`, `GraphicsSettings.ini`, `FFGraphicsSettings.ini`, `GameSettings.ini`) to force high FPS modes (`HighFPSMode=1`, `FrameRateLevel=9`, `MaxFrameRate=120`, `FPS=120`, `FPS=144`, `FPS=165`) for MLBB, CODM, PUBG, Wild Rift, Free Fire, HOK, Genshin, Roblox, etc.
* 🚀 **Dual-Scanner Package Detection Engine**: Combines `pm.queryIntentActivities(...)` and `pm.getInstalledApplications(...)` with multi-tier app label & package keyword matching to 100% guarantee all installed games on Android 11–15 are discovered and displayed.
* 🎯 **Responsive Floating Overlay HUD Fix**: Separated touch drag listener onto header/pill view so action buttons (`CLEAN`, `MAX Hz`, `DND`, `AIM`, collapse button) receive click events without touch interference. Features magnetic edge snapping to screen borders on drag release.
* ⚡ **144Hz & 165Hz Extreme FPS Unlock**: Support for 144Hz and 165Hz display refresh rates on high refresh rate screens with vendor-specific key tuning for Xiaomi, Poco, Redmi, Samsung, OnePlus, Oppo, Realme, ASUS ROG, Vivo, iQOO, Motorola, Infinix, and Tecno.
* ⚡ **Automatic Shizuku Permission Grant Combo**: Automatically grants 18+ system permissions (`SYSTEM_ALERT_WINDOW`, `WRITE_SECURE_SETTINGS`, `PACKAGE_USAGE_STATS`, `MANAGE_GAME_MODE`, `OVERRIDE_WIFI_CONFIG`, `FORCE_STOP_PACKAGES`, `SET_PROCESS_LIMIT`, `RUN_IN_BACKGROUND`, `AUTO_START`) via ADB as soon as Shizuku binder connects.

---

## ⚡ Master Features

### 🔒 1. 100% Non-Rooted Shizuku ADB Control
* **Automated 28-Permission Shizuku ADB Combo**: Automatically grants `WRITE_SECURE_SETTINGS`, `SYSTEM_ALERT_WINDOW`, `MANAGE_GAME_MODE`, `FORCE_STOP_PACKAGES`, `CLEAR_APP_CACHE`, `SET_PROCESS_LIMIT`, `POST_NOTIFICATIONS`, and `FOREGROUND_SERVICE_SPECIAL_USE` via Shizuku binder IPC.
* **Dual Execution Engine (`Shizuku` vs `System Settings`)**: Seamless fallback execution if Shizuku is not running.

### 🎯 2. Hardware Refresh Rate (Hz) & FPS Lock
* Detects hardware display modes and locks refresh rates to **60Hz, 90Hz, 120Hz, 144Hz, or 165Hz**.
* Integrates Android 12+ Game Mode API (`cmd game mode performance <package>`) and per-app refresh rate overrides (`cmd window set-app-refresh-rate`).

### 🎨 3. Graphics & GPU Engine Optimization
* **Vulkan HWUI Graphics Renderer** (`setprop debug.hwui.renderer vulkan`).
* **SurfaceFlinger Unsignaled Latching** (`setprop debug.sf.latch_unsignaled 1`).
* **Force 4x MSAA Anti-Aliasing** (`setprop debug.egl.force_msaa 1`).
* **High-Speed 16-Bit Alpha Textures** (`setprop persist.sys.use_16bpp_alpha 1`).

### 👆 4. Touch Latency & Digitizer Sensitivity
* **Touch Pressure Scale Reduction** (`setprop persist.sys.touch.pressure.scale 0.001`).
* **Touch Slop Gesture Sensitivity** (`settings put system touch_slop_reduction 1`).
* **Zero Scroll Cache Compression** (`setprop persist.sys.scrollingcache 3`).

### 🧊 5. Thermal Throttling & PowerHAL Bypass
* Overrides system thermal throttling caps (`cmd thermalservice override-status 0`).
* Triggers PowerHAL sustained performance mode (`cmd power set-mode 0 1`).

### 🌐 6. Native JavaScript Bridge & Modular Web Scripts
* Exposes `@JavascriptInterface` (`window.AndroidBridge`) for WebViews.
* Modular web scripts (`js/app.js`, `js/shizuku-shell.js`, `js/game-manager.js`, `js/monitors.js`) supporting web dashboard controls.


---

## 🚀 Building the APK

To build the debug APK, run:

```bash
cd android
./gradlew clean assembleDebug
```

### 📦 Output APK Location:
```
Game_Launcher_Pro_v4.1.0.apk
android/app/build/outputs/apk/debug/Game_Space_Debug.apk
```

---

## 🌐 Latest Releases & Downloads

| Release Tag | Title | Link |
| :--- | :--- | :--- |
| **v4.1.0 (Latest)** | Dual-Scan Game Detection Engine & Game Storage Config Auto-Patcher | [Download v4.1.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v4.1.0) |
| **v4.0.0** | Floating Overlay HUD Fix, Online Games Library, 144Hz/165Hz FPS Unlock & Auto Shizuku Permissions | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v4.0.0) |
| **v3.6.0** | Real-Time Choreographer FPS Engine, Redesigned Glassmorphism HUD & Persistent GPU/CPU Switches | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.6.0) |
| **v3.5.1** | Persistent GPU & CPU Manual Switch Engine | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.5.1) |
| **v3.5.0** | 165Hz Lock, Game Turbo Floating HUD, Auto Game Boost & Live Shell Command Feedback | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.5.0) |
| **v3.4.0** | 34 Legitimate AOSP Tweaks, Persistence Fix & Network Optimizer | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.4.0) |
| **v3.3.0** | Persistent Tweaks & Hardware Refresh Rate Filtering | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.3.0) |

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
