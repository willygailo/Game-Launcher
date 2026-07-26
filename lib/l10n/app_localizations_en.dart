// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'GAME SPACE';

  @override
  String get dashboard => 'Dashboard';

  @override
  String get metrics => 'Metrics';

  @override
  String get quickBoost => 'QUICK BOOST';

  @override
  String get cpuTweaks => 'CPU Tweaks';

  @override
  String get gpuTweaks => 'GPU Tweaks';

  @override
  String get touchTweaks => 'Touch Sensitivity';

  @override
  String get networkTweaks => 'Network Latency';

  @override
  String get performance => 'Performance';

  @override
  String get profiles => 'Game Profiles';

  @override
  String get permissions => 'Permissions';

  @override
  String get settings => 'Settings';

  @override
  String get rootStatus => 'Root Access Status';

  @override
  String get rootGranted => 'ROOT GRANTED';

  @override
  String get rootDenied => 'NON-ROOTED (INFO MODE)';

  @override
  String get deviceInfo => 'Device Specification';

  @override
  String get manufacturer => 'Manufacturer';

  @override
  String get chipset => 'Chipset';

  @override
  String get cpuCores => 'CPU Cores';

  @override
  String get ramTotal => 'Total RAM';

  @override
  String get activeTweaks => 'Active Tweaks';

  @override
  String get applyAllTweaks => 'Apply Gaming Tweaks';

  @override
  String get resetTweaks => 'Reset Defaults';

  @override
  String get language => 'Language';

  @override
  String get theme => 'Theme';

  @override
  String get cpuOptimizations => 'CPU OPTIMIZATIONS';

  @override
  String get cpuGovernorMode => 'CPU GOVERNOR MODE';

  @override
  String get cpuGovernorDesc =>
      'Select governor mode to dictate how CPU frequency scales under load.';

  @override
  String get setpropCpuTweaks => 'SETPROP CPU TWEAKS';

  @override
  String get gpuGraphicsTweaks => 'GPU & GRAPHICS TWEAKS';

  @override
  String get surfaceFlingerComposition => 'SURFACEFLINGER COMPOSITION';

  @override
  String get surfaceFlingerDesc =>
      'Force hardware GPU composition to offload UI layer rendering from CPU.';

  @override
  String get setpropGpuTweaks => 'SETPROP GPU HARDWARE TWEAKS';

  @override
  String get touchResponsiveness => 'TOUCH RESPONSIVENESS';

  @override
  String get touchSamplingRate => 'TOUCH SAMPLING RATE SLIDER';

  @override
  String samplingRateLabel(Object rate) {
    return 'Sampling Rate: $rate Hz';
  }

  @override
  String get latencyTestPad => 'LATENCY TEST PAD (TAP HERE)';

  @override
  String get tapAnywhere => 'Tap anywhere inside box';

  @override
  String registeredTaps(Object count) {
    return 'Registered Taps: $count';
  }

  @override
  String lastPos(Object x, Object y) {
    return 'Last Pos: ($x, $y)';
  }

  @override
  String get setpropTouchTweaks => 'SETPROP TOUCH SENSITIVITY TWEAKS';

  @override
  String get networkLatency => 'NETWORK & LATENCY';

  @override
  String get gamingDnsSelector => 'GAMING DNS RESOLVER SELECTOR';

  @override
  String activeResolver(Object dns) {
    return 'Active Resolver: $dns';
  }

  @override
  String get setpropNetworkTweaks => 'SETPROP NETWORK LATENCY TWEAKS';

  @override
  String get systemPerformanceTweaks => 'SYSTEM PERFORMANCE TWEAKS';

  @override
  String get systemDalvikTweaks => 'SYSTEM & DALVIK VM TWEAKS';

  @override
  String get gameTweakProfiles => 'GAME TWEAK PROFILES';

  @override
  String exportedMagisk(Object path) {
    return 'Exported Magisk Zip to:\n$path';
  }

  @override
  String get failedExportMagisk => 'Failed to export Magisk zip.';

  @override
  String get active => 'ACTIVE';

  @override
  String containsTweaks(Object count) {
    return 'Contains $count setprop configurations';
  }

  @override
  String get applyProfile => 'APPLY PROFILE';

  @override
  String get profileApplied => 'PROFILE APPLIED';

  @override
  String get exportMagiskZip => 'Export Magisk Zip';

  @override
  String get systemPermissions => 'SYSTEM PERMISSIONS';

  @override
  String get rootAccessSu => 'Root Access (SU binary)';

  @override
  String get rootGrantedDesc => 'Granted - setprop tweaks unlocked';

  @override
  String get rootDeniedDesc => 'Denied - Running in Read-only Info mode';

  @override
  String get requestRootAccess => 'REQUEST ROOT ACCESS';

  @override
  String get cpuLoad => 'CPU Load';

  @override
  String get ramUsage => 'RAM Usage';

  @override
  String get batteryPercent => 'Battery %';

  @override
  String get optimizationModules => 'OPTIMIZATION MODULES';

  @override
  String errorLoadingDevice(Object message) {
    return 'Error loading device info: $message';
  }

  @override
  String get cores => 'Cores';

  @override
  String get appVersion => 'GAME SPACE PRO v1.0.0';

  @override
  String get supportedDevices =>
      'Supports Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).';
}
