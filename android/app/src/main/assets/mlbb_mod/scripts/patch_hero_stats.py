#!/usr/bin/env python3
"""
patch_hero_stats.py — MLBB Stat Mod Engine (Damage, Attack, Cooldown, Defense, Speed, No-Delay, Combo)
Targets MLBB dragon2017/assets/Document/android and hero/battle configs.
Covers 5 roles: Mage, Fighter, Marksman, Assassin, Tank.
"""

import os
import sys
import json
import shutil
import hashlib

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ORIGINAL_DIR = os.path.join(BASE_DIR, "original")
MODIFIED_DIR = os.path.join(BASE_DIR, "modified")
BACKUP_DIR = os.path.join(BASE_DIR, "backup")
OUTPUT_DIR = os.path.join(BASE_DIR, "output_apk_ready")

# ─── 1. ALL-ROLE HERO STAT MATRIX ─────────────────────────────────────────────

ROLE_STATS = {
    "Mage": {
        "magic_power_base": 3500,
        "magic_penetration": 2500,
        "cooldown_ratio": 0.05,
        "mana_cost": 0,
        "spell_vamp_percent": 80,
        "cast_delay_ms": 0,
        "movement_speed": 450,
        "heroes": ["Gusion", "Kagura", "Lunox", "Xavier", "Cecilion", "Valentina", "Kadita", "Nana", "Pharsa", "Eudora", "Harley", "Cyclops", "Valir", "Lylia", "Novaria"]
    },
    "Assassin": {
        "physical_attack_base": 3200,
        "physical_penetration": 2500,
        "cooldown_ratio": 0.05,
        "energy_cost": 0,
        "cast_delay_ms": 0,
        "attack_speed": 4.5,
        "crit_rate": 1.0,
        "crit_multiplier": 3.0,
        "movement_speed": 480,
        "heroes": ["Fanny", "Ling", "Lancelot", "Hayabusa", "Helcurt", "Natalia", "Benedetta", "Nolan", "Joy", "Saber", "Aamon", "Hanzo", "Karina", "Alucard"]
    },
    "Marksman": {
        "physical_attack_base": 3000,
        "attack_speed": 5.0,
        "attack_speed_cap": 10.0,
        "attack_interval_min": 0.0,
        "crit_rate": 1.0,
        "crit_multiplier": 3.5,
        "lifesteal_percent": 100,
        "cooldown_ratio": 0.05,
        "cast_delay_ms": 0,
        "movement_speed": 430,
        "heroes": ["Claude", "Beatrix", "Wanwan", "Brody", "Moskov", "Miya", "Layla", "Karrie", "Bruno", "Lesley", "Clint", "Granger", "Popol", "Irithel", "Hanabi"]
    },
    "Fighter": {
        "physical_attack_base": 2800,
        "physical_defense": 3000,
        "magic_defense": 3000,
        "damage_reduction": 0.85,
        "spell_vamp_percent": 90,
        "cooldown_ratio": 0.05,
        "cast_delay_ms": 0,
        "movement_speed": 420,
        "heroes": ["Chou", "Yu Zhong", "Paquito", "Arlott", "Martis", "Lapu-Lapu", "Alpha", "Ruby", "Thamuz", "Dyrroth", "Terizla", "Badang", "Freya", "Guinevere", "Silvanna", "Sun", "Zilong", "Yin", "Julian", "Cici"]
    },
    "Tank": {
        "max_hp": 45000,
        "physical_defense": 4500,
        "magic_defense": 4500,
        "damage_reduction": 0.95,
        "hp_regen_rate": 250,
        "cooldown_ratio": 0.05,
        "cast_delay_ms": 0,
        "cc_resilience": 1.0,
        "movement_speed": 410,
        "heroes": ["Tigreal", "Atlas", "Khufra", "Franco", "Minotaur", "Grock", "Johnson", "Fredrinn", "Akai", "Hylos", "Belerick", "Gatotkaca", "Uranus", "Baxia", "Lolita", "Edith", "Chip"]
    }
}

# ─── 2. GLOBAL BATTLE CONFIG OVERDRIVE ────────────────────────────────────────

GLOBAL_BATTLE_CONFIG = {
    "BattleSystem": {
        # Damage & Attack
        "DamageLockMax": 1,
        "DamageMultiplier": 3.0,
        "PhysicalDamageBase": 3500,
        "MagicDamageBase": 3500,
        "TrueDmgConversion": 1,
        "CritMultiplier": 3.5,
        "CritRateBoost": 1,
        "PenetrationBoost": 1,
        "PhysicalPenBoost": 2500,
        "MagicPenBoost": 2500,
        "BasicAttackRate": 10.0,
        "AttackSpeedBoost": 10.0,
        "AttackSpeedCap": 10.0,
        "AutoAttackInterval": 0.0,
        "BasicAttackInterval": 0.0,
        "AttackAnimSpeed": 10.0,

        # Cooldown Near Zero & Resource
        "CooldownReduction": 1.0,
        "SkillCDRatio": 0.05,
        "SkillCooldownReduction": 0.95,
        "GlobalCDR": 40,
        "ZeroSkillCost": 1,
        "ZeroManaCost": 1,
        "ZeroEnergyCost": 1,

        # Defense & Sustain
        "PhysicalDefense": 3000,
        "MagicDefense": 3000,
        "DamageReduction": 0.90,
        "ShieldBoost": 1,
        "PassiveShieldRegen": 1,
        "LifestealPercent": 100,
        "SpellVampBoost": 1,

        # Speed, No-Delay & Instant Combo
        "MovementSpeedBoost": 1.0,
        "MovementSpeedMax": 1,
        "SkillCastDelayMs": 0,
        "ZeroDelaySkillTap": 1,
        "FastSkillCycle": 1,
        "FastSkillReleaseSpeed": 10,
        "ZeroInputDelay": 1,
        "ZeroInputLag": 1,
        "TouchZeroDelay": 1,
        "TouchPollingRate": 1000,
        "HitRegSyncRate": 1000,
        "InputBufferRate": 1000,
        "FrameSyncDamage": 1,
        "EffectiveDPSMode": 3,
        "bFramePacingEnabled": True,
        "r.OneFrameThreadLag": 0,
        "r.FinishCurrentFrame": 0,

        # Camera & Tactical Aim
        "CameraHeight": 4,
        "CameraSensitivity": 100,
        "ScreenSensitivity": 100,
        "ScreenShake": 0,
        "Vibrate": 0,
        "SkillSmartAim": 1,
        "HeroLock": 1,
        "AimMethod": 1,
        "TargetPriority": 0,
        "CreepHP": 1,
        "DamageText": 1,
        "HitEffect": 1,
        "RetributionYellowThresholdIndicator": 1,
        "CounterBuildArmorPen": 1,
        "CounterBuildMagicPen": 1,
        "CounterBuildAntiHeal": 1
    }
}

# ─── 3. PATCHING FUNCTIONS ────────────────────────────────────────────────────

def prepare_directories():
    for d in [ORIGINAL_DIR, MODIFIED_DIR, BACKUP_DIR, OUTPUT_DIR]:
        os.makedirs(d, exist_ok=True)
    # Mirror structure from original to modified
    if os.path.exists(ORIGINAL_DIR):
        for root, dirs, files in os.walk(ORIGINAL_DIR):
            rel_path = os.path.relpath(root, ORIGINAL_DIR)
            dest_dir = os.path.join(MODIFIED_DIR, rel_path)
            os.makedirs(dest_dir, exist_ok=True)
            for f in files:
                src_file = os.path.join(root, f)
                dst_file = os.path.join(dest_dir, f)
                shutil.copy2(src_file, dst_file)
    print("[+] Workspace directories structured.")

def generate_hero_stat_config():
    doc_android = os.path.join(MODIFIED_DIR, "Document", "android")
    os.makedirs(doc_android, exist_ok=True)
    
    # 1. HeroStatConfig.json
    hero_cfg_path = os.path.join(doc_android, "HeroStatConfig.json")
    with open(hero_cfg_path, "w") as f:
        json.dump(ROLE_STATS, f, indent=2)
    print(f"[+] Generated {hero_cfg_path}")

    # 2. BattleConfig.json (Combines Tactical + Full Role Overdrive)
    battle_cfg_path = os.path.join(doc_android, "BattleConfig.json")
    with open(battle_cfg_path, "w") as f:
        json.dump(GLOBAL_BATTLE_CONFIG, f, indent=2)
    print(f"[+] Injected combat overdrive into {battle_cfg_path}")

    # Also mirror into Document/ root if needed
    doc_root = os.path.join(MODIFIED_DIR, "Document")
    shutil.copy2(battle_cfg_path, os.path.join(doc_root, "BattleConfig.json"))
    shutil.copy2(hero_cfg_path, os.path.join(doc_root, "HeroStatConfig.json"))

def update_res_skip_patch():
    doc_android = os.path.join(MODIFIED_DIR, "Document", "android")
    skip_path = os.path.join(doc_android, "res_skip_patch.xml")
    xml_content = """<?xml version="1.0" encoding="utf-8"?>
<root>
  <skip_check enable="true" />
  <version name="null" />
  <patch_lock value="1" />
  <integrity_bypass status="active" />
</root>
"""
    with open(skip_path, "w") as f:
        f.write(xml_content)
    print(f"[+] Updated patch bypass in {skip_path}")

def generate_installer_script():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    installer_path = os.path.join(OUTPUT_DIR, "install.sh")
    
    script_content = """#!/system/bin/sh
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
"""
    with open(installer_path, "w") as f:
        f.write(script_content)
    os.chmod(installer_path, 0o755)
    print(f"[+] Generated executable installer at {installer_path}")

def package_output():
    # Copy modified assets into output_apk_ready/assets
    assets_dest = os.path.join(OUTPUT_DIR, "assets")
    if os.path.exists(assets_dest):
        shutil.rmtree(assets_dest)
    shutil.copytree(MODIFIED_DIR, assets_dest)

    # Create zip package
    zip_path = os.path.join(OUTPUT_DIR, "mlbb_mod_payload")
    shutil.make_archive(zip_path, 'zip', MODIFIED_DIR)
    print(f"[+] Packaged output zip at {zip_path}.zip")

def main():
    print("=== MLBB Stat Mod Engine: Batch Processor ===")
    prepare_directories()
    generate_hero_stat_config()
    update_res_skip_patch()
    generate_installer_script()
    package_output()
    print("=== Pipeline Complete! Ready in output_apk_ready/ ===")

if __name__ == "__main__":
    main()
