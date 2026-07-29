<div align="center">

# 🎮 GAME SPACE — Pure Native Non-Rooted Android Gaming Optimizer & FPS Unlocker

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-00F0FF?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Non--Rooted%20Shizuku%20ADB-00FF66?style=for-the-badge&logo=linux&logoColor=white" alt="100% Non-Rooted Shizuku ADB" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Unlock 90Hz / 120Hz / 144Hz Refresh Rates, Vulkan HWUI, 4x MSAA, Touch Slop Reduction & Thermal Bypass on ALL Non-Rooted Android Devices!* ⚡

---

[📖 Features](#-master-features) • [🏛️ Architecture](#%EF%B8%8F-project-architecture--file-map) • [⚡ 1-Tap Boost](#-1-tap-ultra-booster) • [🚀 Build APK](#-building-the-release-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 Master Features

> [!IMPORTANT]
> **GAME SPACE** is built **100% in Native Java (JDK 17)** designed specifically for **Non-Rooted Android Devices**. It communicates via **Shizuku ADB Binder API**, **Android System Settings**, and **Android 12+ Game Mode API** with ZERO root requirement or process hangs.

### ⚡ 1. 100% Non-Rooted Dual Execution Engine
* **⚡ Shizuku ADB Engine (`shizuku`)**: **Non-Rooted High Refresh Rate & Thermal Lock**: Locks Max Refresh Rate (90Hz / 120Hz / 144Hz), forces Vulkan HWUI graphics pipeline, disables thermal throttling caps (`cmd thermalservice`), and triggers Android 12+ Game Mode interventions.
* **⚙️ System Settings & Monitor Engine**: Non-rooted fallback utilizing standard Android `Settings.System`, `Settings.Global`, `ActivityManager`, and `DisplayManager` APIs.

### 📱 2. 0-ADB Direct Shizuku Connection
* Connects directly to Shizuku service without requiring a PC connection.
* Auto-grants `WRITE_SECURE_SETTINGS`, `WRITE_SETTINGS`, and `PACKAGE_USAGE_STATS` permissions on launch.

### 🚀 3. Multi-OEM Refresh Rate (Hz) & FPS Lock
* Lock display refresh rate to **60Hz, 90Hz, 120Hz, 144Hz, or Maximum Supported Display Mode**.
* Supports **Stock Pixel, Xiaomi/MIUI, Samsung OneUI, and OnePlus/Realme** system setting overrides.
* Android 12+ Game Mode API integration (`cmd game set --fps <hz> <package>`).

### 🧊 4. Thermal & Throttling Bypass Module
* Overrides system thermal throttling caps (`cmd thermalservice override-status 0` / `cmd thermal override-status 0`).

### 🎨 5. Graphics & GPU Unlocking Module
* Forces **Vulkan HWUI Graphics Renderer** (`debug.hwui.renderer=vulkan`).
* Forces **SurfaceFlinger GPU Composition** (`debug.sf.hw=1`).
* Unlocks **4x MSAA Anti-Aliasing** (`debug.egl.force_msaa=1`).

### 🧠 6. CPU PowerHAL & Game Mode Governor
* Triggers PowerHAL sustained performance mode (`cmd power set-mode 0 1`).
* Configures Android 12+ Game Mode (`cmd game mode performance <package>`).

### 👆 7. Touch Slop & Input Latency Module
* Ultra-low touch slop sensitivity reduction (`settings put system touch_slop_reduction 1`).
* Disables scroll cache delay (`persist.sys.scrollingcache=3`).

### 🌐 8. Network & TCP Latency Tuning
* High-throughput TCP buffer sizing for Wi-Fi and 4G/5G mobile data.

---

## ⚡ 1-Tap Ultra Booster

```
┌────────────────────────────────────────────────────────┐
│               ⚡ ONE-TAP ULTRA BOOST                   │
├────────────────────────────────────────────────────────┤
│  [1] Trim RAM & Kill Background Junk Apps              │
│  [2] Lock Display to 120Hz / Max Hardware Mode        │
│  [3] Enable Vulkan HWUI & SurfaceFlinger GPU Render   │
│  [4] Bypass Thermal Throttling Caps (ThermalService)  │
│  [5] Trigger PowerHAL & Android Game Mode Performance │
└────────────────────────────────────────────────────────┘
```

---

## 🏛️ Project Architecture & File Map

```
Game_Launcher_Pro/
├── README.md                                          # Master documentation
└── android/                                           # Pure Native Android Java Project
    ├── build.gradle                                   # AGP 8.7.3 buildscript
    └── app/src/main/
        ├── AndroidManifest.xml                        # Native Manifest & Permissions
        ├── res/
        │   ├── layout/
        │   │   ├── activity_main.xml                  # Main Fragment Container
        │   │   ├── fragment_home.xml                  # Cyberpunk HUD & 1-Tap Boost
        │   │   ├── fragment_hz_fps.xml                 # Hz & FPS Unlocker UI
        │   │   ├── fragment_gpu_tweaks.xml            # GPU Vulkan & MSAA UI
        │   │   ├── fragment_touch_latency.xml         # Touch Sampling UI
        │   │   ├── fragment_profiles.xml              # Gamer Preset Profiles UI
        │   │   └── fragment_games.xml                 # Game Launcher UI
        │   └── values/
        │       ├── colors.xml                         # Cyberpunk dark neon palette
        │       └── strings.xml
        └── java/com/gamespace/app/
            ├── MainActivity.java                      # Main Activity & Navigation
            ├── channels/                              # Subsystem Execution Channels
            │   ├── ShizukuChannel.java                # Shizuku binder channel
            │   ├── HzFpsChannel.java                  # Refresh rate lock channel
            │   ├── ThermalChannel.java                # Thermal override channel
            │   ├── CpuGovernorChannel.java            # PowerHAL & Game Mode channel
            │   ├── GpuTweaksChannel.java              # Vulkan & MSAA GPU channel
            │   ├── TouchLatencyChannel.java           # Touch slop channel
            │   ├── NetworkTweaksChannel.java          # TCP buffer channel
            │   ├── RamZramChannel.java                # Memory trim channel
            │   ├── PermissionChannel.java             # Permission grant channel
            │   ├── PerformanceChannel.java            # Profile orchestrator channel
            │   ├── GameLibraryChannel.java            # Installed games channel
            │   └── DeviceInfoChannel.java             # Hardware monitor channel
            ├── ui/                                    # UI Fragments per subsystem
            │   ├── HomeFragment.java
            │   ├── HzFpsFragment.java
            │   ├── ProfilesFragment.java
            │   ├── GamesFragment.java
            │   └── PermissionsFragment.java
            └── core/ & utils/                         # Core execution strategies
                ├── CommandEngine.java
                ├── PropertyResolver.java
                ├── ShellExecutor.java
                └── ShizukuExecutor.java
```

---

## 🧩 Subsystem Module Map

| Feature Area | Fragment (UI) | Channel (Logic) | Non-Rooted Execution Surface |
| :--- | :--- | :--- | :--- |
| **Refresh Rate / FPS** | `HzFpsFragment.java` | `HzFpsChannel.java` | `settings put system/secure/global` |
| **Thermal Bypass** | `HzFpsFragment.java` | `ThermalChannel.java` | `cmd thermalservice override-status 0` |
| **PowerHAL / Game Mode** | `HomeFragment.java` | `CpuGovernorChannel.java` | `cmd power set-mode` / `cmd game` |
| **GPU / Vulkan** | `GpuTweaksFragment.java` | `GpuTweaksChannel.java` | `setprop debug.hwui.renderer vulkan` |
| **Touch Response** | `TouchLatencyFragment.java` | `TouchLatencyChannel.java` | `settings put system touch_slop_reduction` |
| **RAM Trim** | `HomeFragment.java` | `RamZramChannel.java` | `ActivityManager` / `am kill-all` |
| **Preset Profiles** | `ProfilesFragment.java` | `PerformanceChannel.java` | Profile Orchestrator |

---

## 🚀 Building the Release APK

Navigate to the `android/` directory and build with Gradle:

```bash
cd android
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew assembleRelease
```

### 📦 Output APK Path:
```
android/app/build/outputs/apk/release/Game_Space.apk
```

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
