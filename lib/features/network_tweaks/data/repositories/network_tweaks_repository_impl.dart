import '../../domain/entities/network_tweak.dart';
import '../../domain/repositories/network_tweaks_repository.dart';
import '../datasources/network_tweaks_datasource.dart';

class NetworkTweaksRepositoryImpl implements NetworkTweaksRepository {
  final NetworkTweaksDatasource datasource;
  NetworkTweaksRepositoryImpl({required this.datasource});

  @override
  Future<List<NetworkTweak>> getNetworkTweaks() async =>
      datasource.getNetworkTweaks();

  @override
  Future<bool> applyTweak(NetworkTweak tweak) => datasource.applyTweak(tweak);

  @override
  Future<bool> revertTweak(NetworkTweak tweak) =>
      datasource.revertTweak(tweak);
}
