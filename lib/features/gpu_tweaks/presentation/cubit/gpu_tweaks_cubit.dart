import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import 'gpu_tweaks_state.dart';

class GpuTweaksCubit extends Cubit<GpuTweaksState> {
  final RootCommandService rootCommandService;

  GpuTweaksCubit(this.rootCommandService)
      : super(GpuTweaksState(
          tweakStates: {for (var t in TweakConstants.gpuTweaks) t.key: false},
        ));

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await rootCommandService.setSystemProperty(tweak.key, targetVal);
  }

  Future<void> setComposition(String comp) async {
    emit(state.copyWith(compositionType: comp));
    await rootCommandService.setSystemProperty('debug.composition.type', comp);
  }
}
