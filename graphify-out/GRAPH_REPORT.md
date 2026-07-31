# Graph Report - Game_Launcher_Pro  (2026-07-31)

## Corpus Check
- 49 files · ~15,758 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 481 nodes · 1006 edges · 19 communities (16 shown, 3 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3093ceba`
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
- Gradle Wrapper Scripts

## God Nodes (most connected - your core abstractions)
1. `PropResult` - 34 edges
2. `TweakItem` - 21 edges
3. `GameAppInfo` - 17 edges
4. `DeviceSpecModel` - 15 edges
5. `BaseManager` - 15 edges
6. `RefreshRateController` - 14 edges
7. `TweaksAdapter` - 13 edges
8. `ChipsetVendor` - 12 edges
9. `ShizukuManager` - 12 edges
10. `TweaksFragment` - 12 edges

## Surprising Connections (you probably didn't know these)
- `RefreshRateController` --references--> `PropertyResolver`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/core/DisplayCapabilitiesDetector.java → android/app/src/main/java/com/gamebooster/app/core/PropertyResolver.java
- `TweaksFragment` --implements--> `ShizukuStateListener`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksFragment.java → android/app/src/main/java/com/gamebooster/app/shizuku/ShizukuManager.java
- `TweaksFragment` --references--> `TweaksAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksAdapter.java
- `DeviceSpecModel` --references--> `ChipsetVendor`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/core/DeviceSpecModel.java → android/app/src/main/java/com/gamebooster/app/core/DeviceDetector.java
- `TweakItem` --references--> `TweakCategory`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/functions/TweakItem.java → android/app/src/main/java/com/gamebooster/app/functions/TweakCategory.java

## Import Cycles
- None detected.

## Communities (19 total, 3 thin omitted)

### Community 0 - "CPU Governor Subsystem"
Cohesion: 0.06
Nodes (27): CpuGovernorChannel, GpuTweaksChannel, HzFpsChannel, NetworkTweaksChannel, Context, PerformanceChannel, Profile, BALANCED (+19 more)

### Community 1 - "Main Activity & UI Lifecycle"
Cohesion: 0.07
Nodes (22): Bundle, LayoutInflater, Nullable, Override, TextView, View, ViewGroup, PermissionsFragment (+14 more)

### Community 2 - "Tweaks & Command Executor"
Cohesion: 0.07
Nodes (22): TweakCategory, ALL, CPU_GPU, SHIZUKU_SYSTEM, TOUCH_DISPLAY, TweakItem, TweakManagerRepository, EngineMode (+14 more)

### Community 3 - "Permission Management Channel"
Cohesion: 0.07
Nodes (18): BaseManager, CpuManager, DisplayManager, FileSystemManager, GlobalSettingsManager, GpuManager, Context, Override (+10 more)

### Community 4 - "Property Resolver Base"
Cohesion: 0.11
Nodes (19): EngineUIHelper, TextView, Bundle, LayoutInflater, Nullable, Override, TextView, View (+11 more)

### Community 5 - "Game Library & Scanner"
Cohesion: 0.09
Nodes (25): Adapter, AppExecutors, GameAppInfo, Intent, GamesAdapter, GameViewHolder, Context, ImageView (+17 more)

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
Cohesion: 0.15
Nodes (10): GameBoosterService, Intent, Nullable, Override, ShizukuUserServiceConnector, IBinder, IUserService, Service (+2 more)

### Community 10 - "Command Engine Cache Control"
Cohesion: 0.25
Nodes (8): HomeFragment, Bundle, LayoutInflater, Nullable, Override, TextView, View, ViewGroup

### Community 11 - "Property Resolver Managers"
Cohesion: 0.26
Nodes (9): HzFpsFragment, Bundle, ImageView, LayoutInflater, Nullable, Override, Switch, View (+1 more)

### Community 12 - "Hz & FPS Fragment UI"
Cohesion: 0.32
Nodes (3): Override, UserService, Stub

### Community 21 - "Gradle Wrapper Scripts"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **40 isolated node(s):** `QUALCOMM`, `MEDIATEK`, `EXYNOS`, `UNISOC`, `TENSOR` (+35 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `PropertyResolver` connect `Permission Management Channel` to `Command Engine ADB Shell`?**
  _High betweenness centrality (0.060) - this node is a cross-community bridge._
- **What connects `QUALCOMM`, `MEDIATEK`, `EXYNOS` to the rest of the system?**
  _40 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CPU Governor Subsystem` be split into smaller, more focused modules?**
  _Cohesion score 0.05924920850293985 - nodes in this community are weakly interconnected._
- **Should `Main Activity & UI Lifecycle` be split into smaller, more focused modules?**
  _Cohesion score 0.06988120195667366 - nodes in this community are weakly interconnected._
- **Should `Tweaks & Command Executor` be split into smaller, more focused modules?**
  _Cohesion score 0.07183673469387755 - nodes in this community are weakly interconnected._
- **Should `Permission Management Channel` be split into smaller, more focused modules?**
  _Cohesion score 0.06867088607594937 - nodes in this community are weakly interconnected._
- **Should `Property Resolver Base` be split into smaller, more focused modules?**
  _Cohesion score 0.11363636363636363 - nodes in this community are weakly interconnected._