# 🚀 Game Launcher Pro & Precision Aim Tuner 🎯

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android SDK](https://img.shields.io/badge/Android-SDK%2036-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Shizuku API](https://img.shields.io/badge/Shizuku-Privileged%20ADB-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#)
[![Glide Engine](https://img.shields.io/badge/Glide-Animated%20GIF-FF6F00?style=for-the-badge&logo=glide&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#)
[![Facebook Profile](https://img.shields.io/badge/Facebook-Follow%20Me-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Star%20Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher)

**An ultra-high-performance Game Booster, Input Latency Tuner, and Custom Launcher designed for mobile eSports players.**

---

### 📢 Share • Like • React • Star ⭐
If you find this project helpful, please consider **Starring ⭐ the Repository**, **Sharing**, and leaving a **Like & Reaction**!

</div>

---

## 📌 1. Introduction

**Game Launcher Pro (Precision Aim Input Tuner)** is a legitimate, ban-safe, high-performance Android utility app engineered for competitive mobile gamers and eSports players across titles such as *PUBG Mobile*, *COD Mobile*, *Free Fire*, and *Mobile Legends*.

It optimizes device-level touch sampling frequency, digitizer response rates, gyroscope polling, and RAM/ZRAM allocation using **Shizuku (ADB privileged execution)** — with **ZERO game memory tampering** or executable modification.

> [!IMPORTANT]
> **SAFETY & COMPLIANCE GUARANTEE**:
> - ❌ **Zero Game Memory Tampering**: Never reads, injects into, or modifies game binaries, RAM, or APK data.
> - ❌ **No Anti-Cheat Violation**: Avoids prohibited UE4 console variable injections (`r.AimSensitivityScale`, `r.TouchDeadZone`) detected by Tencent ACE / anti-cheat solutions.
> - ❌ **No Automation or Macros**: Strictly zero aimbots, auto-fire, recoil macros, or automated inputs.
> - ✅ **100% Reversible**: Every system property modification is backed up and automatically restored to system defaults upon app termination.

---

## 📄 2. Technical Documents & Architecture

### 🏗️ Directory & Module Architecture

```
Game_Launcher_Pro/
├── android/
│   ├── app/
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/          # RamZramChannel & Background Process Optimizer
│   │   │   ├── device/           # DeviceInfoChannel (CPU/RAM/Display Refresh Rate)
│   │   │   ├── engine/           # CommandExecutor & Hardware Engine Modes
│   │   │   ├── games/            # GameLauncherHelper & Zero-Delay Auto Scanner
│   │   │   ├── overlay/          # FloatingOverlayService & Crosshair Canvas
│   │   │   ├── tweaks/           # TweakManagerRepository & System Property Knobs
│   │   │   └── ui/screens/       # HomeFragment, SettingsFragment, MainActivity
│   │   └── src/main/res/
│   │       └── drawable/         # Custom UI Assets (hero_banner.gif, home_bg_new.jpg, settings_bg_new.jpg)
└── README.md
```

### ⚡ System Property Tuning Matrix

| System Knob / Setting | Default Range | Tuned Value | Description |
| :--- | :--- | :--- | :--- |
| `debug.input.max_events_per_sec` | 60 - 240 Hz | **1000 Hz** | Maximizes digitizer event dispatch rate for instant touch input |
| `view.touch_slop` | 8 - 24 px | **0 px** | Eliminates drag deadzone for immediate aim response |
| `touch_slop_reduction` | 0 | **1** | Reduces touch slop scaling factor across views |
| `debug.sensor.gyro.rate` | 100 - 200 Hz | **1000 Hz** | Unlocks max sensor polling for smooth scope tracking |
| `pointer_speed` | 0 (scaled) | **7** | Applies 1:1 linear pointer acceleration curve |
| `background_process_limit` | -1 (Unlimited) | **2** | Restricts cached apps to free 200-400MB RAM for games |

### 🛠️ Setup Instructions (Shizuku Privileged Access)

1. Download and install **Shizuku** from Google Play Store or GitHub.
2. Enable **Developer Options** and **Wireless Debugging** in your Android system settings.
3. Start Shizuku via Wireless Debugging or connect your device to PC and run:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
4. Open **Game Launcher Pro** and grant Shizuku permission when prompted.

---

## ⚡ 3. Recent Upgrades & Improvements

- 🎨 **Hardware-Accelerated Animated GIF Banner**: Integrated `com.github.bumptech.glide:glide:4.16.0` to render high-definition animated GIF banners (`hero_banner.gif`) seamlessly in `HomeFragment`.
- 🖼️ **Premium High-Resolution Backgrounds**:
  - **Home Screen**: Customized backdrop scrim (`home_bg_new.jpg`).
  - **Settings Screen**: High-contrast theme backdrop (`settings_bg_new.jpg`).
- 🚀 **Zero-Delay Game Scanner**: Instant local package indexing to populate installed games on launch.
- 🧹 **Shizuku RAM & ZRAM Purge**: Automatic background process cleanup to eliminate micro-stutters during heavy gaming sessions.
- 🎯 **Tactical HUD Overlay**: Real-time FPS, hardware temperature, memory monitor, and customizable crosshair overlay.

---

## 🙏 4. Acknowledgments & Special Thanks

Special thanks to the open-source community and technologies that made this project possible:

- ❤️ **Shizuku Developer Team (Rikka)** — For providing the privileged ADB binder bridge API without requiring full device root access.
- ❤️ **Android Open Source Project (AOSP)** — For system developer properties and flexible OS framework capabilities.
- ❤️ **Glide Image Loading Library (Bumptech)** — For high-performance, memory-efficient animated GIF and image rendering.
- ❤️ **Community Testers & Gamers** — For continuous feedback, performance logs, and testing across different device chipsets.

---

## ☕ 5. Support & Donations

If **Game Launcher Pro** has improved your gaming latency and device performance, consider supporting ongoing development:

- 💸 **Direct Sponsorship**: Contact via Facebook for direct QR and sponsorship details.
- ⭐ **GitHub Star**: Simply starring this repository helps increase visibility and support for future updates!

---

## 👤 Developer Profiles & Connect

<div align="center">

| Profile | Link |
| :--- | :--- |
| 🌐 **Facebook Profile** | [Willy Jr Carnasa Gailo](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027) |
| 🐙 **GitHub Repository** | [Willy Gailo / Game-Launcher](https://github.com/willygailo/Game-Launcher) |

---

### 👍 Don't Forget to Share, Like, React & Star! 👍

**Thank you for your support! Enjoy ultra-low input latency and maximum frame stability! 🎮🔥**

</div>
