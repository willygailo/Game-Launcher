# Changelog

All notable changes are derived from conventional commits (`git log --format=%s`).

## v16.0.0-PRO (code 16000)

### Diagnostics (1.4)
- New `com.gamebooster.app.diagnostics` package: `CrashLog` captures uncaught
  crashes to app storage via `Thread.setDefaultUncaughtExceptionHandler`;
  `DiagnosticsExporter` builds a shareable snapshot (app/device/Android
  version, `verifyEnforcementStatus()` output, spoof state, crash tail) and
  exports via FileProvider + `ACTION_SEND`.
- New "Diagnostics" card in Settings (REFRESH / EXPORT).

### Real-game effectiveness (2.x)
- Revert on game exit (2.2): AIDL `restoreCpuGpuGovernors()` (id 20), network
  restore, `GameStateReverter`, auto-monitor exits now revert system instead of
  re-locking (home-screen 185Hz lock removed). Backup-restore safety net (0.2).
- Patch read-back verification (2.3): `GameConfigPatchVerifier` + written-file
  read-back with CONFIRMED/UNVERIFIED reporting; per-game reset/integrity notes
  on the Games screen.
- Spoof pre-validation (2.4): `SpoofSanityChecker` blocks applying a profile
  whose GPU/SoC feature set provably mismatches the real device (ban vector),
  with explanation; post-apply validation logging.
- Other: SDK-specific command gating (2.1), per-step reporting (1.2),
  Shizuku lifecycle (1.1).

### Hygiene (3.x)
- Manifest cleanup (3.1): `allowBackup=false`, non-exported `BootReceiver`,
  shell-only permission annotations.
- Versioning (3.2): debug builds carry `-debug-<git sha>` suffix.
- Docs (3.3): honest per-feature risk table + FAQ (see README).

_Release commit: `a84e3fc` ("bump version to v16.0.0-PRO (code 16000)") — see
git log for the full commit list since v15.0.0-PRO (code 15000)._

## v15.0.0-PRO (code 15000)

- Android 12–16 dedicated platform; minSdk 31; GameBoosterApp crash shield;
  foreground service type fixes.
- New JankAndCacheCleanerEngine; universal game scanner; terminal UI theme
  refresh.
- Game patch configs: gyro settings, damage multipliers, optimized touch boost
  rates.

## v14.0.0-PRO (code 14000)

- Binary patcher test suite at 100% pass; additional supported games;
  automatic device spoofing on service startup; dynamic refresh resolution up
  to 185Hz for Extreme Performance profile.

_Format: each release section groups commits by conventional-commit scope.
Regenerate for a new release with the git log from the previous release tag._