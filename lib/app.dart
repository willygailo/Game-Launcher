import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import 'core/theme/app_theme.dart';
import 'core/router/app_router.dart';
import 'injection.dart';

import 'features/home/presentation/cubit/home_cubit.dart';
import 'features/cpu_tweaks/presentation/cubit/cpu_tweaks_cubit.dart';
import 'features/gpu_tweaks/presentation/cubit/gpu_tweaks_cubit.dart';
import 'features/touch_tweaks/presentation/cubit/touch_tweaks_cubit.dart';
import 'features/network_tweaks/presentation/cubit/network_tweaks_cubit.dart';
import 'features/performance/presentation/cubit/performance_cubit.dart';
import 'features/profiles/presentation/cubit/profiles_cubit.dart';
import 'features/permissions/presentation/cubit/permissions_cubit.dart';
import 'features/settings/presentation/cubit/settings_cubit.dart';
import 'features/settings/presentation/cubit/settings_state.dart';

class GameSpaceApp extends StatelessWidget {
  const GameSpaceApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider<SettingsCubit>(
          create: (_) => sl<SettingsCubit>()..init(),
        ),
        BlocProvider<HomeCubit>(create: (_) => sl<HomeCubit>()),
        BlocProvider<CpuTweaksCubit>(create: (_) => sl<CpuTweaksCubit>()),
        BlocProvider<GpuTweaksCubit>(create: (_) => sl<GpuTweaksCubit>()),
        BlocProvider<TouchTweaksCubit>(create: (_) => sl<TouchTweaksCubit>()),
        BlocProvider<NetworkTweaksCubit>(create: (_) => sl<NetworkTweaksCubit>()),
        BlocProvider<PerformanceCubit>(create: (_) => sl<PerformanceCubit>()),
        BlocProvider<ProfilesCubit>(create: (_) => sl<ProfilesCubit>()),
        BlocProvider<PermissionsCubit>(create: (_) => sl<PermissionsCubit>()),
      ],
      child: BlocBuilder<SettingsCubit, SettingsState>(
        builder: (context, settingsState) {
          return MaterialApp.router(
            title: 'GAME SPACE',
            debugShowCheckedModeBanner: false,
            theme: AppTheme.lightTheme,
            darkTheme: AppTheme.darkTheme,
            themeMode: settingsState.themeMode,
            locale: settingsState.locale,
            localizationsDelegates: const [
              AppLocalizations.delegate,
              GlobalMaterialLocalizations.delegate,
              GlobalWidgetsLocalizations.delegate,
              GlobalCupertinoLocalizations.delegate,
            ],
            supportedLocales: const [
              Locale('en'),
              Locale('fr'),
              Locale('ar'),
              Locale('es'),
              Locale('id'),
              Locale('sw'),
            ],
            routerConfig: AppRouter.router,
          );
        },
      ),
    );
  }
}
