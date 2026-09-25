#!/system/bin/sh
# ─────────────────────────────────────────────────────────────────────────────
# com.tencent.ig.sh — PUBG Mobile / BGMI tuning
# Covers: com.tencent.ig, com.pubg.imobile, com.vng.pubgmobile, com.krafton.bgmi
# ─────────────────────────────────────────────────────────────────────────────

# PUBG uses Unreal Engine — push OpenGL ES path fast
setprop debug.egl.swapinterval -1
setprop debug.egl.buffcount 3
setprop debug.hwui.renderer opengl

# Unreal Engine threading — ensure game threads hit big cores
for p in /sys/devices/system/cpu/cpufreq/policy4; do
  echo performance > "$p/scaling_governor" 2>/dev/null
done
for p in /sys/devices/system/cpu/cpufreq/policy7; do
  echo performance > "$p/scaling_governor" 2>/dev/null
done

# Hz lock
settings put system peak_refresh_rate {TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate {TARGET_HZ}.0 2>/dev/null
setprop debug.sf.fps_limit {TARGET_HZ}

# PUBG network — anti jitter
sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null
sysctl -w net.ipv4.tcp_notsent_lowat=16384 2>/dev/null
sysctl -w net.ipv4.tcp_mtu_probing=1 2>/dev/null

# GPU — PUBG Lite/UE benefits from buffer preload
setprop debug.adreno.turbo 1
setprop vendor.gpu.power_mode 1

# Memory — kill background aggressively for UE
echo 1 > /proc/sys/vm/drop_caches 2>/dev/null
setprop ro.lmk.kill_heaviest_task true
