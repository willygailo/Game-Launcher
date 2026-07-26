// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for French (`fr`).
class AppLocalizationsFr extends AppLocalizations {
  AppLocalizationsFr([String locale = 'fr']) : super(locale);

  @override
  String get appTitle => 'GAME SPACE';

  @override
  String get dashboard => 'Tableau de bord';

  @override
  String get metrics => 'Métriques';

  @override
  String get quickBoost => 'BOOST RAPIDE';

  @override
  String get cpuTweaks => 'Optimisations CPU';

  @override
  String get gpuTweaks => 'Optimisations GPU';

  @override
  String get touchTweaks => 'Sensibilité Tactile';

  @override
  String get networkTweaks => 'Réseau & Latence';

  @override
  String get performance => 'Performance';

  @override
  String get profiles => 'Profils de Jeu';

  @override
  String get permissions => 'Autorisations';

  @override
  String get settings => 'Paramètres';

  @override
  String get rootStatus => 'Statut d\'Accès Root';

  @override
  String get rootGranted => 'ROOT ACCORDÉ';

  @override
  String get rootDenied => 'NON-ROOTÉ (MODE INFO)';

  @override
  String get deviceInfo => 'Spécification de l\'Appareil';

  @override
  String get manufacturer => 'Fabricant';

  @override
  String get chipset => 'Processeur';

  @override
  String get cpuCores => 'Cœurs CPU';

  @override
  String get ramTotal => 'RAM Totale';

  @override
  String get activeTweaks => 'Optimisations Actives';

  @override
  String get applyAllTweaks => 'Appliquer les Réglages';

  @override
  String get resetTweaks => 'Réinitialiser';

  @override
  String get language => 'Langue';

  @override
  String get theme => 'Thème';

  @override
  String get cpuOptimizations => 'OPTIMISATIONS CPU';

  @override
  String get cpuGovernorMode => 'MODE GOVERNOR CPU';

  @override
  String get cpuGovernorDesc =>
      'Sélectionnez le mode governor pour réguler la fréquence du CPU sous charge.';

  @override
  String get setpropCpuTweaks => 'RÉGLAGES SETPROP CPU';

  @override
  String get gpuGraphicsTweaks => 'RÉGLAGES GPU ET GRAPHISMES';

  @override
  String get surfaceFlingerComposition => 'COMPOSITION SURFACEFLINGER';

  @override
  String get surfaceFlingerDesc =>
      'Forcer la composition GPU matérielle pour alléger le processeur.';

  @override
  String get setpropGpuTweaks => 'RÉGLAGES SETPROP GPU';

  @override
  String get touchResponsiveness => 'RÉACIVITÉ TACTILE';

  @override
  String get touchSamplingRate => 'TAUX D\'ÉCHANTILLONNAGE TACTILE';

  @override
  String samplingRateLabel(Object rate) {
    return 'Taux d\'échantillonnage: $rate Hz';
  }

  @override
  String get latencyTestPad => 'TEST DE LATENCE (TOUCHEZ ICI)';

  @override
  String get tapAnywhere => 'Touchez n\'importe où dans le cadre';

  @override
  String registeredTaps(Object count) {
    return 'Touches enregistrées: $count';
  }

  @override
  String lastPos(Object x, Object y) {
    return 'Dernière Pos: ($x, $y)';
  }

  @override
  String get setpropTouchTweaks => 'RÉGLAGES SETPROP TACTILE';

  @override
  String get networkLatency => 'RÉSEAU ET LATENCE';

  @override
  String get gamingDnsSelector => 'SÉLECTEUR DE RESOLVEUR DNS JEUX';

  @override
  String activeResolver(Object dns) {
    return 'Résolveur Actif: $dns';
  }

  @override
  String get setpropNetworkTweaks => 'RÉGLAGES SETPROP RÉSEAU';

  @override
  String get systemPerformanceTweaks => 'RÉGLAGES DE PERFORMANCE SYSTÈME';

  @override
  String get systemDalvikTweaks => 'RÉGLAGES SYSTÈME ET DALVIK VM';

  @override
  String get gameTweakProfiles => 'PROFILS D\'OPTIMISATION DE JEU';

  @override
  String exportedMagisk(Object path) {
    return 'Zip Magisk exporté vers:\n$path';
  }

  @override
  String get failedExportMagisk => 'Échec de l\'exportation du zip Magisk.';

  @override
  String get active => 'ACTIF';

  @override
  String containsTweaks(Object count) {
    return 'Contient $count configurations setprop';
  }

  @override
  String get applyProfile => 'APPLIQUER LE PROFIL';

  @override
  String get profileApplied => 'PROFIL APPLIQUÉ';

  @override
  String get exportMagiskZip => 'Exporter Zip Magisk';

  @override
  String get systemPermissions => 'AUTORISATIONS SYSTÈME';

  @override
  String get rootAccessSu => 'Accès Root (binaire SU)';

  @override
  String get rootGrantedDesc => 'Accordé - Réglages setprop déverrouillés';

  @override
  String get rootDeniedDesc =>
      'Refusé - Exécution en mode info (Lecture seule)';

  @override
  String get requestRootAccess => 'DEMANDER L\'ACCÈS ROOT';

  @override
  String get cpuLoad => 'Charge CPU';

  @override
  String get ramUsage => 'Utilisation RAM';

  @override
  String get batteryPercent => 'Batterie %';

  @override
  String get optimizationModules => 'MODULES D\'OPTIMISATION';

  @override
  String errorLoadingDevice(Object message) {
    return 'Erreur lors du chargement de l\'appareil: $message';
  }

  @override
  String get cores => 'Cœurs';

  @override
  String get appVersion => 'GAME SPACE PRO v1.0.0';

  @override
  String get supportedDevices =>
      'Prend en charge Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).';
}
