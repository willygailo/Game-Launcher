# Game Launcher Pro

<div align="center">

---

## Overview

**Game Launcher Pro** provides low-level performance tuning and per-title optimization for competitive mobile gaming without requiring traditional device root access. Leveraging the **Shizuku API**, it safely manages hardware governors, display refresh rates, and runtime configurations.

### Key Capabilities

- **Display & Frame Rate Synchronization**: Enforces stable 120Hz, 144Hz, 165Hz, and 185Hz refresh rates using multi-tier SurfaceFlinger, Game Mode API, and display mode overrides.
- **Low-Latency Input Engine**: Configures 1000Hz touch sampling rate and minimizes touch deadzones.
- **Privileged File Management Engine**: Full control over protected game configurations (`edit`, `add`, `delete`, `upload`, `backup`, and `restore`) across `/data/data/<pkg>/` and `/sdcard/Android/data/<pkg>/`.
- **Per-Game Configuration Profiles**: Manages custom graphic, FOV, and performance presets for supported titles.
- **Network Optimization**: Activates low-latency Wi-Fi mode and optimizes TCP buffer parameters for reduced jitter.
- **Hardware Profile Emulation**: Emulates high-end gaming hardware signatures to access higher in-game graphic and framerate tiers.

---

## Supported Titles

| Game                                              | Optimization Capabilities                                                                              |
| :------------------------------------------------ | :----------------------------------------------------------------------------------------------------- |
| **Mobile Legends: Bang Bang**               | 120/144/165/185 FPS mode, Camera & FOV adjustments, Touch response acceleration                        |
| **PUBG Mobile / BGMI**                      | 120/144/165/185 FPS UE4 CVars, FOV & Perspective scaling, Gyroscope response tuning                    |
| **Call of Duty: Mobile**                    | 120/144/165/185 FPS presets, Touch acceleration, Sensitivity curve alignment                           |
| **Free Fire / Free Fire MAX**               | 120/144/165/185 FPS mode unlocking, Touch polling optimization, Aim response tuning                    |
| **Genshin Impact**                          | 120/144/165/185 FPS, Vulkan backend preference, Expanded rendering resolution, Camera distance scaling |
| **Honor of Kings**                          | 120/144/165/185 FPS presets, Response latency reduction, Rendering mode tuning                         |
| **Roblox**                                  | 120/144/165/185 FPS FastFlags management, Vulkan rendering, Unlocked frame rate scheduler              |
| **Valorant Mobile (CN Project C / Global)** | 120/144/165/185 FPS UE4 CVars, 1000Hz touch & gyro tuning, Zero-delay aim & crosshair stabilizer       |
| **Farlight 84**                             | 120/144/165/185 FPS Solarland graphics engine, Low-latency touch boost, Recoil reduction               |

---

## System Architecture

```
Game-Launcher-PRO/
├── android/
│   ├── app/
│   │   ├── src/main/assets/shizuku/ # Bundled rish binary & dex runtime assets
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
├── platform-tools-latest-linux/  # Bundled official Android SDK Platform Tools (ADB/Fastboot)
├── shizuku/                      # Standalone Shizuku shell toolkit (rish & rish_shizuku.dex)
├── tools/                        # Automated device setup & Shizuku activation scripts
│   ├── activate_shizuku.sh       # One-click Shizuku service starter via ADB
│   ├── grant_permissions.sh      # Privileged permission granter for Game Launcher Pro
│   └── setup_device.sh           # All-in-one onboarding and installation tool
└── README.md
```

---

## Installation & Setup

### Prerequisites

- Device running **Android 12 (API 31) through Android 16 (API 36)**.
- **[Shizuku](https://shizuku.rikka.app/)** installed and running via Wireless Debugging or ADB.

### Quick Start (On Device)

1. Download and install `Game_Space.apk` from the **[Releases](https://github.com/willygailo/Game-Launcher/releases)** page.
2. Ensure Shizuku is active on your device.
3. Launch **Game Launcher Pro** and grant Shizuku privileged access when prompted.
4. Select your installed game from the dashboard to apply performance profiles and launch.

### PC / ADB Automated Setup

Connect your device to your PC via USB with USB Debugging enabled, then run:

```bash
# 1. Run all-in-one setup (Installs APK, grants permissions, and activates Shizuku)
./tools/setup_device.sh

# Or run individual tasks:
./tools/activate_shizuku.sh    # Starts Shizuku privileged service
./tools/grant_permissions.sh   # Grants WRITE_SECURE_SETTINGS, DUMP, etc.
```

---

## Building from Source

Ensure you have **JDK 17** (or **JDK 21**) configured:

```bash
cd android
./gradlew outgoingVariants && ./gradlew clean assembleDebug
```

The built debug APK will be generated at:
`android/app/build/outputs/apk/debug/Game_Space_Debug.apk`

---

## Connect & Developer

<div align="center">

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
