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
import '../../../performance/presentation/cubit/performance_cubit.dart';
import '../../../performance/presentation/cubit/performance_state.dart';
import '../../../permissions/presentation/cubit/permissions_cubit.dart';
import '../../../permissions/presentation/cubit/permissions_state.dart';
import '../../../permissions/domain/entities/app_permissions.dart';
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
    context.read<PerformanceCubit>().startPolling();
    context.read<PermissionsCubit>().checkPermissions();
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
                  // Hero Banner Card
                  ClipRRect(
                    borderRadius: BorderRadius.circular(16),
                    child: Container(
                      height: 140,
                      width: double.infinity,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: AppColors.glassBorder),
                      ),
                      child: Image.asset(
                        'assets/images/game_banner.png',
                        fit: BoxFit.cover,
                        errorBuilder: (context, error, stackTrace) => Container(
                          decoration: const BoxDecoration(
                            gradient: AppColors.primaryGradient,
                          ),
                          child: const Center(
                            child: Icon(Icons.rocket_launch, size: 48, color: Colors.white),
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Execution Engine Status Card
                  BlocBuilder<PermissionsCubit, PermissionsState>(
                    builder: (context, permState) {
                      final mode = permState.executionMode;
                      final titleStr = mode == ExecutionMode.root
                          ? 'ROOT (su) ACTIVE'
                          : mode == ExecutionMode.shizuku
                              ? 'SHIZUKU (ADB) ACTIVE'
                              : 'READ-ONLY MODE';

                      final color = mode == ExecutionMode.root
                          ? AppColors.neonGreen
                          : mode == ExecutionMode.shizuku
                              ? AppColors.neonCyan
                              : AppColors.warning;

                      final icon = mode == ExecutionMode.root
                          ? Icons.verified_user
                          : mode == ExecutionMode.shizuku
                              ? Icons.bolt
                              : Icons.info_outline;

                      return GlassmorphicCard(
                        borderColor: color,
                        child: Row(
                          children: [
                            Icon(icon, color: color, size: 32),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text('EXECUTION ENGINE', style: AppTypography.caption),
                                  Text(
                                    titleStr,
                                    style: AppTypography.bodyLarge.copyWith(
                                      color: color,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            IconButton(
                              icon: const Icon(Icons.tune, color: AppColors.neonCyan),
                              onPressed: () => context.push(AppRouter.permissions),
                            ),
                          ],
                        ),
                      );
                    },
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
                        _buildSpecRow(l10n.cpuCores, '${info.cpuCores} ${l10n.cores}'),
                        const Divider(color: AppColors.glassBorder),
                        _buildSpecRow(l10n.ramTotal, '${info.totalRamMb} MB'),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Live Performance Gauges
                  BlocBuilder<PerformanceCubit, PerformanceState>(
                    builder: (context, perfState) {
                      final m = perfState.metrics;
                      final cpuLoad = m?.cpuLoadPercent ?? 35.0;
                      final ramUsage = m?.ramUsagePercent ?? 50.0;
                      final battery = m?.batteryPercent ?? 85.0;

                      return Row(
                        mainAxisAlignment: MainAxisAlignment.spaceAround,
                        children: [
                          PerformanceGauge(value: cpuLoad, title: l10n.cpuLoad),
                          PerformanceGauge(value: ramUsage, title: l10n.ramUsage),
                          PerformanceGauge(value: battery, title: l10n.batteryPercent),
                        ],
                      );
                    },
                  ),
                  const SizedBox(height: 24),

                  // Feature Navigation Grid
                  Text(l10n.optimizationModules, style: AppTypography.titleSection),
                  const SizedBox(height: 12),
                  GridView.count(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisCount: 2,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.35,
                    children: [
                      _buildNavTile(context, l10n.cpuTweaks, Icons.memory, AppColors.neonCyan, AppRouter.cpuTweaks),
                      _buildNavTile(context, l10n.gpuTweaks, Icons.speed, AppColors.neonPurple, AppRouter.gpuTweaks),
                      _buildNavTile(context, 'Hz & FPS Unlocker', Icons.bolt, AppColors.neonCyan, AppRouter.hzFpsTweaks),
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
              child: Text(l10n.errorLoadingDevice(state.message), style: AppTypography.bodyMedium),
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
      padding: EdgeInsets.zero,
      borderColor: color.withOpacity(0.4),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: () => context.push(route),
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, color: color, size: 28),
              const SizedBox(height: 6),
              Text(
                title,
                style: AppTypography.bodyLarge.copyWith(fontSize: 13),
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
