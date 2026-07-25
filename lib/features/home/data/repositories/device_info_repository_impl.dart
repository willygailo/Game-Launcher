import '../../domain/entities/device_info.dart';
import '../../domain/repositories/device_info_repository.dart';
import '../datasources/device_info_datasource.dart';

class DeviceInfoRepositoryImpl implements DeviceInfoRepository {
  final DeviceInfoDataSource dataSource;

  DeviceInfoRepositoryImpl(this.dataSource);

  @override
  Future<DeviceInfoEntity> getDeviceInfo() async {
    final rawMap = await dataSource.fetchRawDeviceInfo();
    return DeviceInfoEntity(
      manufacturer: rawMap['manufacturer'] as String? ?? 'Android',
      model: rawMap['model'] as String? ?? 'Device',
      chipset: rawMap['chipset'] as String? ?? 'ARM Chipset',
      cpuCores: (rawMap['cpuCores'] as num?)?.toInt() ?? 8,
      totalRamMb: (rawMap['totalRamMb'] as num?)?.toInt() ?? 4096,
      androidVersion: rawMap['androidVersion'] as String? ?? '13',
      isRooted: rawMap['isRooted'] as bool? ?? false,
    );
  }
}
