# v3.2.9 - Extreme Gaming Mode Update

This release brings several highly requested features and optimizations to unlock the absolute maximum performance from your device. 

## What's New
- **Force Max Performance (Danger Mode):** A new setting in Secure Settings that completely bypasses OS thermal throttling, locks the CPU/GPU to maximum frequencies, and forces hardware acceleration properties for the smoothest gaming experience possible. 
- **Aggressive Network Restrictor:** Block background network activity from other apps to ensure the lowest ping for your active game.
- **Enhanced Overlay:** The in-game overlay now displays real-time **CPU & Battery Temperatures**, as well as RAM and Network status.
- **Improved Bypass Charging:** Refactored command execution checks and sysfs path handling to ensure Bypass Charging functions more reliably across various devices.
- **Usage Access Fix:** Ensured proper usage of `AppOpsManager` so the app correctly requests and handles usage access for Android 10+.

**Warning:** The new *Force Max Performance* toggle is intended for extreme scenarios. It forces `thermalservice override-status 0` and locks the power mode. This will generate significant heat and drain the battery faster. Use at your own risk!
