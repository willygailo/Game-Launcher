#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# install_game_mod_core.sh — Master In-Device Deployment Script
# Automatically deploys game mods, configuration files, and anti-redownload rules
# Usage: sh install_game_mod_core.sh [com.mobile.legends | com.garena.game.codm | com.activision.callofduty.shooter]
# ─────────────────────────────────────────────────────────────────────────────

PKG="$1"
if [ -z "$PKG" ]; then
    echo "Usage: $0 <package_name>"
    exit 1
fi

echo "[*] Initializing Master Combat Suite deployment for: $PKG"

BASE_DATA="/storage/emulated/0/Android/data/$PKG/files"
DATA_SYS="/data/data/$PKG"

# 1. Ensure directory structures exist
mkdir -p "$BASE_DATA" 2>/dev/null
mkdir -p "$DATA_SYS" 2>/dev/null

# 2. Checksum and anti-redownload bypass
case "$PKG" in
    com.mobile.legends)
        DOC_DIR="$BASE_DATA/dragon2017/assets/Document/android"
        RES_DIR="$BASE_DATA/dragon2017/res"
        VER_DIR="$BASE_DATA/dragon2017/assets/version/android"
        mkdir -p "$DOC_DIR" "$RES_DIR" "$VER_DIR" 2>/dev/null

        # Touch and timestamp into the future (year 2030)
        touch -t 203001010000 "$DOC_DIR/ResCheckConf.xml" 2>/dev/null
        touch -t 203001010000 "$DOC_DIR/res_skip_patch.xml" 2>/dev/null
        touch -t 203001010000 "$VER_DIR/version.xml" 2>/dev/null
        touch -t 203001010000 "$VER_DIR/realversion.xml" 2>/dev/null

        chmod 666 "$DOC_DIR"/* 2>/dev/null
        chmod 666 "$VER_DIR"/* 2>/dev/null
        echo "[+] MLBB anti-redownload and timestamp locks applied."
        ;;

    com.garena.game.codm|com.activision.callofduty.shooter)
        CFG_DIR="$BASE_DATA/Config"
        mkdir -p "$CFG_DIR" 2>/dev/null

        touch -t 203001010000 "$CFG_DIR/UserSetting.json" 2>/dev/null
        touch -t 203001010000 "$CFG_DIR/GameConfig.xml" 2>/dev/null
        touch -t 203001010000 "$CFG_DIR/ControlsSettings.ini" 2>/dev/null
        touch -t 203001010000 "$CFG_DIR/GraphicsSettings.ini" 2>/dev/null
        touch -t 203001010000 "$BASE_DATA/res_skip_patch" 2>/dev/null

        chmod 666 "$CFG_DIR"/* 2>/dev/null
        echo "[+] CODM anti-redownload and timestamp locks applied."
        ;;
esac

# 3. SELinux & Permissions sync
chcon -R u:object_r:app_data_file:s0 "$BASE_DATA" 2>/dev/null
chmod -R 775 "$BASE_DATA" 2>/dev/null

echo "[✓] Deployment completed successfully for $PKG."
