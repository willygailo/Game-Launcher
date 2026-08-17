# Game Launcher PRO ⚡

<div align="center">

![Hero Banner](android/app/src/main/res/drawable/hero_banner.gif)

[![Android Platform](https://img.shields.io/badge/Platform-Android%2012--16%20(API%2036)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Version](https://img.shields.io/badge/Release-v13.0.0--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases)
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
