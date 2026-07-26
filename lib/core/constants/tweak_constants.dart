class TweakItem {
  final String key;
  final String defaultValue;
  final String tweakValue;
  final String title;
  final String description;
  final String category;
  final bool isReadOnly; // ro.* properties marked true

  const TweakItem({
    required this.key,
    required this.defaultValue,
    required this.tweakValue,
    required this.title,
    required this.description,
    required this.category,
    this.isReadOnly = false,
  });
}

class TweakConstants {
  // GPU & Graphics Tweaks (Writable debug.* / persist.*)
  static const List<TweakItem> gpuTweaks = [
    TweakItem(
      key: 'debug.composition.type',
      defaultValue: 'c2d',
      tweakValue: 'gpu',
      title: 'Force GPU Composition',
      description: 'Forces SurfaceFlinger to use GPU for all UI composition rendering.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'debug.sf.hw',
      defaultValue: '0',
      tweakValue: '1',
      title: 'Hardware Acceleration',
      description: 'Enables 2D hardware acceleration for smoother frame rates.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'debug.egl.hw',
      defaultValue: '0',
      tweakValue: '1',
      title: 'EGL HW Acceleration',
      description: 'Accelerates OpenGL EGL graphics calls via GPU.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'video.accelerate.hw',
      defaultValue: '0',
      tweakValue: '1',
      title: 'Video Hardware Accel',
      description: 'Offloads video rendering workloads to GPU hardware pipelines.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'hw3d.force',
      defaultValue: '0',
      tweakValue: '1',
      title: 'Force 3D Hardware',
      description: 'Enforces 3D graphics hardware pipeline for high-demand games.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'debug.gr.swapinterval',
      defaultValue: '1',
      tweakValue: '0',
      title: 'Disable V-Sync Limit',
      description: 'Disables V-Sync swap interval cap to unlock maximum FPS.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'persist.sys.ui.hw',
      defaultValue: 'false',
      tweakValue: 'true',
      title: 'System UI Hardware Accel',
      description: 'Forces System UI and overlays to use GPU acceleration.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'debug.egl.force_msaa',
      defaultValue: 'false',
      tweakValue: 'true',
      title: 'Force 4x MSAA Graphics',
      description: 'Forces 4x Multi-Sample Anti-Aliasing in OpenGL ES 2.0+ games.',
      category: 'GPU',
    ),
    TweakItem(
      key: 'debug.hwui.renderer',
      defaultValue: 'skiagl',
      tweakValue: 'vulkan',
      title: 'Vulkan HWUI Graphics Pipeline',
      description: 'Forces Vulkan graphics renderer backend for HWUI rendering engine.',
      category: 'GPU',
    ),
  ];

  // CPU & Memory Tweaks (Writable persist.* / dalvik.vm.* / debug.*)
  static const List<TweakItem> cpuTweaks = [
    TweakItem(
      key: 'persist.sys.purgeable_assets',
      defaultValue: '0',
      tweakValue: '1',
      title: 'Purgeable RAM Assets',
      description: 'Allows Android OS to purge dormant bitmap assets to free RAM.',
      category: 'CPU',
    ),
    TweakItem(
      key: 'dalvik.vm.heaputilization',
      defaultValue: '0.75',
      tweakValue: '0.25',
      title: 'Dalvik Heap Utilization',
      description: 'Optimizes JVM heap allocation frequency for lower latency.',
      category: 'CPU',
    ),
    TweakItem(
      key: 'dalvik.vm.heaptargetutilization',
      defaultValue: '0.75',
      tweakValue: '0.50',
      title: 'Heap Target Ratio',
      description: 'Sets lower threshold ratio before GC collection runs.',
      category: 'CPU',
    ),
    TweakItem(
      key: 'debug.rs.max-threads',
      defaultValue: '4',
      tweakValue: '8',
      title: 'Max CPU Render Threads',
      description: 'Increases RenderScript parallel worker thread count to 8.',
      category: 'CPU',
    ),
    // Read-only specs moved & marked as isReadOnly
    TweakItem(
      key: 'ro.config.low_ram',
      defaultValue: 'false',
      tweakValue: 'false',
      title: 'Low RAM Hardware Spec',
      description: 'Read-only system build property indicating low memory profile.',
      category: 'CPU',
      isReadOnly: true,
    ),
  ];

  // Touch Responsiveness Tweaks
  static const List<TweakItem> touchTweaks = [
    TweakItem(
      key: 'windowsmgr.max_events_per_sec',
      defaultValue: '90',
      tweakValue: '300',
      title: 'Max Touch Sampling Rate',
      description: 'Boosts touch input sampling frequency up to 300Hz.',
      category: 'Touch',
    ),
    TweakItem(
      key: 'persist.sys.scrollingcache',
      defaultValue: '1',
      tweakValue: '3',
      title: 'Touch Scrolling Cache',
      description: 'Disables touch scroll caching overhead during fast gestures.',
      category: 'Touch',
    ),
    TweakItem(
      key: 'ro.min_pointer_dur',
      defaultValue: '16',
      tweakValue: '16',
      title: 'Min Pointer Duration (Read-only)',
      description: 'Read-only kernel touch sampling window property.',
      category: 'Touch',
      isReadOnly: true,
    ),
    TweakItem(
      key: 'ro.max.fling_velocity',
      defaultValue: '8000',
      tweakValue: '8000',
      title: 'Max Fling Velocity (Read-only)',
      description: 'Read-only system gesture fling threshold.',
      category: 'Touch',
      isReadOnly: true,
    ),
  ];

  // Network Performance Tweaks
  static const List<TweakItem> networkTweaks = [
    TweakItem(
      key: 'wifi.supplicant_scan_interval',
      defaultValue: '15',
      tweakValue: '180',
      title: 'WiFi Scan Interval',
      description: 'Increases WiFi scan delay to 180s to prevent ping spikes during gaming.',
      category: 'Network',
    ),
    TweakItem(
      key: 'net.tcp.buffersize.wifi',
      defaultValue: '524288,1048576,2097152',
      tweakValue: '4096,87380,256960,4096,16384,256960',
      title: 'Gaming TCP Buffer Size',
      description: 'Optimizes TCP window size for low-latency multiplayer gaming packet flow.',
      category: 'Network',
    ),
    TweakItem(
      key: 'net.dns1',
      defaultValue: '',
      tweakValue: '8.8.8.8',
      title: 'Google Primary DNS',
      description: 'Sets primary DNS resolver to 8.8.8.8 for faster domain lookup.',
      category: 'Network',
    ),
    TweakItem(
      key: 'net.dns2',
      defaultValue: '',
      tweakValue: '8.8.4.4',
      title: 'Google Secondary DNS',
      description: 'Sets secondary DNS resolver to 8.8.4.4 backup server.',
      category: 'Network',
    ),
  ];

  // System & Performance Tweaks
  static const List<TweakItem> systemTweaks = [
    TweakItem(
      key: 'persist.android.strictmode',
      defaultValue: '1',
      tweakValue: '0',
      title: 'Disable StrictMode Logging',
      description: 'Disables system disk/network I/O strict mode checks for less jank.',
      category: 'System',
    ),
    TweakItem(
      key: 'debug.sf.nobootanimation',
      defaultValue: '0',
      tweakValue: '1',
      title: 'Disable Boot Animation',
      description: 'Frees boot memory reserves by disabling splash animation.',
      category: 'System',
    ),
    TweakItem(
      key: 'profiler.force_disable_err_rpt',
      defaultValue: '0',
      tweakValue: '1',
      title: 'Disable Error Reporting',
      description: 'Stops background crash loggers from consuming CPU cycles.',
      category: 'System',
    ),
  ];
}
