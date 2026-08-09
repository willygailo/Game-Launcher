<p align="center">
  <img src="BANNER.gif" alt="Precision Aim Banner" width="100%" style="border-radius: 12px;">
</p>

<h1 align="center">🎮 Game Launcher Pro V2.0 (v4.8.0) — Real-Time 120/144/165 FPS & MAX Graphics Engine</h1>

<p align="center">
  <b>Ban-Safe Hardware Identity Spoofer, Real-Time SurfaceFlinger FPS HUD & 165Hz Game Patcher for Mobile eSports</b>
</p>

<p align="center">
  <a href="https://github.com/willygailo/Game-Launcher"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"></a>
  <a href="https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027"><img src="https://img.shields.io/badge/Facebook-1877F2?style=for-the-badge&logo=facebook&logoColor=white" alt="Facebook"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License"></a>
  <a href="android"><img src="https://img.shields.io/badge/Android-API_36-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"></a>
  <a href=".github/workflows/android-build.yml"><img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions" alt="Build Status"></a>
  <a href="https://github.com/willygailo/Game-Launcher/releases"><img src="https://img.shields.io/badge/Version-v4.8.0-emerald?style=for-the-badge" alt="Version 4.8.0"></a>
</p>

---

## 🚀 Overview

**Game Launcher Pro V2.0 (v4.8.0)** is an advanced, device-level performance utility and game launcher designed for competitive eSports titles (*Mobile Legends, PUBG Mobile, COD Mobile, Honor of Kings, Genshin Impact, Roblox, Free Fire, Wild Rift*).

By leveraging **Shizuku API (privileged ADB Binder IPC)**, it unlocks **120 FPS, 144 FPS, and 165 FPS** and **MAX Graphics** across all target games through legal system file modifications and multi-layer hardware refresh rate overrides.

> [!IMPORTANT]
> **SAFETY & COMPLIANCE GUARANTEE**:
> - ⚡ **Zero Executable Tampering**: Operates using standard system properties (`setprop`, `resetprop`, `settings put`, `device_config`) and native game INI/JSON/XML configuration files.
> - ⚡ **Shizuku Legal System Bridge**: Uses ADB shell privileges (uid 2000) with automatic `.bak` backups and read-only file locks (`chmod 444`) to prevent game clients resetting graphics settings on startup.
> - ⚡ **100% Reversible**: All system property overrides are volatile and safely revert to device factory defaults upon reboot.

---

## ⚡ Key Modules & Real-Time Engines

### 1. Real-Time SurfaceFlinger FPS Monitoring HUD
- Displays live active foreground game frame rate via `cmd SurfaceFlinger get_fps` directly over active gameplay.
- Non-blocking floating overlay pill (`FLAG_NOT_TOUCHABLE`) with live memory usage, battery temperature, and mA power draw.

### 2. Flagship Hardware Identity Spoofer (CPU, GPU, RAM, Model, SoC)
- Overrides all 6 Android system property namespaces (`product`, `vendor`, `system`, `odm`, `product.product`, `system_ext`).
- Spoofs Snapdragon 8 Gen 3, Adreno 750, 24GB LPDDR5X RAM, and OpenGL ES 3.2 identities (ROG Phone 8 Pro, REDMAGIC 9 Pro, Galaxy S24 Ultra, Xiaomi 14 Ultra).

### 3. Dedicated 120/144/165 FPS & MAX Graphics Game Patchers
- **Mobile Legends: Bang Bang**: `FrameRateLevel=9` (165Hz/144Hz unlock), `GraphicsQuality=4`, `UltraHDMode=1`, `HighFreqTouchHz=165`.
- **PUBG Mobile / BGMI**: `UserCustom.ini` + `Active.sav` binary patcher forcing 120/144/165 FPS and Extreme HDR graphics.
- **Call of Duty Mobile**: `UserSetting.json` + `playerprefs.xml` forcing `MaxFPS=165` & `GraphicQuality=4`.
- **Honor of Kings (HOK)**: `SystemConfig.ini` forcing `FrameRate=120`, `FrameRateLevel=4`, `GraphicsLevel=5`.
- **Genshin Impact & Star Rail**: `setting_data` JSON forcing `fps: 120` & `graphics_quality: 5`.
- **Roblox**: `ClientAppSettings.json` forcing `"DFIntTaskSchedulerTargetFps": 165` & `"FFIntDebugForceGraphicsQuality": 10`.

---

## 🏗️ Project Architecture

```
Game-Launcher/
├── .github/
│   └── workflows/        # GitHub Actions CI build & release workflow
├── android/
│   ├── app/              # Android app source code (SDK 36, Java 17)
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/  # MaxHzForceChannel (6-Layer 165Hz Refresh Engine)
│   │   │   ├── config/   # GameConfigPatcher, Mlbb, Pubg, Codm, Hok, Genshin, Roblox patchers
│   │   │   ├── core/     # AppExecutors & SettingsManager
│   │   │   ├── overlay/  # FloatingOverlayService (SurfaceFlinger Real-time FPS HUD)
│   │   │   ├── shizuku/  # ShizukuExecutor & ShizukuFileBridge (Legal System File IPC)
│   │   │   ├── spoofer/  # DeviceSpooferEngine (CPU, GPU, RAM, Model, SoC spoofer)
│   │   │   └── ui/       # MainActivity & WebView interface
│   │   └── src/test/     # Unit tests
├── BANNER.gif            # Project banner header
├── CONTRIBUTING.md       # Open-source developer contribution guide
├── LICENSE               # Apache 2.0 Open Source License
├── SECURITY.md          # Security policy & ban-safety rules
└── README.md             # Project documentation
```

---

## 🛠️ Setup Guide

1. Download & Install [Shizuku from Google Play](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) or GitHub.
2. Enable **Developer Options** and **Wireless Debugging** on your device.
3. Start Shizuku via Wireless Debugging or PC ADB.
4. Launch **Game Launcher Pro V2.0** and grant Shizuku permission when prompted.

---

## ⚙️ Building

```bash
cd android

# Clean & Build Release APK
./gradlew clean assembleDebug assembleRelease
```

Output APKs:
- `android/app/build/outputs/apk/debug/Game_Space_Debug.apk`
- `android/app/build/outputs/apk/release/Game_Space.apk`

---

## 📜 Legal & License

Distributed under the **Apache License 2.0**. See `LICENSE` for details.
