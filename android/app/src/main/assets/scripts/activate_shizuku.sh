#!/usr/bin/env sh
# ==============================================================================
# Game Launcher Pro - Shizuku Service Self-Activator & Verifier (On-Device)
# ==============================================================================

echo "=== [GAME LAUNCHER PRO - SHIZUKU VERIFIER] ==="
if which rish >/dev/null 2>&1; then
    echo "[OK] rish binary found in environment PATH."
fi

# Check active Shizuku service
SHIZUKU_PID=$(pidof shizuku_starter 2>/dev/null || pidof moe.shizuku.privileged.api 2>/dev/null)
if [ -n "$SHIZUKU_PID" ]; then
    echo "[OK] Shizuku Privileged Binder Service is RUNNING (PID: $SHIZUKU_PID)."
else
    echo "[INFO] Starting Shizuku starter trigger..."
    sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh 2>/dev/null || \
    sh /data/user/0/moe.shizuku.privileged.api/start.sh 2>/dev/null
fi

echo "[INFO] Shizuku Verification Finished."
