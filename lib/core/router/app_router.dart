import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../features/home/presentation/pages/home_page.dart';
import '../../features/cpu_tweaks/presentation/pages/cpu_tweaks_page.dart';
import '../../features/gpu_tweaks/presentation/pages/gpu_tweaks_page.dart';
import '../../features/touch_tweaks/presentation/pages/touch_tweaks_page.dart';
import '../../features/network_tweaks/presentation/pages/network_tweaks_page.dart';
import '../../features/performance/presentation/pages/performance_page.dart';
import '../../features/profiles/presentation/pages/profiles_page.dart';
import '../../features/permissions/presentation/pages/permissions_page.dart';
import '../../features/settings/presentation/pages/settings_page.dart';

class AppRouter {
  static const String home = '/';
  static const String cpuTweaks = '/cpu';
  static const String gpuTweaks = '/gpu';
  static const String touchTweaks = '/touch';
  static const String networkTweaks = '/network';
  static const String performance = '/performance';
  static const String profiles = '/profiles';
  static const String permissions = '/permissions';
  static const String settings = '/settings';

  static final GoRouter router = GoRouter(
    initialLocation: home,
    routes: [
      GoRoute(
        path: home,
        builder: (context, state) => const HomePage(),
      ),
      GoRoute(
        path: cpuTweaks,
        builder: (context, state) => const CpuTweaksPage(),
      ),
      GoRoute(
        path: gpuTweaks,
        builder: (context, state) => const GpuTweaksPage(),
      ),
      GoRoute(
        path: touchTweaks,
        builder: (context, state) => const TouchTweaksPage(),
      ),
      GoRoute(
        path: networkTweaks,
        builder: (context, state) => const NetworkTweaksPage(),
      ),
      GoRoute(
        path: performance,
        builder: (context, state) => const PerformancePage(),
      ),
      GoRoute(
        path: profiles,
        builder: (context, state) => const ProfilesPage(),
      ),
      GoRoute(
        path: permissions,
        builder: (context, state) => const PermissionsPage(),
      ),
      GoRoute(
        path: settings,
        builder: (context, state) => const SettingsPage(),
      ),
    ],
  );
}
