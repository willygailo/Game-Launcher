#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.riotgames.league.wildrift.sh — League of Legends: Wild Rift tuning
# Covers: com.riotgames.league.wildrift, wildrifttw, wildriftvn
# Synchronized Vulkan pipeline + Dynamic Hz unlock + Safe Thermal Governor
# ─────────────────────────────────────────────────────────────────────────────

# Riot engine — custom C++ renderer, benefits from Vulkan path
setprop debug.hwui.renderer skiavk
setprop debug.renderengine.backend vulkan
setprop debug.vulkan.enable_validation_layers 0
setprop debug.egl.swapinterval 0
setprop debug.egl.buffcount 3
setprop debug.sf.disable_backpressure 1
setprop debug.sf.latch_unsignaled 0
setprop debug.sf.auto_latch_unsignaled 0

# CPU — Wild Rift is low-latency sensitive over raw throughput
for p in /sys/devices/system/cpu/cpufreq/policy*; do
  echo performance > "$p/scaling_governor" 2>/dev/null
  cat "$p/cpuinfo_max_freq" > "$p/scaling_min_freq" 2>/dev/null
done

# ── Dynamic Hz Resolution ─────────────────────────────────────────────────────
TARGET_HZ="{TARGET_HZ}"
case "$TARGET_HZ" in
  *{*}*|"") TARGET_HZ=120 ;;
esac

# Hz lock — Wild Rift officially supports up to 120Hz/144Hz
settings put system peak_refresh_rate ${TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate ${TARGET_HZ}.0 2>/dev/null
setprop debug.sf.fps_limit $TARGET_HZ
setprop persist.sys.NV_FPSLIMIT $TARGET_HZ
service call SurfaceFlinger 1034 i32 $TARGET_HZ 2>/dev/null
service call SurfaceFlinger 1035 i32 $TARGET_HZ 2>/dev/null

# Thermal ceiling clamp (75°C hardware-safe trip lock + cooling reset)
for t in /sys/class/thermal/thermal_zone*/trip_point_*_temp; do
  echo 75000 > "$t" 2>/dev/null
done
for c in /sys/class/thermal/cooling_device*/cur_state; do
  echo 0 > "$c" 2>/dev/null
done
cmd thermalservice override-status 0 2>/dev/null
cmd thermal override-status 0 2>/dev/null
cmd power set-fixed-performance-mode-enabled true 2>/dev/null
setprop debug.thermal.throttle.disable 1
setprop vendor.thermal.mode performance

# Wild Rift network — dedicated Riot servers, BBR reduces jitter
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null
sysctl -w net.ipv4.tcp_notsent_lowat=16384 2>/dev/null

echo 1 > /proc/sys/vm/drop_caches 2>/dev/null
