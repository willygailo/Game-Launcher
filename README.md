# ⚡ Game Booster Pro 2 — Ultimate Esports FPS & Refresh Rate Engine (v6.8.0-PRO)

[![Android SDK](https://img.shields.io/badge/Android%20SDK-36%20%28Android%2016%29-brightgreen)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Shizuku Privileged](https://img.shields.io/badge/Shizuku-ADB%20Binder%20Direct-orange)](https://shizuku.rikka.app/)
[![Target FPS](https://img.shields.io/badge/Target%20FPS-120%20%7C%20144%20%7C%20165%20Hz-red)](#-120--144--165-hz--fps-display-forcing-engine)

**Game Booster Pro 2** is an advanced Android game optimization system engineered for competitive mobile gamers. It provides zero-delay display refresh rate forcing (up to 165Hz), real-time device spoofing, UE4 CVar injection, and direct internal file patching for **Mobile Legends: Bang Bang (MLBB)**, **PUBG Mobile / BGMI (PUBGM)**, and **Call of Duty: Mobile (CODM)** across Android 13, 14, 15, and 16.

---

## 🔥 Key Features

### ⚡ 120 / 144 / 165 Hz & FPS Display Forcing Engine
Bypasses OS display constraints and forces peak refresh rates across 6 system layers via direct Shizuku ADB Binder execution:
- **Layer 1 (AOSP System & Global)**: `peak_refresh_rate`, `min_refresh_rate`, `user_refresh_rate`.
- **Layer 2 (Android Game Mode API)**: `cmd game mode performance global`, `cmd window set-app-refresh-rate global 165`.
- **Layer 3 (Device Config Overlay)**: `device_config put game_overlay global mode=2,fps=165`.
- **Layer 4 (SurfaceFlinger Direct Binder)**: `service call SurfaceFlinger 1035 i32 165` and `1036`.
- **Layer 5 (Setprop Overrides)**: `debug.sf.fps_limit 165`, `persist.sys.NV_FPSLIMIT 165`, `debug.gr.swapinterval 0`.
- **Layer 6 (OEM Brand Keys)**: Auto-detected overrides for Xiaomi (HyperOS/MIUI), Samsung (OneUI), OnePlus/Oppo/Realme (OxygenOS/ColorOS), ASUS ROG, RedMagic, Vivo/iQOO, Motorola, Transsion (Infinix/Tecno), etc.

### 🎮 Dedicated Game Config Patchers (MLBB / PUBGM / CODM)
Directly patches game config files in `/sdcard/Android/data/` and protected `/data/data/` directories using temporary ADB root shell routing:
- **Mobile Legends: Bang Bang (MLBB)**: Unlocks 165 FPS, Ultra/HDR graphics, 165Hz touch boost, and damage script asset configs.
- **PUBG Mobile / BGMI (PUBGM)**: Injects UE4 CVars for 165 FPS, MobileTouchBoostRate=165, Aim Assist, and 80% Aimbot lock sensitivity.
- **Call of Duty: Mobile (CODM)**: Writes formatted `UserSetting.json`, `playerprefs.xml`, `GraphicsSettings.ini`, and `ControlsSettings.ini` (`MaxFrameRate=165`, `TouchBoostHz=165`, `AimAssist=1`).

### 📱 Full Android 13 to 16 Compatibility
- **Scoped Storage Bypass**: Routes protected directory operations through Shizuku ADB shell process context.
- **Foreground Service Compliance**: Configured with `foregroundServiceType="specialUse"` and `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` for Android 14–16.
- **Android 14+ Broadcast Receiver Security**: Utilizes `ContextCompat.registerReceiver()` with `RECEIVER_NOT_EXPORTED` flags to prevent security crashes.

---

## 🎯 Target Game Packages & Modified File Paths

### 1. Mobile Legends: Bang Bang (MLBB)
* **Package Variants**: `com.mobile.legends`, `com.mobile.legends.vng`, `com.mobilelegends.mi`, `com.mobilelegends.hw`, `com.mobile.legends.kr`, `com.mobile.legends.jp`, `com.mobile.legends.moonton`
* **Target File Paths**:
  * `/sdcard/Android/data/<pkg>/files/dragon2017/assets/UI/Config/UserSystem.ini`
  * `/sdcard/Android/data/<pkg>/files/dragon2017/assets/UI/Config/DamageSystem.ini`
  * `/sdcard/Android/data/<pkg>/files/dragon2017/assets/UI/HighFPSConfig.ini`
  * `/sdcard/Android/data/<pkg>/files/dragon2017/assets/Com/MobileLegendsSettings.ini`
  * `/data/data/<pkg>/files/dragon2017/assets/Com/MobileLegendsSettings.ini`
  * `/data/data/<pkg>/files/dragon2017/assets/UI/Config/UserSystem.ini`
  * `/data/data/<pkg>/files/dragon2017/assets/UI/Config/DamageSystem.ini`

### 2. PUBG Mobile & BGMI (PUBGM)
* **Package Variants**: `com.tencent.ig`, `com.pubg.imobile`, `com.pubg.krmobile`, `com.vng.pubgmobile`, `com.rekoo.pubgm`, `com.tencent.tmgp.pubgmhd`, `com.tencent.iglite`, `com.pubg.newstate`
* **Target File Paths**:
  * `/sdcard/Android/data/<pkg>/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini`
  * `/sdcard/Android/data/<pkg>/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini`
  * `/sdcard/Android/data/<pkg>/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/EnjoyCJ.ini`
  * `/data/data/<pkg>/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini`
  * `/data/data/<pkg>/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini`
  * `/data/data/<pkg>/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/EnjoyCJ.ini`

### 3. Call of Duty: Mobile (CODM)
* **Package Variants**: `com.activision.callofduty.shooter`, `com.garena.game.codm`, `com.tencent.tmgp.kr.codm`, `com.vng.codmvn`
* **Target File Paths**:
  * `/sdcard/Android/data/<pkg>/files/Config/UserSetting.json`
  * `/sdcard/Android/data/<pkg>/files/<pkg>.v2.playerprefs.xml`
  * `/sdcard/Android/data/<pkg>/files/GraphicsSettings.ini`
  * `/sdcard/Android/data/<pkg>/files/ControlsSettings.ini`
  * `/data/data/<pkg>/files/GraphicsSettings.ini`
  * `/data/data/<pkg>/files/ControlsSettings.ini`
  * `/data/data/<pkg>/files/Config/UserSetting.json`

---

## 🛠 Shizuku Setup Instructions

1. Install **Shizuku** from Google Play or GitHub.
2. Open Shizuku and select **Start via Wireless Debugging** (Android 11+) or **Start via PC ADB**.
3. Launch **Game Booster Pro 2**; the app will automatically connect to Shizuku and request system permission grants.
4. Select your target refresh rate (120Hz, 144Hz, or 165Hz) or activate game competitive profiles.

---

## 🏗 Building & Installation

### Prerequisites
- JDK 17
- Android SDK 36 (Android 16 Build Tools)
- Gradle 8.13+

### Clean Build Commands
```bash
# Navigate to android root
cd android

# Clean build debug APK
./gradlew clean assembleDebug

# Clean build release APK
./gradlew clean assembleRelease
```

Generated APKs:
- Debug: `android/app/build/outputs/apk/debug/Game_Space_Debug.apk`
- Release: `android/app/build/outputs/apk/release/Game_Space.apk`

---

## 📝 Release Notes (v6.8.0-PRO)

- **165Hz Display Forcing**: Fully upgraded 6-layer refresh rate forcing engine with SurfaceFlinger binder transactions.
- **MLBB / PUBGM / CODM Patchers**: Direct internal config file injection for 165 FPS, 165Hz touch boost, and graphics quality.
- **Android 13–16 Compliance**: Fixed Scoped Storage barriers, Foreground Service types, and dynamic receiver export flags.
- **Zero-Conflict Engine**: Updated `ForegroundAppDetector` and `HzFpsFragment` for flawless execution across all games.
