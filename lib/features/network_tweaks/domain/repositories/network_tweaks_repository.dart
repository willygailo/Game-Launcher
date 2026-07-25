import '../entities/network_tweak.dart';

abstract class NetworkTweaksRepository {
  Future<List<NetworkTweak>> getNetworkTweaks();
  Future<bool> applyTweak(NetworkTweak tweak);
  Future<bool> revertTweak(NetworkTweak tweak);
}
