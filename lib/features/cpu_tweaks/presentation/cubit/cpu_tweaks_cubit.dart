import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/cpu_tweak.dart';
import '../../domain/usecases/apply_cpu_tweak.dart';
import '../../domain/usecases/get_cpu_tweaks.dart';
import 'cpu_tweaks_state.dart';

class CpuTweaksCubit extends Cubit<CpuTweaksState> {
  final GetCpuTweaks _getCpuTweaks;
  final ApplyCpuTweak _applyCpuTweak;
  final RootCommandService _rootCommandService; // kept for governor sysfs write

  CpuTweaksCubit({
    required GetCpuTweaks getCpuTweaks,
    required ApplyCpuTweak applyCpuTweak,
    required RootCommandService rootCommandService,
  })  : _getCpuTweaks = getCpuTweaks,
        _applyCpuTweak = applyCpuTweak,
        _rootCommandService = rootCommandService,
        super(CpuTweaksState(
          tweakStates: {for (var t in TweakConstants.cpuTweaks) t.key: false},
        ));

  /// Loads domain-layer tweak list and syncs state keys.
  Future<void> loadTweaks() async {
    final List<CpuTweak> tweaks = await _getCpuTweaks();
    emit(state.copyWith(
      tweakStates: {for (var t in tweaks) t.key: state.tweakStates[t.key] ?? false},
    ));
  }

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await _applyCpuTweak(CpuTweak(
      key: tweak.key,
      value: targetVal,
      description: tweak.description,
      isEnabled: value,
    ));
  }

  Future<void> setGovernor(String gov) async {
    emit(state.copyWith(governor: gov));
    await _rootCommandService.setSystemProperty('scaling_governor', gov);
  }
}

