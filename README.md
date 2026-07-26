# 🎮 GAME SPACE — Ultimate Android Gaming Optimizer & FPS Unlocker

**GAME SPACE** is a high-performance system-level gaming optimization framework engineered for display refresh rate locking (90Hz / 120Hz / 144Hz), graphics unlocking (4x MSAA & Vulkan HWUI), CPU/GPU scheduling, touch sampling rate enhancements, and thermal throttling bypass across **all Android chipsets and OEM brands** (Xiaomi, Infinix, Tecno, Samsung, Realme, OnePlus, etc.).

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
- **🎮 2D & Pixel Games Profile**:
  - 300Hz zero-delay touch sampling and zero-lag gesture caching for 2D arcade, RPGs, and platformers.

---

## 📂 Full Directory & File Structure

```
Game_Launcher_Pro/
├── README.md                                          # Complete project documentation & guide
├── analysis_options.yaml                              # Strict Dart & Flutter static analysis rules
├── pubspec.yaml                                       # Project dependencies, assets, and assets declarations
├── test/                                              # Comprehensive automated unit test suite
│   └── features/
│       ├── hz_fps_tweaks/
│       │   └── hz_fps_tweaks_test.dart                # Unit tests for Hz & FPS state mutations
│       └── permissions/
│           └── permissions_test.dart                  # Unit tests for AppPermissions & ExecutionMode
├── android/                                           # Native Android Project & Shizuku Binder Bridge
│   ├── build.gradle                                   # Top-level Gradle configuration
│   ├── settings.gradle                                # Gradle plugins loader & dependency settings
│   ├── gradle.properties                              # JDK home (/usr/lib/jvm/java-17-openjdk-amd64)
│   ├── local.properties                               # Android SDK & Flutter paths
│   └── app/
│       ├── build.gradle                               # App module compileSdk 35 & Shizuku API dependencies
│       └── src/
│           └── main/
│               ├── AndroidManifest.xml                # ShizukuProvider registration & system permissions
│               └── kotlin/com/gamespace/app/
│                   ├── MainActivity.kt                # FlutterActivity & MethodChannel bridge registrar
│                   ├── BootReceiver.kt                # Re-applies performance tweaks on BOOT_COMPLETED
│                   ├── channels/
│                   │   ├── DeviceInfoChannel.kt       # Native hardware & CPU spec channel
│                   │   ├── GameLibraryChannel.kt      # Installed games scanner & launcher channel
│                   │   ├── GpuTweaksChannel.kt        # GPU composition & HWUI renderer channel
│                   │   ├── HzFpsChannel.kt            # Refresh rate locking & thermal bypass channel
│                   │   ├── MagiskExporterChannel.kt   # Magisk module generator channel
│                   │   ├── PerformanceChannel.kt      # Real-time CPU, RAM, Battery metrics channel
│                   │   ├── PermissionChannel.kt       # Root & system permission checker channel
│                   │   ├── RootCommandChannel.kt      # Shell command executor with Shizuku fallback
│                   │   └── ShizukuChannel.kt          # Shizuku API binder status & auto-granting channel
│                   └── utils/
│                       ├── DeviceDetector.kt          # Snapdragon, MediaTek, Exynos, Tensor chipset detector
│                       ├── ShellExecutor.kt           # Root shell command execution & SELinux sanitizer
│                       └── ShizukuExecutor.kt         # Native Shizuku process runner & auto-perm granter
├── lib/                                               # Clean Architecture Core & Feature Modules
│   ├── main.dart                                      # Application entry point
│   ├── app.dart                                       # MaterialApp, MultiBlocProvider, & GoRouter router
│   ├── injection.dart                                 # GetIt dependency injection registry
│   ├── core/
│   │   ├── constants/
│   │   │   ├── app_constants.dart                     # App branding and global constants
│   │   │   └── tweak_constants.dart                   # Sysfs paths, setprop keys, and tweak definitions
│   │   ├── platform/
│   │   │   ├── device_info_service.dart               # Platform channel for device specs
│   │   │   ├── game_library_service.dart              # Platform channel for installed games
│   │   │   ├── hz_fps_service.dart                    # Platform channel for Hz, FPS, and thermal status
│   │   │   ├── magisk_exporter_service.dart           # Platform channel for Magisk exports
│   │   │   ├── performance_service.dart               # Platform channel for hardware metrics
│   │   │   ├── permission_service.dart                # Platform channel for permission checks
│   │   │   ├── root_command_service.dart              # Platform channel for shell execution
│   │   │   └── shizuku_service.dart                   # Platform channel for Shizuku binder connection
│   │   ├── router/
│   │   │   └── app_router.dart                        # GoRouter navigation rules and page transitions
│   │   ├── theme/
│   │   │   ├── app_colors.dart                        # Cyberpunk dark neon palette
│   │   │   ├── app_theme.dart                         # ThemeData & component styles
│   │   │   └── app_typography.dart                    # Google Fonts typography tokens
│   │   └── widgets/
│   │       ├── glassmorphic_card.dart                 # Glassmorphic card UI component
│   │       ├── main_shell_page.dart                   # Bottom navigation shell layout
│   │       ├── neon_button.dart                       # Cyberpunk neon action button
│   │       ├── performance_gauge.dart                 # Circular hardware metric gauge
│   │       └── tweak_toggle_tile.dart                 # Interactive tweak switch list tile
│   ├── features/
│   │   ├── cpu_tweaks/                                # CPU Governors & Heap Tuning
│   │   ├── gpu_tweaks/                                # GPU Composition & Graphics Unlocking
│   │   ├── home/                                      # Hero Dashboard & Live Gauges
│   │   ├── hz_fps_tweaks/                             # Refresh Rate Locking & Game Mode API
│   │   ├── network_tweaks/                            # Wi-Fi Scan Interval & TCP Buffers
│   │   ├── performance/                               # Real-time CPU, RAM, Battery Monitor
│   │   ├── permissions/                               # Root & Shizuku ADB Permission Manager
│   │   ├── profiles/                                  # Gaming Preset Profiles (PUBG, 2D Games, Genshin)
│   │   ├── settings/                                  # Language & Theme Settings
│   │   └── touch_tweaks/                              # Touch Sampling Rate & Fling Velocity
│   └── l10n/                                          # Localization files (en, fr, ar, es, id, sw, fil)
└── web/                                               # Web / JavaScript App Hosting Shell
    ├── index.html                                     # Web application HTML5 shell
    ├── manifest.json                                  # Web PWA manifest
    └── favicon.png                                    # Web icon
```

---

## ☕ 🌐 Java & JavaScript / Web Tech Architecture

If you decide to migrate or extend the project to **Java + JavaScript / Web Tech**:

### ☕ Native Java Layer (`android/app/src/main/java/com/gamespace/app/`)
- Use **Java (JDK 17)** for native Android execution:
  - `ShizukuExecutor.java`: Implement `rikka.shizuku.Shizuku` binder connection and process management via `Shizuku.newProcess()`.
  - `RootExecutor.java`: Execute root commands via `java.lang.ProcessBuilder("su")`.
  - `SystemSettings.java`: Direct call to `android.provider.Settings.Secure.putString()` and `Settings.System.putInt()`.

### 🌐 JavaScript / Web Frontend (`web/` or `js/`)
- Use **JavaScript (Vanilla JS, React, or Vite)** inside an Android `WebView` interface or PWA:
  - `bridge.js`: JavaScript bridge communicating with Native Java via `@JavascriptInterface`:
  ```javascript
  // JavaScript to Java Bridge Call
  window.GameSpaceNative.applyTweak("peak_refresh_rate", "120.0");
  ```
  - `app.js`: JavaScript state management for profiles, Hz/FPS locks, and live gauge visualizers.

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

### Build Release APK
```bash
flutter build apk --release --target-platform android-arm64
```

### Install via ADB
```bash
adb install build/app/outputs/flutter-apk/app-release.apk
```

---

## 👤 Developer Profiles

- **Facebook**: [Willy Jr Carnasa Gailo](https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027)
- **GitHub**: [willygailo](https://github.com/willygailo)
