import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/entities/app_settings.dart';
import '../../domain/usecases/get_settings.dart';
import '../../domain/usecases/save_settings.dart';
import 'settings_state.dart';

class SettingsCubit extends Cubit<SettingsState> {
  final GetSettings _getSettings;
  final SaveSettings _saveSettings;

  SettingsCubit({
    required GetSettings getSettings,
    required SaveSettings saveSettings,
  })  : _getSettings = getSettings,
        _saveSettings = saveSettings,
        super(const SettingsState(
          locale: Locale('en'),
          themeMode: ThemeMode.dark,
        ));

  /// Load persisted settings at startup.
  Future<void> init() async {
    final AppSettings s = await _getSettings();
    emit(SettingsState(
      locale: Locale(s.languageCode),
      themeMode: s.isDarkMode ? ThemeMode.dark : ThemeMode.light,
    ));
  }

  Future<void> changeLanguage(String languageCode) async {
    final newState = state.copyWith(locale: Locale(languageCode));
    emit(newState);
    await _persist(newState);
  }

  Future<void> toggleTheme(bool isDark) async {
    final newState = state.copyWith(
      themeMode: isDark ? ThemeMode.dark : ThemeMode.light,
    );
    emit(newState);
    await _persist(newState);
  }

  Future<void> _persist(SettingsState s) => _saveSettings(
        AppSettings(
          languageCode: s.locale.languageCode,
          isDarkMode: s.themeMode == ThemeMode.dark,
          applyTweaksOnBoot: false, // extended in P1.2
        ),
      );
}
