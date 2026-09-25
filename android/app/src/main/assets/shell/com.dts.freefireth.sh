#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.dts.freefireth.sh — Free Fire & Free Fire MAX tuning
# Covers: com.dts.freefireth, com.dts.freefiremax
# ─────────────────────────────────────────────────────────────────────────────

# Free Fire uses Unity — OpenGL ES is default, but push perf flags
setprop debug.hwui.renderer opengl
setprop debug.egl.swapinterval -1
setprop debug.egl.buffcount 2

# CPU — Free Fire is lighter than PUBG, focus on LITTLE cluster latency
for p in /sys/devices/system/cpu/cpufreq/policy0; do
  echo performance > "$p/scaling_governor" 2>/dev/null
  cat "$p/cpuinfo_max_freq" > "$p/scaling_min_freq" 2>/dev/null
done
for p in /sys/devices/system/cpu/cpufreq/policy4; do
  echo performance > "$p/scaling_governor" 2>/dev/null
done

# Hz lock
settings put system peak_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate {TARGET_HZ}.0 2>/dev/null
setprop debug.sf.fps_limit {TARGET_HZ}

# Garena network servers
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null

# Memory — Unity garbage collector works better with prepped RAM
echo 1 > /proc/sys/vm/drop_caches 2>/dev/null
echo 0 > /proc/sys/vm/swappiness 2>/dev/null
