import 'package:shared_preferences/shared_preferences.dart';

import '../../domain/entities/app_settings.dart';

const _kLanguageKey = 'settings_language';
const _kDarkModeKey = 'settings_dark_mode';
const _kBootTweaksKey = 'settings_apply_on_boot';

class SettingsDatasource {
  final SharedPreferences prefs;

  SettingsDatasource({required this.prefs});

  AppSettings getSettings() {
    return AppSettings(
      languageCode: prefs.getString(_kLanguageKey) ?? 'en',
      isDarkMode: prefs.getBool(_kDarkModeKey) ?? true,
      applyTweaksOnBoot: prefs.getBool(_kBootTweaksKey) ?? false,
    );
  }

  Future<bool> saveSettings(AppSettings settings) async {
    await prefs.setString(_kLanguageKey, settings.languageCode);
    await prefs.setBool(_kDarkModeKey, settings.isDarkMode);
    await prefs.setBool(_kBootTweaksKey, settings.applyTweaksOnBoot);
    return true;
  }
}
