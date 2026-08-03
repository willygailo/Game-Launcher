# Graph Report - Game_Launcher_Pro  (2026-08-03)

## Corpus Check
- 66 files · ~130,687 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 649 nodes · 1415 edges · 33 communities (26 shown, 7 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 33 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8a117058`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Override
- TweakItem
- .executeSystemCommand
- CommandExecutor.java
- DeviceSpecModel
- Fragment
- .isSuccessOutput
- ShizukuManager
- FloatingOverlayService
- RefreshRateController
- HomeFragment.java
- HzFpsFragment.java
- 📌 Master Features
- WebDashboardFragment.java
- UserService
- GameInfoSpec
- gradlew
- shizuku-shell.js
- .enableUltraTouchResponse
- .executeSystemCommand
- CommandExecutor.java
- GameBoosterService.java
- CrosshairOverlayManager
- CommandResult
- EsportsAudioEnhancer
- .applyGameFpsPatch
- .performDeepGameCacheClean
- .trimMemoryAndCleanCache

## God Nodes (most connected - your core abstractions)
1. `PropResult` - 34 edges
2. `TweakItem` - 22 edges
3. `GameAppInfo` - 20 edges
4. `FloatingOverlayService` - 19 edges
5. `DeviceSpecModel` - 15 edges
6. `BaseManager` - 15 edges
7. `AutoGameMonitorService` - 15 edges
8. `SettingsFragment` - 15 edges
9. `TweaksAdapter` - 15 edges
10. `RefreshRateController` - 14 edges

## Surprising Connections (you probably didn't know these)
- `RefreshRateController` --references--> `PropertyResolver`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/core/DisplayCapabilitiesDetector.java → android/app/src/main/java/com/gamebooster/app/core/PropertyResolver.java
- `TweaksFragment` --implements--> `ShizukuStateListener`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksFragment.java → android/app/src/main/java/com/gamebooster/app/shizuku/ShizukuManager.java
- `SettingsFragment` --references--> `TweaksAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/SettingsFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksAdapter.java
- `TweaksFragment` --references--> `TweaksAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksAdapter.java
- `DeviceSpecModel` --references--> `ChipsetVendor`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/core/DeviceSpecModel.java → android/app/src/main/java/com/gamebooster/app/core/DeviceDetector.java

## Import Cycles
- None detected.

## Communities (33 total, 7 thin omitted)

### Community 0 - "Override"
Cohesion: 0.07
Nodes (18): BaseManager, CpuManager, DisplayManager, FileSystemManager, GlobalSettingsManager, GpuManager, Context, Override (+10 more)

### Community 1 - "TweakItem"
Cohesion: 0.06
Nodes (27): TweakCategory, ALL, CPU_GPU, NETWORK_LATENCY, SHIZUKU_SYSTEM, TOUCH_DISPLAY, TweakItem, Context (+19 more)

### Community 2 - ".executeSystemCommand"
Cohesion: 0.18
Nodes (8): CpuGovernorChannel, Context, PerformanceChannel, Profile, BALANCED, EXTREME_PERFORMANCE, PERFORMANCE, ThermalChannel

### Community 3 - "CommandExecutor.java"
Cohesion: 0.06
Nodes (39): Adapter, AppExecutors, Handler, GameAppInfo, Intent, GameLauncherHelper, Context, GameManagerRepository (+31 more)

### Community 4 - "DeviceSpecModel"
Cohesion: 0.09
Nodes (13): ChipsetVendor, EXYNOS, GENERIC, KIRIN, MEDIATEK, QUALCOMM, TENSOR, UNISOC (+5 more)

### Community 5 - "Fragment"
Cohesion: 0.07
Nodes (29): EngineUIHelper, TextView, GameSpaceDndManager, Context, Bundle, LayoutInflater, Nullable, Override (+21 more)

### Community 6 - ".isSuccessOutput"
Cohesion: 0.21
Nodes (4): GpuTweaksChannel, HzFpsChannel, NetworkTweaksChannel, CommandExecutor

### Community 7 - "ShizukuManager"
Cohesion: 0.07
Nodes (23): Context, PermissionChannel, ShizukuChannel, Context, ShizukuExecutor, Context, ShizukuManager, ShizukuStateListener (+15 more)

### Community 8 - "FloatingOverlayService"
Cohesion: 0.09
Nodes (20): AutoGameMonitorService, Context, Handler, IBinder, Intent, Notification, Nullable, Override (+12 more)

### Community 9 - "RefreshRateController"
Cohesion: 0.14
Nodes (9): DisplayCapabilitiesDetector, DisplayCaps, Context, Mode, EXACT, MIN, PEAK, USER (+1 more)

### Community 10 - "HomeFragment.java"
Cohesion: 0.28
Nodes (4): ShizukuUserServiceConnector, IUserService, ServiceConnection, UserServiceArgs

### Community 11 - "HzFpsFragment.java"
Cohesion: 0.28
Nodes (9): HzFpsFragment, Bundle, ImageView, LayoutInflater, Nullable, Override, Switch, View (+1 more)

### Community 12 - "📌 Master Features"
Cohesion: 0.13
Nodes (14): 🔒 1. 100% Non-Rooted Shizuku ADB Control, 🎯 2. Hardware Refresh Rate (Hz) & FPS Lock, 🎨 3. Graphics & GPU Engine Optimization, 👆 4. Touch Latency & Digitizer Sensitivity, 🧊 5. Thermal Throttling & PowerHAL Bypass, 🌐 6. Native JavaScript Bridge & Modular Web Scripts, 🚀 Building the APK, 👤 Developer & Contact (+6 more)

### Community 13 - "WebDashboardFragment.java"
Cohesion: 0.11
Nodes (15): GameBoosterJsInterface, Context, GameProfileAutoConfigurator, Context, OnAutoConfigListener, Bundle, LayoutInflater, Nullable (+7 more)

### Community 14 - "UserService"
Cohesion: 0.32
Nodes (3): Override, UserService, Stub

### Community 16 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 23 - ".enableUltraTouchResponse"
Cohesion: 0.25
Nodes (6): TouchLatencyChannel, BootReceiver, Context, Intent, Override, BroadcastReceiver

### Community 24 - ".executeSystemCommand"
Cohesion: 0.27
Nodes (6): DnsMode, CLOUDFLARE_1_1_1_1, GOOGLE_8_8_8_8, SYSTEM_DEFAULT, Context, NetworkOptimizer

### Community 26 - "GameBoosterService.java"
Cohesion: 0.36
Nodes (5): GameBoosterService, IBinder, Intent, Nullable, Override

### Community 27 - "CrosshairOverlayManager"
Cohesion: 0.44
Nodes (4): CrosshairOverlayManager, Context, View, WindowManager

### Community 29 - "EsportsAudioEnhancer"
Cohesion: 0.46
Nodes (3): EsportsAudioEnhancer, Context, Equalizer

## Knowledge Gaps
- **36 isolated node(s):** `QUALCOMM`, `MEDIATEK`, `EXYNOS`, `UNISOC`, `TENSOR` (+31 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `QUALCOMM`, `MEDIATEK`, `EXYNOS` to the rest of the system?**
  _36 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.06867088607594937 - nodes in this community are weakly interconnected._
- **Should `TweakItem` be split into smaller, more focused modules?**
  _Cohesion score 0.06394230769230769 - nodes in this community are weakly interconnected._
- **Should `CommandExecutor.java` be split into smaller, more focused modules?**
  _Cohesion score 0.05701592002961866 - nodes in this community are weakly interconnected._
- **Should `DeviceSpecModel` be split into smaller, more focused modules?**
  _Cohesion score 0.09475806451612903 - nodes in this community are weakly interconnected._
- **Should `Fragment` be split into smaller, more focused modules?**
  _Cohesion score 0.07111756168359942 - nodes in this community are weakly interconnected._
- **Should `ShizukuManager` be split into smaller, more focused modules?**
  _Cohesion score 0.06830601092896176 - nodes in this community are weakly interconnected._