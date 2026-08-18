#!/usr/bin/env bash
# ==============================================================================
# Game Launcher Pro - Privileged Permission Granter
# Grants required Android system permissions to com.gamebooster.app via ADB.
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ADB_BIN="$ROOT_DIR/platform-tools-latest-linux/platform-tools/adb"
PACKAGE_NAME="com.gamebooster.app"

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
echo "    Game Launcher Pro - Permission Granter"
echo "======================================================"
echo "[INFO] Using ADB: $ADB_BIN"
echo "[INFO] Target Package: $PACKAGE_NAME"

DEVICE_COUNT=$("$ADB_BIN" devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "[WARNING] No authorized Android device detected."
    echo "          Please ensure USB Debugging is ENABLED and device is connected."
    exit 1
fi

echo "[INFO] Granting privileged system permissions..."

PERMISSIONS=(
    "android.permission.WRITE_SECURE_SETTINGS"
    "android.permission.DUMP"
    "android.permission.PACKAGE_USAGE_STATS"
    "android.permission.SYSTEM_ALERT_WINDOW"
    "android.permission.READ_LOGS"
    "android.permission.CHANGE_CONFIGURATION"
    "android.permission.BATTERY_STATS"
    "android.permission.POST_NOTIFICATIONS"
)

for PERM in "${PERMISSIONS[@]}"; do
    echo -n " -> Granting $PERM ... "
    "$ADB_BIN" shell pm grant "$PACKAGE_NAME" "$PERM" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "[OK]"
    else
        echo "[FAILED / NOT REQUIRED]"
    fi
done

echo "[INFO] Granting Special AppOps permissions..."

# Special AppOps
echo -n " -> AppOp GET_USAGE_STATS ... "
"$ADB_BIN" shell appops set "$PACKAGE_NAME" GET_USAGE_STATS allow 2>/dev/null && echo "[OK]" || echo "[FAILED]"

echo -n " -> AppOp SYSTEM_ALERT_WINDOW ... "
"$ADB_BIN" shell appops set "$PACKAGE_NAME" SYSTEM_ALERT_WINDOW allow 2>/dev/null && echo "[OK]" || echo "[FAILED]"

echo -n " -> AppOp MANAGE_EXTERNAL_STORAGE ... "
"$ADB_BIN" shell appops set "$PACKAGE_NAME" MANAGE_EXTERNAL_STORAGE allow 2>/dev/null && echo "[OK]" || echo "[FAILED]"

echo "======================================================"
echo " [SUCCESS] Permissions successfully provisioned for $PACKAGE_NAME!"
echo "======================================================"
