<div align="center">

# 🎮 GAME SPACE — Pure Native Non-Rooted Android Gaming Optimizer & FPS Unlocker

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-00F0FF?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Pure%20Java%2017-7000FF?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java JDK 17" />
  <img src="https://img.shields.io/badge/Mode-100%25%20Non--Rooted%20Shizuku%20ADB-00FF66?style=for-the-badge&logo=linux&logoColor=white" alt="100% Non-Rooted Shizuku ADB" />
  <img src="https://img.shields.io/badge/Architecture-Asynchronous%20AppExecutors-FF9900?style=for-the-badge" alt="Async Execution Model" />
  <img src="https://img.shields.io/badge/License-MIT-FF0055?style=for-the-badge" alt="License" />
</p>

### ⚡ *Unlock 90Hz / 120Hz / 144Hz / 165Hz Refresh Rates, Vulkan HWUI, 4x MSAA, Touch Slop Reduction & Thermal Bypass on ALL Non-Rooted Android Devices!* ⚡

---

[📖 Features](#-master-features) • [🏛️ Architecture](#%EF%B8%8F-project-architecture--file-map) • [⚡ 1-Tap Boost](#-1-tap-ultra-booster) • [⚡ Async Threading](#-non-blocking-asynchronous-threading-model) • [🚀 Build APK](#-building-the-release-apk) • [👤 Developer](#-developer--contact)

---

</div>

## 📌 Master Features

> [!IMPORTANT]
> **GAME SPACE** is built **100% in Native Java (JDK 17)** designed specifically for **Non-Rooted Android Devices**. It communicates via **Shizuku ADB Binder IPC (`rikka.shizuku.ShizukuProvider`)**, **Android System Settings**, and **Android 12+ Game Mode API** with zero root requirement, non-blocking background I/O, and zero process hangs.

### ⚡ 1. 100% Non-Rooted Dual Execution Engine
* **⚡ Shizuku ADB Engine (`shizuku`)**: **Non-Rooted High Refresh Rate & Thermal Lock**: Locks Max Refresh Rate (90Hz / 120Hz / 144Hz / 165Hz), forces Vulkan HWUI graphics pipeline, disables thermal throttling caps (`cmd thermalservice`), and triggers Android 12+ Game Mode interventions via IPC binder.
* **⚙️ System Settings & Monitor Engine**: Non-rooted fallback utilizing standard Android `Settings.System`, `Settings.Global`, `ActivityManager`, and `DisplayManager` APIs.

### 📱 2. Direct Shizuku Binder IPC Integration
* Connects directly to the running Shizuku service via `rikka.shizuku.ShizukuProvider`.
* Auto-grants `WRITE_SECURE_SETTINGS`, `WRITE_SETTINGS`, and `PACKAGE_USAGE_STATS` permissions on launch.
* Live `EngineUIHelper` dynamic engine badges across all fragments (`Active Engine: SHIZUKU ADB ENGINE` vs `Active Engine: SYSTEM SETTINGS ENGINE`).

### 🚀 3. Multi-OEM Refresh Rate (Hz) & FPS Lock
* Lock display refresh rate to **60Hz, 90Hz, 120Hz, 144Hz, or 165Hz (Max Hardware Mode)**.
* Supports **Stock Pixel, Xiaomi/MIUI, Samsung OneUI, TECNO/Infinix HiOS, and OnePlus/Realme** system setting overrides.
* Android 12+ Game Mode API integration (`cmd game set --fps <hz> <package>`).

### 🧊 4. Thermal & Throttling Bypass Module
* Overrides system thermal throttling caps (`cmd thermalservice override-status 0` / `cmd thermal override-status 0`).
* Safe layout state modulation with lock overlays (`iv_thermal_lock`).

### 🎯 5. Preset Gaming Profiles Suite
* **🔥 EXTREME PERFORMANCE PROFILE**: Full PowerHAL Mode 2 1, 480Hz touch slop, Vulkan renderer, thermal override status 0.
* **⚡ PERFORMANCE PROFILE**: 120Hz/144Hz lock, PowerHAL Mode 0 1, 300Hz touch slop, Vulkan renderer.
* **⚖️ BALANCED PROFILE**: 90Hz refresh rate, Schedutil CPU governor, balanced thermal management.

### 🎨 6. Graphics & GPU Unlocking Module
* Forces **Vulkan HWUI Graphics Renderer** (`debug.hwui.renderer=vulkan`).
* Forces **SurfaceFlinger GPU Composition** (`debug.sf.hw=1`).
* Unlocks **4x MSAA Anti-Aliasing** (`debug.egl.force_msaa=1`).

### 🧠 7. CPU PowerHAL & Game Mode Governor
* Triggers PowerHAL sustained performance mode (`cmd power set-mode 0 1`).
* Configures Android 12+ Game Mode (`cmd game mode performance <package>`).

### 👆 8. Touch Slop & Input Latency Module
* Ultra-low touch slop sensitivity reduction (`settings put system touch_slop_reduction 1`).
* Disables scroll cache delay (`persist.sys.scrollingcache=3`).

### 🌐 9. Network & TCP Latency Tuning
* High-throughput TCP buffer sizing for Wi-Fi and 4G/5G mobile data.

---

## ⚡ Non-Blocking Asynchronous Threading Model

> [!NOTE]
> All background process executions (`sh -c`, `setprop`, `settings put`, `cmd thermalservice`) and `PackageManager` package scanning are offloaded to a centralized thread-safe executor (`AppExecutors`). This ensures **zero main-thread blocking** and **zero ANRs**.

```
┌────────────────────────────────────────────────────────────────────────┐
│                          AppExecutors                                  │
├────────────────────────────────────────────────────────────────────────┤
│  Disk I/O Single-Thread Executor ──► Background Process / Shell Run   │
│                                                   │                    │
│                                                   ▼                    │
│  Main Looper Handler ◄───────────── Fragment Lifecycle Guard           │
│                                     (!isAdded() / getContext() != null)│
│                                                   │                    │
│                                                   ▼                    │
│  UI Re-enable & Toast ◄────────────── Double-Tap Re-entrancy Lock      │
└────────────────────────────────────────────────────────────────────────┘
```

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
        ├── AndroidManifest.xml                        # Native Manifest & ShizukuProvider IPC
        ├── res/
        │   ├── layout/
        │   │   ├── activity_main.xml                  # Main Fragment Container
        │   │   ├── fragment_home.xml                  # Cyberpunk HUD & 1-Tap Boost
        │   │   ├── fragment_hz_fps.xml                 # Hz & FPS Unlocker UI
        │   │   ├── fragment_profiles.xml              # Gamer Preset Profiles UI (4 Cards)
        │   │   ├── fragment_permissions.xml           # Engine status & WRITE_SETTINGS state
        │   │   ├── fragment_games.xml                 # Game Launcher UI
        │   │   └── item_tweak_card.xml                # Tweak Card layout with lock overlay
        │   └── values/
        │       ├── colors.xml                         # Cyberpunk dark neon palette
        │       └── strings.xml
        └── java/com/gamespace/app/
            ├── MainActivity.java                      # Main Activity & Navigation
            ├── data/                                  # Repository layer
            │   ├── CommandExecutor.java               # Engine mode switcher & stdout parser
            │   └── GameManagerRepository.java         # Game scanner & memory optimizer
            ├── channels/                              # Subsystem Execution Channels
            │   ├── HzFpsChannel.java                  # Refresh rate lock channel
            │   ├── ThermalChannel.java                # Thermal override channel
            │   ├── CpuGovernorChannel.java            # PowerHAL & Game Mode channel
            │   ├── GpuTweaksChannel.java              # Vulkan & MSAA GPU channel
            │   ├── TouchLatencyChannel.java           # Touch slop channel
            │   ├── NetworkTweaksChannel.java          # TCP buffer channel
            │   ├── RamZramChannel.java                # Memory trim channel
            │   ├── PerformanceChannel.java            # Profile orchestrator channel
            │   └── DeviceInfoChannel.java             # Hardware monitor channel
            ├── ui/                                    # UI Fragments per subsystem
            │   ├── HomeFragment.java                  # Home HUD & metrics
            │   ├── HzFpsFragment.java                 # Refresh rate unlocker
            │   ├── TweaksFragment.java                # Dynamic tweaks card list
            │   ├── TweaksAdapter.java                 # Tweaks RecyclerView adapter
            │   ├── ProfilesFragment.java              # 4 Gamer preset profiles
            │   ├── GamesFragment.java                 # Installed games launcher
            │   ├── GamesAdapter.java                  # Games RecyclerView adapter
            │   └── PermissionsFragment.java           # Access engine & permissions UI
            └── utils/                                 # Core execution & helper utilities
                ├── AppExecutors.java                  # Centralized async thread pool
                ├── EngineUIHelper.java                # Live engine badge formatter
                ├── ShellExecutor.java                 # Native shell process wrapper
                ├── ShizukuExecutor.java               # Shizuku IPC binder & command executor
                └── ShizukuUtils.java                  # Permission dialog launcher
```

---

## 🧩 Subsystem Module Map

| Feature Area | Fragment (UI) | Channel / Utility | Non-Rooted Execution Surface |
| :--- | :--- | :--- | :--- |
| **Refresh Rate / FPS** | `HzFpsFragment.java` | `HzFpsChannel.java` | `settings put system/secure/global` |
| **Thermal Bypass** | `HzFpsFragment.java` | `ThermalChannel.java` | `cmd thermalservice override-status 0` |
| **PowerHAL / Game Mode** | `HomeFragment.java` | `CpuGovernorChannel.java` | `cmd power set-mode` / `cmd game` |
| **GPU / Vulkan** | `TweaksFragment.java` | `GpuTweaksChannel.java` | `setprop debug.hwui.renderer vulkan` |
| **Touch Response** | `TweaksFragment.java` | `TouchLatencyChannel.java` | `settings put system touch_slop_reduction` |
| **RAM Trim** | `HomeFragment.java` | `RamZramChannel.java` | `ActivityManager` / `am kill-all` |
| **Preset Profiles** | `ProfilesFragment.java` | `PerformanceChannel.java` | Profile Orchestrator (2D, PUBG, Balanced, Battery) |
| **Async Execution** | All Fragments | `AppExecutors.java` | Background SingleThreadExecutor + Main Looper |
| **Engine Status** | All Fragments | `EngineUIHelper.java` | Dynamic Shizuku vs System Settings Header |

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
