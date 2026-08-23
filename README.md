<div align="center">

<img src="android/app/src/main/res/drawable/hero_banner.gif" alt="Game Launcher PRO Hero Banner" width="100%" />

# ⚡ GAME LAUNCHER PRO — ULTIMATE eSPORTS GAMING SUITE ⚡
### 🚀 *Strict 90–185Hz High-Refresh Lock • Adreno 840 Flagship ART Spoofing • Shizuku + LSPatch Non-Root Dual Engine*

<br/>

[![Latest Release](https://img.shields.io/badge/Release-v16.2.0--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=black&labelColor=0D1117)](https://github.com/willygailo/Game-Launcher/releases/latest)
[![Android Support](https://img.shields.io/badge/Android-13--16%20(API%2033--36)-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=0D1117)](#-system-requirements)
[![Privilege Layer](https://img.shields.io/badge/Engine-Shizuku%20%2B%20LSPatch%20Combo-9D4EDD?style=for-the-badge&logo=shield&logoColor=white&labelColor=0D1117)](#-privilege-layer)
[![Display Lock](https://img.shields.io/badge/Display-90Hz%20to%20185Hz%20Only-FF0055?style=for-the-badge&logo=speedtest&logoColor=white&labelColor=0D1117)](#-1-extreme-display--high-refresh-engine)
[![GPU Spoof](https://img.shields.io/badge/GPU%20Hook-Adreno%20840%20Elite-FFAA00?style=for-the-badge&logo=qualcomm&logoColor=black&labelColor=0D1117)](#-2-in-game-art-hardware-spoofing--adreno-840)
[![License](https://img.shields.io/badge/License-MIT-00E5FF?style=for-the-badge&labelColor=0D1117)](#-license)

<br/>

[📥 **Download Latest APK (v16.2.0-PRO)**](https://github.com/willygailo/Game-Launcher/releases/latest) • [✨ **Core Capabilities**](#-core-capabilities) • [🎮 **Supported Games**](#-supported-esports-titles) • [🧬 **LSPatch Non-Root Guide**](#-method-2-non-root-lspatch-in-game-art-hooking-zero-root) • [🏗️ **Architecture**](#-system-architecture) • [🚀 **Installation**](#-installation--setup)

<br/>

---

</div>

## 🌟 Executive Overview

**Game Launcher PRO (v16.2.0-PRO)** is a state-of-the-art Android gaming performance platform built specifically for high-level competitive eSports mobile gaming. Operating via **Shizuku Privileged System IPC (UID 2000)** and **LSPatch / LSPosed in-memory ART hooking**, it delivers real-time display unthrottling, removes 60Hz display bottlenecks, overrides GPU identity to **Adreno (TM) 840 (Snapdragon 8 Elite)**, and synchronizes 1000Hz touch polling—**completely non-root and 100% free**.

```
  ┌─────────────────────────────────────────────────────────────────────────────────────────────┐
  │ ⚡ 90Hz – 185Hz ONLY  │ 🎮 Adreno 840 GPU Hook │ 🧬 LSPatch Dual Combo │ 🎯 1000Hz Touch Sync │
  └─────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## ✨ Core Capabilities

### ⚡ 1. Extreme Display & High-Refresh Engine (Zero 60Hz)
* 🚫 **100% Elimination of 60Hz**: Purged all 60Hz and 60 FPS caps across system settings, SurfaceFlinger, and display mode filters.
* 🚀 **Strict 5-Tier High-Refresh Schedulers**:
  * 🟢 **90 FPS / 90Hz** — Ultra Smooth Competitive Tier
  * 🔵 **120 FPS / 120Hz** — eSports High Frame-Rate Standard
  * 🟣 **144 FPS / 144Hz** — ROG Gaming Display Synchronizer
  * 🔴 **165 FPS / 165Hz** — RedMagic Pro Hyper-Refresh Tier
  * 🔥 **185 FPS / 185Hz** — Extreme Max Unthrottled Panel Override
* 🛡️ **SurfaceFlinger Unthrottler & Zero-Latency Latching**: Dispatches `debug.sf.latch_unsignaled=1`, `enable_gl_backpressure=0`, and `service call SurfaceFlinger 1035/1036 i32 185` for zero-latency frame presentation.
* 📱 **Android 14–16 Game Mode API**: Automates `cmd game mode performance`, sets dynamic app refresh rate clamps globally, and activates Updatable Game Driver preferences.

---

### 🎮 2. In-Game ART Hardware Spoofing & Adreno 840
* 🧬 **LSPatch & LSPosed In-Memory Hooking**: Masks hardware signatures inside the game process itself without modifying APK files on disk.
* 💎 **Adreno (TM) 840 Flagship Architecture**: Hooks OpenGL ES `glGetString(GL_RENDERER/GL_VENDOR)` and Vulkan layers to report **Adreno (TM) 840 / Qualcomm** to unlock 185 FPS and max graphic presets in competitive titles.
* 🏆 **Flagship Device Presets**:
  * 🔴 **ASUS ROG Phone 9 Pro** *(Snapdragon 8 Elite • Adreno 840 • 24GB RAM • 185Hz)*
  * ⚡ **Nubia RedMagic 10 Pro+** *(Snapdragon 8 Elite • ICE 13.0 Cooling • 185Hz)*
  * 🌌 **Samsung Galaxy S26 Ultra** *(Snapdragon 8 Elite Gen 5 • Vulkan 1.3)*
  * 🐉 **Xiaomi 15 Ultra** *(Snapdragon 8 Elite • Leica HDR Video Engine)*
  * ⚡ **Vivo iQOO 13 / 15 Pro** *(OriginOS 6.0 Supercharged Frame-Rate)*
  * 🍏 **Apple iPad Pro M4 / iPhone 16 Pro Max** *(Uncapped Graphic Whitelists)*

---

### 🧠 3. Native C++ CPU Affinity & Linux Kernel Governors
* ⚡ **POSIX Prime Core Affinity**: Native C++ `sched_setaffinity` syscall locks game render threads (`MainThread`, `UnityMain`, `RHIThread`) directly to Cortex-X4 / Prime CPU cores.
* 🛡️ **LMK OOM Score Immunity**: Pins game PID `oom_score_adj` to `-1000` (immune to Android LowMemoryKiller) with real-time `renice -20` priority.
* ⚙️ **GPU Sysfs Performance Locks**: Direct sysfs override for Qualcomm Adreno `kgsl-3d0` and ARM Mali governors to prevent thermal down-stepping.

---

### 🎯 4. Zero-Delay Touch & Combat Physics Optimization
* ⚡ **1000Hz Ultra Touch Polling**: Lowers touch slop (`touch_slop_reduction=1`) and scales pressure thresholds to `0.001` for zero crosshair delay.
* 🎯 **Zero Recoil & Combat Tuning**: Injects real-time game physics optimizations (`ZeroRecoil=1`, `DamageMultiplier=4.50`, `AimAssistMagnet=1`).
* ⏱️ **Sub-Millisecond Threading**: High-priority background executor pool for instant, stutter-free Shizuku command execution.
* 📜 **185Hz Butter-Smooth UI**: Dual-cached RecyclerViews (`setItemViewCacheSize=25`) for zero frame drops in launcher navigation.

---

### 🕹️ 5. Engine-Specific FastFlags & INI Injectors
* 🧱 **Roblox FastFlags Auto-Injector**: Generates `ClientAppSettings.json` with `DFIntTaskSchedulerTargetFps=185`, Vulkan prefer flags, and uncapped memory limits.
* ⚡ **Unreal Engine 4/5 `Engine.ini` Tuner**: Direct injection of `r.VSync=0`, `r.FinishCurrentFrame=0`, `r.OneFrameThreadLag=0`, and `t.MaxFPS=185`.
* 🎮 **Unity `boot.config` Pipeline**: Forces `gfx-enable-native-gles=1`, `gc-max-time-slice=3`, and removes debug overhead.

---

### 📡 6. Low-Latency Turbo Network & TCP BBR Engine
* 📶 **Wi-Fi 7 / 6E Gaming QoS**: Locks Wi-Fi hardware into Android `WIFI_MODE_FULL_LOW_LATENCY` state.
* 🌐 **Linux TCP BBR Congestion Control**: Applies TCP BBR congestion algorithm and disables Nagle packet buffering (`tcp_nodelay=1`) to eliminate ping spikes.
* 🛡️ **Ultra-Fast eSports DNS**: Routes game server traffic through prioritized latency-optimized nodes.

---

### 📊 7. Pro 1% Low & 0.1% Low FPS HUD Telemetry
* 📈 **SurfaceFlinger Latency Parser**: Real-time 99th and 99.9th percentile frame latency computation.
* ⏱️ **Frame-Time Jitter Metrics**: Displays live frame-time variance (e.g. `5.4ms ± 0.1ms`) to track and eliminate micro-stutters.
* 🎯 **Custom Floating Pro HUD**: Draggable overlay featuring real-time FPS, 1% Low, SoC thermal status, RAM utilization, and ping monitor.

---

## 🎮 Supported eSports Titles

| Game Title | Category | Unlocked Engine Features | Max Target |
| :--- | :---: | :--- | :---: |
| **PUBG Mobile / BGMI** *(Global, KR, VN, TW, IN)* | Battle Royale | `Active.sav` 185 FPS Unlock, UE4/5 `Engine.ini`, 1000Hz Gyro, Zero Recoil | 🔥 **185 FPS** |
| **Mobile Legends: Bang Bang** | MOBA | 185Hz Ultra Mode, Unity `boot.config`, Drone FOV, Super Sampling, Zero Touch Slop | 🔥 **185 FPS** |
| **Call of Duty: Mobile & Warzone** | FPS | 185 FPS Ultra Preset, 1000Hz Touch Poll, Instant Crosshair Stabilizer | 🔥 **185 FPS** |
| **Free Fire & Free Fire MAX** | Battle Royale | 185 FPS Uncap, Unity Pipeline, High Sensitivity Curve Sync, Jitter Buffer Reduction | 🔥 **185 FPS** |
| **Genshin Impact & Wuthering Waves** | Action RPG | Adreno 840 Whitelist Override, 120 FPS Uncapped Renderer, Vulkan Backend | ⚡ **120 FPS** |
| **CarX Street & Asphalt Legends** | Racing | Ultra Graphics Unlock, 185 FPS Shader Pipeline, Nitro/Torque Multiplier | 🔥 **185 FPS** |
| **Blood Strike** | FPS | 165/185 FPS High Refresh Mode, Low-Latency Touch, Fast Aim Response | 🔥 **185 FPS** |
| **Arena Breakout** | Tactical FPS | 144 FPS High-Tier Presets, UE4 `Engine.ini`, Thermal Throttle Bypass | 🚀 **144 FPS** |
| **Farlight 84** | Hero Shooter | 185 FPS Solarland Engine Override, Recoil Stabilization | 🔥 **185 FPS** |
| **Roblox** | Sandbox | FastFlags `ClientAppSettings.json` 185 FPS Scheduler, Uncapped Memory | 🔥 **185 FPS** |

---

## 🚀 Installation & Setup

### 📱 Method 1: On-Device Quick Start (Zero PC, Non-Root)

1. **Download APK**:
   * Grab the latest [**`Game_Space.apk`**](https://github.com/willygailo/Game-Launcher/releases/latest).
2. **Start Shizuku**:
   * Install [**Shizuku**](https://shizuku.rikka.app/) from Google Play or GitHub.
   * Start Shizuku via **Wireless Debugging** in Android Developer Options.
3. **Launch & Enjoy**:
   * Open **Game Launcher PRO**, grant Shizuku access, select your target game, set your **Target FPS (90–185)**, and tap **BOOST & LAUNCH**!

---

### 🧬 Method 2: Non-Root LSPatch In-Game ART Hooking (Zero-Root)

To unlock in-game **Adreno 840 GPU Spoofing** without Root:

1. Install **LSPatch** (v0.6+).
2. Open **LSPatch** → tap **Manage** → tap **+** → select your game (e.g. *PUBG Mobile* / *MLBB*).
3. Select **Portable Mode** (or Local Mode) → tap **Embed Modules**.
4. Check **Game Booster** (Game Launcher PRO).
5. Tap **Patch & Install**!
6. Launch your patched game — in-game GPU identity is now **Adreno (TM) 840** and all FPS options (90–185) are unlocked!

---

### 💻 Method 3: PC / ADB Automated Setup (One-Click)

Connect your phone to your PC via USB with **USB Debugging enabled**, then run:

```bash
# 1. Run all-in-one setup (Installs APK, grants permissions, activates Shizuku)
./tools/setup_device.sh

# Or run individual scripts:
./tools/activate_shizuku.sh    # Starts Shizuku privileged daemon via ADB
./tools/grant_permissions.sh   # Grants WRITE_SECURE_SETTINGS & DUMP permissions
```

---

## 🛠️ Building from Source

### Prerequisites:
- **JDK 17** or **JDK 21** (`JAVA_HOME`).
- **Android SDK Build Tools** (API 36 / Android 16).

```bash
# Clone the repository
git clone https://github.com/willygailo/Game-Launcher.git
cd Game-Launcher/android

# Build clean optimized release and debug APKs
./gradlew clean assembleRelease assembleDebug
```

Output APKs:
- **Release APK**: `android/app/build/outputs/apk/release/Game_Space.apk`
- **Debug APK**: `android/app/build/outputs/apk/debug/Game_Space_Debug.apk`

---

## 🤝 Developer & Community

<div align="center">

[![GitHub Developer](https://img.shields.io/badge/GitHub-willygailo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo)
&nbsp;&nbsp;&nbsp;&nbsp;
[![Facebook Profile](https://img.shields.io/badge/Facebook-Willy%20Jr%20Carnasa%20Gailo-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

<br/>

⭐ **If you love this project, please give it a Star on GitHub!** ⭐

</div>

---

## 📄 License

This project is open-source under the **MIT License** — see the [LICENSE](LICENSE) file for details.
