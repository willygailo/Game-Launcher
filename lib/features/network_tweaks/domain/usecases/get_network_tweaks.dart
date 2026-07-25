import '../entities/network_tweak.dart';
import '../repositories/network_tweaks_repository.dart';

class GetNetworkTweaks {
  final NetworkTweaksRepository repository;
  const GetNetworkTweaks(this.repository);
  Future<List<NetworkTweak>> call() => repository.getNetworkTweaks();
}
