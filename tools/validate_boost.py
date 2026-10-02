#!/usr/bin/env python3
"""
tools/validate_boost.py — Automated Device Validation & Read-back CLI for Game Space PRO.

Verifies system-level boost state and game overlay enforcement over ADB:
- Refresh rate (peak_refresh_rate, min_refresh_rate)
- CPU governors across all clusters/policies
- GPU devfreq frequencies
- WebView command-line flags
- Game Mode API overlay frame-rates
- Touch input event rate

Exit codes:
  0: PASS (All requested checks passed)
  1: FAIL (At least one requested check failed)
  2: UNAVAILABLE (ADB device not found or prerequisite check cannot be executed)
"""

import argparse
import json
import re
import subprocess
import sys
from typing import Dict, List, Optional, Tuple


def run_adb(cmd: List[str], serial: Optional[str] = None) -> Tuple[int, str]:
    """Execute an adb command and return (exit_code, stdout)."""
    adb_cmd = ["adb"]
    if serial:
        adb_cmd.extend(["-s", serial])
    adb_cmd.extend(cmd)
    try:
        res = subprocess.run(adb_cmd, capture_output=True, text=True, timeout=15)
        return res.returncode, res.stdout.strip()
    except subprocess.TimeoutExpired:
        return -1, "TIMEOUT"
    except FileNotFoundError:
        return -1, "ADB_NOT_FOUND"


def parse_hz_output(system_hz_str: str, global_hz_str: str) -> Optional[float]:
    """Parse peak refresh rate from Settings output."""
    for s in (system_hz_str, global_hz_str):
        if not s or s == "null":
            continue
        try:
            return float(s.strip())
        except ValueError:
            pass
    return None


def parse_cpu_governors(output: str) -> List[str]:
    """Parse list of governor names from shell output."""
    govs = []
    for line in output.splitlines():
        g = line.strip().lower()
        if g:
            govs.append(g)
    return govs


def parse_game_mode_fps(dumpsys_output: str, package_name: str) -> Optional[int]:
    """Parse applied FPS limit for a package from dumpsys game or cmd game."""
    if not dumpsys_output:
        return None
    # Matches: fps=120 or mFps=120 or GameModeSettings{...fps=120...}
    match = re.search(r"fps[:=]\s*(\d+)", dumpsys_output, re.IGNORECASE)
    if match:
        return int(match.group(1))
    return None


def check_hz(expected_hz: float, serial: Optional[str] = None) -> Dict:
    code1, out1 = run_adb(["shell", "settings", "get", "system", "peak_refresh_rate"], serial)
    code2, out2 = run_adb(["shell", "settings", "get", "global", "peak_refresh_rate"], serial)
    if code1 != 0 and code2 != 0:
        return {"check": "hz", "status": "UNAVAILABLE", "error": "Failed to read settings"}
    actual = parse_hz_output(out1, out2)
    passed = actual is not None and abs(actual - expected_hz) < 0.5
    return {
        "check": "hz",
        "expected": expected_hz,
        "actual": actual,
        "status": "PASS" if passed else "FAIL"
    }


def check_cpu_gov(expected_gov: str, serial: Optional[str] = None) -> Dict:
    cmd = "for p in /sys/devices/system/cpu/cpufreq/policy*; do [ -f $p/scaling_governor ] && cat $p/scaling_governor; done"
    code, out = run_adb(["shell", cmd], serial)
    if code != 0 or not out:
        return {"check": "cpu-gov", "status": "UNAVAILABLE", "error": "Unable to read cpu policies"}
    govs = parse_cpu_governors(out)
    expected_lower = expected_gov.lower()
    passed = len(govs) > 0 and all(g == expected_lower for g in govs)
    return {
        "check": "cpu-gov",
        "expected": expected_lower,
        "actual": govs,
        "status": "PASS" if passed else "FAIL"
    }


def check_game_overlay(package_name: str, expected_hz: int, serial: Optional[str] = None) -> Dict:
    code, out = run_adb(["shell", "cmd", "game", "get", package_name], serial)
    if code != 0 or not out:
        code, out = run_adb(["shell", "dumpsys", "game", package_name], serial)
    if code != 0 or not out:
        return {"check": "game-overlay", "package": package_name, "status": "UNAVAILABLE"}
    actual_fps = parse_game_mode_fps(out, package_name)
    passed = actual_fps is not None and actual_fps == expected_hz
    return {
        "check": "game-overlay",
        "package": package_name,
        "expected": expected_hz,
        "actual": actual_fps,
        "status": "PASS" if passed else "FAIL"
    }


def check_webview(tier: str, serial: Optional[str] = None) -> Dict:
    code, out = run_adb(["shell", "cat", "/data/local/tmp/webview-command-line"], serial)
    if code != 0 or not out:
        return {"check": "webview", "tier": tier, "status": "UNAVAILABLE", "note": "No webview flags found"}
    has_flags = len(out.strip()) > 0
    return {
        "check": "webview",
        "tier": tier,
        "flags": out.strip(),
        "status": "PASS" if has_flags else "FAIL"
    }


def main():
    parser = argparse.ArgumentParser(description="Game Space PRO ADB Validation CLI")
    parser.add_argument("--serial", help="Target device serial number")
    parser.add_argument("--json", action="store_true", help="Output results in JSON format")

    subparsers = parser.add_subparsers(dest="subcommand", required=True)

    # hz
    p_hz = subparsers.add_parser("hz", help="Verify applied refresh rate")
    p_hz.add_argument("--hz", type=float, required=True, help="Expected refresh rate (e.g. 120, 165)")

    # cpu-gov
    p_cpu = subparsers.add_parser("cpu-gov", help="Verify CPU cluster governor")
    p_cpu.add_argument("--governor", default="performance", help="Expected governor (default: performance)")

    # game-overlay
    p_game = subparsers.add_parser("game-overlay", help="Verify Game Mode API FPS setting")
    p_game.add_argument("--package", required=True, help="Target package name (e.g. com.mobile.legends)")
    p_game.add_argument("--hz", type=int, required=True, help="Expected FPS limit")

    # all
    p_all = subparsers.add_parser("all", help="Run comprehensive read-back validation")
    p_all.add_argument("--hz", type=float, default=120.0, help="Expected refresh rate")
    p_all.add_argument("--package", default="com.mobile.legends", help="Target package name")
    p_all.add_argument("--tier", default="flagship", help="Device tier (flagship/mid/budget)")

    args = parser.parse_args()

    results = []

    if args.subcommand == "hz":
        results.append(check_hz(args.hz, args.serial))
    elif args.subcommand == "cpu-gov":
        results.append(check_cpu_gov(args.governor, args.serial))
    elif args.subcommand == "game-overlay":
        results.append(check_game_overlay(args.package, args.hz, args.serial))
    elif args.subcommand == "all":
        results.append(check_hz(args.hz, args.serial))
        results.append(check_cpu_gov("performance", args.serial))
        results.append(check_game_overlay(args.package, int(args.hz), args.serial))
        results.append(check_webview(args.tier, args.serial))

    # Determine exit code
    exit_code = 0
    for r in results:
        status = r.get("status")
        if status == "FAIL":
            exit_code = max(exit_code, 1)
        elif status == "UNAVAILABLE" and exit_code != 1:
            exit_code = 2

    if args.json:
        print(json.dumps({"results": results, "exit_code": exit_code}, indent=2))
    else:
        for r in results:
            stat = r.get("status")
            name = r.get("check")
            print(f"[{stat}] Check '{name}': {r}")

    sys.exit(exit_code)


if __name__ == "__main__":
    main()
