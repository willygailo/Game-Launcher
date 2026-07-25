import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/network_tweak.dart';
import '../../domain/usecases/apply_network_tweak.dart';
import '../../domain/usecases/get_network_tweaks.dart';
import 'network_tweaks_state.dart';

class NetworkTweaksCubit extends Cubit<NetworkTweaksState> {
  final GetNetworkTweaks _getNetworkTweaks;
  final ApplyNetworkTweak _applyNetworkTweak;
  final RootCommandService _rootCommandService;

  NetworkTweaksCubit({
    required GetNetworkTweaks getNetworkTweaks,
    required ApplyNetworkTweak applyNetworkTweak,
    required RootCommandService rootCommandService,
  })  : _getNetworkTweaks = getNetworkTweaks,
        _applyNetworkTweak = applyNetworkTweak,
        _rootCommandService = rootCommandService,
        super(NetworkTweaksState(
          tweakStates: {for (var t in TweakConstants.networkTweaks) t.key: false},
        ));

  Future<void> loadTweaks() async {
    final tweaks = await _getNetworkTweaks();
    emit(state.copyWith(
      tweakStates: {for (var t in tweaks) t.key: state.tweakStates[t.key] ?? false},
    ));
  }

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await _applyNetworkTweak(NetworkTweak(
      key: tweak.key,
      value: targetVal,
      description: tweak.description,
      isEnabled: value,
    ));
  }

  Future<void> setDns(String dnsName, String primary, String secondary) async {
    emit(state.copyWith(activeDns: dnsName));
    await _rootCommandService.setSystemProperty('net.dns1', primary);
    await _rootCommandService.setSystemProperty('net.dns2', secondary);
  }
}

