import '../entities/game_profile.dart';

abstract class ProfilesRepository {
  Future<List<GameProfile>> getProfiles();
  Future<bool> activateProfile(GameProfile profile);
  Future<bool> saveProfile(GameProfile profile);
  Future<bool> deleteProfile(String profileId);
}
