#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# base_perf.sh — Universal Game Booster (auto-loaded on every game launch)
# Replaces {TARGET_HZ} at runtime with per-game Hz from GameProfilePreferences
# ─────────────────────────────────────────────────────────────────────────────

# CPU — performance governor on ALL cpufreq policies
for p in /sys/devices/system/cpu/cpufreq/policy*; do
  echo performance > "$p/scaling_governor" 2>/dev/null
  cat "$p/cpuinfo_max_freq" > "$p/scaling_min_freq" 2>/dev/null
done

# CPUset — push all cores to top-app cgroup
echo 0-7 > /dev/cpuset/top-app/cpus 2>/dev/null
echo 0-7 > /dev/cpuset/foreground/cpus 2>/dev/null

# uclamp — boost scheduler minimum utilization
echo 1024 > /dev/cpuset/top-app/cpu.uclamp.min 2>/dev/null
for p in /sys/devices/system/cpu/cpu*/sched_load_boost; do
  echo -6 > "$p" 2>/dev/null
done

# GPU Adreno turbo
setprop debug.adreno.turbo 1
setprop debug.adreno.perf_level 0
setprop vendor.perf.gestureFlingBoost 1
setprop debug.gpu.performance 1
setprop vendor.gpu.power_mode 1

# GPU Mali boost
setprop debug.mali.sched.priority -20
setprop debug.mali.force_gpu_boost 1
setprop debug.hwui.renderer vulkan 2>/dev/null

# Thermal bypass — disable thermal zones & daemons
for z in /sys/class/thermal/thermal_zone*; do
  echo disabled > "$z/mode" 2>/dev/null
done
stop thermal-engine 2>/dev/null
stop thermald 2>/dev/null
stop vendor.thermal-engine 2>/dev/null
setprop persist.sys.thermal.ignore 1
setprop vendor.thermal.config "" 2>/dev/null

# Hz enforcement — hardware adaptive
settings put system peak_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system match_content_frame_rate 0 2>/dev/null
setprop debug.sf.fps_limit {TARGET_HZ}
setprop persist.sys.NV_FPSLIMIT {TARGET_HZ}
service call SurfaceFlinger 1035 i32 {TARGET_HZ} 2>/dev/null

# SurfaceFlinger — HW composition, disable idle timer
setprop debug.sf.hw 1
setprop debug.sf.disable_hwc_vds 1
setprop debug.sf.enable_gl_backpressure 0
setprop debug.sf.layer_caching_enabled 0
setprop debug.egl.hw 1

# Touch — max precision
setprop view.touch_slop 0
setprop persist.sys.touch.report_rate 1000
setprop persist.vendor.touch.sampling_rate 1000
setprop persist.vendor.touch.hovering 0

# Memory — aggressive background killer
setprop ro.lmk.kill_heaviest_task true
setprop ro.lmk.kill_timeout_ms 100
echo 0 > /proc/sys/vm/swappiness 2>/dev/null
echo 0 > /proc/sys/vm/page-cluster 2>/dev/null

# Network — BBR + TCP fast open
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_quickack=1 2>/dev/null
sysctl -w net.ipv4.tcp_fastopen=3 2>/dev/null
sysctl -w net.ipv4.tcp_low_latency=1 2>/dev/null

# Scheduler tweaks
setprop debug.binder.slow_dispatch_threshold 0
setprop debug.binder.slow_delivery_threshold 0
