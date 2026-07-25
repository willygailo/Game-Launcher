import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import 'touch_tweaks_state.dart';

class TouchTweaksCubit extends Cubit<TouchTweaksState> {
  final RootCommandService rootCommandService;

  TouchTweaksCubit(this.rootCommandService)
      : super(TouchTweaksState(
          tweakStates: {for (var t in TweakConstants.touchTweaks) t.key: false},
        ));

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await rootCommandService.setSystemProperty(tweak.key, targetVal);
  }

  Future<void> updateSamplingRate(double rate) async {
    emit(state.copyWith(samplingRate: rate));
    await rootCommandService.setSystemProperty('windowsmgr.max_events_per_sec', rate.toInt().toString());
  }
}
