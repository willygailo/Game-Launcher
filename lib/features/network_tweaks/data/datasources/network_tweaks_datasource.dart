import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/network_tweak.dart';

class NetworkTweaksDatasource {
  final RootCommandService rootCommandService;
  NetworkTweaksDatasource({required this.rootCommandService});

  List<NetworkTweak> getNetworkTweaks() {
    return TweakConstants.networkTweaks
        .where((t) => !t.isReadOnly)
        .map(
          (t) => NetworkTweak(
            key: t.key,
            value: t.tweakValue,
            description: t.description,
          ),
        )
        .toList();
  }

  Future<bool> applyTweak(NetworkTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, tweak.value);

  Future<bool> revertTweak(NetworkTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, '');
}
