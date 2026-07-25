import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/neon_button.dart';
import '../../../../core/widgets/performance_gauge.dart';
import '../../../../core/router/app_router.dart';
import 'cubit/home_cubit.dart';
import 'cubit/home_state.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  void initState() {
    super.initState();
    context.read<HomeCubit>().loadDashboard();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.appTitle, style: AppTypography.titleHeader),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings, color: AppColors.neonCyan),
            onPressed: () => context.push(AppRouter.settings),
          ),
        ],
      ),
      body: BlocBuilder<HomeCubit, HomeState>(
        builder: (context, state) {
          if (state is HomeLoading) {
            return const Center(
              child: CircularProgressIndicator(color: AppColors.neonCyan),
            );
          } else if (state is HomeLoaded) {
            final info = state.deviceInfo;
            return SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Root Status Card
                  GlassmorphicCard(
                    borderColor: info.isRooted ? AppColors.neonGreen : AppColors.warning,
                    child: Row(
                      children: [
                        Icon(
                          info.isRooted ? Icons.verified_user : Icons.gpp_maybe,
                          color: info.isRooted ? AppColors.neonGreen : AppColors.warning,
                          size: 32,
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(l10n.rootStatus, style: AppTypography.caption),
                              Text(
                                info.isRooted ? l10n.rootGranted : l10n.rootDenied,
                                style: AppTypography.bodyLarge.copyWith(
                                  color: info.isRooted ? AppColors.neonGreen : AppColors.warning,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.security, color: AppColors.neonCyan),
                          onPressed: () => context.push(AppRouter.permissions),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Quick Boost Neon Banner
                  GlassmorphicCard(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(l10n.quickBoost, style: AppTypography.titleSection),
                                const SizedBox(height: 4),
                                Text(
                                  '${state.activeTweaksCount} ${l10n.activeTweaks}',
                                  style: AppTypography.bodyMedium,
                                ),
                              ],
                            ),
                            const Icon(Icons.flash_on, color: AppColors.neonPink, size: 40),
                          ],
                        ),
                        const SizedBox(height: 16),
                        SizedBox(
                          width: double.infinity,
                          child: NeonButton(
                            label: l10n.applyAllTweaks,
                            icon: Icons.rocket_launch,
                            gradient: AppColors.boostGradient,
                            isLoading: state.isBoosting,
                            onPressed: () {
                              context.read<HomeCubit>().triggerQuickBoost();
                            },
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Device Spec Card
                  Text(l10n.deviceInfo, style: AppTypography.titleSection),
                  const SizedBox(height: 10),
                  GlassmorphicCard(
                    child: Column(
                      children: [
                        _buildSpecRow(l10n.manufacturer, '${info.manufacturer} ${info.model}'),
                        const Divider(color: AppColors.glassBorder),
                        _buildSpecRow(l10n.chipset, info.chipset),
                        const Divider(color: AppColors.glassBorder),
                        _buildSpecRow(l10n.cpuCores, '${info.cpuCores} Cores'),
                        const Divider(color: AppColors.glassBorder),
                        _buildSpecRow(l10n.ramTotal, '${info.totalRamMb} MB'),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Live Performance Gauges
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: const [
                      PerformanceGauge(value: 38.0, title: 'CPU Load'),
                      PerformanceGauge(value: 62.0, title: 'RAM Usage'),
                      PerformanceGauge(value: 24.0, title: 'FPS Lock'),
                    ],
                  ),
                  const SizedBox(height: 24),

                  // Feature Navigation Grid
                  Text('OPTIMIZATION MODULES', style: AppTypography.titleSection),
                  const SizedBox(height: 12),
                  GridView.count(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisCount: 2,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.4,
                    children: [
                      _buildNavTile(context, l10n.cpuTweaks, Icons.memory, AppColors.neonCyan, AppRouter.cpuTweaks),
                      _buildNavTile(context, l10n.gpuTweaks, Icons.speed, AppColors.neonPurple, AppRouter.gpuTweaks),
                      _buildNavTile(context, l10n.touchTweaks, Icons.touch_app, AppColors.neonPink, AppRouter.touchTweaks),
                      _buildNavTile(context, l10n.networkTweaks, Icons.wifi_tethering, AppColors.neonGreen, AppRouter.networkTweaks),
                      _buildNavTile(context, l10n.performance, Icons.tune, AppColors.neonOrange, AppRouter.performance),
                      _buildNavTile(context, l10n.profiles, Icons.folder_special, AppColors.neonCyan, AppRouter.profiles),
                    ],
                  ),
                ],
              ),
            );
          } else if (state is HomeError) {
            return Center(
              child: Text('Error loading device info: ${state.message}', style: AppTypography.bodyMedium),
            );
          }
          return const SizedBox.shrink();
        },
      ),
    );
  }

  Widget _buildSpecRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: AppTypography.bodyMedium),
          Text(value, style: AppTypography.bodyLarge.copyWith(fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  Widget _buildNavTile(BuildContext context, String title, IconData icon, Color color, String route) {
    return GlassmorphicCard(
      borderColor: color.withOpacity(0.4),
      child: InkWell(
        onTap: () => context.push(route),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: color, size: 32),
            const SizedBox(height: 8),
            Text(title, style: AppTypography.bodyLarge.copyWith(fontSize: 14), textAlign: TextAlign.center),
          ],
        ),
      ),
    );
  }
}
