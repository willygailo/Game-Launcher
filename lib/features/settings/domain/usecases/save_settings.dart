import '../entities/app_settings.dart';
import '../repositories/settings_repository.dart';

class SaveSettings {
  final SettingsRepository repository;
  const SaveSettings(this.repository);
  Future<bool> call(AppSettings settings) => repository.saveSettings(settings);
}
