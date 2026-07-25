import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import 'cpu_tweaks_state.dart';

class CpuTweaksCubit extends Cubit<CpuTweaksState> {
  final RootCommandService rootCommandService;

  CpuTweaksCubit(this.rootCommandService)
      : super(CpuTweaksState(
          tweakStates: {for (var t in TweakConstants.cpuTweaks) t.key: false},
        ));

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await rootCommandService.setSystemProperty(tweak.key, targetVal);
  }

  Future<void> setGovernor(String gov) async {
    emit(state.copyWith(governor: gov));
    await rootCommandService.setSystemProperty('scaling_governor', gov);
  }
}
