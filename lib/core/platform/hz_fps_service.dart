import 'package:flutter/services.dart';

class HzFpsService {
  static const _channel = MethodChannel('com.gamespace.app/hz_fps');

  Future<Map<String, dynamic>> getDisplayModes() async {
    try {
      final Map<dynamic, dynamic> info =
          await _channel.invokeMethod('getDisplayModes');
      return Map<String, dynamic>.from(info);
    } on PlatformException {
      return {
        'currentHz': 60.0,
        'supportedRates': [60.0, 90.0, 120.0],
      };
    }
  }

  Future<bool> setTargetRefreshRate(double hz, {String mode = 'auto'}) async {
    try {
      final bool res = await _channel.invokeMethod('setTargetRefreshRate', {
        'hz': hz,
        'mode': mode,
      });
      return res;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> setGameModeFps(String packageName, int targetFps,
      {String mode = 'auto'}) async {
    try {
      final bool res = await _channel.invokeMethod('setGameModeFps', {
        'packageName': packageName,
        'fps': targetFps,
        'mode': mode,
      });
      return res;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> setThermalOverride({String mode = 'auto'}) async {
    try {
      final bool res = await _channel.invokeMethod('setThermalOverride', {
        'mode': mode,
      });
      return res;
    } on PlatformException {
      return false;
    }
  }
}
