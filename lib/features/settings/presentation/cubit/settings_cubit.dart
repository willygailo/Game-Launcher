import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'settings_state.dart';

class SettingsCubit extends Cubit<SettingsState> {
  SettingsCubit()
      : super(const SettingsState(
          locale: Locale('en'),
          themeMode: ThemeMode.dark,
        ));

  void changeLanguage(String languageCode) {
    emit(state.copyWith(locale: Locale(languageCode)));
  }

  void toggleTheme(bool isDark) {
    emit(state.copyWith(themeMode: isDark ? ThemeMode.dark : ThemeMode.light));
  }
}
