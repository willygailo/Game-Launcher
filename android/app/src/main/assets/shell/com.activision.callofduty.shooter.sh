#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.activision.callofduty.shooter.sh — Call of Duty Mobile tuning
# Covers: com.activision.callofduty.shooter, com.garena.game.codm, com.vng.codmvn
# ─────────────────────────────────────────────────────────────────────────────

# IW8 (COD) engine — Vulkan preferred renderer
setprop debug.hwui.renderer vulkan
setprop debug.vulkan.enable_validation_layers 0
setprop debug.egl.swapinterval -1

# Force max CPU on all policies — IW8 is multi-threaded heavily
for p in /sys/devices/system/cpu/cpufreq/policy*; do
  echo performance > "$p/scaling_governor" 2>/dev/null
  cat "$p/cpuinfo_max_freq" > "$p/scaling_min_freq" 2>/dev/null
done

# Hz lock for COD match
settings put system peak_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate {TARGET_HZ}.0 2>/dev/null
setprop debug.sf.fps_limit {TARGET_HZ}
setprop persist.sys.NV_FPSLIMIT {TARGET_HZ}

# COD network — latency sensitive
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null
sysctl -w net.ipv4.tcp_nodelay=1 2>/dev/null

# Thermal off — COD runs hot on heavy maps
stop thermal-engine 2>/dev/null
stop vendor.thermal-engine 2>/dev/null
for z in /sys/class/thermal/thermal_zone*; do
  echo disabled > "$z/mode" 2>/dev/null
done

# Drop caches — COD map loads need max contiguous RAM
echo 3 > /proc/sys/vm/drop_caches 2>/dev/null
