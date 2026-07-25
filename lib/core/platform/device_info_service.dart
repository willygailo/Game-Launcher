import 'package:flutter/services.dart';

class DeviceInfoService {
  static const _channel = MethodChannel('com.gamespace.app/device_info');

  Future<Map<String, dynamic>> getDeviceInfo() async {
    try {
      final Map<dynamic, dynamic> rawInfo = await _channel.invokeMethod('getDeviceInfo');
      return Map<String, dynamic>.from(rawInfo);
    } on PlatformException {
      return {
        'manufacturer': 'Unknown',
        'model': 'Android Device',
        'chipset': 'Generic ARM Chipset',
        'cpuCores': 8,
        'totalRamMb': 4096,
        'androidVersion': '13',
      };
    }
  }
}
