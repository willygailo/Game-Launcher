# v3.5.2 - Performance Profile Persistence & System Tweaks Fixes

This release resolves key UI persistence issues and improves OEM thermal throttling bypass and FPS overlay controls.

## 🚀 What's New & Fixed in v3.5.2

- **Persistent Performance Profiles (ECO / BALANCED / PRO):**
  - Performance mode selections (ECO, BALANCED, PRO) are now saved directly in DataStore (`SettingsPreferences`) and managed by `DashboardViewModel`.
  - Selecting **PRO** mode or **ECO** mode persists when navigating away from the Dashboard and pressing Back.

- **FPS Telemetry Overlay Toggle Fix:**
  - Integrated `SettingsPreferences` with `MonitorViewModel` to persist the overlay state across updates and screen re-entry.
  - Toggling FPS overlay in the Monitor screen immediately controls `OverlayService` (starts if enabled, stops if disabled).

- **OEM Thermal Throttling Bypass Enhancements:**
  - `TweaksRepositoryImpl` now reads saved tweak preferences on screen initialization.
  - Toggling OEM Thermal Throttling Bypass executes multi-tier fallback commands (`cmd thermalservice override-status 0`, global/secure `thermal_limit_enabled`, `thermal_throttling_disabled`, `thermal_control_limit`).
