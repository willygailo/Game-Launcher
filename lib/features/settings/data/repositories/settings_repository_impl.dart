import '../../domain/entities/app_settings.dart';
import '../../domain/repositories/settings_repository.dart';
import '../datasources/settings_datasource.dart';

class SettingsRepositoryImpl implements SettingsRepository {
  final SettingsDatasource datasource;
  SettingsRepositoryImpl({required this.datasource});

  @override
  Future<AppSettings> getSettings() async => datasource.getSettings();

  @override
  Future<bool> saveSettings(AppSettings settings) =>
      datasource.saveSettings(settings);
}
