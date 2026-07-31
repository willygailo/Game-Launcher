# Graph Report - .  (2026-07-31)

## Corpus Check
- Corpus is ~15,162 words - fits in a single context window. You may not need a graph.

## Summary
- 481 nodes · 1061 edges · 24 communities (16 shown, 8 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 28 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

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
- GPU Manager Module
- Memory Manager Module
- System Properties Manager
- System Settings Manager
- Thermal Manager Module
- Gradle Wrapper Scripts

## God Nodes (most connected - your core abstractions)
1. `Result` - 40 edges
2. `CommandEngine` - 29 edges
3. `TweakItem` - 21 edges
4. `BaseManager` - 17 edges
5. `Command` - 16 edges
6. `DeviceSpecModel` - 15 edges
7. `GameAppInfo` - 15 edges
8. `RefreshRateController` - 14 edges
9. `PropertyResolver` - 12 edges
10. `TweaksAdapter` - 12 edges

## Surprising Connections (you probably didn't know these)
- `BaseManager` --references--> `CommandEngine`  [EXTRACTED]
  android/app/src/main/java/com/gamespace/app/core/PropertyResolver.java → android/app/src/main/java/com/gamespace/app/core/CommandEngine.java
- `PropertyResolver` --references--> `CommandEngine`  [EXTRACTED]
  android/app/src/main/java/com/gamespace/app/core/PropertyResolver.java → android/app/src/main/java/com/gamespace/app/core/CommandEngine.java
- `RefreshRateController` --references--> `PropertyResolver`  [EXTRACTED]
  android/app/src/main/java/com/gamespace/app/core/DisplayCapabilitiesDetector.java → android/app/src/main/java/com/gamespace/app/core/PropertyResolver.java
- `TweaksFragment` --references--> `TweaksAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamespace/app/ui/TweaksFragment.java → android/app/src/main/java/com/gamespace/app/ui/TweaksAdapter.java
- `DeviceSpecModel` --references--> `ChipsetVendor`  [EXTRACTED]
  android/app/src/main/java/com/gamespace/app/core/DeviceSpecModel.java → android/app/src/main/java/com/gamespace/app/utils/DeviceDetector.java

## Import Cycles
- None detected.

## Communities (24 total, 8 thin omitted)

### Community 0 - "CPU Governor Subsystem"
Cohesion: 0.05
Nodes (28): CpuGovernorChannel, GameLibraryChannel, GpuTweaksChannel, HzFpsChannel, NetworkTweaksChannel, Context, PerformanceChannel, Profile (+20 more)

### Community 1 - "Main Activity & UI Lifecycle"
Cohesion: 0.08
Nodes (32): Bundle, Override, MainActivity, GamesFragment, Bundle, LayoutInflater, Nullable, Override (+24 more)

### Community 2 - "Tweaks & Command Executor"
Cohesion: 0.09
Nodes (19): Adapter, TweakCategory, ALL, CPU_GPU, SHIZUKU_SYSTEM, TOUCH_DISPLAY, TweakItem, TweakManagerRepository (+11 more)

### Community 3 - "Permission Management Channel"
Cohesion: 0.10
Nodes (15): Context, PermissionChannel, ShizukuChannel, Bundle, LayoutInflater, Nullable, Override, TextView (+7 more)

### Community 4 - "Property Resolver Base"
Cohesion: 0.12
Nodes (8): BaseManager, CpuManager, DisplayManager, FileSystemManager, Override, KernelParameterManager, NetworkManager, SecureSettingsManager

### Community 5 - "Game Library & Scanner"
Cohesion: 0.12
Nodes (16): Context, GameAppInfo, Intent, GameManagerRepository, Context, GamesAdapter, GameViewHolder, Context (+8 more)

### Community 6 - "Device Metrics Channel"
Cohesion: 0.09
Nodes (13): DeviceInfoChannel, Context, Metrics, DeviceSpecModel, ChipsetVendor, EXYNOS, GENERIC, KIRIN (+5 more)

### Community 7 - "Command Engine ADB Shell"
Cohesion: 0.13
Nodes (11): AdbLocalExecutor, CapabilitySet, ExecutorStrategy, Context, Override, Method, ADB_LOCAL, SHIZUKU (+3 more)

### Community 8 - "Display Capabilities & Hz"
Cohesion: 0.14
Nodes (9): DisplayCapabilitiesDetector, DisplayCaps, Context, Mode, EXACT, MIN, PEAK, USER (+1 more)

### Community 9 - "Game Booster Background Service"
Cohesion: 0.15
Nodes (14): GameBoosterService, Intent, Nullable, Override, HomeFragment, Bundle, LayoutInflater, Nullable (+6 more)

### Community 11 - "Property Resolver Managers"
Cohesion: 0.26
Nodes (3): PropertyManager, PropertyResolver, PropResult

### Community 12 - "Hz & FPS Fragment UI"
Cohesion: 0.26
Nodes (9): HzFpsFragment, Bundle, ImageView, LayoutInflater, Nullable, Override, Switch, View (+1 more)

### Community 13 - "Command Engine Builder"
Cohesion: 0.19
Nodes (6): Builder, Priority, CRITICAL, HIGH, LOW, NORMAL

### Community 21 - "Gradle Wrapper Scripts"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **29 isolated node(s):** `EXTREME_3D_FPS`, `ULTRA_SMOOTH_2D`, `BALANCED`, `BATTERY_SAVER`, `SHIZUKU` (+24 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Result` connect `Command Execution Engine` to `Property Resolver Base`, `Command Engine ADB Shell`, `Command Engine Cache Control`, `Property Resolver Managers`, `Global Settings Manager`, `GPU Manager Module`, `Memory Manager Module`, `System Properties Manager`, `System Settings Manager`, `Thermal Manager Module`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `CommandEngine` connect `Command Engine Cache Control` to `Property Resolver Managers`, `Property Resolver Base`, `Command Execution Engine`, `Command Engine ADB Shell`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **What connects `EXTREME_3D_FPS`, `ULTRA_SMOOTH_2D`, `BALANCED` to the rest of the system?**
  _29 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CPU Governor Subsystem` be split into smaller, more focused modules?**
  _Cohesion score 0.05472636815920398 - nodes in this community are weakly interconnected._
- **Should `Main Activity & UI Lifecycle` be split into smaller, more focused modules?**
  _Cohesion score 0.07918367346938776 - nodes in this community are weakly interconnected._
- **Should `Tweaks & Command Executor` be split into smaller, more focused modules?**
  _Cohesion score 0.08637873754152824 - nodes in this community are weakly interconnected._
- **Should `Permission Management Channel` be split into smaller, more focused modules?**
  _Cohesion score 0.10476190476190476 - nodes in this community are weakly interconnected._