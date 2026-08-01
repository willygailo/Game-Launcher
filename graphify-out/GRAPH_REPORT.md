# Graph Report - Game_Launcher_Pro  (2026-08-01)

## Corpus Check
- 58 files · ~31,656 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 564 nodes · 1217 edges · 23 communities (17 shown, 6 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 32 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `26501944`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CPU Governor Subsystem
- Main Activity & UI Lifecycle
- Tweaks & Command Executor
- Permission Management Channel
- Property Resolver Base
- Game Library & Scanner
- Device Metrics Channel
- Command Engine ADB Shell
- Display Capabilities & Hz
- Game Booster Background Service
- Command Engine Cache Control
- Property Resolver Managers
- Hz & FPS Fragment UI
- Command Engine Builder
- Command Execution Engine
- Global Settings Manager
- .applyGameFpsPatch
- GameInfoSpec
- HzFpsFragment.java
- .applyGameFpsPatch
- Gradle Wrapper Scripts

## God Nodes (most connected - your core abstractions)
1. `PropResult` - 34 edges
2. `TweakItem` - 22 edges
3. `GameAppInfo` - 20 edges
4. `FloatingOverlayService` - 17 edges
5. `DeviceSpecModel` - 15 edges
6. `BaseManager` - 15 edges
7. `RefreshRateController` - 14 edges
8. `GamesAdapter` - 13 edges
9. `TweaksAdapter` - 13 edges
10. `ChipsetVendor` - 12 edges

## Surprising Connections (you probably didn't know these)
- `RefreshRateController` --references--> `PropertyResolver`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/core/DisplayCapabilitiesDetector.java → android/app/src/main/java/com/gamebooster/app/core/PropertyResolver.java
- `HomeFragment` --references--> `GameAppInfo`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/HomeFragment.java → android/app/src/main/java/com/gamebooster/app/games/GameAppInfo.java
- `TweaksFragment` --implements--> `ShizukuStateListener`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksFragment.java → android/app/src/main/java/com/gamebooster/app/shizuku/ShizukuManager.java
- `HomeFragment` --references--> `GamesAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/HomeFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/GamesAdapter.java
- `TweaksFragment` --references--> `TweaksAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksAdapter.java

## Import Cycles
- None detected.

## Communities (23 total, 6 thin omitted)

### Community 0 - "CPU Governor Subsystem"
Cohesion: 0.05
Nodes (30): CpuGovernorChannel, GameSpaceDndManager, Context, GpuTweaksChannel, HzFpsChannel, DnsMode, CLOUDFLARE_1_1_1_1, GOOGLE_8_8_8_8 (+22 more)

### Community 1 - "Main Activity & UI Lifecycle"
Cohesion: 0.28
Nodes (4): ShizukuUserServiceConnector, IUserService, ServiceConnection, UserServiceArgs

### Community 2 - "Tweaks & Command Executor"
Cohesion: 0.07
Nodes (23): TweakCategory, ALL, CPU_GPU, SHIZUKU_SYSTEM, TOUCH_DISPLAY, TweakItem, Context, OnBatchCompleteListener (+15 more)

### Community 3 - "Permission Management Channel"
Cohesion: 0.07
Nodes (18): BaseManager, CpuManager, DisplayManager, FileSystemManager, GlobalSettingsManager, GpuManager, Context, Override (+10 more)

### Community 4 - "Property Resolver Base"
Cohesion: 0.07
Nodes (22): Bundle, LayoutInflater, Nullable, Override, TextView, View, ViewGroup, PermissionsFragment (+14 more)

### Community 5 - "Game Library & Scanner"
Cohesion: 0.08
Nodes (27): Adapter, GameAppInfo, Intent, GameLauncherHelper, Context, GameManagerRepository, Context, GamesAdapter (+19 more)

### Community 6 - "Device Metrics Channel"
Cohesion: 0.09
Nodes (13): ChipsetVendor, EXYNOS, GENERIC, KIRIN, MEDIATEK, QUALCOMM, TENSOR, UNISOC (+5 more)

### Community 7 - "Command Engine ADB Shell"
Cohesion: 0.14
Nodes (9): DisplayCapabilitiesDetector, DisplayCaps, Context, Mode, EXACT, MIN, PEAK, USER (+1 more)

### Community 8 - "Display Capabilities & Hz"
Cohesion: 0.10
Nodes (19): ⚡ 1. 100% Non-Rooted Dual Execution Engine, ⚡ 1-Tap Ultra Booster, 📱 2. Direct Shizuku Binder IPC Integration, 🚀 3. Multi-OEM Refresh Rate (Hz) & FPS Lock, 🧊 4. Thermal & Throttling Bypass Module, 🎯 5. Preset Gaming Profiles Suite, 🎨 6. Graphics & GPU Unlocking Module, 🧠 7. CPU PowerHAL & Game Mode Governor (+11 more)

### Community 9 - "Game Booster Background Service"
Cohesion: 0.12
Nodes (17): GameBoosterService, IBinder, Intent, Nullable, Override, FloatingOverlayService, Context, Handler (+9 more)

### Community 10 - "Command Engine Cache Control"
Cohesion: 0.12
Nodes (14): AppExecutors, Handler, GameProfileAutoConfigurator, Context, OnAutoConfigListener, HomeFragment, Bundle, LayoutInflater (+6 more)

### Community 12 - "Hz & FPS Fragment UI"
Cohesion: 0.32
Nodes (3): Override, UserService, Stub

### Community 13 - "Command Engine Builder"
Cohesion: 0.12
Nodes (19): EngineUIHelper, TextView, Bundle, LayoutInflater, Nullable, Override, TextView, View (+11 more)

### Community 18 - "HzFpsFragment.java"
Cohesion: 0.15
Nodes (13): EngineMode, READ_ONLY, SHIZUKU, SYSTEM_SETTINGS, HzFpsFragment, Bundle, ImageView, LayoutInflater (+5 more)

### Community 21 - "Gradle Wrapper Scripts"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **43 isolated node(s):** `QUALCOMM`, `MEDIATEK`, `EXYNOS`, `UNISOC`, `TENSOR` (+38 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `PropertyResolver` connect `Permission Management Channel` to `Command Engine ADB Shell`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **What connects `QUALCOMM`, `MEDIATEK`, `EXYNOS` to the rest of the system?**
  _43 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CPU Governor Subsystem` be split into smaller, more focused modules?**
  _Cohesion score 0.05225225225225225 - nodes in this community are weakly interconnected._
- **Should `Tweaks & Command Executor` be split into smaller, more focused modules?**
  _Cohesion score 0.07320024198427103 - nodes in this community are weakly interconnected._
- **Should `Permission Management Channel` be split into smaller, more focused modules?**
  _Cohesion score 0.06867088607594937 - nodes in this community are weakly interconnected._
- **Should `Property Resolver Base` be split into smaller, more focused modules?**
  _Cohesion score 0.07003367003367003 - nodes in this community are weakly interconnected._
- **Should `Game Library & Scanner` be split into smaller, more focused modules?**
  _Cohesion score 0.08313725490196078 - nodes in this community are weakly interconnected._