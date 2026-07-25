import '../entities/device_info.dart';
import '../repositories/device_info_repository.dart';

class GetDeviceInfo {
  final DeviceInfoRepository repository;

  GetDeviceInfo(this.repository);

  Future<DeviceInfoEntity> call() async {
    return await repository.getDeviceInfo();
  }
}
