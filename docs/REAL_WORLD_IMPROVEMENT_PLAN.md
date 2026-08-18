# Real-World Improvement Plan — Game Launcher PRO

Staged roadmap to take the app from a feature-rich demo to something safe,
reliable, and effective on real devices. Every item is grounded in a concrete
finding in the current codebase (file:line cited).

---

## Phase 0 — Safety First (P0, ship-blocking)

### 0.1 Fix the hard-locked 185 FPS bug — ✅ DONE
The UI lets users pick 90/120/144/165/185 Hz, but the engine ignores the choice:

- `config/GameConfigPatcher.java:40` — `final int forcedFps = 185; // hard-locked — caller targetFps is ignored`
- `engine/MasterOptimizationEnforcer.java:149` — `final int forcedFps = 185;`
- `config/GameProfileAutoConfigurator.java:53` — `final int forcedFpsHz = DEFAULT_TARGET_HZ;`

**Fix:** thread the user-selected target through all three entry points, then
clamp it to the display's real capability via
`device/DisplayCapabilitiesDetector` / `DevicePerformanceCapabilities`
(applying 185 Hz to a 120 Hz panel is a silent no-op today).

**Acceptance criteria:** selecting 120 Hz in the UI results in `cmd game`,
`device_config game_overlay`, and every config patcher writing 120 — verified
by reading the patched files back.

### 0.2 Config backup & restore safety net — ✅ DONE
`config/ConfigBackupManager` + `ShizukuFileManager.readFileBytes`: before any patcher writes, true originals are captured once per path (idempotent) into app-private storage (`files/config_backups/<pkg>/<sha256(path)>.bin`), SHA-256-verified round-trip, records in SharedPreferences (`game_booster_backups`). Hooked in `GameConfigPatcher.applyGameFpsPatch` and `CfgProfileManager.applyProfile`; auto-restore on failed generic write; per-game "♻️ Restore originals" button on the Home tab game config dialog (dead `GamesFragment` was R8-removed in 0.3). Context registered from `MainActivity` + `GameBoosterService`.
All 15 game patchers overwrite game files (`Active.sav`, FastFlags, INI/JSON)
with **zero backup and zero restore path**. One bad patch bricks the user's
game settings with no recovery.

**Fix:** new `config/ConfigBackupManager`:
- Before first patch of a file, copy it to app-private storage (hash-named, sha256 verified).
- One-tap "Restore originals" per game in `GamesFragment` / `SettingsFragment`.
- Every patch writes a before/after hash to the backup record; if a write fails mid-way, auto-restore.

**Acceptance criteria:** patch → restore → file is byte-identical to original (sha256 assert in tests).

### 0.3 Real release signing — ✅ DONE
- `keystore.properties` (gitignored) + `android/keystore/game_booster_pro.jks` (generated, 10,000-day validity) wired into `signingConfigs.release`; missing/incomplete keystore.properties now **fails** `assembleRelease` per plan.
- R8 enabled (`minifyEnabled` + `shrinkResources`) with `proguard-rules.pro` keep rules for the WebView JS bridge (`@JavascriptInterface`) and reflective `Shizuku.newProcess`.
- Verified with `apksigner verify --print-certs`: release APK signed with `CN=Game Launcher PRO` (non-debug cert). Release APK shrunk 8.3 MB → 3.5 MB.
- R8 surfaced dead code: `GamesFragment`/`WebDashboardFragment` (only self-references, no tab shows them) got stripped — real UI host is `HomeFragment` + `HomeGamesAdapter`; restore UI + FPS tier list were placed there instead.

---

## Phase 1 — Reliability & Observability (P1)

### 1.1 Shizuku lifecycle hardening — ✅ DONE
- `shizuku/ShizukuManager.java` / `ShizukuExecutor.java` lack reconnection/backoff on `onBinderDied`.
- UI actions call `ShizukuUserServiceConnector.getInstance().bindService()` without awaiting readiness.

**Fix:** state machine (IDLE → BINDING → READY → DEAD → RETRY with exponential backoff), a shared `ShizukuStateFlow` the UI observes, and one `ensureReady()` gate all actions await.

**Acceptance criteria:** killing Shizuku mid-session shows "Reconnecting…" in UI and auto-recovers; no crash, no silent no-op.

**Implemented:**
- New `shizuku/ShizukuConnectionManager.java`: singleton state machine (`State { IDLE, BINDING, READY, DEAD, RETRY }`), `ConnectionListener` push notifications, exponential backoff 500ms → 8s cap, 60-attempt reconnect loop on `AppExecutors.executeCommand`, `ensureReady(timeoutMs)` as the single gate (returns false for degraded no-Shizuku operation — Tier-2/Tier-3 fallbacks untouched), `start()`/`stop()`, `isReady()`.
- `ShizukuManager`: binder-received / binder-dead / permission-result listeners now converge the manager (`onBinderReceived()` / `onBinderDead()`) — registered via `registerBinderListeners()` from `MainActivity.onCreate` (line 95).
- `ShizukuUserServiceConnector`: 5s bind-stuck timeout forces rebind (a wedged bind previously blocked all retries); `executeCommandDirect()` added and used by `ShizukuExecutor` (removed the mutual-recursion fallback `executeCommand()` → `executeShizukuCommand()` → `executeCommand()`); `onServiceConnected`/`onServiceDisconnected`/`binderDied()` now notify the manager.
- `ShizukuExecutor.executeShizukuCommand`: when permission is granted but AIDL service isn't up yet, waits ≤150ms via `ensureReady` (no cost when degraded).
- `HomeFragment`: subscribes to `ConnectionListener` (onResume/onDestroyView); status strip renders `🔄 Shizuku reconnecting… (STATE)` in red (`#FCA5A5`) for BINDING/RETRY/DEAD and auto-recovers to normal engine status on READY.
- Verified: `assembleDebug` + `assembleRelease` green; R8 keeps manager/connector/`HomeFragment` (usage.txt); release APK re-verified signed `CN=Game Launcher PRO`. ShizukuManager listeners wired at `MainActivity:95`.

### 1.2 Per-step failure reporting — ✅ DONE
`MasterOptimizationEnforcer.enforceGameLaunchOptimizations` wraps everything in
`catch (Throwable)` + `Log.w` — the user gets zero feedback on which of ~20
steps failed.

**Fix:** collect per-step results (step name → ok/fail + reason) into a report
shown in `CyberActionDialog`; on failure of a Tier 1 step, skip dependent
Tier 3 steps instead of continuing blindly (today `sed -i` runs even when
Shizuku is dead via `CommandExecutor.executeSystemCommand`, which fails).

**Implemented:**
- `MasterOptimizationEnforcer`: `StepResult` (tier/step/ok/detail), `EnforcementReport` (per-step list, ok/fail/skipped counters, `tierSucceededWithoutFailures`, `toDialogLines()` with ✓/✗/⤳ icons, `fullyApplied()`), `OnEnforcementReportListener`; new 4-arg `enforceGameLaunchOptimizations(context, pkg, fps, listener)` overload — old 3-arg delegates.
- ~18 steps individually recorded with per-step try/catch: 6 `cmd game`/`settings` AIDL commands (via `ShizukuConnectionManager.ensureReady(300)` gate from 1.1), `forceDisplayRefreshRate`, `setCpuGpuPerformanceGovernors` (Tier 1); `setGameModePerformance`, wifi/sustained locks, `requestHighPriorityNetwork` (Tier 2, no root needed); Tier 3 root-dependent steps (`GameConfigPatcher.applyGameFpsPatch` = the `sed -i`, `MaxHzForceChannel.forceApply`, `PerformanceChannel.applyProfile(EXTREME)`, `writeAndExecuteRootTweaksScript`, `DeviceSpooferEngine.applySpoofing`) are **SKIPPED** with reason "depends on Tier 1 (Shizuku)" when any Tier-1 step failed; only `NetworkOptimizer.flushDnsCache` (native) still runs unconditionally.
- `CyberActionDialog.showDetailed(context, title, activated, autoDismissMs, lines)` — refactored `show()` to delegate; duration now configurable (4500ms for reports vs 1800ms flash).
- `GameLauncherHelper` launch path passes the listener; when `!fullyApplied()` it renders the report through `showDetailed` — silent on full success (no friction added to the happy path).
- Verified: `assembleDebug` + `assembleRelease` green; R8 keeps `EnforcementReport`/`OnEnforcementReportListener`/`CyberActionDialog.showDetailed` with signature (usage.txt); release APK re-verified signed `CN=Game Launcher PRO`.

### 1.3 Unit tests + CI
`junit` is already a dependency but there are **zero tests**.

**Fix:** add tests for pure logic first (highest value, no emulator needed):
- `FpsUnlockTier` mapping + clamping to display capability
- `GameConfigPathResolver` path generation (incl. OBB vs data dir variants)
- `ConfigBackupManager` hash/restore round-trip
- sed/generic patcher escaping — package names & paths must be validated (no `;`/`'` injection)
- `SpoofProfileRegistry` profile data integrity (RAM/GPU consistency)

GitHub Actions workflow: `./gradlew test lint assembleRelease` on push/PR.

**Acceptance criteria:** ≥ 90% coverage on the config/spoofer packages; CI green gates merges.

### 1.4 On-device diagnostics
Real users can't send logs. Add a "Diagnostics" section in `SettingsFragment`:
- export logcat + settings snapshot to a shareable text file
- render `verifyEnforcementStatus()` output visibly
- crash capture via `Thread.setDefaultUncaughtExceptionHandler` writing to app storage

---

## Phase 2 — Real-Game Effectiveness (P2)

### 2.1 Gate SDK-specific commands
`cmd game mode performance`, `cmd game set --fps`, `device_config put
game_overlay`, `cmd window set-app-refresh-rate` are Android 14+ (API 34)
only; the app targets API 36 with `minSdk 24`. On older devices these fail
silently or error on stderr (still counted as "applied" today).

**Fix:** one `GameModeApiSupport.isAvailable(sdk)` gate; fall back to
SurfaceFlinger display-mode override + config patchers on API < 34.

### 2.2 Restore system state after game exit
Today, forcing 185 Hz and turbo settings persists forever after launch — the
phone stays locked at 185 Hz and boosted CPU/network even in the launcher.

**Fix:** `AutoGameMonitorService` already detects foreground app; add a
"Revert on exit" step that restores the user's baseline refresh rate, CPU
governor, and network profile when the game leaves the foreground.

### 2.3 Verify patch effectiveness (read-back)
Patchers write files but never confirm the game honors them.

**Fix:** after patching, read the file back and assert the expected key/value
exists; report "patch confirmed" vs "written but unverified" in the result
message. Add a per-game compatibility note in the Games screen (e.g., "CODM
may reset config on update — re-apply after game update").

### 2.4 Spoofing sanity checks
`HardwareMaskEngine` writes `/proc` masks and `SpoofValidator` exists — ensure
validation runs **before** applying (device without the spoofed SoC's feature
set, e.g., spoofing an Apple A18 Pro onto a Mali device, is a known ban vector
today).

**Acceptance criteria:** applying a spoof profile whose required GPU feature
level is missing is blocked with an explanation, not applied.

---

## Phase 3 — Hygiene & Docs (P3)

### 3.1 Manifest cleanup
- `android:allowBackup="true"` — spoof profiles, tweak state, and game settings leak into cloud backup/restore and can resurrect a banned-profile state on a new device. Set `android:allowBackup="false"` (or exclude via `backup_rules.xml`).
- `BootReceiver` exported — set `exported="false"` (BOOT_COMPLETED still arrives for non-exported receivers registered in manifest for system broadcasts — verify; otherwise use `android:exported="true"` + permission `android.permission.RECEIVE_BOOT_COMPLETED` check inside).
- Trim `tools:ignore="ProtectedPermissions"` claims that are shell-only at runtime and add Play Console-friendly comments; move shell-granted perms to a runtime "Grant via Shizuku/ADB" flow (already partially in `tools/grant_permissions.sh`).

### 3.2 Versioning & release notes
`versionCode 16000 / "16.0.0-PRO"` is fine, but add:
- auto-generated `CHANGELOG.md` per release
- per-build `versionName` including git short sha for debug builds
- `README` "Known limitations" section (per-game risks, panel Hz limits)

### 3.3 Realistic docs & risk transparency
README currently claims "100% safe for online anti-cheat ecosystems" while the
app modifies game configs and spoofs device identity — contradiction that will
bite users (and the project) in real-world support. Replace with an honest
per-feature risk table + FAQ (Shizuku died / FPS not applying / game reset my
config).

---

## Suggested execution order

| Step | Item | Est. effort |
| :-- | :--- | :-- |
| 1 | 0.1 Fix FPS hard-lock + display clamp | 0.5 day |
| 2 | 0.2 Backup/restore safety net | 1 day |
| 3 | 0.3 Release signing + R8 | 0.5 day |
| 4 | 1.3 Unit tests + CI | 1.5 days |
| 5 | 1.1 Shizuku lifecycle | 1 day |
| 6 | 1.2 Per-step reporting | 0.5 day |
| 7 | 2.1 SDK gating | 0.5 day |
| 8 | 2.2 Revert on game exit | 1 day |
| 9 | 2.3 Read-back verification | 0.5 day |
| 10 | 2.4 Spoofer pre-validation | 0.5 day |
| 11 | 1.4 Diagnostics | 0.5 day |
| 12 | Phase 3 hygiene/docs | 1 day |

Total ≈ 9 days of focused work. Phases 0–1 are the ship gate; Phase 2 is where
real users notice the difference; Phase 3 is polish + honesty.
