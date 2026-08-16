#!/usr/bin/env bash
# ==============================================================================
# Game Launcher Pro - Shizuku Service Activator
# Activates the Shizuku Privileged Service on connected Android device via ADB.
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ADB_BIN="$ROOT_DIR/platform-tools-latest-linux/platform-tools/adb"

# Fallback to system ADB if bundled binary not found
if [ ! -x "$ADB_BIN" ]; then
    if command -v adb >/dev/null 2>&1; then
        ADB_BIN="$(command -v adb)"
    else
        echo "[ERROR] ADB binary not found at $ADB_BIN and not in PATH."
        exit 1
    fi
fi

echo "======================================================"
echo "    Game Launcher Pro - Shizuku Activator"
echo "======================================================"
echo "[INFO] Using ADB: $ADB_BIN"

# Check connected devices
echo "[INFO] Detecting connected Android devices..."
"$ADB_BIN" devices

DEVICE_COUNT=$("$ADB_BIN" devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "[WARNING] No authorized Android device detected."
    echo "          Please ensure USB Debugging or Wireless Debugging is ENABLED"
    echo "          and your device is plugged in."
    exit 1
fi

echo "[INFO] Found $DEVICE_COUNT active device(s)."
echo "[INFO] Starting Shizuku service via ADB..."

# Start Shizuku service using the standard privileged API starter script
RESULT=$("$ADB_BIN" shell "sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh 2>/dev/null || sh /data/user/0/moe.shizuku.privileged.api/start.sh 2>/dev/null || (which rish >/dev/null && echo 'rish-present')")

echo "$RESULT"

# Verify if Shizuku binder process is active
SHIZUKU_PID=$("$ADB_BIN" shell "pidof shizuku_starter || pidof moe.shizuku.privileged.api" 2>/dev/null)

if [ -n "$SHIZUKU_PID" ] || echo "$RESULT" | grep -q "shizuku_starter exit with 0"; then
    echo "======================================================"
    echo " [SUCCESS] Shizuku has been activated successfully!"
    echo "======================================================"
else
    echo "======================================================"
    echo " [INFO] Shizuku starter command executed."
    echo " Please open the Shizuku app on your device to confirm."
    echo "======================================================"
fi
