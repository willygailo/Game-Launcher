import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/platform/hz_fps_service.dart';
import 'hz_fps_state.dart';

class HzFpsCubit extends Cubit<HzFpsState> {
  final HzFpsService hzFpsService;

  HzFpsCubit(this.hzFpsService) : super(HzFpsInitial());

  Future<void> loadDisplayModes() async {
    emit(HzFpsLoading());
    try {
      final info = await hzFpsService.getDisplayModes();
      final currentHz = (info['currentHz'] as num?)?.toDouble() ?? 60.0;
      final rawRates = (info['supportedRates'] as List?)?.cast<num>() ?? [60.0, 90.0, 120.0];
      final rates = rawRates.map((e) => e.toDouble()).toList();

      if (!rates.contains(60.0)) rates.add(60.0);
      if (!rates.contains(90.0)) rates.add(90.0);
      if (!rates.contains(120.0)) rates.add(120.0);
      rates.sort();

      emit(HzFpsLoaded(
        currentHz: currentHz,
        supportedRates: rates,
        activeHz: currentHz,
      ));
    } catch (e) {
      emit(HzFpsError(e.toString()));
    }
  }

  Future<void> setRefreshRate(double targetHz, {String mode = 'auto'}) async {
    final currentState = state;
    if (currentState is! HzFpsLoaded) return;

    emit(currentState.copyWith(isApplying: true));
    try {
      final success = await hzFpsService.setTargetRefreshRate(targetHz, mode: mode);
      final newDisplayInfo = await hzFpsService.getDisplayModes();
      final currentHz = (newDisplayInfo['currentHz'] as num?)?.toDouble() ?? targetHz;

      emit(currentState.copyWith(
        isApplying: false,
        activeHz: targetHz,
        currentHz: currentHz,
        message: success ? 'Display locked to ${targetHz.toInt()}Hz' : 'Failed to set refresh rate',
      ));
    } catch (e) {
      emit(currentState.copyWith(
        isApplying: false,
        message: 'Error: ${e.toString()}',
      ));
    }
  }

  Future<void> toggleThermalBypass({String mode = 'auto'}) async {
    final currentState = state;
    if (currentState is! HzFpsLoaded) return;

    final newState = !currentState.isThermalBypassActive;
    emit(currentState.copyWith(isApplying: true));

    try {
      final success = await hzFpsService.setThermalOverride(mode: mode);
      emit(currentState.copyWith(
        isApplying: false,
        isThermalBypassActive: success ? newState : currentState.isThermalBypassActive,
        message: success
            ? (newState ? 'Thermal Throttling Bypassed' : 'Thermal Throttling Restored')
            : 'Failed to toggle thermal status',
      ));
    } catch (e) {
      emit(currentState.copyWith(
        isApplying: false,
        message: 'Error toggling thermal status',
      ));
    }
  }
}
