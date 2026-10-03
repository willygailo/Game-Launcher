#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.tencent.ig.sh — PUBG Mobile Ultra Tuning (Zero Deadlock + Tear-Free)
# Target: com.tencent.ig (GLOBAL ONLY)
# Synchronized Vulkan pipeline + Dynamic Hz unlock + Safe Thermal Governor
# ─────────────────────────────────────────────────────────────────────────────

# HWUI & RenderEngine — Unified SkiaVK
setprop debug.hwui.renderer skiavk 2>/dev/null
setprop debug.renderengine.backend vulkan 2>/dev/null
setprop debug.sf.disable_backpressure 1 2>/dev/null
setprop debug.sf.latch_unsignaled 0 2>/dev/null
setprop debug.sf.auto_latch_unsignaled 0 2>/dev/null

# CPU Governor — Lock all CPU policy clusters to performance
for p in /sys/devices/system/cpu/cpufreq/policy*/scaling_governor; do
  echo performance > "$p" 2>/dev/null
done

# ── Dynamic Refresh Rate Resolution ──────────────────────────────────────────
TARGET_HZ="{TARGET_HZ}"
case "$TARGET_HZ" in
  *{*}*|"") TARGET_HZ=120 ;;
esac

# ── Refresh Rate Unlock (PUBGM — UE4 FPS unlock) ──────────────────────────────
settings put system peak_refresh_rate ${TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate ${TARGET_HZ}.0 2>/dev/null
settings put global game_mode_config 0 2>/dev/null
setprop debug.sf.fps_limit $TARGET_HZ 2>/dev/null
setprop persist.sys.NV_FPSLIMIT $TARGET_HZ 2>/dev/null
setprop persist.game_mode.performance.fps $TARGET_HZ 2>/dev/null
setprop persist.sys.game.fps $TARGET_HZ 2>/dev/null

# SurfaceFlinger binder calls
service call SurfaceFlinger 1034 i32 $TARGET_HZ 2>/dev/null
service call SurfaceFlinger 1035 i32 $TARGET_HZ 2>/dev/null

# Kernel display nodes (chipset vendors)
echo $TARGET_HZ > /sys/devices/virtual/graphics/fb0/dynamic_fps 2>/dev/null
echo $TARGET_HZ > /sys/class/graphics/fb0/dynamic_fps 2>/dev/null
echo $TARGET_HZ > /sys/devices/platform/mtk_disp_mgr.0/refresh_rate 2>/dev/null
echo $TARGET_HZ > /proc/mtk_display/fps 2>/dev/null

# GPU — Maximum clock level
setprop debug.adreno.turbo 1 2>/dev/null
setprop vendor.gpu.power_mode 1 2>/dev/null

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

# Network — Anti-jitter & low latency TCP
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_notsent_lowat=16384 2>/dev/null
sysctl -w net.ipv4.tcp_mtu_probing=1 2>/dev/null

# Safe Network Resolver Flush (Never wipe OUTPUT iptables!)
ndc resolver flushdefaultif 2>/dev/null

# GameMode API clamp removal for PUBGM
cmd game set --mode 2 --user 0 com.tencent.ig 2>/dev/null
