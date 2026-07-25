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
    final allProfiles = datasource.getProfiles();
    for (final p in allProfiles) {
      if (p.isActive && p.id != profile.id) {
        await datasource.saveProfile(p.copyWith(isActive: false));
      }
    }

    final count = await rootCommandService.executeBatchTweaks(profile.tweaks);
    await datasource.setActiveBootTweaks(profile.tweaks);

    final updated = profile.copyWith(isActive: true);
    await datasource.saveProfile(updated);
    return count > 0 || profile.tweaks.isEmpty;
  }


  @override
  Future<bool> saveProfile(GameProfile profile) =>
      datasource.saveProfile(profile);

  @override
  Future<bool> deleteProfile(String profileId) =>
      datasource.deleteProfile(profileId);
}
