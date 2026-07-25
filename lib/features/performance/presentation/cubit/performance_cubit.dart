import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import 'performance_state.dart';

class PerformanceCubit extends Cubit<PerformanceState> {
  final RootCommandService rootCommandService;

  PerformanceCubit(this.rootCommandService)
      : super(PerformanceState(
          tweakStates: {for (var t in TweakConstants.systemTweaks) t.key: false},
        ));

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await rootCommandService.setSystemProperty(tweak.key, targetVal);
  }
}
