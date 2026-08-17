#!/usr/bin/env sh
# ==============================================================================
# Game Launcher Pro - Self Permission Provisioner via Shizuku Shell
# ==============================================================================

PACKAGE_NAME="com.gamebooster.app"
echo "=== [PROVISIONING PRIVILEGED SYSTEM PERMISSIONS] ==="
echo "Target Package: $PACKAGE_NAME"

pm grant "$PACKAGE_NAME" android.permission.WRITE_SECURE_SETTINGS 2>/dev/null && echo "[OK] WRITE_SECURE_SETTINGS granted"
pm grant "$PACKAGE_NAME" android.permission.DUMP 2>/dev/null && echo "[OK] DUMP granted"
pm grant "$PACKAGE_NAME" android.permission.PACKAGE_USAGE_STATS 2>/dev/null && echo "[OK] PACKAGE_USAGE_STATS granted"
pm grant "$PACKAGE_NAME" android.permission.SYSTEM_ALERT_WINDOW 2>/dev/null && echo "[OK] SYSTEM_ALERT_WINDOW granted"
pm grant "$PACKAGE_NAME" android.permission.READ_LOGS 2>/dev/null && echo "[OK] READ_LOGS granted"
pm grant "$PACKAGE_NAME" android.permission.BATTERY_STATS 2>/dev/null && echo "[OK] BATTERY_STATS granted"

appops set "$PACKAGE_NAME" GET_USAGE_STATS allow 2>/dev/null && echo "[OK] AppOp GET_USAGE_STATS allow"
appops set "$PACKAGE_NAME" SYSTEM_ALERT_WINDOW allow 2>/dev/null && echo "[OK] AppOp SYSTEM_ALERT_WINDOW allow"
appops set "$PACKAGE_NAME" MANAGE_EXTERNAL_STORAGE allow 2>/dev/null && echo "[OK] AppOp MANAGE_EXTERNAL_STORAGE allow"

chmod -R 777 /sdcard/Android/data /sdcard/Android/obb 2>/dev/null
echo "=== [PERMISSIONS PROVISIONING COMPLETE] ==="
