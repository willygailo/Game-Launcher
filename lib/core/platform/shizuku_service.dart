import 'package:flutter/services.dart';

class ShizukuService {
  static const _channel = MethodChannel('com.gamespace.app/shizuku');

  Future<bool> isShizukuAvailable() async {
    try {
      final bool available = await _channel.invokeMethod('isShizukuAvailable');
      return available;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> isPermissionGranted() async {
    try {
      final bool granted = await _channel.invokeMethod('isPermissionGranted');
      return granted;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> requestPermission() async {
    try {
      final bool requested = await _channel.invokeMethod('requestPermission');
      return requested;
    } on PlatformException {
      return false;
    }
  }

  Future<Map<String, dynamic>> executeCommand(String command) async {
    try {
      final Map<dynamic, dynamic> res =
          await _channel.invokeMethod('executeCommand', {'command': command});
      return Map<String, dynamic>.from(res);
    } on PlatformException catch (e) {
      return {
        'success': false,
        'exitCode': -1,
        'stdout': '',
        'stderr': e.message ?? 'PlatformException',
      };
    }
  }

  Future<int> executeBatchCommands(List<String> commands) async {
    try {
      final int count = await _channel
          .invokeMethod('executeBatchCommands', {'commands': commands});
      return count;
    } on PlatformException {
      return 0;
    }
  }
}
