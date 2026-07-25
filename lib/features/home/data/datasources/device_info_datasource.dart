import '../../../../core/platform/device_info_service.dart';
import '../../../../core/platform/root_command_service.dart';

class DeviceInfoDataSource {
  final DeviceInfoService deviceInfoService;
  final RootCommandService rootCommandService;

  DeviceInfoDataSource({
    required this.deviceInfoService,
    required this.rootCommandService,
  });

  Future<Map<String, dynamic>> fetchRawDeviceInfo() async {
    final info = await deviceInfoService.getDeviceInfo();
    final isRooted = await rootCommandService.isRootAvailable();
    info['isRooted'] = isRooted;
    return info;
  }
}
