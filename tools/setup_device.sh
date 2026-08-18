#!/usr/bin/env bash
# ==============================================================================
# Game Launcher Pro - Complete Device Setup & Onboarding Tool
# Installs Game Launcher Pro, activates Shizuku, and grants permissions.
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
echo "    Game Launcher Pro - All-in-One Device Setup"
echo "======================================================"
echo "[INFO] Using ADB: $ADB_BIN"

# Check connection
DEVICE_COUNT=$("$ADB_BIN" devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "[WARNING] No connected Android device found."
    echo "          Please connect your phone via USB with USB Debugging enabled."
    exit 1
fi

DEVICE_MODEL=$("$ADB_BIN" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
ANDROID_VER=$("$ADB_BIN" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')
SDK_VER=$("$ADB_BIN" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')

echo "[INFO] Connected Device: $DEVICE_MODEL (Android $ANDROID_VER, API $SDK_VER)"

# Step 1: Install APK if present in build outputs
APK_PATH="$ROOT_DIR/android/app/build/outputs/apk/release/Game_Space.apk"
if [ ! -f "$APK_PATH" ]; then
    APK_PATH="$ROOT_DIR/android/app/build/outputs/apk/debug/Game_Space_Debug.apk"
fi

if [ -f "$APK_PATH" ]; then
    echo "[STEP 1/3] Installing Game Launcher Pro ($APK_PATH)..."
    "$ADB_BIN" install -r -d -g "$APK_PATH"
else
    echo "[STEP 1/3] No local build APK found at default build paths (Skipping install)."
fi

# Step 2: Grant permissions
echo "[STEP 2/3] Granting privileged system permissions..."
"$SCRIPT_DIR/grant_permissions.sh"

# Step 3: Activate Shizuku
echo "[STEP 3/3] Activating Shizuku Service..."
"$SCRIPT_DIR/activate_shizuku.sh"

echo "======================================================"
echo " [COMPLETE] Game Launcher Pro is ready to use!"
echo "======================================================"
