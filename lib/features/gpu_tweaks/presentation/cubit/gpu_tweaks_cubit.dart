import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/gpu_tweak.dart';
import '../../domain/usecases/apply_gpu_tweak.dart';
import '../../domain/usecases/get_gpu_tweaks.dart';
import 'gpu_tweaks_state.dart';

class GpuTweaksCubit extends Cubit<GpuTweaksState> {
  final GetGpuTweaks _getGpuTweaks;
  final ApplyGpuTweak _applyGpuTweak;
  final RootCommandService _rootCommandService;

  GpuTweaksCubit({
    required GetGpuTweaks getGpuTweaks,
    required ApplyGpuTweak applyGpuTweak,
    required RootCommandService rootCommandService,
  })  : _getGpuTweaks = getGpuTweaks,
        _applyGpuTweak = applyGpuTweak,
        _rootCommandService = rootCommandService,
        super(GpuTweaksState(
          tweakStates: {for (var t in TweakConstants.gpuTweaks) t.key: false},
        ));

  Future<void> loadTweaks() async {
    final tweaks = await _getGpuTweaks();
    emit(state.copyWith(
      tweakStates: {for (var t in tweaks) t.key: state.tweakStates[t.key] ?? false},
    ));
  }

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await _applyGpuTweak(GpuTweak(
      key: tweak.key,
      value: targetVal,
      description: tweak.description,
      isEnabled: value,
    ));
  }

  Future<void> setComposition(String comp) async {
    emit(state.copyWith(compositionType: comp));
    await _rootCommandService.setSystemProperty('debug.composition.type', comp);
  }
}

