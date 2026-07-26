// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Filipino Pilipino (`fil`).
class AppLocalizationsFil extends AppLocalizations {
  AppLocalizationsFil([String locale = 'fil']) : super(locale);

  @override
  String get appTitle => 'GAME SPACE';

  @override
  String get dashboard => 'Dashboard';

  @override
  String get metrics => 'Metrika';

  @override
  String get quickBoost => 'MABILIS NA BOOST';

  @override
  String get cpuTweaks => 'Mga Tweak sa CPU';

  @override
  String get gpuTweaks => 'Mga Tweak sa GPU';

  @override
  String get touchTweaks => 'Sensitibidad ng Touch';

  @override
  String get networkTweaks => 'Latency ng Network';

  @override
  String get performance => 'Pagganap';

  @override
  String get profiles => 'Mga Profile sa Laro';

  @override
  String get permissions => 'Mga Permiso';

  @override
  String get settings => 'Mga Setting';

  @override
  String get rootStatus => 'Katayuan ng Root Access';

  @override
  String get rootGranted => 'PINAGKALOOBAN NG ROOT';

  @override
  String get rootDenied => 'WALANG ROOT (INFO MODE)';

  @override
  String get deviceInfo => 'Espesipikasyon ng Aparato';

  @override
  String get manufacturer => 'Tagagawa';

  @override
  String get chipset => 'Chipset';

  @override
  String get cpuCores => 'Mga Core ng CPU';

  @override
  String get ramTotal => 'Kabuuan ng RAM';

  @override
  String get activeTweaks => 'Mga Aktibong Tweak';

  @override
  String get applyAllTweaks => 'I-apply ang Gaming Tweaks';

  @override
  String get resetTweaks => 'I-reset sa Default';

  @override
  String get language => 'Wika';

  @override
  String get theme => 'Tema';

  @override
  String get cpuOptimizations => 'OPTIMISASYON NG CPU';

  @override
  String get cpuGovernorMode => 'MODO NG CPU GOVERNOR';

  @override
  String get cpuGovernorDesc =>
      'Pumili ng governor mode upang kontrolin ang pag-scale ng frequency ng CPU.';

  @override
  String get setpropCpuTweaks => 'MGA CPU TWEAK NG SETPROP';

  @override
  String get gpuGraphicsTweaks => 'MGA TWEAK SA GPU AT GRAPHICS';

  @override
  String get surfaceFlingerComposition => 'SURFACEFLINGER COMPOSITION';

  @override
  String get surfaceFlingerDesc =>
      'I-force ang hardware GPU composition para mabawasan ang karga sa CPU.';

  @override
  String get setpropGpuTweaks => 'MGA GPU HARDWARE TWEAK NG SETPROP';

  @override
  String get touchResponsiveness => 'PAGTUGON NG TOUCH';

  @override
  String get touchSamplingRate => 'SLIDER NG TOUCH SAMPLING RATE';

  @override
  String samplingRateLabel(Object rate) {
    return 'Sampling Rate: $rate Hz';
  }

  @override
  String get latencyTestPad => 'PAGSUBOK SA LATENCY (I-TAP DITO)';

  @override
  String get tapAnywhere => 'I-tap kahit saan sa loob ng kahon';

  @override
  String registeredTaps(Object count) {
    return 'Naitalang Taps: $count';
  }

  @override
  String lastPos(Object x, Object y) {
    return 'Huling Posisyon: ($x, $y)';
  }

  @override
  String get setpropTouchTweaks => 'MGA TOUCH TWEAK NG SETPROP';

  @override
  String get networkLatency => 'NETWORK AT LATENCY';

  @override
  String get gamingDnsSelector => 'TAGAPAGPILI NG GAMING DNS RESOLVER';

  @override
  String activeResolver(Object dns) {
    return 'Aktibong Resolver: $dns';
  }

  @override
  String get setpropNetworkTweaks => 'MGA NETWORK LATENCY TWEAK NG SETPROP';

  @override
  String get systemPerformanceTweaks => 'MGA PERFORMANCE TWEAK NG SISTEMA';

  @override
  String get systemDalvikTweaks => 'MGA TWEAK SA SISTEMA AT DALVIK VM';

  @override
  String get gameTweakProfiles => 'MGA PROFILE NG GAME TWEAK';

  @override
  String exportedMagisk(Object path) {
    return 'Na-export ang Magisk Zip sa:\n$path';
  }

  @override
  String get failedExportMagisk => 'Nabigo ang pag-export ng Magisk zip.';

  @override
  String get active => 'AKTIBO';

  @override
  String containsTweaks(Object count) {
    return 'Naglalaman ng $count setprop configurations';
  }

  @override
  String get applyProfile => 'I-APPLY ANG PROFILE';

  @override
  String get profileApplied => 'NA-APPLY NA ANG PROFILE';

  @override
  String get exportMagiskZip => 'I-export ang Magisk Zip';

  @override
  String get systemPermissions => 'PERMISO NG SISTEMA';

  @override
  String get rootAccessSu => 'Root Access (SU binary)';

  @override
  String get rootGrantedDesc => 'Pinagkalooban - Nakabukas ang setprop tweaks';

  @override
  String get rootDeniedDesc => 'Tinanggihan - Tumatakbo sa Read-only Info mode';

  @override
  String get requestRootAccess => 'HUMILING NG ROOT ACCESS';

  @override
  String get cpuLoad => 'Karga ng CPU';

  @override
  String get ramUsage => 'Paggamit ng RAM';

  @override
  String get batteryPercent => 'Baterya %';

  @override
  String get optimizationModules => 'MGA MODYUL NG OPTIMISASYON';

  @override
  String errorLoadingDevice(Object message) {
    return 'May error sa pag-load ng impormasyon ng aparato: $message';
  }

  @override
  String get cores => 'Cores';

  @override
  String get appVersion => 'GAME SPACE PRO v1.0.0';

  @override
  String get supportedDevices =>
      'Sumusuporta sa Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).';
}
