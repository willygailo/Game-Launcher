#!/usr/bin/env python3
"""validate_boost.py — adb read-back validation for Game Booster (IMPROVEMENT_PLAN §9.1).

Validates that tweaks actually applied by reading device state back over adb,
mirroring the in-app VerifyResults. Designed for device-farm use: machine
readable (--json), stable exit codes (0 pass, 1 fail, 2 unavailable/error).

Checks:
  hz         applied refresh rate == requested (settings get + dumpsys display)
  cpu-gov    every cpufreq policy is the expected governor (default: performance)
  gpu-freq   every GPU devfreq node runs performance at max_freq
  webview    command-line flag file matches the tier marker flags
  touch      input report rate >= threshold (requires touching the screen)
  frame      p99 frame duration <= 1000/hz ms (dumpsys gfxinfo framestats)
  game-overlay  device_config game_overlay fps read-back for a package
  all        runs every applicable check

Examples:
  tools/validate_boost.py hz --hz 165
  tools/validate_boost.py all --hz 120 --package com.mobile.legends --tier flagship
  tools/validate_boost.py game-overlay --package com.mobile.legends --hz 120
  tools/validate_boost.py cpu-gov --json --serial R58M123
"""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
import time

CHECKS = ("hz", "cpu-gov", "gpu-freq", "webview", "touch", "frame", "game-overlay")

TIER_FLAGS = {
    "flagship": {"required": "--enable-webgpu", "forbidden": None},
    "mid": {"required": "--enable-drdc", "forbidden": "--enable-webgpu"},
    "budget": {"required": "--enable-gpu-rasterization", "forbidden": "--enable-webgpu"},
}

WEBVIEW_FLAG_FILES = (
    "/data/local/tmp/webview-command-line",
    "/data/local/tmp/chrome-command-line",
    "/data/local/tmp/android-webview-command-line",
)


class Result:
    def __init__(self, name, status, detail):
        self.name = name
        self.status = status  # "pass" | "fail" | "unavailable"
        self.detail = detail

    def ok(self):
        return self.status == "pass"

    def to_json(self):
        return {"check": self.name, "status": self.status, "detail": self.detail}

    def badge(self):
        icon = {"pass": "✓", "fail": "✗", "unavailable": "?"}[self.status]
        return f"{icon} {self.name}: {self.detail}"


class Adb:
    def __init__(self, serial=None, timeout=20):
        self.base = ["adb"] + (["-s", serial] if serial else [])
        self.timeout = timeout

    def cmd(self, *args, timeout=None):
        proc = subprocess.run(
            self.base + list(args), capture_output=True, timeout=timeout or self.timeout
        )
        return proc.returncode, proc.stdout.decode("utf-8", "replace")

    def shell(self, script, timeout=None):
        """Runs a shell command; falls back to `su -c` when plain shell is denied."""
        code, out = self.cmd("shell", script, timeout=timeout)
        if code == 0 and out.strip() and "Permission denied" not in out:
            return out
        code2, out2 = self.cmd("shell", "su", "-c", script, timeout=timeout)
        if code2 == 0:
            return out2
        return out

    def present(self):
        code, out = self.cmd("devices")
        if code != 0:
            return False
        lines = [l for l in out.splitlines()[1:] if l.strip()]
        devices = [l.split()[0] for l in lines if "\tdevice" in l]
        return bool(devices)


# ── Individual checks ────────────────────────────────────────────────────────────


def parse_hz(raw):
    """'165.0' -> 165, unset/invalid -> None."""
    if raw is None:
        return None
    t = raw.strip()
    if not t or t.lower() in ("null", "undefined"):
        return None
    try:
        v = float(t)
    except ValueError:
        return None
    return round(v) if v > 0 else None


def check_hz(adb, requested_hz):
    if not requested_hz:
        return Result("hz", "unavailable", "no --hz requested")
    raw = adb.shell("settings get system peak_refresh_rate")
    applied = parse_hz(raw)
    source = "settings get system peak_refresh_rate"
    if applied is None:
        out = adb.shell("dumpsys display | grep -E 'mActiveModeId|refresh rate' | head -5")
        found = [int(x) for x in re.findall(r"(\d+(?:\.\d+)?)\s*Hz", out)]
        found = [round(f) for f in found if 24 <= f <= 1000]
        applied = found[0] if found else None
        source = "dumpsys display"
    if applied is None:
        return Result("hz", "unavailable", f"cannot read refresh rate ({source})")
    if applied == requested_hz:
        return Result("hz", "pass", f"{applied} Hz == requested ({source})")
    return Result(
        "hz", "fail", f"requested {requested_hz} Hz, read back {applied} Hz ({source})"
    )


def parse_governor_map(output):
    """'policy0=performance' lines -> {policy: governor}."""
    result = {}
    for line in (output or "").splitlines():
        if "=" not in line:
            continue
        policy, gov = line.split("=", 1)
        policy, gov = policy.strip(), gov.strip()
        if policy and gov:
            result[policy] = gov
    return result


def check_cpu_gov(adb, expected_gov):
    out = adb.shell(
        "for p in /sys/devices/system/cpu/cpufreq/policy*; do "
        'n=$(basename "$p"); g=$(cat "$p/scaling_governor" 2>/dev/null); '
        '[ -n "$g" ] && echo "$n=$g"; done'
    )
    governors = parse_governor_map(out)
    if not governors:
        return Result("cpu-gov", "unavailable", "no cpufreq policies readable (root needed?)")
    wrong = {p: g for p, g in governors.items() if g != expected_gov}
    if not wrong:
        return Result(
            "cpu-gov", "pass", f"{expected_gov} on all {len(governors)} policies"
        )
    return Result("cpu-gov", "fail", f"expected {expected_gov}, got {wrong}")


def parse_gpu_nodes(output):
    """'name=gov,cur,max' lines -> list of dicts."""
    nodes = []
    for line in (output or "").splitlines():
        if "=" not in line:
            continue
        name, rest = line.split("=", 1)
        parts = rest.split(",")
        if len(parts) != 3 or not name.strip():
            continue
        nodes.append(
            {"name": name.strip(), "gov": parts[0].strip(), "cur": parts[1].strip(),
             "max": parts[2].strip()}
        )
    return nodes


def check_gpu_freq(adb):
    out = adb.shell(
        'for d in /sys/class/devfreq/*; do n=$(basename "$d"); '
        'case "$n" in *gpu*|*kgsl*|*mali*|*adreno*|*img*) ;; *) continue;; esac; '
        'g=$(cat "$d/governor" 2>/dev/null); c=$(cat "$d/cur_freq" 2>/dev/null); '
        'm=$(cat "$d/max_freq" 2>/dev/null); '
        '[ -n "$g" ] && [ -n "$m" ] && echo "$n=$g,$c,$m"; done'
    )
    nodes = parse_gpu_nodes(out)
    if not nodes:
        return Result("gpu-freq", "unavailable", "no GPU devfreq nodes readable (root needed?)")
    wrong = [
        f"{n['name']} gov={n['gov']} cur={n['cur']} max={n['max']}"
        for n in nodes
        if n["gov"] != "performance" or n["cur"] != n["max"]
    ]
    if not wrong:
        return Result("gpu-freq", "pass", f"locked at max on {len(nodes)} GPU node(s)")
    return Result("gpu-freq", "fail", "; ".join(wrong))


def check_webview(adb, tier):
    spec = TIER_FLAGS.get(tier)
    if spec is None:
        return Result("webview", "unavailable", f"unknown tier {tier!r} (use flagship|mid|budget)")
    cmd = "cat " + " ".join(WEBVIEW_FLAG_FILES) + " 2>/dev/null"
    content = adb.shell(cmd)
    prop = adb.shell("getprop debug.chromium.flags")
    content = (content or "") + " " + (prop or "")
    if not content.strip():
        return Result("webview", "unavailable", "no flag file or debug.chromium.flags set")
    missing = spec["required"] and spec["required"] not in content
    forbidden = spec["forbidden"] and spec["forbidden"] in content
    if missing:
        return Result("webview", "fail", f"{tier}: missing {spec['required']}")
    if forbidden:
        return Result("webview", "fail", f"{tier}: must not contain {spec['forbidden']}")
    return Result("webview", "pass", f"{tier}: marker flag {spec['required']} present")


def check_touch(adb, min_hz, duration):
    print(
        f"  sampling input for {duration}s — keep touching/swiping the screen...",
        file=sys.stderr,
    )
    code, out = adb.cmd(
        "shell", f"timeout {duration} getevent -l", timeout=duration + 15
    )
    reports = len(re.findall(r"EV_SYN\s+SYN_REPORT", out))
    if reports == 0:
        return Result("touch", "unavailable", "no touch events captured (screen idle?)")
    rate = round(reports / duration)
    if rate >= min_hz:
        return Result("touch", "pass", f"~{rate} reports/s >= {min_hz}")
    return Result("touch", "fail", f"~{rate} reports/s < {min_hz}")


def frame_durations_ms(out):
    """Parses dumpsys gfxinfo framestats PROFILEDATA rows -> frame durations (ms).

    Columns are resolved by header names (IntendedVsync .. FrameCompleted) so
    the parser survives column-order differences across Android versions; the
    last FrameCompleted column (frame end) is used when the header repeats it.
    """
    durations = []
    in_table = False
    intended_idx, completed_idx = 1, 3  # fallback layout
    for line in (out or "").splitlines():
        if "---PROFILEDATA---" in line:
            if durations:
                break  # second marker closes the first table
            in_table = True
            intended_idx, completed_idx = 1, 3
            continue
        if not in_table:
            continue
        if line.startswith("Flags,"):
            cols = [c.strip() for c in line.split(",")]
            if "IntendedVsync" in cols:
                intended_idx = cols.index("IntendedVsync")
            frame_completed = [i for i, c in enumerate(cols) if c == "FrameCompleted"]
            if frame_completed:
                completed_idx = frame_completed[-1]
            continue
        if line.startswith("---") or not line.strip():
            break
        cols = line.split(",")
        if len(cols) <= max(intended_idx, completed_idx):
            continue
        try:
            intended = int(cols[intended_idx])
            completed = int(cols[completed_idx])
        except ValueError:
            continue
        if completed > intended >= 0:
            durations.append((completed - intended) / 1_000_000.0)
    return durations


def percentile(sorted_values, p):
    if not sorted_values:
        return None
    idx = min(len(sorted_values) - 1, int(round(p / 100.0 * (len(sorted_values) - 1))))
    return sorted_values[idx]


def parse_overlay_fps(raw):
    """Parses device_config game_overlay value -> applied FPS or None.

    Format: mode=2,useAngle=false,fps=120,...:mode=3,...,fps=120,...
    Returns the FPS only when every fps= token is present and agrees;
    unset ('null'), empty, malformed, or disagreeing values yield None.
    """
    if raw is None:
        return None
    t = raw.strip()
    if not t or t.lower() == "null":
        return None
    values = set()
    for token in re.split(r"[,:\s]+", t):
        if token.startswith("fps="):
            try:
                values.add(int(float(token[4:])))
            except ValueError:
                return None
    if len(values) == 1:
        return values.pop()
    return None


def check_game_overlay(adb, package, hz):
    if not package or not hz:
        return Result("game-overlay", "unavailable", "needs --package and --hz")
    out = adb.shell(f"device_config get game_overlay {package}")
    actual = parse_overlay_fps(out)
    if actual is None:
        return Result(
            "game-overlay", "unavailable",
            f"no readable game_overlay fps for {package} (root/shizuku needed)",
        )
    if actual == hz:
        return Result("game-overlay", "pass", f"{package} fps={actual} == requested")
    return Result(
        "game-overlay", "fail",
        f"{package}: requested fps={hz}, read back fps={actual}",
    )


def check_frame(adb, package, hz):
    if not package:
        return Result("frame", "unavailable", "no --package given")
    if not hz:
        return Result("frame", "unavailable", "no --hz given (needed for frame budget)")
    out = adb.shell(f"dumpsys gfxinfo {package} framestats")
    durations = sorted(frame_durations_ms(out))
    if len(durations) < 30:
        return Result(
            "frame", "unavailable",
            f"only {len(durations)} frame samples — launch the game and play first",
        )
    budget = 1000.0 / hz
    p99 = percentile(durations, 99)
    avg = sum(durations) / len(durations)
    detail = f"p99 {p99:.2f}ms (budget {budget:.2f}ms), avg {avg:.2f}ms, n={len(durations)}"
    if p99 <= budget:
        return Result("frame", "pass", detail)
    return Result("frame", "fail", detail)


# ── Runner ───────────────────────────────────────────────────────────────────────


def run_checks(args, adb):
    runners = {
        "hz": lambda: check_hz(adb, args.hz),
        "cpu-gov": lambda: check_cpu_gov(adb, args.governor),
        "gpu-freq": lambda: check_gpu_freq(adb),
        "webview": lambda: check_webview(adb, args.tier),
        "touch": lambda: check_touch(adb, args.min_touch_hz, args.touch_seconds),
        "frame": lambda: check_frame(adb, args.package, args.hz),
        "game-overlay": lambda: check_game_overlay(adb, args.package, args.hz),
    }
    selected = CHECKS if args.check == "all" else (args.check,)
    results = []
    for name in selected:
        try:
            results.append(runners[name]())
        except subprocess.TimeoutExpired:
            results.append(Result(name, "unavailable", "adb timeout"))
        except Exception as exc:  # noqa: BLE001 — report, never crash the farm job
            results.append(Result(name, "unavailable", f"error: {exc}"))
    return results


def main(argv=None):
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("check", choices=CHECKS + ("all",), help="check to run")
    parser.add_argument("--serial", help="adb device serial")
    parser.add_argument("--hz", type=int, help="requested refresh rate (Hz)")
    parser.add_argument("--package", help="game package for framestats")
    parser.add_argument("--tier", choices=sorted(TIER_FLAGS), default="mid",
                        help="WebView tier (default: mid)")
    parser.add_argument("--governor", default="performance",
                        help="expected CPU governor (default: performance)")
    parser.add_argument("--min-touch-hz", type=int, default=500,
                        help="min input reports/s (default: 500)")
    parser.add_argument("--touch-seconds", type=int, default=5,
                        help="touch sampling window (default: 5s)")
    parser.add_argument("--json", action="store_true", help="machine-readable output")
    args = parser.parse_args(argv)

    adb = Adb(serial=args.serial)
    if not adb.present():
        msg = "no adb device connected"
        if args.json:
            print(json.dumps({"results": [], "error": msg}))
        else:
            print(f"✗ {msg}", file=sys.stderr)
        return 2

    results = run_checks(args, adb)

    if args.json:
        print(json.dumps({"results": [r.to_json() for r in results]}, indent=2))
    else:
        for r in results:
            print(r.badge())
        passed = sum(1 for r in results if r.ok())
        print(f"-- {passed}/{len(results)} passed")

    if all(r.status == "unavailable" for r in results):
        return 2
    return 0 if all(r.ok() for r in results) else 1


if __name__ == "__main__":
    sys.exit(main())
