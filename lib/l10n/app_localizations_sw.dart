// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Swahili (`sw`).
class AppLocalizationsSw extends AppLocalizations {
  AppLocalizationsSw([String locale = 'sw']) : super(locale);

  @override
  String get appTitle => 'GAME SPACE';

  @override
  String get dashboard => 'Dashibodi';

  @override
  String get metrics => 'Vipimo';

  @override
  String get quickBoost => 'BOOST YA HARAKA';

  @override
  String get cpuTweaks => 'Marekebisho ya CPU';

  @override
  String get gpuTweaks => 'Marekebisho ya GPU';

  @override
  String get touchTweaks => 'Unyeti wa Kugusa';

  @override
  String get networkTweaks => 'Mtandao na Latency';

  @override
  String get performance => 'Utendaji';

  @override
  String get profiles => 'Wasifu wa Michezo';

  @override
  String get permissions => 'Ruhusa';

  @override
  String get settings => 'Mipangilio';

  @override
  String get rootStatus => 'Hali ya Ufikiaji wa Root';

  @override
  String get rootGranted => 'ROOT IMERUHUSIWA';

  @override
  String get rootDenied => 'BILA ROOT (HALI YA TAARIFA)';

  @override
  String get deviceInfo => 'Vipimo vya Kifaa';

  @override
  String get manufacturer => 'Mtengenezaji';

  @override
  String get chipset => 'Chipset';

  @override
  String get cpuCores => 'Viini vya CPU';

  @override
  String get ramTotal => 'Jumla ya RAM';

  @override
  String get activeTweaks => 'Marekebisho Yaliyofanya Kazi';

  @override
  String get applyAllTweaks => 'Weka Marekebisho';

  @override
  String get resetTweaks => 'Rejesha Chaguomsingi';

  @override
  String get language => 'Lugha';

  @override
  String get theme => 'Mada';

  @override
  String get cpuOptimizations => 'UTENDANJI WA CPU';

  @override
  String get cpuGovernorMode => 'HALI YA GOVERNOR WA CPU';

  @override
  String get cpuGovernorDesc =>
      'Chagua hali ya governor kudhibiti jinsi mzunguko wa CPU unavyobadilika.';

  @override
  String get setpropCpuTweaks => 'MAREKEBISHO YA SETPROP YA CPU';

  @override
  String get gpuGraphicsTweaks => 'MAREKEBISHO YA GPU NA PICHA';

  @override
  String get surfaceFlingerComposition => 'MUUNDO WA SURFACEFLINGER';

  @override
  String get surfaceFlingerDesc =>
      'Weka muundo wa GPU wa kifaa ili kupunguza mzigo kwenye CPU.';

  @override
  String get setpropGpuTweaks => 'MAREKEBISHO YA SETPROP YA GPU';

  @override
  String get touchResponsiveness => 'MJIBU WA KUGUSA';

  @override
  String get touchSamplingRate => 'KITELEZI CHA KIWANGO CHA KUGUSA';

  @override
  String samplingRateLabel(Object rate) {
    return 'Kiwango cha Kugusa: $rate Hz';
  }

  @override
  String get latencyTestPad => 'SEHEMU YA KUPIMA LATENCY (BONYEZA HAPA)';

  @override
  String get tapAnywhere => 'Bonyeza mahali popote ndani ya sanduku';

  @override
  String registeredTaps(Object count) {
    return 'Mibonyezo iliyosajiliwa: $count';
  }

  @override
  String lastPos(Object x, Object y) {
    return 'Eneo la Mwisho: ($x, $y)';
  }

  @override
  String get setpropTouchTweaks => 'MAREKEBISHO YA SETPROP YA KUGUSA';

  @override
  String get networkLatency => 'MTANDAO NA LATENCY';

  @override
  String get gamingDnsSelector => 'MCHAGUZI WA GAMING DNS RESOLVER';

  @override
  String activeResolver(Object dns) {
    return 'Resolver Inayofanya Kazi: $dns';
  }

  @override
  String get setpropNetworkTweaks => 'MAREKEBISHO YA SETPROP YA MTANDAO';

  @override
  String get systemPerformanceTweaks => 'MAREKEBISHO YA UTENDANJI WA MFUMO';

  @override
  String get systemDalvikTweaks => 'MAREKEBISHO YA MFUMO NA DALVIK VM';

  @override
  String get gameTweakProfiles => 'WASIFU WA MAREKEBISHO YA MICHEZO';

  @override
  String exportedMagisk(Object path) {
    return 'Magisk Zip imetolewa kwa:\n$path';
  }

  @override
  String get failedExportMagisk => 'Imeshindwa kutoa Magisk zip.';

  @override
  String get active => 'INAFANYA KAZI';

  @override
  String containsTweaks(Object count) {
    return 'Inajumuisha mipangilio $count ya setprop';
  }

  @override
  String get applyProfile => 'WEKA WASIFU';

  @override
  String get profileApplied => 'WASIFU UMEWEKWA';

  @override
  String get exportMagiskZip => 'Toa Magisk Zip';

  @override
  String get systemPermissions => 'RUHUSA ZA MFUMO';

  @override
  String get rootAccessSu => 'Ufikiaji wa Root (SU binary)';

  @override
  String get rootGrantedDesc =>
      'Imeruhusiwa - Marekebisho ya setprop yamefunguliwa';

  @override
  String get rootDeniedDesc => 'Imekataliwa - Inafanya kazi kwa Somo pekee';

  @override
  String get requestRootAccess => 'OMBA UFIKIAJI WA ROOT';

  @override
  String get cpuLoad => 'Mzigo wa CPU';

  @override
  String get ramUsage => 'Matumizi ya RAM';

  @override
  String get batteryPercent => 'Betri %';

  @override
  String get optimizationModules => 'MODULI ZA OPTIMIZATION';

  @override
  String errorLoadingDevice(Object message) {
    return 'Itilafu wakati wa kupakia taarifa za kifaa: $message';
  }

  @override
  String get cores => 'Viini';

  @override
  String get appVersion => 'GAME SPACE PRO v1.0.0';

  @override
  String get supportedDevices =>
      'Inasaidia Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).';
}
