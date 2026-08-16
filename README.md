# Game Launcher Pro

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android SDK](https://img.shields.io/badge/Platform-Android%2012--16%20(API%2036)-3DDC84?style=flat-square&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v2.2.2.1-00F0FF?style=flat-square&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
[![Shizuku API](https://img.shields.io/badge/Privileged-Shizuku%20ADB-7B2CBF?style=flat-square&logo=android&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](#)
[![GitHub Profile](https://img.shields.io/badge/GitHub-willygailo-181717?style=flat-square&logo=github&logoColor=white)](https://github.com/willygailo)
[![Facebook Profile](https://img.shields.io/badge/Facebook-Willy%20Gailo-1877F2?style=flat-square&logo=facebook&logoColor=white)](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

A high-performance Android gaming optimization suite designed to minimize input latency, unlock maximum refresh rates (up to 165Hz), and streamline per-title performance configurations via privileged ADB access (Shizuku).

[Download Latest Release](https://github.com/willygailo/Game-Launcher/releases) • [Report Issue](https://github.com/willygailo/Game-Launcher/issues)

</div>

---

## Overview

**Game Launcher Pro** provides low-level performance tuning and per-title optimization for competitive mobile gaming without requiring traditional device root access. Leveraging the **Shizuku API**, it safely manages hardware governors, display refresh rates, and runtime configurations.

### Key Capabilities

- **Display & Frame Rate Synchronization**: Enforces stable 120Hz, 144Hz, and 165Hz refresh rates using multi-tier SurfaceFlinger and display mode overrides.
- **Low-Latency Input Engine**: Configures 1000Hz touch sampling rate and minimizes touch deadzones.
- **Per-Game Configuration Profiles**: Manages custom graphic, FOV, and performance presets for supported titles.
- **Network Optimization**: Activates low-latency Wi-Fi mode and optimizes TCP buffer parameters for reduced jitter.
- **Hardware Profile Emulation**: Emulates high-end gaming hardware signatures to access higher in-game graphic and framerate tiers.

---

## Supported Titles

| Game | Optimization Capabilities |
| :--- | :--- |
| **Mobile Legends: Bang Bang** | 120/165 FPS mode, Camera & FOV adjustments, Touch response acceleration |
| **PUBG Mobile / BGMI** | 120/165 FPS UE4 CVars, FOV & Perspective scaling, Gyroscope response tuning |
| **Call of Duty: Mobile** | Ultra frame rate presets, Touch acceleration, Sensitivity curve alignment |
| **Free Fire / Free Fire MAX** | High-FPS mode unlocking, Touch polling optimization, Aim response tuning |
| **Genshin Impact** | Vulkan backend preference, Expanded rendering resolution, Camera distance scaling |
| **Honor of Kings** | Ultra frame rate presets, Response latency reduction, Rendering mode tuning |
| **Roblox** | Client FastFlags management, Vulkan rendering, Unlocked frame rate scheduler |

---

## System Architecture

```
Game-Launcher-PRO/
├── android/
│   ├── app/
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/          # Refresh rate, GPU driver, and network engines
│   │   │   ├── config/           # Per-game configuration patchers & managers
│   │   │   ├── device/           # Hardware and display capability detectors
│   │   │   ├── engine/           # Command execution & override controllers
│   │   │   ├── games/            # Installed title scanning and launch helpers
│   │   │   ├── overlay/          # HUD indicators and assistive overlays
│   │   │   ├── shizuku/          # Shizuku Binder communication & privileged file manager
│   │   │   ├── spoofer/          # Hardware device signature emulators
│   │   │   ├── tweaks/           # System-level performance parameter controllers
│   │   │   └── ui/               # Interface views, fragments, and tools
│   │   └── src/main/res/         # UI layouts, vector assets, and design tokens
│   └── build.gradle              # Android build configuration (API 36 / Java 17)
└── README.md
```

---

## Installation & Setup

### Prerequisites
- Device running **Android 12 (API 31) through Android 16 (API 36)**.
- **[Shizuku](https://shizuku.rikka.app/)** installed and running via Wireless Debugging or ADB.

### Quick Start
1. Download and install `Game_Space.apk` from the **[Releases](https://github.com/willygailo/Game-Launcher/releases)** page.
2. Ensure Shizuku is active on your device.
3. Launch **Game Launcher Pro** and grant Shizuku privileged access when prompted.
4. Select your installed game from the dashboard to apply performance profiles and launch.

---

## Connect & Developer

<div align="center">

[![GitHub](https://img.shields.io/badge/GitHub-willygailo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo)
&nbsp;&nbsp;
[![Facebook](https://img.shields.io/badge/Facebook-Willy%20Jr%20Carnasa%20Gailo-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

</div>

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
