# Game Launcher PRO ⚡

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android Platform](https://img.shields.io/badge/Platform-Android%2012--16%20(API%2036)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v2.4.0.0--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
[![Shizuku Privileged](https://img.shields.io/badge/Privileged-Shizuku%20Zero--Root%20UID%202000-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#)

[![GitHub Profile](https://img.shields.io/badge/GitHub-willygailo-181717?style=flat-square&logo=github&logoColor=white)](https://github.com/willygailo)
&nbsp;
[![Facebook Profile](https://img.shields.io/badge/Facebook-Willy%20Gailo-1877F2?style=flat-square&logo=facebook&logoColor=white)](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

**The Ultimate Low-Latency Gaming Optimization, Hardware Masking & Universal SetEdit Terminal Suite for Android 12 through Android 16 (Baklava).**

[📥 Download Latest Release](https://github.com/willygailo/Game-Launcher/releases) • [🐛 Report Bug / Feature Request](https://github.com/willygailo/Game-Launcher/issues)

</div>

---

## ⚡ Overview

**Game Launcher PRO** is an enterprise-grade mobile gaming engine designed to eliminate frame pacing jitter, enforce unlocked display refresh rates (up to **185Hz Extreme Gaming Mode**), mask hardware signatures across all target game engines, and provide an integrated **SetEdit Terminal Engine** for complete low-level Android database control.

Powered by the **Shizuku API (Privileged ADB UID 2000)**, it achieves full system-level and storage-level modification **without requiring bootloader unlocking or traditional root access (Zero-Root Architecture)**.

---

## 🌟 Core Highlights & Features

### 1. 📝 Built-in SetEdit Engine & Universal Cyber Terminal
* **No 3rd-Party SetEdit App Needed:** Inspect, search, modify, and delete keys in `system`, `secure`, and `global` Android database tables directly.
* **Storage Script File Runner:** Execute `.sh` script files directly from device storage (`/storage/emulated/0/Download/`) with auto-copy to `/data/local/tmp/` and execution via temporary root.
* **SAF Script Picker:** Tap **`📂 Run .sh File`** in the terminal toolbar to pick and run shell tweak files with real-time logs.
* **One-Click Quick Action Chips:** Instant presets for 0.5x UI Speed, 120-185 FPS SurfaceFlinger, ANGLE Game Driver, Deep RAM Flush, and 1000Hz Touch Slop.

### 2. 🛡️ 6-Layer Deep Hardware Masking & Device Spoofer
* **Engine Storage Hardware Injection:** Injects flagship hardware profiles (*Snapdragon 8 Elite / Adreno 840 / 24GB RAM*) directly into game configs in `/sdcard/Android/data/<pkg>/`.
* **Android OS Game Manager:** Enforces `cmd game mode performance`, `cmd game set --fps 185`, and `device_config game_overlay`.
* **SurfaceFlinger Refresh Locks:** Overrides display buffer limits via `service call SurfaceFlinger 1035/1036/1022`.
* **Dynamic Property Masking:** Modifies runtime `ro.product.*`, `ro.soc.*`, `debug.hwui.renderer vulkan`, and Game Driver mappings.
* **Deep Thermal & Anti-Throttle Bypass:** Overrides Android 12–16 thermal status to 0 (Cold/Normal) and whitelists games in Doze.
* **Flagship Profiles:** ASUS ROG Phone 9 Pro, Vivo iQOO 15 Pro, Samsung Galaxy S26 Ultra, Xiaomi 15 Ultra, Nubia RedMagic 10 Pro+, and Apple iPhone Profiles.

### 3. 🎮 10 Dedicated Game Optimization Engines
* **Mobile Legends: Bang Bang (MLBB):** 120/144/165/185 FPS mode, Ultra refresh rate unlock, touch response acceleration.
* **PUBG Mobile / BGMI:** 120/144/165/185 FPS UE4 CVars, FOV & camera scaling, gyro zero-delay tuning.
* **Call of Duty: Mobile / Warzone:** 120/185 FPS presets, sensitivity curve alignment, JSON hardware profiling.
* **Free Fire / Free Fire MAX:** High FPS mode unlocking, touch polling optimization, aim response tuning.
* **Genshin Impact / Honkai / ZZZ:** 120/185 FPS unlock, Vulkan backend preference, expanded rendering resolution.
* **Honor of Kings (HOK):** Ultra frame rate presets, latency reduction, frame pacing stabilization.
* **Roblox:** FastFlags unlocked frame rate scheduler, Vulkan renderer binding.
* **Valorant Mobile (CN / Global):** UE4 CVars, 1000Hz touch & gyro tuning, crosshair stabilizer.
* **Farlight 84:** Solarland graphics engine unlock, low-latency touch boost.

### 4. 🌐 100% Universal Compatibility
* **All Device Brands:** Samsung, Xiaomi, POCO, Vivo, iQOO, Realme, OPPO, ASUS ROG, Infinix, TECNO, Google Pixel, Motorola, OnePlus, Sony, and more.
* **All Chipsets:** Qualcomm Snapdragon, MediaTek Dimensity/Helio, Samsung Exynos, Google Tensor, Unisoc Tiger.
* **Supported Android OS:** Android 12 (API 31/32), Android 13 (API 33), Android 14 (API 34), Android 15 (API 35), and Android 16 (API 36 Baklava).

---

## 🏗️ System Architecture

```
Game-Launcher-PRO/
├── android/
│   ├── app/
│   │   ├── src/main/assets/
│   │   │   ├── scripts/          # Bundled device setup & permission scripts (.sh)
│   │   │   └── shizuku/          # Bundled rish binary & rish_shizuku.dex runtime
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/          # Refresh rate, GPU driver, and network optimizers
│   │   │   ├── config/           # 10 dedicated per-game configuration patchers
│   │   │   ├── device/           # Hardware capability & display detection
│   │   │   ├── engine/           # Execution runtime & command handlers
│   │   │   ├── games/            # Installed title scanner & launch handlers
│   │   │   ├── shizuku/          # Shizuku Binder IPC & privileged file manager
│   │   │   ├── spoofer/          # 6-Layer hardware identity masking & brand profiles
│   │   │   │   └── brands/       # Flagship brand profiles (ROG, iQOO, Samsung, etc.)
│   │   │   ├── terminal/         # Built-in Cyber Terminal & SetEdit engine
│   │   │   └── ui/               # Cyberpunk UI activities, fragments, and adapters
│   │   └── src/main/res/         # Modern vector icons, glassmorphism layouts
│   └── build.gradle              # Android Build Config (API 36 / Java 17 LTS / AGP 8.7.3)
├── platform-tools-latest-linux/  # PC ADB/Fastboot toolchain for desktop setup
├── tools/                        # USB ADB provisioning tools
│   ├── activate_shizuku.sh       # One-click Shizuku starter script
│   ├── grant_permissions.sh      # Privileged permission granter
│   └── setup_device.sh           # All-in-one onboarding script
└── README.md
```

---

## 💻 Terminal & SetEdit Command Guide

The built-in Cyber Terminal accepts standard Linux shell commands, Android `settings`, `device_config`, `setprop`, and custom `setedit` syntax:

### SetEdit Commands:
```bash
# Put / Edit database values:
setedit put system peak_refresh_rate 120
setedit put global window_animation_scale 0.5
setedit put global game_driver_all_apps 1

# Inspect / Get database values:
setedit get system peak_refresh_rate

# Search across all tables (System, Secure, Global):
setedit search refresh

# List table contents:
setedit list system
setedit list global
```

### Storage Script Execution:
```bash
# Execute from storage path:
sh /storage/emulated/0/Download/custom_boost.sh

# Shortcut (Auto-searches /sdcard/Download/):
run my_tweak.sh
```

### Hardware & Low-Level Properties:
```bash
setprop debug.egl.hw 1
setprop debug.sf.hw 1
setprop debug.hwui.renderer vulkan
getprop ro.product.model
```

---

## 📥 Installation & Setup

### Method 1: On-Device Quick Start (Recommended)
1. Download **`Game_Space.apk`** from the [Releases](https://github.com/willygailo/Game-Launcher/releases) page.
2. Install and launch **[Shizuku](https://shizuku.rikka.app/)** on your device (via Wireless Debugging or ADB).
3. Open **Game Launcher PRO** and grant Shizuku permission when prompted.
4. Select your game and apply flagship masking, custom FPS locks, or run terminal tweaks.

### Method 2: Automated PC / USB Setup
Connect your device to your PC via USB with **USB Debugging** enabled, then run:

```bash
# Complete setup (Installs APK, grants permissions, and starts Shizuku):
./tools/setup_device.sh

# Or run individual scripts:
./tools/grant_permissions.sh   # Grants WRITE_SECURE_SETTINGS, DUMP, etc.
./tools/activate_shizuku.sh    # Starts Shizuku privileged service
```

---

## 🤝 Connect & Developer

<div align="center">

[![GitHub](https://img.shields.io/badge/GitHub-willygailo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo)
&nbsp;&nbsp;
[![Facebook](https://img.shields.io/badge/Facebook-Willy%20Jr%20Carnasa%20Gailo-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

</div>

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
