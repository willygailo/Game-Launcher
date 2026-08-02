<div align="center">

# 🎮 GAME BOOSTER PRO & LAUNCHER

<p align="center">
  <img src="https://img.shields.io/badge/Release-v3.3.0-00F0FF?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v3.3.0" />
  <img src="https://img.shields.io/badge/Platform-Android%2015%20(SDK%2035)-00FF66?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android SDK 35" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Non--Rooted%20Shizuku%20ADB-FF9900?style=for-the-badge&logo=linux&logoColor=white" alt="100% Non-Rooted Shizuku ADB" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Pure Native Non-Rooted Android Gaming Optimizer, Hardware FPS/Hz Locker, Vulkan HWUI Engine & Shizuku ADB System Tuner* ⚡

---

[📦 Download v3.3.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v3.3.0) • [📖 Features](#-master-features) • [🏛️ Project Structure](#%EF%B8%8F-project-architecture) • [🚀 Build Guide](#-building-the-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 What's New in v3.3.0

> [!IMPORTANT]
> **GAME BOOSTER PRO** is built **100% in Native Java (JDK 17)** specifically designed for **Non-Rooted Android Devices (Target SDK 35 / Android 15)**. It communicates via **Shizuku ADB Binder IPC (`rikka.shizuku.ShizukuProvider`)**, **System Settings**, and **Android 12+ Game Mode API** with zero root requirement.

* 📱 **Consolidated 2-Tab Dashboard**: Streamlined UI featuring strictly **Home** and **Settings** tabs for fast navigation.
* 🎨 **Glassmorphism Wallpaper Visibility**: Translucent glass card design with low dark scrim opacity (`#450A0E1A` / 27%) showcasing the custom background wallpaper (`app_bg`).
* 🔒 **Persistent Tweak Retention**: Switches remain **ON** persistently once enabled (saved in `TweakPreferences`) and require manual user toggling to turn OFF.
* ⚡ **Hardware-Driven Refresh Rate Selector**: Queries `Display.getSupportedModes()` to display ONLY the target FPS/Hz buttons supported by the phone's physical screen.
* 📱 **Hideable / Collapsible Floating HUD Overlay**: Single tap toggles between full metrics view (`⚡ FPS/Hz: 120Hz | RAM: 42% | Temp: 34.5°C`) and a minimal pill icon (`⚡ 120Hz`) so it never obstructs gameplay.
* 🌐 **Expanded Online Game Package Scanner**: Comprehensive detection for Mobile Legends, PUBG Mobile, COD Mobile, Free Fire, Genshin Impact, Wild Rift, Honor of Kings, Roblox, Apex Legends, etc.
* 🚀 **Auto-Apply Game Profiles on Launch**: Automatically applies target FPS locks, GPU render modes, DND settings, and system tweaks whenever any game is opened from the library.

---

## ⚡ Master Features

### 🔒 1. 100% Non-Rooted Shizuku ADB Control
* **Automated 27-Permission Shizuku ADB Combo**: Automatically grants `WRITE_SECURE_SETTINGS`, `MANAGE_GAME_MODE`, `FORCE_STOP_PACKAGES`, `CLEAR_APP_CACHE`, `SET_PROCESS_LIMIT`, `POST_NOTIFICATIONS`, and `FOREGROUND_SERVICE_SPECIAL_USE` via Shizuku binder IPC.
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
android/app/build/outputs/apk/debug/Game_Space_Debug.apk
```

---

## 🌐 Latest Releases & Downloads

| Release Tag | Title | Link |
| :--- | :--- | :--- |
| **v3.3.0 (Latest)** | Persistent Tweaks & Hardware Refresh Rate Filtering | [Download v3.3.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v3.3.0) |
| **v3.2.0** | Consolidated 2-Tab Dashboard & Glassmorphic UI | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.2.0) |
| **v3.1.0** | Advanced Non-Root Boost & Latency Tuning | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.1.0) |

---

## 👤 Developer & Contact

<table align="center">
  <tr>
    <td align="center" width="150">
      <img src="https://github.com/willygailo.png" width="100px;" alt="Willy Gailo"/><br />
      <sub><b>Willy Gailo</b></sub>
    </td>
    <td>
      <b>Connect with the Developer:</b>
      <br/><br/>
      🐱 <b>GitHub:</b> <a href="https://github.com/willygailo">@willygailo</a><br/>
      📘 <b>Facebook:</b> <a href="https://facebook.com/willygailo">Willy Gailo</a><br/>
      📂 <b>Repository:</b> <a href="https://github.com/willygailo/Game-Launcher">willygailo/Game-Launcher</a>
    </td>
  </tr>
</table>

---

<div align="center">
  <sub>Built with ❤️ and Pure Java 17 for Non-Rooted Android Gamers worldwide.</sub>
</div>
