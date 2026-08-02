# Graph Report - Game_Launcher_Pro  (2026-08-02)

## Corpus Check
- 62 files · ~126,429 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 609 nodes · 1330 edges · 23 communities (21 shown, 2 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 33 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ac11f1e3`
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

## God Nodes (most connected - your core abstractions)
1. `PropResult` - 34 edges
2. `TweakItem` - 22 edges
3. `GameAppInfo` - 20 edges
4. `FloatingOverlayService` - 18 edges
5. `DeviceSpecModel` - 15 edges
6. `BaseManager` - 15 edges
7. `SettingsFragment` - 15 edges
8. `TweaksAdapter` - 15 edges
9. `RefreshRateController` - 14 edges
10. `GamesAdapter` - 13 edges

## Surprising Connections (you probably didn't know these)
- `RefreshRateController` --references--> `PropertyResolver`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/core/DisplayCapabilitiesDetector.java → android/app/src/main/java/com/gamebooster/app/core/PropertyResolver.java
- `TweaksAdapter` --references--> `TweakItem`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/TweaksAdapter.java → android/app/src/main/java/com/gamebooster/app/functions/TweakItem.java
- `GamesFragment` --references--> `GameAppInfo`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/GamesFragment.java → android/app/src/main/java/com/gamebooster/app/games/GameAppInfo.java
- `HomeFragment` --references--> `GameAppInfo`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/HomeFragment.java → android/app/src/main/java/com/gamebooster/app/games/GameAppInfo.java
- `GamesFragment` --references--> `GamesAdapter`  [EXTRACTED]
  android/app/src/main/java/com/gamebooster/app/ui/layout/GamesFragment.java → android/app/src/main/java/com/gamebooster/app/ui/layout/GamesAdapter.java

## Import Cycles
- None detected.

## Communities (23 total, 2 thin omitted)

### Community 0 - "Override"
Cohesion: 0.07
Nodes (18): BaseManager, CpuManager, DisplayManager, FileSystemManager, GlobalSettingsManager, GpuManager, Context, Override (+10 more)

### Community 1 - "TweakItem"
Cohesion: 0.07
Nodes (24): TweakCategory, ALL, CPU_GPU, SHIZUKU_SYSTEM, TOUCH_DISPLAY, TweakItem, Context, OnBatchCompleteListener (+16 more)

### Community 2 - ".executeSystemCommand"
Cohesion: 0.06
Nodes (28): CpuGovernorChannel, GpuTweaksChannel, HzFpsChannel, DnsMode, CLOUDFLARE_1_1_1_1, GOOGLE_8_8_8_8, SYSTEM_DEFAULT, Context (+20 more)

### Community 3 - "CommandExecutor.java"
Cohesion: 0.08
Nodes (22): Adapter, GameAppInfo, Intent, GameConfigPatcher, PatchResult, GameLauncherHelper, Context, Context (+14 more)

### Community 4 - "DeviceSpecModel"
Cohesion: 0.09
Nodes (13): ChipsetVendor, EXYNOS, GENERIC, KIRIN, MEDIATEK, QUALCOMM, TENSOR, UNISOC (+5 more)

### Community 5 - "Fragment"
Cohesion: 0.10
Nodes (28): EngineUIHelper, TextView, Bundle, LayoutInflater, Nullable, Override, TextView, View (+20 more)

### Community 6 - ".onCreateView"
Cohesion: 0.09
Nodes (17): GameBoosterJsInterface, Context, Context, PermissionChannel, ShizukuChannel, Context, ShizukuExecutor, Bundle (+9 more)

### Community 7 - "ShizukuManager"
Cohesion: 0.07
Nodes (28): Context, ShizukuManager, ShizukuStateListener, Bundle, Override, MainActivity, Bundle, Button (+20 more)

### Community 8 - "FloatingOverlayService"
Cohesion: 0.12
Nodes (17): GameBoosterService, IBinder, Intent, Nullable, Override, FloatingOverlayService, Context, Handler (+9 more)

### Community 9 - "RefreshRateController"
Cohesion: 0.10
Nodes (11): DisplayCapabilitiesDetector, DisplayCaps, Context, Mode, EXACT, MIN, PEAK, USER (+3 more)

### Community 10 - "HomeFragment.java"
Cohesion: 0.12
Nodes (14): AppExecutors, Handler, GameProfileAutoConfigurator, Context, OnAutoConfigListener, HomeFragment, Bundle, LayoutInflater (+6 more)

### Community 11 - "HzFpsFragment.java"
Cohesion: 0.22
Nodes (10): ThermalChannel, HzFpsFragment, Bundle, ImageView, LayoutInflater, Nullable, Override, Switch (+2 more)

### Community 12 - "📌 Master Features"
Cohesion: 0.10
Nodes (19): ⚡ 1. 100% Non-Rooted Dual Execution Engine, ⚡ 1-Tap Ultra Booster, 📱 2. Direct Shizuku Binder IPC Integration, 🚀 3. Multi-OEM Refresh Rate (Hz) & FPS Lock, 🧊 4. Thermal & Throttling Bypass Module, 🎯 5. Preset Gaming Profiles Suite, 🎨 6. Graphics & GPU Unlocking Module, 🧠 7. CPU PowerHAL & Game Mode Governor (+11 more)

### Community 13 - "WebDashboardFragment.java"
Cohesion: 0.28
Nodes (4): ShizukuUserServiceConnector, IUserService, ServiceConnection, UserServiceArgs

### Community 14 - "UserService"
Cohesion: 0.32
Nodes (3): Override, UserService, Stub

### Community 16 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **40 isolated node(s):** `QUALCOMM`, `MEDIATEK`, `EXYNOS`, `UNISOC`, `TENSOR` (+35 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `PropertyResolver` connect `Override` to `RefreshRateController`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **Why does `RefreshRateController` connect `RefreshRateController` to `Override`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **What connects `QUALCOMM`, `MEDIATEK`, `EXYNOS` to the rest of the system?**
  _40 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.06867088607594937 - nodes in this community are weakly interconnected._
- **Should `TweakItem` be split into smaller, more focused modules?**
  _Cohesion score 0.06612021857923497 - nodes in this community are weakly interconnected._
- **Should `.executeSystemCommand` be split into smaller, more focused modules?**
  _Cohesion score 0.05576923076923077 - nodes in this community are weakly interconnected._
- **Should `CommandExecutor.java` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._