import '../entities/game_profile.dart';
import '../repositories/profiles_repository.dart';

class GetProfiles {
  final ProfilesRepository repository;
  const GetProfiles(this.repository);
  Future<List<GameProfile>> call() => repository.getProfiles();
}
