import 'package:equatable/equatable.dart';

class DeviceInfoEntity extends Equatable {
  final String manufacturer;
  final String model;
  final String chipset;
  final int cpuCores;
  final int totalRamMb;
  final String androidVersion;
  final bool isRooted;

  const DeviceInfoEntity({
    required this.manufacturer,
    required this.model,
    required this.chipset,
    required this.cpuCores,
    required this.totalRamMb,
    required this.androidVersion,
    required this.isRooted,
  });

  @override
  List<Object?> get props => [
        manufacturer,
        model,
        chipset,
        cpuCores,
        totalRamMb,
        androidVersion,
        isRooted,
      ];
}
