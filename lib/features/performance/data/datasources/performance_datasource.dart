import '../../../../core/platform/device_info_service.dart';
import '../../domain/entities/performance_metrics.dart';

/// Datasource that reads live device performance metrics via native services.
/// Note: CPU/GPU MHz and temperature are not yet exposed by [DeviceInfoService];
/// they are set to 0 as defaults until a dedicated PerformanceChannel is added.
class PerformanceDatasource {
  final DeviceInfoService deviceInfoService;

  PerformanceDatasource({required this.deviceInfoService});

  Future<PerformanceMetrics> getPerformanceMetrics() async {
    final info = await deviceInfoService.getDeviceInfo();
    return PerformanceMetrics(
      chipset: info['chipset'] as String? ?? 'Unknown',
      cpuFreqMhz: 0, // TODO: expose via native PerformanceChannel
      gpuFreqMhz: 0, // TODO: expose via native PerformanceChannel
      cpuTempCelsius: 0.0, // TODO: read /sys/class/thermal
      batteryPercent: 0.0, // TODO: read BatteryManager
    );
  }
}

