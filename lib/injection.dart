import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'core/platform/root_command_service.dart';
import 'core/platform/device_info_service.dart';
import 'core/platform/permission_service.dart';
import 'core/platform/performance_service.dart';
import 'core/platform/magisk_exporter_service.dart';
import 'core/platform/game_library_service.dart';
import 'core/platform/shizuku_service.dart';
import 'core/platform/hz_fps_service.dart';

// Home
import 'features/home/data/datasources/device_info_datasource.dart';
import 'features/home/data/repositories/device_info_repository_impl.dart';
import 'features/home/domain/repositories/device_info_repository.dart';
import 'features/home/domain/usecases/get_device_info.dart';
import 'features/home/presentation/cubit/home_cubit.dart';

// CPU Tweaks
import 'features/cpu_tweaks/data/datasources/cpu_tweaks_datasource.dart';
import 'features/cpu_tweaks/data/repositories/cpu_tweaks_repository_impl.dart';
import 'features/cpu_tweaks/domain/repositories/cpu_tweaks_repository.dart';
import 'features/cpu_tweaks/domain/usecases/get_cpu_tweaks.dart';
import 'features/cpu_tweaks/domain/usecases/apply_cpu_tweak.dart';
import 'features/cpu_tweaks/presentation/cubit/cpu_tweaks_cubit.dart';

// GPU Tweaks
import 'features/gpu_tweaks/data/datasources/gpu_tweaks_datasource.dart';
import 'features/gpu_tweaks/data/repositories/gpu_tweaks_repository_impl.dart';
import 'features/gpu_tweaks/domain/repositories/gpu_tweaks_repository.dart';
import 'features/gpu_tweaks/domain/usecases/get_gpu_tweaks.dart';
import 'features/gpu_tweaks/domain/usecases/apply_gpu_tweak.dart';
import 'features/gpu_tweaks/presentation/cubit/gpu_tweaks_cubit.dart';

// Touch Tweaks
import 'features/touch_tweaks/data/datasources/touch_tweaks_datasource.dart';
import 'features/touch_tweaks/data/repositories/touch_tweaks_repository_impl.dart';
import 'features/touch_tweaks/domain/repositories/touch_tweaks_repository.dart';
import 'features/touch_tweaks/domain/usecases/get_touch_tweaks.dart';
import 'features/touch_tweaks/domain/usecases/apply_touch_tweak.dart';
import 'features/touch_tweaks/presentation/cubit/touch_tweaks_cubit.dart';

// Network Tweaks
import 'features/network_tweaks/data/datasources/network_tweaks_datasource.dart';
import 'features/network_tweaks/data/repositories/network_tweaks_repository_impl.dart';
import 'features/network_tweaks/domain/repositories/network_tweaks_repository.dart';
import 'features/network_tweaks/domain/usecases/get_network_tweaks.dart';
import 'features/network_tweaks/domain/usecases/apply_network_tweak.dart';
import 'features/network_tweaks/presentation/cubit/network_tweaks_cubit.dart';

// Performance
import 'features/performance/data/datasources/performance_datasource.dart';
import 'features/performance/data/repositories/performance_repository_impl.dart';
import 'features/performance/domain/repositories/performance_repository.dart';
import 'features/performance/domain/usecases/get_performance_metrics.dart';
import 'features/performance/presentation/cubit/performance_cubit.dart';

// Profiles
import 'features/profiles/data/datasources/profiles_datasource.dart';
import 'features/profiles/data/repositories/profiles_repository_impl.dart';
import 'features/profiles/domain/repositories/profiles_repository.dart';
import 'features/profiles/domain/usecases/get_profiles.dart';
import 'features/profiles/domain/usecases/activate_profile.dart';
import 'features/profiles/presentation/cubit/profiles_cubit.dart';

// Permissions
import 'features/permissions/data/datasources/permissions_datasource.dart';
import 'features/permissions/data/repositories/permissions_repository_impl.dart';
import 'features/permissions/domain/repositories/permissions_repository.dart';
import 'features/permissions/domain/usecases/check_permissions.dart';
import 'features/permissions/domain/usecases/request_storage_permission.dart';
import 'features/permissions/presentation/cubit/permissions_cubit.dart';

// Hz & FPS Tweaks
import 'features/hz_fps_tweaks/presentation/cubit/hz_fps_cubit.dart';

// Settings
import 'features/settings/data/datasources/settings_datasource.dart';
import 'features/settings/data/repositories/settings_repository_impl.dart';
import 'features/settings/domain/repositories/settings_repository.dart';
import 'features/settings/domain/usecases/get_settings.dart';
import 'features/settings/domain/usecases/save_settings.dart';
import 'features/settings/presentation/cubit/settings_cubit.dart';

final sl = GetIt.instance;

Future<void> initDependencies() async {
  // ─── External ─────────────────────────────────────────────────────────────
  final prefs = await SharedPreferences.getInstance();
  sl.registerLazySingleton<SharedPreferences>(() => prefs);

  // ─── Core Services ────────────────────────────────────────────────────────
  sl.registerLazySingleton<RootCommandService>(() => RootCommandService());
  sl.registerLazySingleton<DeviceInfoService>(() => DeviceInfoService());
  sl.registerLazySingleton<PermissionService>(() => PermissionService());
  sl.registerLazySingleton<PerformanceService>(() => PerformanceService());
  sl.registerLazySingleton<MagiskExporterService>(() => MagiskExporterService());
  sl.registerLazySingleton<GameLibraryService>(() => GameLibraryService());
  sl.registerLazySingleton<ShizukuService>(() => ShizukuService());
  sl.registerLazySingleton<HzFpsService>(() => HzFpsService());

  // ─── Home Feature ─────────────────────────────────────────────────────────
  sl.registerLazySingleton<DeviceInfoDataSource>(
    () => DeviceInfoDataSource(
      deviceInfoService: sl(),
      rootCommandService: sl(),
    ),
  );
  sl.registerLazySingleton<DeviceInfoRepository>(
    () => DeviceInfoRepositoryImpl(sl()),
  );
  sl.registerLazySingleton<GetDeviceInfo>(() => GetDeviceInfo(sl()));

  sl.registerFactory<HomeCubit>(
    () => HomeCubit(getDeviceInfo: sl(), rootCommandService: sl()),
  );

  // ─── CPU Tweaks ───────────────────────────────────────────────────────────
  sl.registerLazySingleton<CpuTweaksDatasource>(
    () => CpuTweaksDatasource(rootCommandService: sl()),
  );
  sl.registerLazySingleton<CpuTweaksRepository>(
    () => CpuTweaksRepositoryImpl(datasource: sl()),
  );
  sl.registerLazySingleton<GetCpuTweaks>(() => GetCpuTweaks(sl()));
  sl.registerLazySingleton<ApplyCpuTweak>(() => ApplyCpuTweak(sl()));

  sl.registerFactory<CpuTweaksCubit>(
    () => CpuTweaksCubit(
      getCpuTweaks: sl(),
      applyCpuTweak: sl(),
      rootCommandService: sl(),
    ),
  );

  // ─── GPU Tweaks ───────────────────────────────────────────────────────────
  sl.registerLazySingleton<GpuTweaksDatasource>(
    () => GpuTweaksDatasource(rootCommandService: sl()),
  );
  sl.registerLazySingleton<GpuTweaksRepository>(
    () => GpuTweaksRepositoryImpl(datasource: sl()),
  );
  sl.registerLazySingleton<GetGpuTweaks>(() => GetGpuTweaks(sl()));
  sl.registerLazySingleton<ApplyGpuTweak>(() => ApplyGpuTweak(sl()));

  sl.registerFactory<GpuTweaksCubit>(
    () => GpuTweaksCubit(
      getGpuTweaks: sl(),
      applyGpuTweak: sl(),
      rootCommandService: sl(),
    ),
  );

  // ─── Touch Tweaks ─────────────────────────────────────────────────────────
  sl.registerLazySingleton<TouchTweaksDatasource>(
    () => TouchTweaksDatasource(rootCommandService: sl()),
  );
  sl.registerLazySingleton<TouchTweaksRepository>(
    () => TouchTweaksRepositoryImpl(datasource: sl()),
  );
  sl.registerLazySingleton<GetTouchTweaks>(() => GetTouchTweaks(sl()));
  sl.registerLazySingleton<ApplyTouchTweak>(() => ApplyTouchTweak(sl()));

  sl.registerFactory<TouchTweaksCubit>(
    () => TouchTweaksCubit(
      getTouchTweaks: sl(),
      applyTouchTweak: sl(),
      rootCommandService: sl(),
    ),
  );

  // ─── Network Tweaks ───────────────────────────────────────────────────────
  sl.registerLazySingleton<NetworkTweaksDatasource>(
    () => NetworkTweaksDatasource(rootCommandService: sl()),
  );
  sl.registerLazySingleton<NetworkTweaksRepository>(
    () => NetworkTweaksRepositoryImpl(datasource: sl()),
  );
  sl.registerLazySingleton<GetNetworkTweaks>(() => GetNetworkTweaks(sl()));
  sl.registerLazySingleton<ApplyNetworkTweak>(() => ApplyNetworkTweak(sl()));

  sl.registerFactory<NetworkTweaksCubit>(
    () => NetworkTweaksCubit(
      getNetworkTweaks: sl(),
      applyNetworkTweak: sl(),
      rootCommandService: sl(),
    ),
  );

  // ─── Performance ──────────────────────────────────────────────────────────
  sl.registerLazySingleton<PerformanceDatasource>(
    () => PerformanceDatasource(
      deviceInfoService: sl(),
      performanceService: sl(),
    ),
  );
  sl.registerLazySingleton<PerformanceRepository>(
    () => PerformanceRepositoryImpl(datasource: sl()),
  );
  sl.registerLazySingleton<GetPerformanceMetrics>(
    () => GetPerformanceMetrics(sl()),
  );

  sl.registerFactory<PerformanceCubit>(
    () => PerformanceCubit(
      getPerformanceMetrics: sl(),
      rootCommandService: sl(),
    ),
  );

  // ─── Profiles ─────────────────────────────────────────────────────────────
  sl.registerLazySingleton<ProfilesDatasource>(
    () => ProfilesDatasource(prefs: sl()),
  );
  sl.registerLazySingleton<ProfilesRepositoryImpl>(
    () => ProfilesRepositoryImpl(datasource: sl(), rootCommandService: sl()),
  );
  sl.registerLazySingleton<ProfilesRepository>(() => sl<ProfilesRepositoryImpl>());
  sl.registerLazySingleton<GetProfiles>(() => GetProfiles(sl()));
  sl.registerLazySingleton<ActivateProfile>(() => ActivateProfile(sl()));

  sl.registerFactory<ProfilesCubit>(
    () => ProfilesCubit(
      getProfiles: sl(),
      activateProfile: sl(),
      repo: sl<ProfilesRepositoryImpl>(),
    ),
  );

  // ─── Permissions ──────────────────────────────────────────────────────────
  sl.registerLazySingleton<PermissionsDatasource>(
    () => PermissionsDatasource(
      permissionService: sl(),
      rootCommandService: sl(),
      shizukuService: sl(),
    ),
  );
  sl.registerLazySingleton<PermissionsRepository>(
    () => PermissionsRepositoryImpl(datasource: sl()),
  );
  sl.registerLazySingleton<CheckPermissions>(() => CheckPermissions(sl()));
  sl.registerLazySingleton<RequestStoragePermission>(
    () => RequestStoragePermission(sl()),
  );

  sl.registerFactory<PermissionsCubit>(
    () => PermissionsCubit(
      permissionService: sl(),
      shizukuService: sl(),
    ),
  );

  // ─── Hz & FPS Tweaks ──────────────────────────────────────────────────────
  sl.registerFactory<HzFpsCubit>(() => HzFpsCubit(sl()));

  // ─── Settings ─────────────────────────────────────────────────────────────
  sl.registerLazySingleton<SettingsDatasource>(
    () => SettingsDatasource(prefs: sl()),
  );
  sl.registerLazySingleton<SettingsRepository>(
    () => SettingsRepositoryImpl(datasource: sl()),
  );
  sl.registerLazySingleton<GetSettings>(() => GetSettings(sl()));
  sl.registerLazySingleton<SaveSettings>(() => SaveSettings(sl()));

  sl.registerLazySingleton<SettingsCubit>(
    () => SettingsCubit(getSettings: sl(), saveSettings: sl()),
  );
}
