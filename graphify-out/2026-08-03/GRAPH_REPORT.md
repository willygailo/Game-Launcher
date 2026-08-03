# Graph Report - Game_Launcher_Pro  (2026-08-03)

## Corpus Check
- 63 files · ~129,642 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 615 nodes · 1340 edges · 30 communities (25 shown, 5 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 33 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9fd5a94f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Override
- TweakItem
- .executeSystemCommand
- CommandExecutor.java
- DeviceSpecModel
- Fragment
- .onCreateView
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
- .onCreateView
- GameProfileAutoConfigurator.java
- CommandExecutor.java
- DnsMode
- CrosshairOverlayManager
- CommandResult
- GameLibraryChannel

## God Nodes (most connected - your core abstractions)
1. `PropResult` - 34 edges
2. `TweakItem` - 22 edges
3. `GameAppInfo` - 20 edges
4. `FloatingOverlayService` - 19 edges
5. `DeviceSpecModel` - 15 edges
6. `BaseManager` - 15 edges
7. `SettingsFragment` - 15 edges
8. `TweaksAdapter` - 15 edges
9. `RefreshRateController` - 14 edges
10. `GamesAdapter` - 13 edges

## Surprising Connections (you probably didn't know these)
- `RefreshRateController` --references--> `PropertyResolver`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/core/DisplayCapabilitiesDetector.java → android/app/src/main/java/com/gamebooster/app/core/PropertyResolver.java
- `GamesFragment` --references--> `GameAppInfo`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/GamesFragment.java → android/app/src/main/java/com/gamebooster/app/games/GameAppInfo.java
- `TweaksFragment` --implements--> `ShizukuStateListener`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksFragment.java → android/app/src/main/java/com/gamebooster/app/shizuku/ShizukuManager.java
- `GamesFragment` --references--> `GamesAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/GamesFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/GamesAdapter.java
- `SettingsFragment` --references--> `TweaksAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/SettingsFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksAdapter.java

## Import Cycles
- None detected.

## Communities (30 total, 5 thin omitted)

### Community 0 - "Override"
Cohesion: 0.07
Nodes (18): BaseManager, CpuManager, DisplayManager, FileSystemManager, GlobalSettingsManager, GpuManager, Context, Override (+10 more)

### Community 1 - "TweakItem"
Cohesion: 0.05
Nodes (35): TweakCategory, ALL, CPU_GPU, NETWORK_LATENCY, SHIZUKU_SYSTEM, TOUCH_DISPLAY, TweakItem, Context (+27 more)

### Community 2 - ".executeSystemCommand"
Cohesion: 0.20
Nodes (8): CpuGovernorChannel, GpuTweaksChannel, Context, PerformanceChannel, Profile, BALANCED, EXTREME_PERFORMANCE, PERFORMANCE

### Community 3 - "CommandExecutor.java"
Cohesion: 0.07
Nodes (31): Adapter, GameAppInfo, Intent, GameLauncherHelper, Context, GameManagerRepository, Context, GameProfileAutoConfigurator (+23 more)

### Community 4 - "DeviceSpecModel"
Cohesion: 0.09
Nodes (13): ChipsetVendor, EXYNOS, GENERIC, KIRIN, MEDIATEK, QUALCOMM, TENSOR, UNISOC (+5 more)

### Community 5 - "Fragment"
Cohesion: 0.10
Nodes (28): EngineUIHelper, TextView, Bundle, LayoutInflater, Nullable, Override, TextView, View (+20 more)

### Community 6 - ".onCreateView"
Cohesion: 0.25
Nodes (3): HzFpsChannel, ThermalChannel, CommandExecutor

### Community 7 - "ShizukuManager"
Cohesion: 0.07
Nodes (20): AppExecutors, Handler, Context, ShizukuManager, ShizukuStateListener, Bundle, Override, MainActivity (+12 more)

### Community 8 - "FloatingOverlayService"
Cohesion: 0.11
Nodes (17): GameBoosterService, IBinder, Intent, Nullable, Override, FloatingOverlayService, Context, Handler (+9 more)

### Community 9 - "RefreshRateController"
Cohesion: 0.10
Nodes (11): DisplayCapabilitiesDetector, DisplayCaps, Context, Mode, EXACT, MIN, PEAK, USER (+3 more)

### Community 10 - "HomeFragment.java"
Cohesion: 0.09
Nodes (12): GameBoosterJsInterface, Context, Context, PermissionChannel, ShizukuChannel, Context, ShizukuExecutor, ShizukuUserServiceConnector (+4 more)

### Community 11 - "HzFpsFragment.java"
Cohesion: 0.28
Nodes (9): HzFpsFragment, Bundle, ImageView, LayoutInflater, Nullable, Override, Switch, View (+1 more)

### Community 12 - "📌 Master Features"
Cohesion: 0.13
Nodes (14): 🔒 1. 100% Non-Rooted Shizuku ADB Control, 🎯 2. Hardware Refresh Rate (Hz) & FPS Lock, 🎨 3. Graphics & GPU Engine Optimization, 👆 4. Touch Latency & Digitizer Sensitivity, 🧊 5. Thermal Throttling & PowerHAL Bypass, 🌐 6. Native JavaScript Bridge & Modular Web Scripts, 🚀 Building the APK, 👤 Developer & Contact (+6 more)

### Community 13 - "WebDashboardFragment.java"
Cohesion: 0.30
Nodes (9): Bundle, LayoutInflater, Nullable, Override, View, ViewGroup, WebDashboardFragment, SuppressLint (+1 more)

### Community 14 - "UserService"
Cohesion: 0.32
Nodes (3): Override, UserService, Stub

### Community 16 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 23 - ".onCreateView"
Cohesion: 0.25
Nodes (6): TouchLatencyChannel, BootReceiver, Context, Intent, Override, BroadcastReceiver

### Community 25 - "CommandExecutor.java"
Cohesion: 0.24
Nodes (3): NetworkTweaksChannel, Context, RamZramChannel

### Community 26 - "DnsMode"
Cohesion: 0.31
Nodes (6): DnsMode, CLOUDFLARE_1_1_1_1, GOOGLE_8_8_8_8, SYSTEM_DEFAULT, Context, NetworkOptimizer

### Community 27 - "CrosshairOverlayManager"
Cohesion: 0.44
Nodes (4): CrosshairOverlayManager, Context, View, WindowManager

## Knowledge Gaps
- **36 isolated node(s):** `QUALCOMM`, `MEDIATEK`, `EXYNOS`, `UNISOC`, `TENSOR` (+31 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GameAppInfo` connect `CommandExecutor.java` to `CommandExecutor.java`, `Fragment`, `GameLibraryChannel`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **What connects `QUALCOMM`, `MEDIATEK`, `EXYNOS` to the rest of the system?**
  _36 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.06867088607594937 - nodes in this community are weakly interconnected._
- **Should `TweakItem` be split into smaller, more focused modules?**
  _Cohesion score 0.05277777777777778 - nodes in this community are weakly interconnected._
- **Should `CommandExecutor.java` be split into smaller, more focused modules?**
  _Cohesion score 0.0670762928827445 - nodes in this community are weakly interconnected._
- **Should `DeviceSpecModel` be split into smaller, more focused modules?**
  _Cohesion score 0.09475806451612903 - nodes in this community are weakly interconnected._
- **Should `Fragment` be split into smaller, more focused modules?**
  _Cohesion score 0.0951219512195122 - nodes in this community are weakly interconnected._