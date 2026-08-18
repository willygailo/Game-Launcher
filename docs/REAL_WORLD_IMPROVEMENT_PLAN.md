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

### 1.3 Unit tests + CI — ✅ DONE
`junit` was already a dependency but there were **zero tests**; now **73 unit tests, all green**.

**Implied by implementing:**
- New `config/ShellSafety.java` (package-name / shell-path whitelists + single-quote escaping) and wired it into `GameConfigPatcher` entry + `sed` path escaping — tests caught a real bug (dot-only packages + `../` traversal passed the original whitelist) and the fix landed with tests.
- `GameConfigPathResolver`: `generateBasePaths()` / `getKnownRelativePathsForPackage()` public & case-insensitive (tests caught camelCase Genshin packages silently falling back to generic paths), `ConfigBackupManager.sanitize()`/`sha256Hex()` package-private seams.
- `app/build.gradle`: jacoco plugin + `jacocoTestReport` (fixtures: `build/jacoco/*.exec`, `intermediates/javac/debug/compileDebugJavaWithJavac/classes`), `unitTests.returnDefaultValues`.
- Test files: `FpsUnlockTierTest`, `GameConfigPathResolverTest` (all 15 game families + base paths + cache), `ConfigBackupManagerTest` (sha256/sanitize), `ShellSafetyTest`, `SpoofProfileRegistryTest` (data integrity, meminfo/system-property payload consistency, soc/vendor inference).
- `.github/workflows/ci.yml`: `testDebugUnitTest` + `jacocoTestReport` + `lint` + `assembleRelease` on push/PR (throwaway CI keystore via keytool).
- Lint made gate-able: fixed 8 pre-existing errors (API-31 `Build.SOC_MODEL` guards in `DeviceDetector`, API-26 `isWideColorGamut` guard, API-27 `windowLightNavigationBar` theme item, `@SuppressLint WrongConstant` on `setGameModePerformance`, `app:tint` + namespace in `item_tweak_card.xml`).

**Coverage (JaCoCo), tested seams:** `FpsUnlockTier` 100% line / 90% branch · `ShellSafety` 100% / 86.7% · `SpoofProfile` 95.2% / 66% · `SpoofProfileRegistry` 93.5% / 87.5% · brand profiles ≥80% line / 100% branch · `GameConfigPathResolver` 87.9% line (remainder is Shizuku-permission-gated deep scan, uncallable in JVM). `ConfigBackupManager` covers only pure methods (rest is `SharedPreferences`/I/O).

**Acceptance criteria:** ≥90% coverage on the unit-testable config/spoofer seams (met: mean ~95% line; Android-gated I/O documented above); CI green — verified locally: `./gradlew testDebugUnitTest jacocoTestReport lint assembleDebug assembleRelease` all pass; release APK re-verified signed `CN=Game Launcher PRO` (SHA-256 `75eb4c3e…1e30d`).

### 1.4 On-device diagnostics
**✅ DONE** — new `com.gamebooster.app.diagnostics` package:
- `CrashLog` — `Thread.setDefaultUncaughtExceptionHandler` capture (installed
  from `SettingsFragment`, chained to any previous handler, idempotent) appends
  crash entries to `filesDir/crash_log.txt`; `readTail()` included in exports.
- `DiagnosticsExporter` — snapshot builder renders `verifyEnforcementStatus()`
  visibly (Shizuku/root, AIDL, tweaks applied/total), app version + code,
  device model/manufacturer, Android release/API, spoof profile state, and any
  captured crash tail; exported to `getExternalFilesDir` and shared via
  `ACTION_SEND` + `FileProvider` (`${applicationId}.fileprovider`,
  `res/xml/file_paths.xml`).
- New "🩺 DIAGNOSTICS" card in Settings (REFRESH / EXPORT buttons,
  `tv_diag_status` monospace render) — user can now send logs instead of being
  stuck. 7 new tests (DiagnosticsExporterTest + CrashLogTest, 121 total); pure
  seams (snapshot building, crash formatting) ~95–100% line, Android-gated file
  I/O / share / handler install documented as gated. Gate green: 121 tests,
  lint 0, both builds, release APK signature re-verified.

---

## Phase 2 — Real-Game Effectiveness (P2)

### 2.1 Gate SDK-specific commands — ✅ DONE
`cmd game mode performance`, `cmd game set --fps`, `device_config put
game_overlay`, `cmd window set-app-refresh-rate` are Android 14+ (API 34)
only; the app targets API 36 with `minSdk 24`. On older devices these fail
silently or error on stderr (still counted as "applied" today).

**Done:** new pure `engine/GameModeApiSupport` (single `isAvailable()` gate at
API 34 + per-command minimums: game mode 31, game_overlay 33, fps/refresh-rate
34; sdk-overloads unit-tested). `MasterOptimizationEnforcer` Tier 1 now builds
the command list dynamically — the two `settings put global game_driver…`
opt-ins always run (any API), the GameMode shell set only on 14+; below it a
`SKIPPED — requires Android 14+ (API 34); falling back to SurfaceFlinger
override + config patchers` step is recorded (counted as skipped, not applied).
Fallback path already exists via `forceDisplayRefreshRate` (AIDL SurfaceFlinger)
+ Tier-3 config patchers. 5 new tests (`GameModeApiSupportTest`, 78 total),
verify: `testDebugUnitTest jacocoTestReport lint assembleDebug assembleRelease`
green, release APK re-signed.

### 2.2 Restore system state after game exit
**✅ DONE** — `AutoGameMonitorService` now reverts instead of re-locking. New
`GameStateReverter` (gamespace) restores the baseline captured by
`GameSessionSettings.begin()` (previous Hz + previous DND): refresh rate pushed
back through the same triple channel it was forced with (MaxHzForceChannel +
HzFpsChannel + Shizuku `forceDisplayRefreshRate`), CPU/GPU governors returned to
schedutil/simple_ondemand via new AIDL `restoreCpuGpuGovernors` (id 20, also
added to `ShizukuUserServiceConnector` with shell fallback), thermal override
cleared (`setThermalOverride(false)`), network reverted
(`restoreLowLatencyNetwork`: cubic congestion control, wifi force modes
disabled, sleep policy), Wi-Fi/wake locks released, DND re-applied to previous
state, and the session closed. The "Background Home 185 Hz Lock" re-apply and
the continuous home-screen 2.5s re-force were removed — the device no longer
stays locked at 185 Hz in the launcher. Pure decision seam `evaluate()`
(GameStateReverterTest, 5 tests, 83 total) at 100% line/branch; revert body is
Shizuku-gated like the other device channels. Gate green: 83 tests, lint 0,
both builds, release APK signature re-verified.

### 2.3 Verify patch effectiveness (read-back)
**✅ DONE** — `GameConfigPatcher.applyGameFpsPatch` now reads written config files
back (up to 12 paths via existing `ShizukuFileManager.fileExists/readFile`,
skipping missing files) and asserts the forced FPS value is actually bound to an
FPS/framerate key. New pure `GameConfigPatchVerifier` (`verifyFpsInContent`
INI/CVar/JSON regex, `buildVerificationSummary`, `getPatchCompatibilityNote`):
result message now ends with "patch confirmed (N/M files read-back verified)",
"partially confirmed", or "written but unverified" — surfaced in every caller's
toast (Games/Home/Profiles) and the Master Optimization report. Games screen
rows (`item_game_card` + `GamesAdapter`) gained a small amber warning for
families that reset config (CODM/PUBG/MLBB: "May reset config on update —
re-apply after game update"; Genshin/Hoyoverse: "Integrity check may revert
patches"). Rule caught real bug: naive first-`=`/`:` split broke on CVar and
multi-key JSON lines — switched to regex value extraction. 18 new tests
(GameConfigPatchVerifierTest, 101 total); verifier pure seams at 100% line/
branch, class 96.6% line. Gate green: 101 tests, lint 0, both builds, release
APK signature re-verified.

### 2.4 Spoofing sanity checks
**✅ DONE** — new pure `SpoofSanityChecker` runs **before** any mask is written in
`DeviceSpooferEngine.applyProfile` (covers auto-apply via monitor, Master
Optimization Tier 3, and all UI paths). Device GPU family is inferred from the
real chipset (Qualcomm→Adreno; MediaTek/Exynos/Kirin/Tensor/Unisoc→Mali), profile
GPU family from `glVendor`/`glRenderer`; a provable mismatch blocks the apply
with an explanation (e.g. "GPU feature set mismatch: this device renders with
MALI but profile 'Apple A18 Pro' advertises APPLE — known ban vector — apply
blocked") and surfaces as a `🚫 Spoof blocked` toast in SettingsFragment instead
of the success dialog. A second check blocks SoC impersonations (e.g.
Snapdragon device + Dimensity 9400 profile) even when the GPU family matches.
Undetectable devices are allowed with a warning, never blocked on guesses.
`ChipsetVendor` gained `APPLE`; `SpoofValidator` (previously unwired, post-apply
read-back) is now logged after every successful apply as an in-app validation
pass. 13 new tests (SpoofSanityCheckerTest, 114 total); checker at 97.7% line /
75.9% branch, pure inference seams 100%. Gate green: 114 tests, lint 0, both
builds, release APK signature re-verified.

---

## Phase 3 — Hygiene & Docs (P3)

### 3.1 Manifest cleanup
**✅ DONE** — `android:allowBackup="false"` (spoof/tweak state no longer leaks
into cloud backup — verified in built APK via aapt2); `BootReceiver` now
`exported="false"` (system BOOT_COMPLETED still delivers to non-exported
manifest-registered receivers); shell-only permission declarations annotated
with a Play Console-friendly comment block pointing at
`tools/grant_permissions.sh` (declarations kept intentionally for ADB
grantability; `tools:ignore` retained for lint).

### 3.2 Versioning & release notes
**✅ DONE** — new `CHANGELOG.md` (root, derived from conventional commits,
grouped per release since v14/v15); debug builds now carry
`versionNameSuffix "-debug-<git sha>"` (verified: generated
`BuildConfig.VERSION_NAME = "16.0.0-PRO-debug-9c1f694"`, release stays
`16.0.0-PRO`); README gained the risk documentation (below) including the
per-game and panel-Hz "Known limitations" items.

### 3.3 Realistic docs & risk transparency
**✅ DONE** — README headline "100% safe for online anti-cheat ecosystems"
claim replaced with an honest summary + per-feature risk table (refresh
overrides low / spoofing high-known ban vector / config patching
medium–high / network low) + FAQ (Shizuku died / FPS not applying / game reset
config / how to export diagnostics / account-risk statement) + as-is MIT
warranty notice. The "🩺 Diagnostics" export path is linked from the FAQ so
support requests arrive with real data.

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
