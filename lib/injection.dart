import 'package:get_it/get_it.dart';

import 'core/platform/root_command_service.dart';
import 'core/platform/device_info_service.dart';
import 'core/platform/permission_service.dart';

import 'features/home/data/datasources/device_info_datasource.dart';
import 'features/home/data/repositories/device_info_repository_impl.dart';
import 'features/home/domain/repositories/device_info_repository.dart';
import 'features/home/domain/usecases/get_device_info.dart';
import 'features/home/presentation/cubit/home_cubit.dart';

import 'features/cpu_tweaks/presentation/cubit/cpu_tweaks_cubit.dart';
import 'features/gpu_tweaks/presentation/cubit/gpu_tweaks_cubit.dart';
import 'features/touch_tweaks/presentation/cubit/touch_tweaks_cubit.dart';
import 'features/network_tweaks/presentation/cubit/network_tweaks_cubit.dart';
import 'features/performance/presentation/cubit/performance_cubit.dart';
import 'features/profiles/presentation/cubit/profiles_cubit.dart';
import 'features/permissions/presentation/cubit/permissions_cubit.dart';
import 'features/settings/presentation/cubit/settings_cubit.dart';

final sl = GetIt.instance;

Future<void> initDependencies() async {
  // Services
  sl.registerLazySingleton<RootCommandService>(() => RootCommandService());
  sl.registerLazySingleton<DeviceInfoService>(() => DeviceInfoService());
  sl.registerLazySingleton<PermissionService>(() => PermissionService());

  // Home Feature Data & Domain
  sl.registerLazySingleton<DeviceInfoDataSource>(
    () => DeviceInfoDataSource(
      deviceInfoService: sl(),
      rootCommandService: sl(),
    ),
  );
  sl.registerLazySingleton<DeviceInfoRepository>(
    () => DeviceInfoRepositoryImpl(sl()),
  );
  sl.registerLazySingleton<GetDeviceInfo>(
    () => GetDeviceInfo(sl()),
  );

  // Cubits
  sl.registerFactory<HomeCubit>(
    () => HomeCubit(
      getDeviceInfo: sl(),
      rootCommandService: sl(),
    ),
  );
  sl.registerFactory<CpuTweaksCubit>(() => CpuTweaksCubit(sl()));
  sl.registerFactory<GpuTweaksCubit>(() => GpuTweaksCubit(sl()));
  sl.registerFactory<TouchTweaksCubit>(() => TouchTweaksCubit(sl()));
  sl.registerFactory<NetworkTweaksCubit>(() => NetworkTweaksCubit(sl()));
  sl.registerFactory<PerformanceCubit>(() => PerformanceCubit(sl()));
  sl.registerFactory<ProfilesCubit>(() => ProfilesCubit(sl()));
  sl.registerFactory<PermissionsCubit>(() => PermissionsCubit(sl()));
  sl.registerLazySingleton<SettingsCubit>(() => SettingsCubit());
}
