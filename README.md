
# 🎮 GAME SPACE — Pure Native Android Gaming Optimizer & FPS Unlocker (Java Only)

**GAME SPACE** is a **100% Pure Native Android Application (built in Java JDK 17)** engineered for system-level gaming optimizations, display refresh rate locking (90Hz / 120Hz / 144Hz), graphics unlocking (4x MSAA & Vulkan HWUI), CPU/GPU scheduling, touch sampling rate enhancements, network tuning, and thermal throttling bypass across **all Android chipsets and OEM brands** (Xiaomi, Infinix, Tecno, Samsung, Realme, OnePlus, etc.).

---

## 📌 Master Implementation Plan & Features

1. **⚡ 3-Tiered Execution Engine**:

   - **👑 Root Mode (`su`)**: Full system property tuning (`setprop`), `sysfs` kernel CPU/GPU governors, I/O scheduler, ZRAM/swappiness tuning, touch sampling rate, TCP network buffer size + ADB settings commands.
   - **⚡ Shizuku ADB Mode (`shizuku`)**: **Full Non-Rooted Support**: Lock Max Refresh Rate (90Hz/120Hz/144Hz), Force 4x MSAA graphics, Vulkan GPU rendering backend, Android 12+ Game Mode API performance overrides, and Thermal Throttling bypass (`cmd thermal`).
   - **ℹ️ Read-Only / Info Mode**: Standard non-rooted device fallback displaying live system metrics, hardware specs, thermal status, and battery percentage.
2. **📱 1-Tap Direct Shizuku Connection**:

   - **0 Manual ADB Commands**: Connects directly to the Shizuku app API for non-rooted refresh rate locking and thermal overrides.
   - **Automatic Self-Permission Granting**: Auto-grants `WRITE_SECURE_SETTINGS`, `WRITE_SETTINGS`, and `PACKAGE_USAGE_STATS` via Shizuku binder.
3. **🚀 Hz & FPS Unlocker Module**:

   - Lock hardware display refresh rate to **60Hz, 90Hz, 120Hz, 144Hz, or Maximum Supported Display Mode** via `peak_refresh_rate`, `min_refresh_rate`, and `user_refresh_rate`.
   - Android 12+ Game Mode API integration (`cmd game mode performance`, `cmd game set --fps`).
4. **🧊 Thermal & Throttling Bypass Module** *(Root)*:

   - Overrides system thermal throttling caps (`cmd thermal override-status 0`) and disables Low Power Mode during gaming sessions.
   - Direct `sysfs` thermal zone read/write (`/sys/class/thermal/thermal_zone*/mode`) for supported chipsets.
5. **🎨 Graphics & GPU Unlocking Module** *(Root)*:

   - Force 4x MSAA in OpenGL ES 2.0+ games (`debug.egl.force_msaa=1`).
   - Force Vulkan vs Skia GL HWUI graphics rendering pipeline (`debug.hwui.renderer=vulkan`).
   - Force SurfaceFlinger GPU UI composition (`debug.sf.hw=1`).
   - GPU frequency governor lock (Adreno `/sys/class/kgsl/kgsl-3d0/devfreq/governor`, Mali `/sys/class/misc/mali0/device/dvfs_governor`).
6. **🧠 CPU Governor & Scheduler Module** *(Root — NEW)*:

   - Per-core CPU governor override (`performance`, `schedutil`, `ondemand`) via `/sys/devices/system/cpu/cpu*/cpufreq/scaling_governor`.
   - Min/max frequency pinning per cluster (little.big.prime) via `scaling_min_freq` / `scaling_max_freq`.
   - I/O scheduler tuning (`/sys/block/*/queue/scheduler` → `noop`/`cfq`/`bfq`/`kyber`).
   - Entropy/idle tuning (`/sys/module/lpm_levels`, `/sys/power/cpuidle`) for reduced input latency.
7. **👆 Touch Sampling & Input Latency Module** *(Root — NEW)*:

   - 300Hz zero-delay touch sampling override (`echo 1 > /sys/class/touch/touch_dev/game_mode` or vendor-specific node, auto-detected).
   - Touch report rate + palm rejection tuning per OEM (Xiaomi `touchpanel`, MediaTek `mtk_tpd`, Samsung `sec_touchscreen`).
   - Zero scroll cache delay (`persist.sys.scrollingcache=3`).
8. **🌐 Network & Latency Module** *(Root — NEW)*:

   - TCP buffer size tuning (`net.tcp.buffersize.*`, `/proc/sys/net/ipv4/tcp_rmem`, `tcp_wmem`).
   - TCP congestion control algorithm override (`bbr`, `cubic`) via `/proc/sys/net/ipv4/tcp_congestion_control`.
   - DNS pre-resolution + low-latency Wi-Fi power save disable (`svc wifi lowpowermode disable` equivalent sysfs write).
9. **💾 RAM / ZRAM & Background Kill Module** *(Root — NEW)*:

   - Swappiness tuning (`/proc/sys/vm/swappiness`), ZRAM disksize resize.
   - LMK (Low Memory Killer) minfree threshold tuning for aggressive/relaxed background app retention.
   - One-tap background process trim (`am kill-all` equivalent via ActivityManager reflection).
10. **🎮 Preset Gaming Profiles**:

    - **2D & Pixel Games Ultra Smooth**: 300Hz zero-delay touch sampling, 2D GPU acceleration, and zero scroll cache delay.
    - **PUBG Mobile / 3D FPS Extreme**: Full SurfaceFlinger GPU composition, 300Hz touch sampling, CPU performance governor lock, low latency network profile.
    - **Battery Saver Gaming**: Balanced governor, thermal headroom preserved, 90Hz cap.

---

## 📂 Correct Pure Native Java Directory & File Structure Map

Each module below has its **own dedicated file** (channel + fragment + layout) — no shared/merged logic files, so root-mode tweaks stay isolated and independently testable per subsystem.

```
Game_Launcher_Pro/
├── README.md                                          # Complete project plans & documentation
├── assets/                                            # Application icons & graphics
└── android/                                           # Pure Native Android Java Project
    ├── build.gradle                                   # Standard Android root buildscript (AGP 8.7.3)
    ├── settings.gradle                                # Standard Android plugin settings (No Flutter)
    ├── gradle.properties                              # Java JDK 17 configuration
    └── app/
        ├── build.gradle                               # Pure Java Android app buildscript (compileSdk 35)
        └── src/main/
            ├── AndroidManifest.xml                    # Pure Native Android Manifest
            ├── res/                                   # Native Android Resources & XML Layouts
            │   ├── layout/
            │   │   ├── activity_main.xml              # CoordinatorLayout + FragmentContainerView + BottomNavigationView
            │   │   ├── fragment_home.xml              # Dashboard XML with live gauges & status banner
            │   │   ├── fragment_hz_fps.xml             # 60Hz - 144Hz refresh rate locking & thermal XML
            │   │   ├── fragment_cpu_governor.xml       # CPU governor / frequency / scheduler tuning XML (NEW)
            │   │   ├── fragment_gpu_tweaks.xml         # GPU governor & rendering pipeline XML (NEW)
            │   │   ├── fragment_touch_latency.xml      # Touch sampling rate & input latency XML (NEW)
            │   │   ├── fragment_network_tweaks.xml     # TCP/network latency tuning XML (NEW)
            │   │   ├── fragment_ram_zram.xml           # RAM/ZRAM/LMK tuning XML (NEW)
            │   │   ├── fragment_profiles.xml           # Gaming preset profile cards XML
            │   │   └── fragment_permissions.xml        # 1-Tap Shizuku & Root permissions XML
            │   ├── menu/
            │   │   └── bottom_nav_menu.xml             # BottomNavigationView menu items
            │   └── values/
            │       ├── colors.xml                      # Cyberpunk dark neon colors
            │       └── strings.xml                     # Multi-language strings
            └── java/com/gamespace/app/                 # Pure Java Source Code (JDK 17)
                ├── MainActivity.java                   # Pure Java AppCompatActivity & Fragment Manager
                ├── ui/
                │   ├── HomeFragment.java               # Dashboard & Live Hardware Metrics Fragment
                │   ├── HzFpsFragment.java              # Hz & FPS Locking & Thermal Bypass Fragment
                │   ├── CpuGovernorFragment.java        # CPU governor/scheduler tuning Fragment (NEW)
                │   ├── GpuTweaksFragment.java          # GPU rendering/governor tuning Fragment (NEW)
                │   ├── TouchLatencyFragment.java       # Touch sampling/input latency Fragment (NEW)
                │   ├── NetworkTweaksFragment.java      # Network/TCP latency Fragment (NEW)
                │   ├── RamZramFragment.java            # RAM/ZRAM/background kill Fragment (NEW)
                │   ├── ProfilesFragment.java           # 2D & 3D Gaming Profiles Fragment
                │   └── PermissionsFragment.java        # Shizuku Binder & Root Permissions Fragment
                ├── receivers/
                │   └── BootReceiver.java               # Pure Java BOOT_COMPLETED broadcast receiver
                ├── utils/
                │   ├── ShizukuExecutor.java            # Pure Java Shizuku Binder API (Shizuku.newProcess)
                │   ├── ShellExecutor.java              # Pure Java ProcessBuilder su shell executor
                │   └── DeviceDetector.java             # Pure Java Snapdragon, MediaTek, Exynos detector
                └── channels/                            # Pure Java execution channel helpers (1 file per subsystem)
                    ├── ShizukuChannel.java              # Non-root Shizuku binder command routing
                    ├── HzFpsChannel.java                # Refresh rate lock commands
                    ├── ThermalChannel.java              # Thermal override commands (split out from RootCommandChannel)
                    ├── CpuGovernorChannel.java          # CPU governor / freq / I/O scheduler root commands (NEW)
                    ├── GpuTweaksChannel.java            # GPU governor / rendering pipeline root commands (NEW)
                    ├── TouchLatencyChannel.java         # Touch sampling rate root commands (NEW)
                    ├── NetworkTweaksChannel.java        # TCP buffer / congestion control root commands (NEW)
                    ├── RamZramChannel.java              # Swappiness / ZRAM / LMK root commands (NEW)
                    ├── RootCommandChannel.java          # Shared su session + generic setprop/sysfs writer
                    ├── PermissionChannel.java           # Root/Shizuku permission grant flow
                    ├── PerformanceChannel.java          # Preset profile orchestrator (calls the channels above)
                    ├── GameLibraryChannel.java          # Installed games detection & launch
                    ├── DeviceInfoChannel.java           # Live hardware metrics (CPU/GPU/RAM/battery/thermal)
                    └── MagiskExporterChannel.java       # Export tweaks as flashable Magisk module
```

### 🧩 Module → File Mapping (root-mode features, isolated per subsystem)

| Feature Area             | Fragment (UI)                          | Layout XML                      | Channel (Java logic)                                          |
| ------------------------ | -------------------------------------- | ------------------------------- | ------------------------------------------------------------- |
| Refresh Rate / FPS       | `HzFpsFragment.java`                 | `fragment_hz_fps.xml`         | `HzFpsChannel.java`                                         |
| Thermal Bypass           | `HzFpsFragment.java` (shared UI tab) | `fragment_hz_fps.xml`         | `ThermalChannel.java`                                       |
| CPU Governor/Scheduler   | `CpuGovernorFragment.java`           | `fragment_cpu_governor.xml`   | `CpuGovernorChannel.java`                                   |
| GPU Governor/Rendering   | `GpuTweaksFragment.java`             | `fragment_gpu_tweaks.xml`     | `GpuTweaksChannel.java`                                     |
| Touch Sampling/Latency   | `TouchLatencyFragment.java`          | `fragment_touch_latency.xml`  | `TouchLatencyChannel.java`                                  |
| Network/TCP Tuning       | `NetworkTweaksFragment.java`         | `fragment_network_tweaks.xml` | `NetworkTweaksChannel.java`                                 |
| RAM/ZRAM/LMK             | `RamZramFragment.java`               | `fragment_ram_zram.xml`       | `RamZramChannel.java`                                       |
| Preset Profiles          | `ProfilesFragment.java`              | `fragment_profiles.xml`       | `PerformanceChannel.java` (orchestrates all channels above) |
| Root/Shizuku Permissions | `PermissionsFragment.java`           | `fragment_permissions.xml`    | `PermissionChannel.java`                                    |

> Each channel owns **one** `sysfs`/`setprop`/`proc` surface only — this keeps root command sets independently maintainable and testable per chipset/OEM, instead of one large god-file handling everything.

---

## 🚀 Building the Pure Java Release APK

Navigate to the `android/` directory and compile with Gradle:

```bash
cd android
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew assembleRelease
```

### 📦 Output APK Location:

Your compiled release APK will be saved at:
`android/app/build/outputs/apk/release/app-release.apk`
