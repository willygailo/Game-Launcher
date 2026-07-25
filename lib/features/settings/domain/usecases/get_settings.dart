import '../entities/app_settings.dart';
import '../repositories/settings_repository.dart';

class GetSettings {
  final SettingsRepository repository;
  const GetSettings(this.repository);
  Future<AppSettings> call() => repository.getSettings();
}
