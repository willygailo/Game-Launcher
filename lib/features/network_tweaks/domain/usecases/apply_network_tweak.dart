import '../entities/network_tweak.dart';
import '../repositories/network_tweaks_repository.dart';

class ApplyNetworkTweak {
  final NetworkTweaksRepository repository;
  const ApplyNetworkTweak(this.repository);
  Future<bool> call(NetworkTweak tweak) => repository.applyTweak(tweak);
}
