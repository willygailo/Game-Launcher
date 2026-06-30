# Game Launcher Pro - Improvement Task List

Goal: improve the overall layout, refresh the visual design, polish core functions, and make the app feel like a professional Android gaming utility.

## Phase 0 - Product Direction And Baseline

- [ ] Define the app identity: "performance launcher", "game booster", or "system monitor first".
- [ ] Pick the primary user flow: open app, check device status, launch game, apply boost, review session.
- [ ] Create a short design brief for the new UI: darker premium gaming style, cleaner neon usage, fewer decorative labels, stronger hierarchy.
- [ ] Audit every screen for cramped text, overlapping rows, repeated components, unclear actions, and missing loading/error states.
- [ ] Capture before screenshots for Dashboard, Games, Game Details, Settings, widget, and overlay.
- [ ] Decide target device classes: small phones, large phones, tablets, high refresh rate devices, root and non-root users.

## Phase 1 - Design System Cleanup

- [ ] Create reusable Compose components for common UI:
  - [ ] `AppScreenScaffold`
  - [x] `SectionHeader`
  - [ ] `MetricCard`
  - [x] `StatusBadge`
  - [ ] `PermissionRow`
  - [ ] `BoostToggleRow`
  - [x] `PrimaryGradientButton`
  - [x] `EmptyState`
  - [x] `LoadingState`
- [ ] Replace emoji-based UI labels with Material icons where possible.
- [ ] Standardize card radius, spacing, borders, icon sizes, and button heights.
- [ ] Reduce neon overload by using neon only for active states, primary actions, and important metrics.
- [ ] Add light theme colors that are actually light instead of reusing the dark palette.
- [ ] Move hardcoded UI strings into `strings.xml`.
- [x] Review typography: remove excessive letter spacing from normal headers and compact UI text.
- [ ] Add Compose previews for major components and screens.

## Phase 2 - Navigation And App Shell

- [ ] Add a more polished app shell with consistent top bars and screen titles.
- [x] Keep bottom navigation for main sections, but hide it on Game Details if it feels crowded.
- [ ] Add animated selected state for bottom navigation without making the bar visually heavy.
- [ ] Add navigation route constants instead of building routes manually in multiple places.
- [x] Add safe argument encoding for `packageName` in game details navigation.
- [ ] Add transition animations between Dashboard, Games, Settings, and Details.
- [ ] Add back handling polish for expanded cards and details screens.

## Phase 3 - Dashboard Redesign

- [x] Redesign Dashboard as a professional "system cockpit".
- [x] Create a top status summary:
  - [x] Device rating
  - [x] Root or ADB mode
  - [x] Current FPS or refresh rate
  - [x] Battery temperature risk
- [ ] Convert CPU, GPU, RAM, Battery, Network, and Sessions into a consistent metric grid.
- [x] Add a clear primary action: "Optimize Now".
- [ ] Make benchmark a focused card with score, last run time, and run button.
- [x] Group advanced/root features into a collapsed "Advanced Controls" section.
- [ ] Add permission-aware messaging when a feature cannot work yet.
- [x] Add thermal warning states for high battery/device temperature.
- [x] Add refresh indicators so users know data is live or stale.
- [ ] Ensure dashboard cards do not overflow on small screens.

## Phase 4 - Games Screen Redesign

- [x] Improve the game list header with count, scan status, and quick refresh.
- [x] Add search icon, clear button, and better placeholder text to search.
- [ ] Add sort options:
  - [x] Recently played
  - [x] Name
  - [x] Most played
  - [x] Performance mode enabled
  - [ ] Installed date if available
- [ ] Add grid/list view toggle.
- [ ] Add category filter chips with stable spacing and horizontal scroll polish.
- [x] Add "scan progress" and "last scanned" copy.
- [ ] Improve empty states:
  - [x] No games detected
  - [x] No search results
  - [ ] Permission required
  - [ ] Scan failed
- [ ] Add pull-to-refresh if it fits the UX.
- [ ] Keep game icons cached to avoid jank while scrolling.

## Phase 5 - Game Card Redesign

- [x] Separate tap targets: card opens details, play button launches game, boost button toggles quick boost.
- [x] Replace the text-only "PLAY" box with a real Material button using a play icon.
- [ ] Add a compact active profile summary:
  - [ ] FPS target
  - [ ] Refresh lock
  - [ ] Touch boost
  - [ ] RAM mode
  - [ ] Graphics mode
- [ ] Move expanded boost settings into a bottom sheet or dedicated details tab if the card becomes too tall.
- [ ] Add disabled states and explanations for settings that require permissions/root/ADB.
- [ ] Clamp target FPS to game and device limits in the ViewModel/domain layer, not only UI.
- [ ] Add "Reset profile" action per game.
- [ ] Add "Duplicate settings to other games" action.
- [ ] Add haptic or visual feedback after launch/boost changes.

## Phase 6 - Game Details Upgrade

- [x] Add a stronger details header with game icon, name, package name, launch button, and boost state.
- [ ] Add tabs or sections:
  - [x] Overview
  - [x] Boost Profile
  - [x] Session History
  - [ ] Diagnostics
- [ ] Add session analytics:
  - [x] Average FPS
  - [ ] Best FPS
  - [x] Total play time
  - [ ] Battery drain per hour
  - [ ] Boosted vs unboosted sessions
- [ ] Add simple charts for FPS, duration, and battery drain.
- [ ] Add filter for session history: today, week, month, all.
- [ ] Add delete/clear session history with confirmation.
- [ ] Add export session report if useful for debugging.

## Phase 7 - Settings Redesign

- [ ] Group settings into clear sections:
  - [ ] Permissions
  - [ ] Booster behavior
  - [ ] Overlay
  - [ ] Advanced ADB
  - [ ] Root tools
  - [ ] Theme and appearance
  - [ ] Backup and restore
- [x] Add a permission health checklist at the top.
- [ ] Show why each permission is needed before asking for it.
- [x] Disable granted permission buttons or replace them with a status badge.
- [ ] Improve the ADB command card:
  - [ ] Copy command
  - [ ] Show package name
  - [ ] Show verification status
  - [ ] Add troubleshooting text
- [ ] Add overlay customization:
  - [ ] Position
  - [ ] Size
  - [ ] Opacity
  - [ ] FPS only or full stats
- [ ] Add import/export result states with success and error messages.
- [ ] Add confirmation before changing risky secure/root settings.

## Phase 8 - Function Tweaks

- [x] Performance maintenance v3.2.3:
  - [x] Bump app version to `3.2.3` / versionCode `323`
  - [x] Boost ON applies max-performance defaults
  - [x] Manual launch and auto detector use the same max-safe planner
  - [x] Known game FPS metadata supports up to `240`
  - [x] README updated with current release notes
- [x] Add full network support:
  - [x] WiFi transport detection
  - [x] Mobile data transport detection
  - [x] Dual WiFi + mobile data status
  - [x] Validated internet status
  - [x] 5G and 5G+ display support
  - [x] WiFi/mobile signal and bandwidth display
- [ ] Persist search query, selected category, sort mode, and list/grid mode.
- [ ] Add boost presets:
  - [ ] Balanced
  - [ ] Performance
  - [ ] Battery Saver
  - [ ] Competitive
  - [ ] Custom
- [ ] Add global default profile for newly detected games.
- [ ] Add per-game profile templates for known supported games.
- [ ] Add automatic profile suggestion based on device specs and game max FPS.
- [ ] Add permission-aware feature gating in ViewModels/use cases.
- [ ] Add proper error states for launch failures, missing package, and permission denial.
- [ ] Add debouncing for rapid toggle changes.
- [ ] Add benchmark history instead of only the latest result.
- [ ] Add session start/end reliability checks for auto game detection.
- [ ] Add notification actions: stop boost, open overlay settings, launch last game.
- [ ] Add widget actions for optimize, launch last game, and toggle overlay.
- [ ] Add Quick Settings tile state sync with actual booster state.

## Phase 9 - Performance And Stability

- [ ] Avoid loading app icons directly inside every recomposition.
- [ ] Cache game icons and metadata in repository or a dedicated icon loader.
- [ ] Use `derivedStateOf` for computed UI values that depend on collected state.
- [ ] Keep LazyColumn item keys stable everywhere.
- [ ] Add paging only if the game list becomes large enough to justify it.
- [ ] Audit background services for battery impact.
- [ ] Add service start/stop idempotency to avoid duplicate foreground services.
- [ ] Add notification channel creation and validation on startup.
- [ ] Add startup tracing around app launch, first screen render, and game scan.
- [ ] Add baseline profile if release performance becomes a target.
- [ ] Review `largeHeap=true`; remove if not clearly needed.

## Phase 10 - Android Policy And Permissions

- [ ] Review `QUERY_ALL_PACKAGES` usage and confirm it is necessary for the launcher use case.
- [ ] Add fallback game discovery when full package query access is limited.
- [ ] Review foreground service types for target SDK 36 behavior.
- [ ] Add Android 13+ notification permission handling state refresh after request.
- [ ] Add Android 14/15/16 compatibility checks for overlay, usage access, and foreground services.
- [ ] Add clear user-facing permission rationales.
- [ ] Make advanced/root features opt-in and clearly marked.
- [ ] Ensure exported receivers/services are intentional and protected where possible.

## Phase 11 - Data, Backup, And Profiles

- [ ] Validate imported profile JSON before writing it.
- [ ] Add profile schema versioning.
- [ ] Add backup restore conflict handling.
- [ ] Add "preview import" before applying imported profiles.
- [ ] Add migration tests for Room database changes.
- [ ] Add data cleanup for uninstalled games.
- [ ] Add "restore defaults" for global settings and per-game profiles.

## Phase 12 - Accessibility And UX Polish

- [ ] Add content descriptions for icon-only actions.
- [ ] Ensure buttons and switches meet minimum touch target size.
- [ ] Support larger font sizes without clipping.
- [ ] Check contrast for neon text on dark surfaces.
- [ ] Avoid using color alone for status.
- [ ] Add keyboard/focus handling where applicable.
- [ ] Add reduced-motion support for heavy animations.
- [ ] Add TalkBack-friendly labels for metrics and toggles.
- [ ] Remove UI text that sounds like marketing and replace it with clear utility labels.

## Phase 13 - Testing Plan

- [ ] Add unit tests for GamesViewModel search, filters, sort, and profile updates.
- [ ] Add unit tests for target FPS clamping.
- [ ] Add unit tests for permission-aware feature gating.
- [ ] Add unit tests for import/export profile validation.
- [ ] Add tests for launch-and-boost use case failure paths.
- [ ] Add Compose UI tests for:
  - [ ] Dashboard loads metrics
  - [ ] Game search filters correctly
  - [ ] Game card expands and updates toggles
  - [ ] Settings permission cards show correct states
  - [ ] Game details loads sessions
- [ ] Add screenshot tests or manual screenshot checklist for major screens.
- [ ] Add emulator test matrix:
  - [ ] Android 10
  - [ ] Android 13
  - [ ] Android 15
  - [ ] Android 16/API 36
  - [ ] small phone
  - [ ] large phone
  - [ ] high refresh rate device

## Phase 14 - Release Readiness

- [ ] Replace debug signing for release builds.
- [ ] Confirm Crashlytics/Analytics behavior is controlled per build type.
- [ ] Review ProGuard/R8 rules for Hilt, Room, Retrofit, Firebase, and TensorFlow Lite.
- [ ] Add release notes template.
- [ ] Add Play Store privacy and permission notes.
- [ ] Create final screenshots after redesign.
- [ ] Add version bump checklist.
- [ ] Add smoke test checklist before APK/AAB export.

## Suggested Build Order

1. Design system cleanup.
2. Dashboard redesign.
3. Games screen and game card redesign.
4. Settings permission UX.
5. Game details analytics.
6. Function tweaks and profile presets.
7. Performance and policy hardening.
8. Tests and release readiness.

## Definition Of Done

- [ ] Main screens look consistent and professional.
- [ ] Important actions are obvious without reading long explanations.
- [ ] Small screens do not clip text or controls.
- [ ] Features explain permission/root/ADB requirements before failing.
- [ ] Game launch, boost settings, overlay, widget, and QS tile stay in sync.
- [ ] Core flows have unit or UI test coverage.
- [ ] Release build passes smoke testing on at least two Android versions.
