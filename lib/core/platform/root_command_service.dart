import 'package:flutter/services.dart';

class RootCommandService {
  static const _channel = MethodChannel('com.gamespace.app/root_command');

  Future<bool> isRootAvailable() async {
    try {
      final bool result = await _channel.invokeMethod('isRootAvailable');
      return result;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> setSystemProperty(String key, String value) async {
    try {
      final bool result = await _channel.invokeMethod('setSystemProperty', {
        'key': key,
        'value': value,
      });
      return result;
    } on PlatformException {
      return false;
    }
  }

  Future<String> getSystemProperty(String key) async {
    try {
      final String result = await _channel.invokeMethod('getSystemProperty', {'key': key});
      return result;
    } on PlatformException {
      return '';
    }
  }

  Future<int> executeBatchTweaks(Map<String, String> tweaks) async {
    try {
      final int appliedCount = await _channel.invokeMethod('executeBatchTweaks', {'tweaks': tweaks});
      return appliedCount;
    } on PlatformException {
      return 0;
    }
  }
}
