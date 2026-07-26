import '../../../../core/platform/hz_fps_service.dart';

class HzFpsDatasource {
  final HzFpsService hzFpsService;

  HzFpsDatasource({required this.hzFpsService});

  Future<Map<String, dynamic>> getDisplayModes() =>
      hzFpsService.getDisplayModes();

  Future<bool> setTargetRefreshRate(double hz, {String mode = 'auto'}) =>
      hzFpsService.setTargetRefreshRate(hz, mode: mode);

  Future<bool> setGameModeFps(String packageName, int targetFps,
          {String mode = 'auto'}) =>
      hzFpsService.setGameModeFps(packageName, targetFps, mode: mode);

  Future<bool> setThermalOverride({String mode = 'auto'}) =>
      hzFpsService.setThermalOverride(mode: mode);
}
