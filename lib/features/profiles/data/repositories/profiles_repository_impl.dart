import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/game_profile.dart';
import '../../domain/repositories/profiles_repository.dart';
import '../datasources/profiles_datasource.dart';

class ProfilesRepositoryImpl implements ProfilesRepository {
  final ProfilesDatasource datasource;
  final RootCommandService rootCommandService;

  ProfilesRepositoryImpl({
    required this.datasource,
    required this.rootCommandService,
  });

  @override
  Future<List<GameProfile>> getProfiles() async => datasource.getProfiles();

  @override
  Future<bool> activateProfile(GameProfile profile) async {
    for (final entry in profile.tweaks.entries) {
      final result = await rootCommandService.executeCommand(
        'setprop ${entry.key} ${entry.value}',
      );
      if (result.exitCode != 0) return false;
    }
    // Mark active in storage.
    final updated = profile.copyWith(isActive: true);
    return datasource.saveProfile(updated);
  }

  @override
  Future<bool> saveProfile(GameProfile profile) =>
      datasource.saveProfile(profile);

  @override
  Future<bool> deleteProfile(String profileId) =>
      datasource.deleteProfile(profileId);
}
