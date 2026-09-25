#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.levelinfinite.sgameGlobal.sh — Honor of Kings (HoK) tuning
# Covers: com.levelinfinite.sgame, com.tencent.tmgp.sgame, com.levelinfinite.sgameGlobal
# ─────────────────────────────────────────────────────────────────────────────

# HoK — Tencent engine, Vulkan + custom shader compiler
setprop debug.hwui.renderer vulkan
setprop debug.vulkan.enable_validation_layers 0
setprop debug.egl.swapinterval -1

# Tencent game engine threads are aggressively multi-core
for p in /sys/devices/system/cpu/cpufreq/policy*; do
  echo performance > "$p/scaling_governor" 2>/dev/null
  cat "$p/cpuinfo_max_freq" > "$p/scaling_min_freq" 2>/dev/null
done

# Hz lock — HoK supports 120Hz in-game on capable devices
settings put system peak_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate {TARGET_HZ}.0 2>/dev/null
setprop debug.sf.fps_limit {TARGET_HZ}

# Tencent network acceleration
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null

echo 1 > /proc/sys/vm/drop_caches 2>/dev/null
