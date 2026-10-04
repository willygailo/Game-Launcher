# Game Launcher

An Android game launcher for Android 14 and newer. It finds launchable games,
stores a per-game display preference, and starts games through Android's normal
launcher intent — with **deep system-level performance tuning** via Root/Shizuku.

## What it does

- Lists launchable games that Android makes visible to the app.
- Reads the display modes reported by Android and offers extended tiers up to **480 Hz**.
- Stores a per-game display preference; a game remains in control of its own
  graphics settings, frame rate, account, and files.
- **Enforces max refresh rate (60–480 Hz)** via Settings API + 8-layer privileged shell commands (Root/Shizuku).
- **Pins CPU governors per-cluster** (Prime/Big → performance, LITTLE → powersave), locks min/max frequencies, disables C-states.
- **Locks GPU clocks at max frequency** for Adreno, Mali, Xclipse, Tensor, Kirin, UNISOC with vendor-specific flags.
- **Optimizes WebView/Chromium** per device tier (Flagship: WebGPU + WASM threads/SIMD, Mid: WASM threads, Budget: stable GL).
- **Eliminates touch latency** via 1000 Hz digitizer overrides, InputFlinger resampling bypass, OEM gaming touch modes.
- **Stabilizes frame pacing** via SurfaceFlinger triple-buffer, Choreographer phase tuning, ART JIT warmup, 256 MB shader cache.
- Provides optional overlay and session-monitoring features when permissions are granted.
- Includes arm64-v8a, armeabi-v7a, and x86_64 native libraries built with 16 KB ELF segment alignment.

## Performance Channels

| Channel | Capability |
|---------|------------|
| `HzFpsChannel` / `MaxHzForceChannel` | 8-layer Hz enforcement (60–480 Hz), VRR disable, per-package Game Mode API, panel direct commands |
| `CpuGovernorChannel` | Per-cluster governors, freq pinning (min=max), C-state disable, uclamp=1024, CFS/WALT/EAS tuning |
| `GpuTweaksChannel` | GPU devfreq lock, Adreno/Mali/Xclipse/Tensor/Kirin/UNISOC extended flags, Game Driver opt-in |
| `WebViewBoosterChannel` | Tiered flags (Flagship/Mid/Budget), WebGPU, WASM SIMD/threads, DrDc, Graphite, V8 TurboShaft |
| `TouchLatencyChannel` | 1000 Hz digitizer, resampling elimination, OEM gaming touch, InputFlinger RT priority |
| `HwuiRenderAccelerator` | RenderThread RT FIFO 6, SkiaVK, Choreographer tuning, triple-buffer, ART speed-profile, 256 MB shader cache |
| `PerformanceChannel` | Unified profile selector (Extreme / Performance / Balanced) |
| `MemoryCleanerPro` / `RamZramChannel` | ZRAM tuning, swappiness=10, mlockall framebuffers, OOM protection |
| `AdvancedNetworkQosEngine` | Wi-Fi power save off, TCP BBR, DSCP EF QoS, DNS DoH |
| `ThermalChannel` | Thermal headroom monitoring, auto-throttle at 50°C, hard disable at 55°C |
| `SafetyGuard` | 2s thermal monitor, device compatibility blocklist (§8.2), boost arming/restore |
| `SnapshotSystem` | Pre-change snapshot of sysfs/settings/props with JSON serialize + rollback |
| `VerifyResult` verifiers | Read-back checks per channel (applied Hz, CPU governors, GPU lock, WebView flags) |
| `FrameMetricsCollector` | Frame time avg/stddev/p99, jank + big-jank counts from gfxinfo framestats (§7.1) |
| `BatteryEstimator` | Per-profile drain estimate, e.g. Extreme "+15-25% per hour" (§8.4) |

## Requirements

- Android 14 (API 34) through Android 16 (API 36)
- **Root (Magisk/KernelSU) or Shizuku** for full enforcement (Hz, CPU, GPU, WebView, Touch)
- A device-reported display mode for any explicit refresh-rate preference
- Optional: notification, overlay, usage-access, or modify-system-settings access for features that request them

> The app does not invent display modes, modify another app's files, force a
> game's FPS, spoof device identity, or use hidden shell commands **without explicit privilege grant**.
> All privileged operations require Root or Shizuku and are logged per-command.

## Build

```bash
cd android
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The debug APK is written to:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

To build a release, create `android/keystore.properties` with `storeFile`,
`storePassword`, `keyAlias`, and `keyPassword`. Release builds intentionally
do not fall back to the public debug signing key.

## Validation

```bash
cd android
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
zipalign -c -p -v 4 app/build/outputs/apk/debug/app-debug.apk
```

For device testing, install the debug APK on an Android 14, 15, and 16 device
or emulator, then confirm that the launcher discovers a game, accurately shows
its display modes, and launches it normally.

### Manual Verification (with Root/Shizuku)

```bash
# Verify applied Hz
adb shell settings get system peak_refresh_rate
adb shell settings get global peak_refresh_rate

# Verify CPU governors
adb shell "for p in /sys/devices/system/cpu/cpufreq/policy*; do cat \$p/scaling_governor; done"

# Verify GPU frequency lock
adb shell "cat /sys/class/kgsl/kgsl-3d0/devfreq/cur_freq"
adb shell "cat /sys/class/kgsl/kgsl-3d0/devfreq/min_freq"
adb shell "cat /sys/class/kgsl/kgsl-3d0/devfreq/max_freq"

# Verify WebView flags
adb shell cat /data/local/tmp/webview-command-line

# Verify touch rate
adb shell getevent -l /dev/input/event* | grep -i touch

# Frame pacing metrics
adb shell dumpsys gfxinfo <package_name> framestats
```

### Automated Device Validation

`tools/validate_boost.py` runs the same read-back checks over adb (device-farm
ready: `--json` output, exit codes 0 pass / 1 fail / 2 unavailable):

```bash
# Single check
tools/validate_boost.py hz --hz 165
tools/validate_boost.py cpu-gov --governor performance

# Per-game FPS unlock read-back (Game Mode API / device_config)
tools/validate_boost.py game-overlay --package com.mobile.legends --hz 120

# Everything (keep touching the screen during the touch check)
tools/validate_boost.py all --hz 120 --package com.mobile.legends --tier flagship

# Machine-readable
tools/validate_boost.py all --hz 120 --json --serial <device-serial>

# Device-free unit tests for the script's parsers
python3 -m unittest discover tools
```

Known device caveats are tracked in `docs/device-compatibility-matrix.json`
(mirrors the in-app `SafetyGuard` blocklist).

## Supported Hz Tiers

Standard gaming tiers: **60, 90, 120, 144, 165, 185, 240, 300, 360, 480 Hz**

The launcher requests the highest tier the panel supports, then enforces it via:
1. `Settings.System` API (when `MODIFY_SYSTEM_SETTINGS` granted)
2. `MaxHzForceChannel` 8-layer privileged commands (Root/Shizuku)
3. Per-game `cmd game set --fps <hz> <package>`
4. Panel/DRM direct (`SurfaceFlinger 1037`, `/sys/class/drm/card0/modes`)

## License

MIT