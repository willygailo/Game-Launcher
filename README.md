<div align="center">

# 🎮 GAME BOOSTER PRO & LAUNCHER

<p align="center">
  <img src="https://img.shields.io/badge/Release-v3.5.0-00F0FF?style=for-the-badge&logo=github&logoColor=white" alt="Latest Release v3.5.0" />
  <img src="https://img.shields.io/badge/Platform-Android%2015%20(SDK%2035)-00FF66?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android SDK 35" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Non--Rooted%20Shizuku%20ADB-FF9900?style=for-the-badge&logo=linux&logoColor=white" alt="100% Non-Rooted Shizuku ADB" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Pure Native Non-Rooted Android Gaming Optimizer, Hardware FPS/Hz Locker, Vulkan HWUI Engine & Shizuku ADB System Tuner* ⚡

---

[📦 Download v3.5.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v3.5.0) • [📖 Features](#-master-features) • [🏛️ Project Structure](#%EF%B8%8F-project-architecture) • [🚀 Build Guide](#-building-the-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 What's New in v3.5.0

> [!IMPORTANT]
> **GAME BOOSTER PRO** is built **100% in Native Java (JDK 17)** specifically designed for **Non-Rooted Android Devices (Target SDK 35 / Android 15)**. It communicates via **Shizuku ADB Binder IPC (`rikka.shizuku.ShizukuProvider`)**, **System Settings**, and **Android 12+ Game Mode API** with zero root requirement.

* ⚡ **Performance Profile Refresh Rate Locks (165Hz / 120Hz / 90Hz)**: Locks display refresh rate with zero fallbacks (`EXTREME PERFORMANCE`: 165Hz, `HIGH PERFORMANCE`: 120Hz/144Hz, `BALANCED GAME`: 90Hz/60Hz).
* 🎮 **Manual GPU & CPU Controls**: Dedicated manual ON/OFF switches for 3D Vulkan HWUI renderer, 2D Skia engine renderer, CPU Performance governor, and CPU Balance governor.
* 📡 **Game Server Ping Tester & 1-Tap Gaming DNS**: Live ICMP Ping Tester (`1.1.1.1`) and 1-tap DNS switcher (Cloudflare 1.1.1.1, Google 8.8.8.8, Default DNS).
* 📱 **Commercial Game Turbo Floating Gaming HUD**: Collapsible neon pill badge (`⚡ 120 FPS | 38°C`) & expanded gaming dock with in-game quick action buttons (1-Tap RAM Clean, 165Hz Lock, DND Silence, FPS Crosshair Overlay) and magnetic edge snapping.
* ⚡ **Shizuku 1-Tap Auto-Grant Permission Engine**: Automatically grants all 17 system permissions & AppOps via ADB as soon as Shizuku connects (`Shizuku.requestPermission(1001)`).
* 🎮 **Auto Game Space Launcher Monitor**: Background service auto-detecting when games open to apply 165Hz Auto-Boost instantly.
* 🔊 **1-Tap Footstep & Gunshot Audio Enhancer**: High-frequency 2kHz-4kHz footstep equalizer boost for competitive FPS games.
* 🧹 **Deep Game Storage & Shader Cache Cleaner**: Purges app caches & temporary shader files to maximize UFS read/write speeds.
* 💻 **Live Shell Command Toast Feedback**: Displays exact executed shell commands (`setprop`, `settings put`, `cmd power`) in real-time Toast notifications on every toggle click.

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
| **v3.4.0 (Latest)** | 34 Legitimate AOSP Tweaks, Persistence Fix & Network Optimizer | [Download v3.4.0 APK](https://github.com/willygailo/Game-Launcher/releases/tag/v3.4.0) |
| **v3.3.0** | Persistent Tweaks & Hardware Refresh Rate Filtering | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.3.0) |
| **v3.2.0** | Consolidated 2-Tab Dashboard & Glassmorphic UI | [View Release](https://github.com/willygailo/Game-Launcher/releases/tag/v3.2.0) |

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
