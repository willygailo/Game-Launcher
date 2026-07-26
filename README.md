# 🎮 GAME SPACE — Ultimate Android Gaming Optimizer & FPS Unlocker

**GAME SPACE** is a high-performance Flutter Android application engineered for system-level gaming optimizations, display refresh rate locking (90Hz / 120Hz / 144Hz), graphics unlocking, CPU/GPU scheduling, touch sampling rate enhancements, and network latency tuning across **all Android chipsets and OEM brands** (Xiaomi, Infinix, Tecno, Samsung, Realme, OnePlus, etc.).

---

## ⚡ Execution Engines (Root & Non-Rooted Shizuku ADB)

GAME SPACE features a **3-Tiered Execution Engine** designed to deliver maximum performance on both rooted and non-rooted Android devices:

| Execution Engine | Requirements | Capabilities |
| :--- | :--- | :--- |
| **👑 Root Mode (`su`)** | Magisk / KernelSU / APatch elevated access | Full system property tuning (`setprop`), `sysfs` kernel CPU/GPU governors, touch sampling rate, TCP network buffer size + ADB settings commands. |
| **⚡ Shizuku ADB Mode (`shizuku`)** | Shizuku app active via Wireless ADB or USB ADB | **Full Non-Rooted Support**: Lock Max Refresh Rate (90Hz/120Hz/144Hz), Force 4x MSAA graphics, Vulkan GPU rendering backend, Android 12+ Game Mode API performance overrides, and Thermal Throttling bypass (`cmd thermal`). |
| **ℹ️ Read-Only / Info Mode** | Standard non-rooted device without Shizuku | Displays live system metrics, hardware specs, thermal status, battery percentage, and interactive setup instructions. |

---

## 🔥 Key Optimization Modules

- **⚡ Hz & FPS Unlocker**:
  - Lock hardware refresh rate to **60Hz, 90Hz, 120Hz, 144Hz, or Maximum Supported Display Mode** via `peak_refresh_rate`, `min_refresh_rate`, and `user_refresh_rate`.
  - Android 12+ Game Mode API integration (`cmd game mode performance`, `cmd game set --fps`).
- **🧊 Thermal & Throttling Bypass**:
  - Overrides system thermal throttling caps (`cmd thermal override-status 0`) and disables Low Power Mode during gaming sessions.
- **🎨 Graphics & GPU Unlocking**:
  - Force 4x MSAA in OpenGL ES 2.0+ games (`debug.egl.force_msaa=1`).
  - Force Vulkan vs Skia GL HWUI graphics rendering pipeline (`debug.hwui.renderer=vulkan`).
  - Force SurfaceFlinger GPU UI composition (`debug.sf.hw=1`).
- **🎯 Touch Sampling & Latency Tuning**:
  - Boost touch input sampling rates up to 300Hz (`windowsmgr.max_events_per_sec=300`).
  - Disable scrolling cache delay overhead (`persist.sys.scrollingcache=3`).
- **🌐 Network Ping & Doze Optimizer**:
  - Gaming TCP buffer size optimization (`net.tcp.buffersize.wifi`).
  - Extends Wi-Fi background scan intervals (`wifi.supplicant_scan_interval=180`) to eliminate ping spikes.
  - Primary & Secondary Google DNS resolvers (`8.8.8.8` / `8.8.4.4`).

---

## 🛠️ Architecture Overview

Built using **Flutter Clean Architecture** + **BLoC/Cubit**:

```
lib/
├── core/                  # Core design system, router, theme, and native services
│   ├── platform/          # RootCommandService, ShizukuService, HzFpsService
│   ├── router/            # GoRouter navigation rules
│   └── theme/             # Dark neon glassmorphic design tokens
├── features/              # Feature modules (Domain, Data, Presentation)
│   ├── cpu_tweaks/        # CPU governor and heap tuning
│   ├── gpu_tweaks/        # GPU composition & graphics unlocking
│   ├── hz_fps_tweaks/     # Refresh rate lock & Game Mode API
│   ├── home/              # Hero dashboard & live performance gauges
│   ├── network_tweaks/    # Wi-Fi scan interval & TCP buffer tuning
│   ├── performance/       # Real-time CPU, RAM, Battery gauges
│   ├── permissions/       # Root & Shizuku ADB status manager
│   ├── profiles/          # Custom gaming preset profiles
│   ├── settings/          # Locale and theme preferences
│   └── touch_tweaks/      # Touch sampling & fling velocity controls
└── l10n/                  # Multi-language localization files
```

### Native Android Layer (`android/app/src/main/kotlin/com/gamespace/app/`)
- `ShellExecutor.kt`: Sanitized command input execution, exit code inspection, and SELinux validation.
- `ShizukuExecutor.kt`: Shizuku binder connection status, permission handling, and privileged process execution.
- `ShizukuChannel.kt`: `com.gamespace.app/shizuku` MethodChannel bridge.
- `HzFpsChannel.kt`: `com.gamespace.app/hz_fps` MethodChannel for display modes, FPS locking, and thermal override.
- `DeviceDetector.kt`: Multi-chipset detector (Qualcomm Snapdragon, MediaTek Helio/Dimensity, Unisoc Tiger, Samsung Exynos, Google Tensor, HiSilicon Kirin) with `/proc/cpuinfo` fallback.
- `BootReceiver.kt`: Re-applies runtime tweaks on `BOOT_COMPLETED`.

---

## 📱 1-Tap Shizuku Direct Connection (Non-Rooted Devices)

No manual ADB commands required! GAME SPACE connects directly to the Shizuku app API:
1. Open the Shizuku app on your device.
2. Launch **GAME SPACE**, go to **Permissions**, and tap **REQUEST SHIZUKU PERMISSION**.
3. Done! Shizuku connects directly to GAME SPACE to run 90Hz/120Hz/144Hz refresh rate locking, MSAA graphics, and thermal overrides automatically.

---

## 🌍 Supported Languages

- 🇬🇧 English (`en`)
- 🇫🇷 French (`fr`)
- 🇸🇦 Arabic (`ar`)
- 🇪🇸 Spanish (`es`)
- 🇮🇩 Indonesian (`id`)
- 🇰🇪 Swahili (`sw`)
- 🇵🇭 Filipino (`fil`)

---

## 🚀 Getting Started

### Build APK
```bash
flutter build apk --release
```

### Install via ADB
```bash
adb install build/app/outputs/flutter-apk/app-release.apk
```

---

## 👤 Developer Profiles

- **Facebook**: [Willy Jr Carnasa Gailo](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
- **GitHub**: [willygailo](https://github.com/willygailo)
