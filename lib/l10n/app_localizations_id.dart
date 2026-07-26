// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Indonesian (`id`).
class AppLocalizationsId extends AppLocalizations {
  AppLocalizationsId([String locale = 'id']) : super(locale);

  @override
  String get appTitle => 'GAME SPACE';

  @override
  String get dashboard => 'Dasbor';

  @override
  String get metrics => 'Metrik';

  @override
  String get quickBoost => 'BOOST CEPAT';

  @override
  String get cpuTweaks => 'Tweak CPU';

  @override
  String get gpuTweaks => 'Tweak GPU';

  @override
  String get touchTweaks => 'Sensitivitas Sentuh';

  @override
  String get networkTweaks => 'Jaringan & Latensi';

  @override
  String get performance => 'Performa';

  @override
  String get profiles => 'Profil Game';

  @override
  String get permissions => 'Izin';

  @override
  String get settings => 'Pengaturan';

  @override
  String get rootStatus => 'Status Akses Root';

  @override
  String get rootGranted => 'ROOT DIIZINKAN';

  @override
  String get rootDenied => 'TANPA ROOT (MODE INFO)';

  @override
  String get deviceInfo => 'Spesifikasi Perangkat';

  @override
  String get manufacturer => 'Produsen';

  @override
  String get chipset => 'Chipset';

  @override
  String get cpuCores => 'Inti CPU';

  @override
  String get ramTotal => 'Total RAM';

  @override
  String get activeTweaks => 'Tweak Aktif';

  @override
  String get applyAllTweaks => 'Terapkan Tweak Game';

  @override
  String get resetTweaks => 'Reset Default';

  @override
  String get language => 'Bahasa';

  @override
  String get theme => 'Tema';

  @override
  String get cpuOptimizations => 'OPTIMASI CPU';

  @override
  String get cpuGovernorMode => 'MODE GOVERNOR CPU';

  @override
  String get cpuGovernorDesc =>
      'Pilih mode governor untuk mengatur skala frekuensi CPU saat beban tinggi.';

  @override
  String get setpropCpuTweaks => 'TWEAK SETPROP CPU';

  @override
  String get gpuGraphicsTweaks => 'TWEAK GPU & GRAFIS';

  @override
  String get surfaceFlingerComposition => 'KOMPOSISI SURFACEFLINGER';

  @override
  String get surfaceFlingerDesc =>
      'Paksa komposisi GPU perangkat keras untuk mengurangi beban CPU.';

  @override
  String get setpropGpuTweaks => 'TWEAK SETPROP GPU HARDWARE';

  @override
  String get touchResponsiveness => 'RESPONSHIVITAS SENTUH';

  @override
  String get touchSamplingRate => 'SLIDER SAMPLING RATE SENTUH';

  @override
  String samplingRateLabel(Object rate) {
    return 'Sampling Rate: $rate Hz';
  }

  @override
  String get latencyTestPad => 'PAD UJI LATENSI (TEKAN DI SINI)';

  @override
  String get tapAnywhere => 'Tekan di mana saja di dalam kotak';

  @override
  String registeredTaps(Object count) {
    return 'Sentuhan Terdaftar: $count';
  }

  @override
  String lastPos(Object x, Object y) {
    return 'Posisi Terakhir: ($x, $y)';
  }

  @override
  String get setpropTouchTweaks => 'TWEAK SETPROP SENSITIVITAS SENTUH';

  @override
  String get networkLatency => 'JARINGAN & LATENSI';

  @override
  String get gamingDnsSelector => 'PEMILIH RESOLVER DNS GAMING';

  @override
  String activeResolver(Object dns) {
    return 'Resolver Aktif: $dns';
  }

  @override
  String get setpropNetworkTweaks => 'TWEAK SETPROP LATENSI JARINGAN';

  @override
  String get systemPerformanceTweaks => 'TWEAK PERFORMA SISTEM';

  @override
  String get systemDalvikTweaks => 'TWEAK SISTEM & DALVIK VM';

  @override
  String get gameTweakProfiles => 'PROFIL TWEAK GAME';

  @override
  String exportedMagisk(Object path) {
    return 'Berhasil mengekspor Magisk Zip ke:\n$path';
  }

  @override
  String get failedExportMagisk => 'Gagal mengekspor Magisk zip.';

  @override
  String get active => 'AKTIF';

  @override
  String containsTweaks(Object count) {
    return 'Berisi $count konfigurasi setprop';
  }

  @override
  String get applyProfile => 'TERAPKAN PROFIL';

  @override
  String get profileApplied => 'PROFIL DITERAPKAN';

  @override
  String get exportMagiskZip => 'Ekspor Magisk Zip';

  @override
  String get systemPermissions => 'IZIN SISTEM';

  @override
  String get rootAccessSu => 'Akses Root (biner SU)';

  @override
  String get rootGrantedDesc => 'Diizinkan - Tweak setprop terbuka';

  @override
  String get rootDeniedDesc => 'Ditolak - Berjalan dalam mode Info Read-only';

  @override
  String get requestRootAccess => 'MINTA AKSES ROOT';

  @override
  String get cpuLoad => 'Beban CPU';

  @override
  String get ramUsage => 'Penggunaan RAM';

  @override
  String get batteryPercent => 'Baterai %';

  @override
  String get optimizationModules => 'MODUL OPTIMASI';

  @override
  String errorLoadingDevice(Object message) {
    return 'Gagal memuat info perangkat: $message';
  }

  @override
  String get cores => 'Inti';

  @override
  String get appVersion => 'GAME SPACE PRO v1.0.0';

  @override
  String get supportedDevices =>
      'Mendukung Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).';
}
