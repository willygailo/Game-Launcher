import 'package:flutter/services.dart';

class PerformanceService {
  static const _channel = MethodChannel('com.gamespace.app/performance');

  Future<Map<String, dynamic>> getPerformanceMetrics() async {
    try {
      final Map<dynamic, dynamic> raw =
          await _channel.invokeMethod('getPerformanceMetrics');
      return Map<String, dynamic>.from(raw);
    } on PlatformException {
      return {
        'cpuFreqMhz': 2400,
        'gpuFreqMhz': 650,
        'cpuTempCelsius': 38.5,
        'batteryPercent': 85.0,
        'cpuLoadPercent': 38.0,
        'ramUsagePercent': 62.0,
      };
    }
  }
}
