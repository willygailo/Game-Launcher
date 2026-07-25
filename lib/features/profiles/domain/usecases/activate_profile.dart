import '../entities/game_profile.dart';
import '../repositories/profiles_repository.dart';

class ActivateProfile {
  final ProfilesRepository repository;
  const ActivateProfile(this.repository);
  Future<bool> call(GameProfile profile) => repository.activateProfile(profile);
}
