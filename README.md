<p align="center">
  <img src="BANNER.gif" alt="Precision Aim Banner" width="100%" style="border-radius: 12px;">
</p>

<h1 align="center">🎮 Game Launcher Pro V2.0 (v2.2.1.0 / versionCode 2210) — Zero Touch Delay & Universal Game Spoofer Engine</h1>

<p align="center">
  <b>0ms Touch Delay, 1000Hz Digitizer Sampling Engine, Per-Game Hardware Identity Spoofer, In-Game HUD Turbo Overlay & Dynamic Panel Refresh Rate Engine for Mobile eSports</b>
</p>

<p align="center">
  <a href="https://github.com/willygailo/Game-Launcher"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"></a>
  <a href="https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027"><img src="https://img.shields.io/badge/Facebook-1877F2?style=for-the-badge&logo=facebook&logoColor=white" alt="Facebook"></a>
  <a href="https://github.com/willygailo/Game-Launcher/releases"><img src="https://img.shields.io/badge/Releases-Download_APK-FF6C37?style=for-the-badge&logo=android&logoColor=white" alt="Download APK"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License"></a>
  <a href="android"><img src="https://img.shields.io/badge/Android-API_36-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"></a>
  <a href=".github/workflows/android-build.yml"><img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions" alt="Build Status"></a>
  <a href="https://github.com/willygailo/Game-Launcher/releases"><img src="https://img.shields.io/badge/Version-v2.2.1.0-emerald?style=for-the-badge" alt="Version v2.2.1.0"></a>
</p>

---

## 👨‍💻 Lead Developer & Author Information

<div align="center">

| 👤 Developer | 🌐 Official Facebook | 🐙 Official GitHub | 📦 Latest Downloads / APK |
| :--- | :--- | :--- | :--- |
| **WILLY JR CARNASA GAILO** | [![Facebook](https://img.shields.io/badge/Facebook-WILLY_JR_CARNASA_GAILO-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027) | [![GitHub](https://img.shields.io/badge/GitHub-willygailo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo) | [![Releases](https://img.shields.io/badge/Releases-v2.2.1.0_APK_Downloads-22c55e?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases) |

</div>

---

## 🚀 Overview

**Game Launcher Pro V2.0 (v2.2.1.0 / versionCode 2210)** is an advanced, device-level performance utility, hardware identity spoofer, and game launcher engineered by **WILLY JR CARNASA GAILO** for competitive eSports gaming (*Mobile Legends, PUBG Mobile, COD Mobile, Honor of Kings, Genshin Impact, Wuthering Waves, Zenless Zone Zero, Delta Force, Free Fire, Wild Rift, Blood Strike, Warzone Mobile, Roblox*).

By leveraging **Shizuku API (privileged ADB Binder IPC - uid 2000)**, it unlocks **Dynamic Refresh Rates (60Hz, 90Hz, 120Hz, 144Hz, 165Hz+)** detected directly from hardware panel capabilities, enforces **0ms Zero Touch Delay with 1000Hz Digitizer Sampling**, applies **Per-Game Package Spoofer Profiles** (each game target receives its own custom hardware identity to eliminate conflicts), integrates an **In-Game HUD Turbo Overlay with Quick Hz Locks, Charge Bypass & Touch Boost**, features a **Transparent Glassmorphic Design across Home, Settings, and Terminal**, and provides a **Deep Search Game Folder Resolver** for Android 13–16 restricted storage.



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
