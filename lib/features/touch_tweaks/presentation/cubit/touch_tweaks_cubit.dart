import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/touch_tweak.dart';
import '../../domain/usecases/apply_touch_tweak.dart';
import '../../domain/usecases/get_touch_tweaks.dart';
import 'touch_tweaks_state.dart';

class TouchTweaksCubit extends Cubit<TouchTweaksState> {
  final GetTouchTweaks _getTouchTweaks;
  final ApplyTouchTweak _applyTouchTweak;
  final RootCommandService _rootCommandService;

  TouchTweaksCubit({
    required GetTouchTweaks getTouchTweaks,
    required ApplyTouchTweak applyTouchTweak,
    required RootCommandService rootCommandService,
  })  : _getTouchTweaks = getTouchTweaks,
        _applyTouchTweak = applyTouchTweak,
        _rootCommandService = rootCommandService,
        super(TouchTweaksState(
          tweakStates: {for (var t in TweakConstants.touchTweaks) t.key: false},
        ));

  Future<void> loadTweaks() async {
    final tweaks = await _getTouchTweaks();
    emit(state.copyWith(
      tweakStates: {for (var t in tweaks) t.key: state.tweakStates[t.key] ?? false},
    ));
  }

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await _applyTouchTweak(TouchTweak(
      key: tweak.key,
      value: targetVal,
      description: tweak.description,
      isEnabled: value,
    ));
  }

  Future<void> updateSamplingRate(double rate) async {
    emit(state.copyWith(samplingRate: rate));
    await _rootCommandService.setSystemProperty(
      'windowsmgr.max_events_per_sec',
      rate.toInt().toString(),
    );
  }
}

