import 'package:equatable/equatable.dart';

/// Entity representing a live performance metric snapshot.
class PerformanceMetrics extends Equatable {
  final String chipset;
  final int cpuFreqMhz;
  final int gpuFreqMhz;
  final double cpuTempCelsius;
  final double batteryPercent;
  final double cpuLoadPercent;
  final double ramUsagePercent;

  const PerformanceMetrics({
    required this.chipset,
    required this.cpuFreqMhz,
    required this.gpuFreqMhz,
    required this.cpuTempCelsius,
    required this.batteryPercent,
    this.cpuLoadPercent = 0.0,
    this.ramUsagePercent = 0.0,
  });

  @override
  List<Object?> get props => [
        chipset,
        cpuFreqMhz,
        gpuFreqMhz,
        cpuTempCelsius,
        batteryPercent,
        cpuLoadPercent,
        ramUsagePercent,
      ];
}

