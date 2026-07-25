import 'package:flutter/services.dart';

class MagiskExporterService {
  static const _channel = MethodChannel('com.gamespace.app/magisk_exporter');

  Future<String?> exportMagiskModule({
    required String moduleName,
    required Map<String, String> tweaks,
  }) async {
    try {
      final String? path = await _channel.invokeMethod('exportMagiskModule', {
        'moduleName': moduleName,
        'tweaks': tweaks,
      });
      return path;
    } on PlatformException {
      return null;
    }
  }
}
