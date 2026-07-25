import '../../../../core/platform/device_info_service.dart';
import '../../../../core/platform/performance_service.dart';
import '../../domain/entities/performance_metrics.dart';

/// Datasource that reads live device performance metrics via native services.
class PerformanceDatasource {
  final DeviceInfoService deviceInfoService;
  final PerformanceService performanceService;

  PerformanceDatasource({
    required this.deviceInfoService,
    required this.performanceService,
  });

  Future<PerformanceMetrics> getPerformanceMetrics() async {
    final info = await deviceInfoService.getDeviceInfo();
    final perf = await performanceService.getPerformanceMetrics();

    return PerformanceMetrics(
      chipset: info['chipset'] as String? ?? 'Unknown',
      cpuFreqMhz: (perf['cpuFreqMhz'] as num?)?.toInt() ?? 0,
      gpuFreqMhz: (perf['gpuFreqMhz'] as num?)?.toInt() ?? 0,
      cpuTempCelsius: (perf['cpuTempCelsius'] as num?)?.toDouble() ?? 0.0,
      batteryPercent: (perf['batteryPercent'] as num?)?.toDouble() ?? 0.0,
      cpuLoadPercent: (perf['cpuLoadPercent'] as num?)?.toDouble() ?? 0.0,
      ramUsagePercent: (perf['ramUsagePercent'] as num?)?.toDouble() ?? 0.0,
    );
  }
}


