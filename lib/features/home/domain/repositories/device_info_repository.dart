import '../entities/device_info.dart';

abstract class DeviceInfoRepository {
  Future<DeviceInfoEntity> getDeviceInfo();
}
