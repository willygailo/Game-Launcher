#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.riotgames.league.wildrift.sh — League of Legends: Wild Rift tuning
# ─────────────────────────────────────────────────────────────────────────────

# Riot engine — custom C++ renderer, benefits from Vulkan path
setprop debug.hwui.renderer vulkan
setprop debug.vulkan.enable_validation_layers 0
setprop debug.egl.swapinterval -1
setprop debug.egl.buffcount 3

# CPU — Wild Rift is low-latency sensitive over raw throughput
for p in /sys/devices/system/cpu/cpufreq/policy*; do
  echo performance > "$p/scaling_governor" 2>/dev/null
done

# Hz lock — Wild Rift officially supports up to 120Hz
settings put system peak_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate {TARGET_HZ}.0 2>/dev/null
setprop debug.sf.fps_limit {TARGET_HZ}

# Wild Rift network — dedicated Riot servers, BBR reduces jitter
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null
sysctl -w net.ipv4.tcp_notsent_lowat=16384 2>/dev/null

echo 1 > /proc/sys/vm/drop_caches 2>/dev/null
