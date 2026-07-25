import 'dart:async';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/usecases/get_performance_metrics.dart';
import 'performance_state.dart';

class PerformanceCubit extends Cubit<PerformanceState> {
  final GetPerformanceMetrics _getPerformanceMetrics;
  final RootCommandService rootCommandService;
  Timer? _timer;

  PerformanceCubit({
    required GetPerformanceMetrics getPerformanceMetrics,
    required this.rootCommandService,
  })  : _getPerformanceMetrics = getPerformanceMetrics,
        super(PerformanceState(
          tweakStates: {for (var t in TweakConstants.systemTweaks) t.key: false},
        ));

  Future<void> loadMetrics() async {
    try {
      final metrics = await _getPerformanceMetrics();
      emit(state.copyWith(metrics: metrics));
    } catch (_) {}
  }

  void startPolling({Duration interval = const Duration(seconds: 2)}) {
    loadMetrics();
    _timer?.cancel();
    _timer = Timer.periodic(interval, (_) => loadMetrics());
  }

  void stopPolling() {
    _timer?.cancel();
    _timer = null;
  }

  Future<void> toggleTweak(TweakItem tweak, bool value) async {
    final updatedMap = Map<String, bool>.from(state.tweakStates);
    updatedMap[tweak.key] = value;
    emit(state.copyWith(tweakStates: updatedMap));

    final targetVal = value ? tweak.tweakValue : tweak.defaultValue;
    await rootCommandService.setSystemProperty(tweak.key, targetVal);
  }

  @override
  Future<void> close() {
    stopPolling();
    return super.close();
  }
}

