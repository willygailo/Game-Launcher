import 'package:flutter/services.dart';

class PermissionService {
  static const _channel = MethodChannel('com.gamespace.app/permissions');

  Future<bool> checkRootPermission() async {
    try {
      final bool hasRoot = await _channel.invokeMethod('checkRootPermission');
      return hasRoot;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> requestRootPermission() async {
    try {
      final bool granted = await _channel.invokeMethod('requestRootPermission');
      return granted;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> hasStoragePermission() async {
    try {
      final bool has = await _channel.invokeMethod('hasStoragePermission');
      return has;
    } on PlatformException {
      return false;
    }
  }

  Future<bool> requestStoragePermission() async {
    try {
      final bool granted =
          await _channel.invokeMethod('requestStoragePermission');
      return granted;
    } on PlatformException {
      return false;
    }
  }
}
