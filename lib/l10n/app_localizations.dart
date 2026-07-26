import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_ar.dart';
import 'app_localizations_en.dart';
import 'app_localizations_es.dart';
import 'app_localizations_fil.dart';
import 'app_localizations_fr.dart';
import 'app_localizations_id.dart';
import 'app_localizations_sw.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
      : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
    delegate,
    GlobalMaterialLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
  ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('ar'),
    Locale('en'),
    Locale('es'),
    Locale('fil'),
    Locale('fr'),
    Locale('id'),
    Locale('sw')
  ];

  /// No description provided for @appTitle.
  ///
  /// In en, this message translates to:
  /// **'GAME SPACE'**
  String get appTitle;

  /// No description provided for @dashboard.
  ///
  /// In en, this message translates to:
  /// **'Dashboard'**
  String get dashboard;

  /// No description provided for @metrics.
  ///
  /// In en, this message translates to:
  /// **'Metrics'**
  String get metrics;

  /// No description provided for @quickBoost.
  ///
  /// In en, this message translates to:
  /// **'QUICK BOOST'**
  String get quickBoost;

  /// No description provided for @cpuTweaks.
  ///
  /// In en, this message translates to:
  /// **'CPU Tweaks'**
  String get cpuTweaks;

  /// No description provided for @gpuTweaks.
  ///
  /// In en, this message translates to:
  /// **'GPU Tweaks'**
  String get gpuTweaks;

  /// No description provided for @touchTweaks.
  ///
  /// In en, this message translates to:
  /// **'Touch Sensitivity'**
  String get touchTweaks;

  /// No description provided for @networkTweaks.
  ///
  /// In en, this message translates to:
  /// **'Network Latency'**
  String get networkTweaks;

  /// No description provided for @performance.
  ///
  /// In en, this message translates to:
  /// **'Performance'**
  String get performance;

  /// No description provided for @profiles.
  ///
  /// In en, this message translates to:
  /// **'Game Profiles'**
  String get profiles;

  /// No description provided for @permissions.
  ///
  /// In en, this message translates to:
  /// **'Permissions'**
  String get permissions;

  /// No description provided for @settings.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get settings;

  /// No description provided for @rootStatus.
  ///
  /// In en, this message translates to:
  /// **'Root Access Status'**
  String get rootStatus;

  /// No description provided for @rootGranted.
  ///
  /// In en, this message translates to:
  /// **'ROOT GRANTED'**
  String get rootGranted;

  /// No description provided for @rootDenied.
  ///
  /// In en, this message translates to:
  /// **'NON-ROOTED (INFO MODE)'**
  String get rootDenied;

  /// No description provided for @deviceInfo.
  ///
  /// In en, this message translates to:
  /// **'Device Specification'**
  String get deviceInfo;

  /// No description provided for @manufacturer.
  ///
  /// In en, this message translates to:
  /// **'Manufacturer'**
  String get manufacturer;

  /// No description provided for @chipset.
  ///
  /// In en, this message translates to:
  /// **'Chipset'**
  String get chipset;

  /// No description provided for @cpuCores.
  ///
  /// In en, this message translates to:
  /// **'CPU Cores'**
  String get cpuCores;

  /// No description provided for @ramTotal.
  ///
  /// In en, this message translates to:
  /// **'Total RAM'**
  String get ramTotal;

  /// No description provided for @activeTweaks.
  ///
  /// In en, this message translates to:
  /// **'Active Tweaks'**
  String get activeTweaks;

  /// No description provided for @applyAllTweaks.
  ///
  /// In en, this message translates to:
  /// **'Apply Gaming Tweaks'**
  String get applyAllTweaks;

  /// No description provided for @resetTweaks.
  ///
  /// In en, this message translates to:
  /// **'Reset Defaults'**
  String get resetTweaks;

  /// No description provided for @language.
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get language;

  /// No description provided for @theme.
  ///
  /// In en, this message translates to:
  /// **'Theme'**
  String get theme;

  /// No description provided for @cpuOptimizations.
  ///
  /// In en, this message translates to:
  /// **'CPU OPTIMIZATIONS'**
  String get cpuOptimizations;

  /// No description provided for @cpuGovernorMode.
  ///
  /// In en, this message translates to:
  /// **'CPU GOVERNOR MODE'**
  String get cpuGovernorMode;

  /// No description provided for @cpuGovernorDesc.
  ///
  /// In en, this message translates to:
  /// **'Select governor mode to dictate how CPU frequency scales under load.'**
  String get cpuGovernorDesc;

  /// No description provided for @setpropCpuTweaks.
  ///
  /// In en, this message translates to:
  /// **'SETPROP CPU TWEAKS'**
  String get setpropCpuTweaks;

  /// No description provided for @gpuGraphicsTweaks.
  ///
  /// In en, this message translates to:
  /// **'GPU & GRAPHICS TWEAKS'**
  String get gpuGraphicsTweaks;

  /// No description provided for @surfaceFlingerComposition.
  ///
  /// In en, this message translates to:
  /// **'SURFACEFLINGER COMPOSITION'**
  String get surfaceFlingerComposition;

  /// No description provided for @surfaceFlingerDesc.
  ///
  /// In en, this message translates to:
  /// **'Force hardware GPU composition to offload UI layer rendering from CPU.'**
  String get surfaceFlingerDesc;

  /// No description provided for @setpropGpuTweaks.
  ///
  /// In en, this message translates to:
  /// **'SETPROP GPU HARDWARE TWEAKS'**
  String get setpropGpuTweaks;

  /// No description provided for @touchResponsiveness.
  ///
  /// In en, this message translates to:
  /// **'TOUCH RESPONSIVENESS'**
  String get touchResponsiveness;

  /// No description provided for @touchSamplingRate.
  ///
  /// In en, this message translates to:
  /// **'TOUCH SAMPLING RATE SLIDER'**
  String get touchSamplingRate;

  /// No description provided for @samplingRateLabel.
  ///
  /// In en, this message translates to:
  /// **'Sampling Rate: {rate} Hz'**
  String samplingRateLabel(Object rate);

  /// No description provided for @latencyTestPad.
  ///
  /// In en, this message translates to:
  /// **'LATENCY TEST PAD (TAP HERE)'**
  String get latencyTestPad;

  /// No description provided for @tapAnywhere.
  ///
  /// In en, this message translates to:
  /// **'Tap anywhere inside box'**
  String get tapAnywhere;

  /// No description provided for @registeredTaps.
  ///
  /// In en, this message translates to:
  /// **'Registered Taps: {count}'**
  String registeredTaps(Object count);

  /// No description provided for @lastPos.
  ///
  /// In en, this message translates to:
  /// **'Last Pos: ({x}, {y})'**
  String lastPos(Object x, Object y);

  /// No description provided for @setpropTouchTweaks.
  ///
  /// In en, this message translates to:
  /// **'SETPROP TOUCH SENSITIVITY TWEAKS'**
  String get setpropTouchTweaks;

  /// No description provided for @networkLatency.
  ///
  /// In en, this message translates to:
  /// **'NETWORK & LATENCY'**
  String get networkLatency;

  /// No description provided for @gamingDnsSelector.
  ///
  /// In en, this message translates to:
  /// **'GAMING DNS RESOLVER SELECTOR'**
  String get gamingDnsSelector;

  /// No description provided for @activeResolver.
  ///
  /// In en, this message translates to:
  /// **'Active Resolver: {dns}'**
  String activeResolver(Object dns);

  /// No description provided for @setpropNetworkTweaks.
  ///
  /// In en, this message translates to:
  /// **'SETPROP NETWORK LATENCY TWEAKS'**
  String get setpropNetworkTweaks;

  /// No description provided for @systemPerformanceTweaks.
  ///
  /// In en, this message translates to:
  /// **'SYSTEM PERFORMANCE TWEAKS'**
  String get systemPerformanceTweaks;

  /// No description provided for @systemDalvikTweaks.
  ///
  /// In en, this message translates to:
  /// **'SYSTEM & DALVIK VM TWEAKS'**
  String get systemDalvikTweaks;

  /// No description provided for @gameTweakProfiles.
  ///
  /// In en, this message translates to:
  /// **'GAME TWEAK PROFILES'**
  String get gameTweakProfiles;

  /// No description provided for @exportedMagisk.
  ///
  /// In en, this message translates to:
  /// **'Exported Magisk Zip to:\n{path}'**
  String exportedMagisk(Object path);

  /// No description provided for @failedExportMagisk.
  ///
  /// In en, this message translates to:
  /// **'Failed to export Magisk zip.'**
  String get failedExportMagisk;

  /// No description provided for @active.
  ///
  /// In en, this message translates to:
  /// **'ACTIVE'**
  String get active;

  /// No description provided for @containsTweaks.
  ///
  /// In en, this message translates to:
  /// **'Contains {count} setprop configurations'**
  String containsTweaks(Object count);

  /// No description provided for @applyProfile.
  ///
  /// In en, this message translates to:
  /// **'APPLY PROFILE'**
  String get applyProfile;

  /// No description provided for @profileApplied.
  ///
  /// In en, this message translates to:
  /// **'PROFILE APPLIED'**
  String get profileApplied;

  /// No description provided for @exportMagiskZip.
  ///
  /// In en, this message translates to:
  /// **'Export Magisk Zip'**
  String get exportMagiskZip;

  /// No description provided for @systemPermissions.
  ///
  /// In en, this message translates to:
  /// **'SYSTEM PERMISSIONS'**
  String get systemPermissions;

  /// No description provided for @rootAccessSu.
  ///
  /// In en, this message translates to:
  /// **'Root Access (SU binary)'**
  String get rootAccessSu;

  /// No description provided for @rootGrantedDesc.
  ///
  /// In en, this message translates to:
  /// **'Granted - setprop tweaks unlocked'**
  String get rootGrantedDesc;

  /// No description provided for @rootDeniedDesc.
  ///
  /// In en, this message translates to:
  /// **'Denied - Running in Read-only Info mode'**
  String get rootDeniedDesc;

  /// No description provided for @requestRootAccess.
  ///
  /// In en, this message translates to:
  /// **'REQUEST ROOT ACCESS'**
  String get requestRootAccess;

  /// No description provided for @cpuLoad.
  ///
  /// In en, this message translates to:
  /// **'CPU Load'**
  String get cpuLoad;

  /// No description provided for @ramUsage.
  ///
  /// In en, this message translates to:
  /// **'RAM Usage'**
  String get ramUsage;

  /// No description provided for @batteryPercent.
  ///
  /// In en, this message translates to:
  /// **'Battery %'**
  String get batteryPercent;

  /// No description provided for @optimizationModules.
  ///
  /// In en, this message translates to:
  /// **'OPTIMIZATION MODULES'**
  String get optimizationModules;

  /// No description provided for @errorLoadingDevice.
  ///
  /// In en, this message translates to:
  /// **'Error loading device info: {message}'**
  String errorLoadingDevice(Object message);

  /// No description provided for @cores.
  ///
  /// In en, this message translates to:
  /// **'Cores'**
  String get cores;

  /// No description provided for @appVersion.
  ///
  /// In en, this message translates to:
  /// **'GAME SPACE PRO v1.0.0'**
  String get appVersion;

  /// No description provided for @supportedDevices.
  ///
  /// In en, this message translates to:
  /// **'Supports Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).'**
  String get supportedDevices;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) => <String>[
        'ar',
        'en',
        'es',
        'fil',
        'fr',
        'id',
        'sw'
      ].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'ar':
      return AppLocalizationsAr();
    case 'en':
      return AppLocalizationsEn();
    case 'es':
      return AppLocalizationsEs();
    case 'fil':
      return AppLocalizationsFil();
    case 'fr':
      return AppLocalizationsFr();
    case 'id':
      return AppLocalizationsId();
    case 'sw':
      return AppLocalizationsSw();
  }

  throw FlutterError(
      'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
      'an issue with the localizations generation tool. Please file an issue '
      'on GitHub with a reproducible sample app and the gen-l10n configuration '
      'that was used.');
}
