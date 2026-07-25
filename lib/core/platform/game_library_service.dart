import 'package:flutter/services.dart';

class GameLibraryService {
  static const _channel = MethodChannel('com.gamespace.app/game_library');

  Future<List<Map<String, dynamic>>> getInstalledGames() async {
    try {
      final List<dynamic> raw = await _channel.invokeMethod('getInstalledGames');
      return raw.map((item) => Map<String, dynamic>.from(item)).toList();
    } on PlatformException {
      return [];
    }
  }
}
