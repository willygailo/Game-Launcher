import 'package:equatable/equatable.dart';

/// Entity representing user-configurable app settings.
class AppSettings extends Equatable {
  final String languageCode;
  final bool isDarkMode;
  final bool applyTweaksOnBoot;

  const AppSettings({
    required this.languageCode,
    required this.isDarkMode,
    required this.applyTweaksOnBoot,
  });

  AppSettings copyWith({
    String? languageCode,
    bool? isDarkMode,
    bool? applyTweaksOnBoot,
  }) {
    return AppSettings(
      languageCode: languageCode ?? this.languageCode,
      isDarkMode: isDarkMode ?? this.isDarkMode,
      applyTweaksOnBoot: applyTweaksOnBoot ?? this.applyTweaksOnBoot,
    );
  }

  @override
  List<Object?> get props => [languageCode, isDarkMode, applyTweaksOnBoot];
}
