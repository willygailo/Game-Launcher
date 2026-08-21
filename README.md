<div align="center">

<img src="android/app/src/main/res/drawable/hero_banner.gif" alt="Game Launcher PRO Hero Banner" width="100%" />

# ⚡ GAME LAUNCHER PRO — ULTIMATE GAMING SUITE ⚡
### *Next-Gen 185 FPS Display Lock • In-Game ART-Level Hardware Spoofing • Zero-Root Shizuku Engine*

[![Release](https://img.shields.io/badge/Release-v16.0.9--PRO-00F0FF?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo/Game-Launcher/releases/latest)
[![Android Support](https://img.shields.io/badge/Dedicated-Android%2012--16%20(API%2031--36)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#-system-requirements)
[![Zero Root](https://img.shields.io/badge/Shizuku-Zero--Root%20UID%202000-7B2CBF?style=for-the-badge&logo=android&logoColor=white)](#-privilege-layer)
[![Build Tool](https://img.shields.io/badge/Java-17%20%7C%20Gradle%208.13-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#-building-from-source)
[![License](https://img.shields.io/badge/License-MIT-FFB703?style=for-the-badge)](#-license)

<br/>

[📥 **Download Latest APK (v16.0.9-PRO)**](https://github.com/willygailo/Game-Launcher/releases/latest) • [✨ **Core Capabilities**](#-core-capabilities) • [🎮 **Supported Games**](#-supported-esports-titles) • [🏗️ **Architecture**](#-system-architecture) • [🚀 **Installation & Setup**](#-installation--setup)

<br/>

---

</div>

## 🌟 Executive Overview

**Game Launcher PRO** transforms your Android device into a dedicated, low-latency flagship gaming machine. Engineered directly on top of the **Shizuku Privileged IPC Engine (UID 2000)**, it unlocks hardware display overrides, eliminates thermal throttling governors, synchronizes touch polling frequencies, and dynamically injects competitive in-game configurations—**completely non-root and zero IAP**. Online anti-cheat compatibility is never guaranteed — see [Security & Risk Transparency](#-security--risk-transparency).

```
  ⚡ 185Hz Extreme ROG Mode  │  🎭 5-Layer Flagship Spoofer  │  🎯 1000Hz Touch Polling  │  🔒 Zero-Root Shizuku Engine
```

---

## ✨ Core Capabilities

### ⚡ 1. Extreme Display & Refresh Rate Engine
* **SurfaceFlinger Binder Synchronization**: Enforces hardware screen refresh rates up to **185Hz, 165Hz, 144Hz, 120Hz, and 90Hz** by overriding system display modes.
* **Android 14–16 Game Mode API**: Automates `cmd game mode performance` and configures target downscaling & framerate schedulers.
* **WindowManager Display Lock**: Neutralizes dynamic refresh rate stepping to prevent frame drops in heated team fights.

### 🎭 2. 5-Layer Flagship Hardware Identity Spoofer
* **Elite Device Signatures**:
  * 🔴 **ASUS ROG Phone 9 Pro** *(Snapdragon 8 Elite / 24GB RAM / 185Hz)*
  * 🌌 **Samsung Galaxy S26 Ultra** *(Snapdragon 8 Elite / Adreno 840)*
  * ⚡ **REDMAGIC 10 Pro+** *(185Hz eSports Edition / Liquid Cooling Profile)*
  * ⚡ **Vivo iQOO 15 Pro** *(Dimensity 9400 / Immortalis-G925)*
  * 🐉 **Xiaomi 15 Ultra** *(Vulkan Ultra Gaming Profile)*
  * 🍏 **Apple A18 Pro / iPad Pro M4** *(High-tier graphics unlocking)*
* **Storage & ProcFS Virtualization**: Masks `/proc/cpuinfo` and `/proc/meminfo` to seamlessly bypass title-level hardware whitelists.

### 🧬 2b. In-Game ART-Level Spoofer — LSPosed Module (Root)
* **True in-process spoofing**: The same APK doubles as an LSPosed module that hooks the game's **own process** at ART level — `Build.*` fields, `SystemProperties.get()`, `Runtime.totalMemory()`, `ActivityManager.getMemoryInfo()`, `glGetString(GL_RENDERER/VENDOR/VERSION)`, `/proc/cpuinfo`, `/proc/meminfo`, `/proc/version`, IMEI/MEID/IMSI/SIM serial, ANDROID_ID, and WebView User-Agent are all masked **inside the game**, where anti-cheat actually checks.
* **Zero file tampering**: No game config files are touched when the module is active — the LSPosed path replaces the file-injection layers, removing the config-file ban vector.
* **Auto-scoped to supported titles**: Hooks apply only to the supported games in the registry (or every app via the `spoof_all_apps` toggle) — system processes are never hooked.
* **Live config sync**: The profile selected in the launcher is read in-game via LSPosed `XSharedPreferences` (world-readable safe-zone), so switching profiles in the app applies on the next game launch — no file drops needed.

### 🎯 3. Zero-Delay Touch & Esports Input Engine
* **1000Hz Fast Touch Polling**: Dramatically minimizes input lag and touch deadzones for millisecond-precision responsiveness.
* **1000Hz Gyroscope Calibration**: Stabilizes micro-jitter and optimizes sensor sampling curves for competitive shooters.
* **AOT Speed Pre-compilation**: Eliminates runtime JIT micro-stutter by compiling DEX bytecode ahead-of-time.

### 🗂️ 4. Privileged File Management & In-Memory Patching
* **Full CRUD Game Storage Access**: Seamlessly read, write, backup, and restore protected configuration files in `/data/data/<package>/` and `/sdcard/Android/data/<package>/`.
* **Atomic Binary Patching**: Safely injects memory offsets into Unreal Engine `.sav` files (e.g. `Active.sav`), Unity player preferences, and FastFlags.

### 📡 5. Network & Low-Jitter Packet Prioritizer
* **Wi-Fi Low-Latency Mode**: Locks Wi-Fi chips into low-jitter gaming state.
* **TCP Buffer Tuning**: Optimizes network buffers for stable ping and reduced packet loss.
* **Ultra-Fast Gaming DNS**: Resolves connection routes with prioritized routing servers.

### 🛠️ 6. Cyber SetEdit & Terminal Suite
* **Real-Time Property Injector**: Modify Android `system`, `secure`, and `global` table variables with persistent boot-time locking.
* **Shell Script Preset Runner**: Execute custom performance scripts directly through the integrated Cyber Terminal.
* **Customizable Floating HUD**: Real-time FPS, RAM, battery temperature, and profile switching overlay.

---

## 🎮 Supported Esports Titles

| Game Title | Category | Unlocked Capabilities |
| :--- | :---: | :--- |
| **PUBG Mobile / BGMI** *(All Versions)* | Battle Royale | 90/120/185 FPS `Active.sav` unlock, Multithreaded Vulkan RHI, Ultra MSAA, 1000Hz Gyro |
| **Mobile Legends: Bang Bang** | MOBA | 120/165/185 FPS Ultra mode, Drone FOV scaling, Ultra Outline, Super Sampling, Touch Boost |
| **Call of Duty: Mobile & Warzone** | FPS | 120/144/185 FPS Max, Real-time dynamic shadows, Zero deadzone touch, Sensitivity curve sync |
| **Free Fire & Free Fire MAX** | Battle Royale | 120/185 FPS Unlock, 1000Hz Touch polling, Aim response acceleration, Jitter reduction |
| **Genshin Impact & Wuthering Waves** | Action RPG | Max Graphics whitelist override, Uncapped 120 FPS, Vulkan backend routing, Camera distance |
| **Arena Breakout** | Tactical FPS | 120/144 FPS high-tier unlocked, Thermal throttle bypass, Audio footprint enhancer |
| **Blood Strike** | FPS | 120/165 FPS high refresh mode, Low latency touch, Memory compaction |
| **CarX Street** | Racing | Ultra graphic profile, 120/144 FPS uncapped renderer, Shading quality enhancement |
| **Farlight 84** | Hero Shooter | 120/165/185 FPS Solarland graphics engine, Recoil stabilization, Fast touch response |
| **Valorant Mobile / Project C** | Tactical FPS | 120/144/185 FPS UE4 CVars, 1000Hz touch & gyro tuning, Zero-delay crosshair stabilizer |
| **League of Legends: Wild Rift** | MOBA | 120 FPS Extreme preset, Dynamic resolution scaling off, Zero-delay input pipeline |
| **Standoff 2** | FPS | 120/144/165/185 FPS mode unlock, Fast touch sync, Jitter buffer reduction |
| **Supercell (Brawl Stars / Clash)** | Competitive | 120/144 FPS unlocked refresh rate, Low-latency touch response, Frame pacing stabilizer |
| **Roblox** | Sandbox | FastFlags 185 FPS scheduler, Vulkan rendering backend, Uncapped memory allocation |
| **Honor of Kings** | MOBA | 120/185 FPS Extreme mode, Ultra resolution preset, Input acceleration |

---

## 🏗️ System Architecture

```
Game-Launcher/
├── android/                             # Android Application Workspace
│   ├── app/
│   │   ├── src/main/aidl/              # Shizuku IUserService IPC definitions
│   │   ├── src/main/assets/shizuku/    # Bundled rish binary & dex runtime assets
│   │   ├── src/main/java/com/gamebooster/app/
│   │   │   ├── booster/                # Audio, GPU driver, and network latency engines
│   │   │   ├── config/                 # Per-game configuration patchers (PUBG, MLBB, CODM, etc.)
│   │   │   ├── core/                   # Threading executors, JS interface & profiles
│   │   │   ├── device/                 # Hardware & display capability detectors
│   │   │   ├── engine/                 # MasterOptimizationEnforcer & CommandExecutor
│   │   │   ├── games/                  # App scanning, registries, and launch helpers
│   │   │   ├── gamespace/              # Auto game monitoring, DND, and cache cleaner
│   │   │   ├── overlay/                # Floating HUD indicators & Crosshair overlay
│   │   │   ├── services/               # Background monitoring & boot receivers
│   │   │   ├── shizuku/                # Shizuku Binder communication & privileged file manager
│   │   │   ├── spoofer/                # 5-Layer HardwareMaskEngine & brand profiles
│   │   │   │   └── lsposed/             # LSPosed module: in-game ART-level hooks (Build, props, RAM, GPU, /proc, identity)
│   │   │   ├── terminal/               # Cyber Terminal engine & script management
│   │   │   ├── tweaks/                 # System-level performance parameter controllers
│   │   │   └── ui/                     # Cyberpunk design system, fragments & adapters
│   │   ├── src/main/assets/xposed_init # LSPosed module entry point (legacy API 82)
│   │   ├── src/main/res/               # High-contrast cyber drawables, layouts & tokens
│   │   └── libs/XposedBridgeApi-82.jar # Vendored official Xposed API (compileOnly, not on Maven Central)
│   └── build.gradle                    # Gradle Build Configuration (API 36 / Java 17)
├── platform-tools-latest-linux/         # Bundled official Android SDK Platform Tools (ADB/Fastboot)
├── shizuku/                             # Standalone Shizuku shell toolkit (rish & rish_shizuku.dex)
├── tools/                               # Automated PC setup & Shizuku activation scripts
│   ├── activate_shizuku.sh             # One-click Shizuku service starter via ADB
│   ├── grant_permissions.sh            # Privileged permission granter for Game Launcher Pro
│   └── setup_device.sh                 # All-in-one onboarding and installation tool
└── README.md                            # Documentation & Release Guide
```

---

## 🚀 Installation & Setup

### 📱 Method 1: On-Device Quick Start (No PC Needed)

1. **Download APK**:
   * Grab the latest [**`Game_Space.apk`**](https://github.com/willygailo/Game-Launcher/releases/latest) from Releases.
2. **Activate Shizuku**:
   * Install [**Shizuku**](https://shizuku.rikka.app/) from Google Play or GitHub.
   * Start Shizuku via **Wireless Debugging** in Android Developer Options.
3. **Launch & Boost**:
   * Open **Game Launcher PRO**, grant Shizuku privileged access when prompted.
   * Select your installed game, configure your optimization profile, and launch!

---

### 💻 Method 2: PC / ADB Automated Setup (One-Click)

Connect your Android device to your computer with **USB Debugging enabled**, then run:

```bash
# 1. Run all-in-one setup (Installs APK, grants permissions, and activates Shizuku)
./tools/setup_device.sh

# Or run individual automated tasks:
./tools/activate_shizuku.sh    # Starts Shizuku privileged service via ADB
./tools/grant_permissions.sh   # Grants WRITE_SECURE_SETTINGS, DUMP, PACKAGE_USAGE_STATS
```

---

### 🧬 Method 3: In-Game Hardware Spoofing (LSPosed Module — Root)

The built-in **LSPosed module** masks the device identity **inside the game process itself** (ART-level hooks — the same APK doubles as the module):

1. **Requirements**: A rooted device (Magisk/KernelSU) with **LSPosed** (Zygisk/Riru) installed and enabled.
2. **Install & activate**:
   * Install the APK, open it once (writes the spoof profile bridge), then reboot.
   * Open **LSPosed Manager → Modules → Game Booster** → enable it.
   * Set **Scope**: check the games you want spoofed (or rely on the `spoof_all_apps` toggle in the launcher).
3. **Select your profile** in the launcher (e.g. ROG Phone 9 Pro, Galaxy S26 Ultra) — it applies automatically in-game on the next launch.
4. **Verify**: LSPosed Manager → Logs shows `SpoofModule active for <game> -> <profile>`.
   * The launcher's SYSTEM ENGINE chip turns **🧬 LSPOSED MODULE ACTIVE** when the module is detected.
   * When the module is active, the file-injection layers are automatically bypassed — no game files are touched.

> 💡 **Note**: Shizuku (Methods 1–2) covers display, settings, permissions, and system tweaks on **any** device. The LSPosed module is **root-only** and covers the *in-process* identity checks that Shizuku cannot reach.

---

## 🛠️ Building from Source

### Prerequisites:
- **JDK 17** or **JDK 21** configured (`JAVA_HOME`).
- **Android SDK Build Tools** (API 36).

```bash
# 1. Clone the repository
git clone https://github.com/willygailo/Game-Launcher.git
cd Game-Launcher/android

# 2. Build clean release and debug APKs
./gradlew clean assembleRelease assembleDebug
```

The compiled binaries will be output to:
- **Release APK**: `android/app/build/outputs/apk/release/Game_Space.apk`
- **Debug APK**: `android/app/build/outputs/apk/debug/Game_Space_Debug.apk`

---

## 🔒 Security & Risk Transparency

> **Honest summary:** Game Launcher PRO runs **zero-root via Shizuku** for
> system-level control and — on rooted devices — ships an **optional LSPosed
> module** that hooks supported game processes at ART level to mask device
> identity *in-process*. Both paths rewrite system display parameters, patch
> game configuration files, and spoof device identity — all of which can be
> detected by anti-cheat systems. **Xposed/LSPosed presence itself is a
> well-known detection signal for kernel-level anti-cheat (e.g. Tencent ACE,
> used by PUBG Mobile / CoD Mobile / Honor of Kings).** No tool can honestly
> promise "100% safe" against online anti-cheat ecosystems. Use at your own
> risk on accounts you care about.

| Feature | What it changes | Risk | Why |
| :-- | :-- | :-- | :-- |
| Refresh-rate / FPS overrides | Android display & Game Mode parameters | Low | Standard system APIs; may be limited by panel hardware (e.g. a 60Hz panel will not run 185Hz) |
| Device identity spoofer | `/proc` masks, build props, device fingerprints | **High — known ban vector** | Anti-cheat flags impossible hardware (e.g. Apple SoC on a Snapdragon device). The app now blocks provable mismatches via `SpoofSanityChecker` (Phase 2.4), but this cannot guarantee safety |
| LSPosed module (in-game hooks) | ART-level hooks inside game process: Build, SystemProperties, RAM, GL, `/proc`, IMEI, ANDROID_ID | **Very High — framework presence detectable** | Requires root. Xposed/LSPosed framework presence is detected by kernel-level anti-cheat (Tencent ACE, etc.) and can trigger immediate flags. Only enable for games you accept losing access to |
| Game config patching (`Active.sav`, FastFlags, CFG) | Per-game config files | Medium–High | Config modifications are detectable; devs may reset, integrity-check, or flag modified configs |
| Network prioritizer | Wi-Fi/cellular QoS settings | Low | Standard Android knobs |

### FAQ

- **Shizuku died / permissions revoked — what now?** Open Shizuku again
  (Settings → Shizuku & System Permissions → Grant) and hit **Master
  Enforce**. The app never requires root; everything runs via UID 2000.
- **My FPS override is not applying.** Open **Settings → Diagnostics →
  REFRESH** and check `Shizuku/root available` and `AIDL service connected`;
  if either is `false`, Shizuku is not running. If both are `true`, your panel
  may not support the target Hz (Check `verifyEnforcementStatus()` output).
- **The game reset my config.** Known behavior: several titles reset or
  integrity-check configs on update or launch (flagged per-game on the Games
  screen — e.g. CoD Mobile, PUBG Mobile, Genshin). Re-apply after updates.
- **The LSPosed module is not applying.** Enable **Game Booster** in LSPosed
  Manager → Modules, set its scope to the games, then force-stop the games and
  relaunch. Check LSPosed Manager → Logs for `SpoofModule active for <game>`.
  The module is root-only — it will never activate on a Shizuku-only device.
- **I need help / want to report a bug.** Export diagnostics: Settings →
  Diagnostics → **EXPORT** — the share sheet gives a text snapshot with app
  version, enforcement status, spoof state, and any captured crash log.
- **Is my account at risk?** Yes, potentially — see the risk table above.
  Spoofing or patching files in online games can lead to bans. This project is
  provided as-is (MIT) with no warranty; the developer is not responsible for
  loss of access to accounts.

---

## 🤝 Connect & Developer

<div align="center">

[![GitHub Profile](https://img.shields.io/badge/GitHub-willygailo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/willygailo)
&nbsp;&nbsp;
[![Facebook Profile](https://img.shields.io/badge/Facebook-Willy%20Jr%20Carnasa%20Gailo-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://web.facebook.com/https.willy.jr.carnasa.gailo2026.2027)

⭐ **If you find this project helpful, please consider giving it a star on GitHub!** ⭐

</div>

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
