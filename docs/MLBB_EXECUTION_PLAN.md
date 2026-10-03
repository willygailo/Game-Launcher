# MLBB High-Performance Execution & Injection Plan
**Target Device:** TECNO CM7 (`CM7-OP`, MediaTek MT6878 / Dimensity)  
**OS Version:** Android 16 (API 36, arm64-v8a)  
**Target Package:** `com.mobile.legends` (v2.2.16.12322)  
**Privilege Level:** Shizuku Privileged Shell (PID active, API_V23 granted)  
**Display:** 1080 x 2436 @ 144 Hz Hardware Panel (Modes: 60Hz [3], 120Hz [1], 144Hz [2])  
**Date:** October 2026

---

## 1. System & Device Telemetry Audit

| Component | Status | Details |
|---|---|---|
| **USB Link** | CONNECTED | `140892554P020737` via USB 3.0, scrcpy active on display 21 |
| **Android OS** | Android 16 | Target SDK 36, Kernel 6.x / MTK EAS, SELinux Enforcing |
| **SoC / GPU** | MT6878 (Dimensity) | 4x A78 @ 2.6 GHz + 4x A55 @ 2.0 GHz, Mali-G610 / G77 MC9 |
| **Display Panel** | 144 Hz Native | Hardware IDs: `1` (120 Hz), `2` (144 Hz), `3` (60 Hz) |
| **Shizuku Server** | RUNNING | Started via `/data/local/tmp/shizuku_starter`, shell UID 2000 |
| **GameBooster App** | INSTALLED | `com.gamebooster.app` v19.5.0 (Build 1950), Shizuku granted |
| **MLBB Installation** | INSTALLED | Primary User (`0`), dataDir: `/data/user/0/com.mobile.legends` |

---

## 2. Storage & Mini-Patch Path Topology

MLBB uses a dynamic dual-layer asset pipeline on Android 16. Shell and Shizuku have direct read/write access to external app storage:

```text
/sdcard/Android/data/com.mobile.legends/files/
├── boot.config                                       <-- Engine overrides (Lifesteal, FPS, Touch)
├── battle_config/
│   ├── BattleConfig.json                             <-- Combat, HitReg, & DamageLock parameters
│   └── QualityConfig.json                            <-- Visual & Resolution scaling
├── com.mobile.legends.v2.playerprefs.xml             <-- Unity PlayerPrefs (HighFPSMode, graphics)
├── dragon2017/assets/
│   ├── Document/android/BattleConfig.json            <-- Secondary combat sync
│   └── Scenes/                                       <-- Map geometry & default camera
└── mini_patch/                                       <-- Live Moonton hot-patch slots
    ├── 1232.1/
    │   ├── fix_1788688455/1/Scenes                   <-- Active scene & camera overrides
    │   ├── fix_1789120431/1/Art
    │   ├── fix_1790076887/1/Document
    │   └── ZC_7125100180/1/UI
    └── 1232.2/
        ├── fix_1789023716/1/Document                 <-- Active Document hot-patches
        ├── fix_1789548222/1/Art
        └── fix_1790147373/1/UI
```

---

## 3. Four-Phase Execution Plan

### Phase 1: Display & Kernel Performance Pinning
1. **Refresh Rate Lock (144 Hz):**
   - Write `peak_refresh_rate` = `144.0` and `min_refresh_rate` = `144.0` via `settings put system`.
   - Apply user preference `user_refresh_rate` = `2` (matching display mode `2`).
2. **MediaTek CPU EAS & Touch Pinning:**
   - Force `perfservice` / `power_hal` high-performance boost.
   - Set touch sampling rate to 1000 Hz digitizer mode via `TouchLatencyChannel`.
   - Lock InputFlinger thread to FIFO RT priority.

### Phase 2: Combat & PlayerPrefs Overdrive Injection
1. **PlayerPrefs XML Patching (`MlbbConfigPatcher`):**
   - Inject `HighFPSMode` = `4` (120/144 FPS unlock) and `FrameRateLevel` = `4`.
   - Inject `TargetPriority` = `0` (lowest HP priority), `SkillSmartAim` = `1`, `HeroLock` = `1`.
   - Disable screen shake (`ScreenShake=0`) and input delay buffers (`ZeroInputLag=1`).
2. **BattleConfig Overdrive:**
   - Deploy `BattleConfig.json` into `/sdcard/Android/data/com.mobile.legends/files/battle_config/` and `dragon2017/assets/Document/`.
   - Set `HitRegSyncRate` = `1000`, `FrameSyncDamage` = `1`, `DamageLockMax` = `1`.

### Phase 3: Drone View & Mini-Patch Slot Integration
1. **Camera Height Tier Selection:**
   - Available Tiers: `1.5X` (15), `2.0X` (20), `3.0X` (30), `4.0X` (40), `5.0X` (50).
   - Source Assets: `android/app/src/main/assets/mlbb_drone/tiers/battle_<tier>.bytes`.
2. **Dual-Slot Deployment (`MlbbDroneViewPatcher`):**
   - Deploy camera coordinates into `mini_patch/1232.1/fix_1788688455/1/Scenes/`.
   - Deploy secondary camera configs into `mini_patch/1232.2/fix_1789023716/1/Document/`.
   - Stamp validation flags: `__ready`, `__active`, `__fix_rescheck`, and `_load_res.bytes` to bypass client hash checks.

### Phase 4: Hero Combo & Lua Script Dispatch
1. **Selected Hero Integration (`MlbbHeroScriptRegistry`):**
   - Match player selection (e.g., Ling Finch Poise auto-sword, Fanny fast-cable, Gusion 10-dagger overdrive).
   - Inject native C++ memory overrides via `NativeConfigInjector_nativeInjectMlbb*`.
   - Link Lua combat routines from `android/app/src/main/assets/lua/mlbb_heroes/`.

---

## 4. Verification & Validation Commands

Run these adb commands to verify real-time application:

```bash
# 1. Verify Refresh Rate
adb shell settings get system peak_refresh_rate
adb shell settings get system min_refresh_rate

# 2. Verify Injected BattleConfig
adb shell cat /sdcard/Android/data/com.mobile.legends/files/battle_config/BattleConfig.json

# 3. Verify Active Mini-Patch Files
adb shell ls -la /sdcard/Android/data/com.mobile.legends/files/mini_patch/1232.1/fix_1788688455/1/

# 4. Verify MLBB Process Framerate & SurfaceFlinger
adb shell dumpsys SurfaceFlinger --latency com.mobile.legends/com.mobile.legends.MlbbMainActivity

# 5. Monitor MLBB Logcat for Shader & Config Ingestion
adb shell logcat -d -s Unity,DEBUG,GameBooster | tail -n 40
```

---

## 5. Rollback & Anti-Ban Safety Protocol

To cleanly revert all modifications back to stock MLBB:
1. Delete injected `BattleConfig.json` files in `battle_config/` and `dragon2017/assets/Document/`.
2. Remove injected files inside `mini_patch/1232.1/` and `mini_patch/1232.2/`.
3. Restore `boot.config` to default Unity configuration.
4. Reset `peak_refresh_rate` and `min_refresh_rate` to default auto mode (`0` or `120.0`).
