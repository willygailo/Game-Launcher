#!/usr/bin/env python3
"""
patch_obb.py — MLBB OBB binary patcher
Run on Linux laptop after pulling OBB via ADB.

Usage:
  python3 patch_obb.py --mode apply    # apply all patches
  python3 patch_obb.py --mode verify   # check patch status
  python3 patch_obb.py --mode revert   # restore originals
  python3 patch_obb.py --mode list     # list all patches
  python3 patch_obb.py --patch fog_disable --mode apply
"""

import argparse
import struct
import shutil
from pathlib import Path

# ─── CONFIG ────────────────────────────────────────────────────────

OBB_DIR    = Path.home() / "mlbb_patch" / "obb"
BACKUP_DIR = Path.home() / "mlbb_patch" / "original_backup"

# ─── PATCH TABLE ───────────────────────────────────────────────────
# !! OFFSETS ARE EXAMPLES — re-derive from YOUR device's libmoba.so !!

def f32(v: float) -> bytes:
    return struct.pack("<f", v)

PATCHES = {
    "fog_disable": {
        "file":        "main.*.com.mobile.legends.obb",
        "offset":      0x1A3F20,
        "original":    bytes([0x01, 0x00, 0x00, 0x54]),  # CBZ
        "patched":     bytes([0x00, 0x00, 0x00, 0x14]),  # B (unconditional)
        "description": "Disable fog of war check",
    },
    "map_visibility": {
        "file":        "main.*.com.mobile.legends.obb",
        "offset":      0x1A4008,
        "original":    bytes([0x00, 0x00, 0x40, 0x39]),  # LDRB W0
        "patched":     bytes([0x01, 0x00, 0x80, 0x52]),  # MOV W0, #1
        "description": "Force map visibility = 1",
    },
    "drone_fov_120": {
        "file":        "main.*.com.mobile.legends.obb",
        "offset":      0x3BC440,
        "original":    f32(60.0),
        "patched":     f32(120.0),
        "description": "Drone FOV 60 → 120",
    },
    "damage_2x": {
        "file":        "main.*.com.mobile.legends.obb",
        "offset":      0x12AB3C0,
        "original":    f32(1.0),
        "patched":     f32(2.0),
        "description": "Damage multiplier 1.0 → 2.0",
    },
}

# ─── UTILS ─────────────────────────────────────────────────────────

def find_obb(pattern):
    matches = list(OBB_DIR.glob(pattern))
    if not matches:
        print(f"[!] OBB not found: {OBB_DIR / pattern}")
        print(f"    Run first: adb pull /sdcard/Android/obb/com.mobile.legends/ ~/mlbb_patch/obb/")
        return None
    return matches[0]

def hex_str(b):
    return " ".join(f"{x:02X}" for x in b)

def backup(obb_path):
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    dst = BACKUP_DIR / obb_path.name
    if not dst.exists():
        print(f"[*] Backup → {dst}")
        shutil.copy2(obb_path, dst)

def read_at(path, offset, n):
    with open(path, "rb") as f:
        f.seek(offset); return f.read(n)

def write_at(path, offset, data):
    with open(path, "r+b") as f:
        f.seek(offset); f.write(data)

# ─── OPERATIONS ────────────────────────────────────────────────────

def verify(key, p):
    obb = find_obb(p["file"])
    if not obb: return "OBB_NOT_FOUND"
    cur = read_at(obb, p["offset"], len(p["original"]))
    if cur == p["patched"]:   return "PATCHED"
    if cur == p["original"]:  return "ORIGINAL"
    return f"MISMATCH({hex_str(cur)})"

def apply(key, p):
    obb = find_obb(p["file"])
    if not obb: return False
    backup(obb)
    cur = read_at(obb, p["offset"], len(p["original"]))
    if cur == p["patched"]:
        print(f"  [{key}] Already patched"); return True
    if cur != p["original"]:
        print(f"  [{key}] MISMATCH @ 0x{p['offset']:X}")
        print(f"          Expected: {hex_str(p['original'])}")
        print(f"          Found:    {hex_str(cur)}")
        print(f"          → Wrong MLBB version — re-derive offsets")
        return False
    write_at(obb, p["offset"], p["patched"])
    print(f"  [{key}] ✓ @ 0x{p['offset']:X} — {p['description']}")
    return True

def revert(key, p):
    obb = find_obb(p["file"])
    if not obb: return False
    write_at(obb, p["offset"], p["original"])
    print(f"  [{key}] ↩ Reverted")
    return True

# ─── MAIN ──────────────────────────────────────────────────────────

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--mode",  choices=["apply","verify","revert","list"], default="verify")
    ap.add_argument("--patch", default=None)
    args = ap.parse_args()

    targets = {args.patch: PATCHES[args.patch]} if args.patch else PATCHES

    if args.mode == "list":
        for k, v in PATCHES.items():
            print(f"  {k:25s} — {v['description']}")
        return

    if args.mode == "verify":
        print("\n=== PATCH STATUS ===")
        for k, p in targets.items():
            s = verify(k, p)
            icon = "✓" if s == "PATCHED" else ("·" if s == "ORIGINAL" else "✗")
            print(f"  [{icon}] {k:25s} → {s}")

    elif args.mode == "apply":
        print("\n=== APPLYING ===")
        ok = sum(apply(k, p) for k, p in targets.items())
        print(f"\n  {ok}/{len(targets)} patches OK")
        print("\n  NEXT:")
        print("  adb push ~/mlbb_patch/obb/*.obb /sdcard/Android/obb/com.mobile.legends/")
        print("  Launch MLBB")

    elif args.mode == "revert":
        print("\n=== REVERTING ===")
        for k, p in targets.items(): revert(k, p)
        print("  Done — push originals back via adb push")

if __name__ == "__main__":
    main()
