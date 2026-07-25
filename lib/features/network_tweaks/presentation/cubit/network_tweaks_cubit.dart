import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import 'network_tweaks_state.dart';

class NetworkTweaksCubit extends Cubit<NetworkTweaksState> {
  final RootCommandService rootCommandService;

  NetworkTweaksCubit(this.rootCommandService)
      : super(NetworkTweaksState(
          tweakStates: {for (var t in TweakConstants.networkTweaks) t.key: false},
        ));

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await rootCommandService.setSystemProperty(tweak.key, targetVal);
  }

  Future<void> setDns(String dnsName, String primary, String secondary) async {
    emit(state.copyWith(activeDns: dnsName));
    await rootCommandService.setSystemProperty('net.dns1', primary);
    await rootCommandService.setSystemProperty('net.dns2', secondary);
  }
}
