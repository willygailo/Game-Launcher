import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import '../theme/app_colors.dart';

class MainShellPage extends StatelessWidget {
  final Widget child;

  const MainShellPage({Key? key, required this.child}) : super(key: key);

  int _calculateSelectedIndex(BuildContext context) {
    final String location = GoRouterState.of(context).uri.toString();
    if (location.startsWith('/performance')) return 1;
    if (location.startsWith('/profiles')) return 2;
    if (location.startsWith('/settings')) return 3;
    return 0; // Home
  }

  void _onItemTapped(int index, BuildContext context) {
    switch (index) {
      case 0:
        context.go('/');
        break;
      case 1:
        context.go('/performance');
        break;
      case 2:
        context.go('/profiles');
        break;
      case 3:
        context.go('/settings');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    final selectedIndex = _calculateSelectedIndex(context);
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      body: child,
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          border: Border(
            top: BorderSide(
              color: isDark ? AppColors.glassBorder : AppColors.lightGlassBorder,
              width: 1,
            ),
          ),
        ),
        child: NavigationBar(
          selectedIndex: selectedIndex,
          onDestinationSelected: (index) => _onItemTapped(index, context),
          backgroundColor: isDark ? AppColors.surface : AppColors.lightSurface,
          indicatorColor: isDark ? AppColors.neonCyan.withOpacity(0.2) : AppColors.neonPurple.withOpacity(0.2),
          destinations: [
            NavigationDestination(
              icon: const Icon(Icons.speed),
              selectedIcon: const Icon(Icons.speed, color: AppColors.neonCyan),
              label: l10n.dashboard,
            ),
            NavigationDestination(
              icon: const Icon(Icons.tune),
              selectedIcon: const Icon(Icons.tune, color: AppColors.neonOrange),
              label: l10n.metrics,
            ),
            NavigationDestination(
              icon: const Icon(Icons.folder_special),
              selectedIcon: const Icon(Icons.folder_special, color: AppColors.neonPink),
              label: l10n.profiles,
            ),
            NavigationDestination(
              icon: const Icon(Icons.settings),
              selectedIcon: const Icon(Icons.settings, color: AppColors.neonGreen),
              label: l10n.settings,
            ),
          ],
        ),
      ),
    );
  }
}
