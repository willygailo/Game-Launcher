import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/neon_button.dart';
import '../../../permissions/presentation/cubit/permissions_cubit.dart';
import '../../../permissions/domain/entities/app_permissions.dart';
import '../cubit/hz_fps_cubit.dart';
import '../cubit/hz_fps_state.dart';

class HzFpsPage extends StatefulWidget {
  const HzFpsPage({Key? key}) : super(key: key);

  @override
  State<HzFpsPage> createState() => _HzFpsPageState();
}

class _HzFpsPageState extends State<HzFpsPage> {
  @override
  void initState() {
    super.initState();
    context.read<HzFpsCubit>().loadDisplayModes();
  }

  @override
  Widget build(BuildContext context) {
    final permissionsState = context.watch<PermissionsCubit>().state;
    final executionMode = permissionsState.executionMode;
    final modeString = executionMode == ExecutionMode.root
        ? 'ROOT (su)'
        : executionMode == ExecutionMode.shizuku
            ? 'SHIZUKU (ADB)'
            : 'READ-ONLY (No Root/Shizuku)';

    final modeColor = executionMode == ExecutionMode.root
        ? AppColors.neonGreen
        : executionMode == ExecutionMode.shizuku
            ? AppColors.neonCyan
            : AppColors.warning;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Hz & FPS Unlocker', style: AppTypography.titleHeader),
      ),
      body: BlocConsumer<HzFpsCubit, HzFpsState>(
        listener: (context, state) {
          if (state is HzFpsLoaded && state.message != null) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.message!),
                backgroundColor: AppColors.cardBackground,
                behavior: SnackBarBehavior.floating,
              ),
            );
          }
        },
        builder: (context, state) {
          if (state is HzFpsLoading) {
            return const Center(
              child: CircularProgressIndicator(color: AppColors.neonCyan),
            );
          } else if (state is HzFpsLoaded) {
            return SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Mode Indicator Card
                  GlassmorphicCard(
                    borderColor: modeColor.withOpacity(0.5),
                    child: Row(
                      children: [
                        Icon(
                          executionMode == ExecutionMode.readOnly
                              ? Icons.info_outline
                              : Icons.bolt,
                          color: modeColor,
                          size: 28,
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Execution Engine', style: AppTypography.caption),
                              Text(
                                modeString,
                                style: AppTypography.bodyLarge.copyWith(
                                  color: modeColor,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Current Display Refresh Rate Gauge Card
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
                                Text('Hardware Refresh Rate', style: AppTypography.caption),
                                const SizedBox(height: 4),
                                Text(
                                  '${state.currentHz.toInt()} Hz',
                                  style: AppTypography.titleHeader.copyWith(
                                    fontSize: 36,
                                    color: AppColors.neonCyan,
                                  ),
                                ),
                              ],
                            ),
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                color: AppColors.neonCyan.withOpacity(0.1),
                                border: Border.all(color: AppColors.neonCyan.withOpacity(0.4)),
                              ),
                              child: const Icon(Icons.speed, color: AppColors.neonCyan, size: 36),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Lock Target Refresh Rate
                  Text('Lock Max Refresh Rate', style: AppTypography.titleSection),
                  const SizedBox(height: 8),
                  Text(
                    'Forces peak & minimum display refresh rate via Shizuku ADB / Root.',
                    style: AppTypography.bodyMedium.copyWith(fontSize: 12),
                  ),
                  const SizedBox(height: 12),

                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: state.supportedRates.map((hz) {
                      final isSelected = (state.activeHz == hz);
                      return ChoiceChip(
                        label: Text(
                          '${hz.toInt()} Hz',
                          style: TextStyle(
                            color: isSelected ? Colors.black : Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        selected: isSelected,
                        selectedColor: AppColors.neonCyan,
                        backgroundColor: AppColors.cardBackground,
                        side: BorderSide(
                          color: isSelected ? AppColors.neonCyan : AppColors.glassBorder,
                        ),
                        onSelected: (selected) {
                          if (selected) {
                            final mode = executionMode == ExecutionMode.root
                                ? 'root'
                                : executionMode == ExecutionMode.shizuku
                                    ? 'shizuku'
                                    : 'auto';
                            context.read<HzFpsCubit>().setRefreshRate(hz, mode: mode);
                          }
                        },
                      );
                    }).toList(),
                  ),
                  const SizedBox(height: 24),

                  // Thermal Throttling Bypass
                  Text('Thermal & FPS Limiters', style: AppTypography.titleSection),
                  const SizedBox(height: 12),
                  GlassmorphicCard(
                    child: SwitchListTile(
                      activeColor: AppColors.neonPink,
                      title: const Text('Bypass Thermal Throttling', style: AppTypography.bodyLarge),
                      subtitle: const Text(
                        'Disable thermal status limits & low power mode to maintain steady max FPS during long sessions.',
                        style: AppTypography.bodyMedium,
                      ),
                      value: state.isThermalBypassActive,
                      onChanged: (val) {
                        final mode = executionMode == ExecutionMode.root
                            ? 'root'
                            : executionMode == ExecutionMode.shizuku
                                ? 'shizuku'
                                : 'auto';
                        context.read<HzFpsCubit>().toggleThermalBypass(mode: mode);
                      },
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Apply Max Boost Button
                  SizedBox(
                    width: double.infinity,
                    child: NeonButton(
                      label: 'FORCE MAX DISPLAY & FPS BOOST',
                      icon: Icons.flash_on,
                      gradient: AppColors.boostGradient,
                      isLoading: state.isApplying,
                      onPressed: () {
                        final maxHz = state.supportedRates.last;
                        final mode = executionMode == ExecutionMode.root
                            ? 'root'
                            : executionMode == ExecutionMode.shizuku
                                ? 'shizuku'
                                : 'auto';
                        context.read<HzFpsCubit>().setRefreshRate(maxHz, mode: mode);
                        context.read<HzFpsCubit>().toggleThermalBypass(mode: mode);
                      },
                    ),
                  ),
                ],
              ),
            );
          } else if (state is HzFpsError) {
            return Center(
              child: Text('Error loading display modes: ${state.message}',
                  style: AppTypography.bodyMedium),
            );
          }
          return const SizedBox.shrink();
        },
      ),
    );
  }
}
