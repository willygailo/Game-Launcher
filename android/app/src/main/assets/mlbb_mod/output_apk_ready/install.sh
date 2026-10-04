#!/system/bin/sh
# ==============================================================================
# MLBB High Damage, Zero-CD, and Combat Overdrive Drop-in Installer
# Target: /storage/emulated/0/Android/data/<pkg>/files/dragon2017/assets/
# ==============================================================================

echo "[*] Initializing MLBB Mod Installer..."

PACKAGES="com.mobile.legends com.mobilelegends.mi com.vng.mlbbvn com.mobilelegends.na com.mobile.legends.vng com.mobile.legends.kr com.mobile.legends.jp com.mobilelegends.hw com.mobile.legends.moonton"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PAYLOAD_DIR="$SCRIPT_DIR/assets"

if [ ! -d "$PAYLOAD_DIR" ]; then
    PAYLOAD_DIR="$SCRIPT_DIR/modified"
fi

if [ ! -d "$PAYLOAD_DIR" ]; then
    echo "[-] Error: Payload directory not found."
    exit 1
fi

COUNT=0
for PKG in $PACKAGES; do
    TARGET_BASE="/storage/emulated/0/Android/data/$PKG/files/dragon2017/assets"
    ALT_TARGET="/sdcard/Android/data/$PKG/files/dragon2017/assets"

    if [ -d "/storage/emulated/0/Android/data/$PKG" ] || [ -d "/sdcard/Android/data/$PKG" ]; then
        echo "[+] Found target MLBB package: $PKG"
        
        # Ensure directories
        mkdir -p "$TARGET_BASE/Document/android" 2>/dev/null
        mkdir -p "$TARGET_BASE/version/android" 2>/dev/null

        # Backup original Document/android if not already backed up
        if [ -d "$TARGET_BASE/Document/android" ] && [ ! -d "$TARGET_BASE/Document/android_backup" ]; then
            echo "[*] Creating backup for $PKG..."
            cp -r "$TARGET_BASE/Document/android" "$TARGET_BASE/Document/android_backup" 2>/dev/null
        fi

        # Copy modified payload
        echo "[*] Injecting stat overdrive into $PKG..."
        cp -rf "$PAYLOAD_DIR"/* "$TARGET_BASE/" 2>/dev/null
        cp -rf "$PAYLOAD_DIR"/* "$ALT_TARGET/" 2>/dev/null

        # Set permissions
        chmod -R 666 "$TARGET_BASE/Document" 2>/dev/null
        chmod -R 777 "$TARGET_BASE/Document/android" 2>/dev/null
        chmod 666 "$TARGET_BASE/Document/android/BattleConfig.json" 2>/dev/null
        chmod 666 "$TARGET_BASE/Document/android/HeroStatConfig.json" 2>/dev/null
        chmod 666 "$TARGET_BASE/Document/android/CameraConfig.json" 2>/dev/null
        chmod 666 "$TARGET_BASE/Document/android/res_skip_patch.xml" 2>/dev/null

        echo "[+] Successfully injected $PKG"
        COUNT=$((COUNT + 1))
    fi
done

if [ "$COUNT" -eq 0 ]; then
    echo "[!] No active MLBB installation found in /sdcard/Android/data."
    echo "[*] Staging into general /sdcard/GameLauncher/mods/com.mobile.legends/ for AutoSync..."
    mkdir -p /sdcard/GameLauncher/mods/com.mobile.legends/dragon2017/assets/
    cp -rf "$PAYLOAD_DIR"/* /sdcard/GameLauncher/mods/com.mobile.legends/dragon2017/assets/ 2>/dev/null
    echo "[+] Staged to /sdcard/GameLauncher/mods/com.mobile.legends/ successfully."
else
    echo "[✓] Deployment complete! Total packages injected: $COUNT"
fi
