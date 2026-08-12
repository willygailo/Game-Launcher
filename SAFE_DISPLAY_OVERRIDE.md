# Safe Display and Game FPS Overrides

Game Launcher Pro uses a capability-gated override path on Android 13–16.

## What it does

- Detects refresh modes exposed by the active Android display.
- Lets the user request only native panel rates such as 60, 90, 120, 144, or 165 Hz.
- Uses Shizuku when connected; otherwise, it can use a user-granted root backend when available.
- Requests Android Game Mode performance configuration for a selected installed package.
- Saves the previous `peak_refresh_rate`, `min_refresh_rate`, `user_refresh_rate`, and Game Mode overlay before changing them.
- Restores the saved values when a game session ends or the user selects restore.

## Important limits

- Shizuku provides privileged **shell** access, not root.
- Root access is optional and must be granted by the device owner through an installed root manager.
- Neither Shizuku nor root can create a missing hardware display mode. A 120 Hz panel cannot become a true 144/165 Hz panel.
- Game FPS is controlled by the game engine, its anti-cheat/policy, Android Game Mode, the display, thermal state, and OEM policy. A request is not proof that a game renders at that FPS.
- Android's documented `game_overlay` FPS intervention supports 30, 40, 45, 60, 90, and 120 FPS. 144/165 FPS requests are sent only through the Game Manager path where the OS and game support them.

## Verification states

- **Applied**: Android reports the requested native display rate.
- **Requested but deferred**: the preference was saved, but the display has not switched yet (commonly due to thermal, battery, or OEM policy).
- **Unsupported**: the rate does not exist in the display modes reported by Android.
- **Permission denied**: connect Shizuku or grant root access.

The project intentionally does not use hard-coded SurfaceFlinger Binder transaction IDs, unverified vendor property writes, or a fake `global` application package for FPS settings.
