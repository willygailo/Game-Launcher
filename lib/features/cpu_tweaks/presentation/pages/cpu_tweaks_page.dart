import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/tweak_toggle_tile.dart';
import 'cubit/cpu_tweaks_cubit.dart';
import 'cubit/cpu_tweaks_state.dart';

class CpuTweaksPage extends StatelessWidget {
  const CpuTweaksPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('CPU OPTIMIZATIONS'),
      ),
      body: BlocBuilder<CpuTweaksCubit, CpuTweaksState>(
        builder: (context, state) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Governor Selector Card
                GlassmorphicCard(
                  borderColor: AppColors.neonCyan,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('CPU GOVERNOR MODE', style: AppTypography.titleSection),
                      const SizedBox(height: 8),
                      Text(
                        'Select governor mode to dictate how CPU frequency scales under load.',
                        style: AppTypography.bodyMedium,
                      ),
                      const SizedBox(height: 12),
                      Wrap(
                        spacing: 8,
                        children: ['performance', 'schedutil', 'interactive', 'powersave'].map((gov) {
                          final isSelected = state.governor == gov;
                          return ChoiceChip(
                            label: Text(
                              gov.toUpperCase(),
                              style: TextStyle(
                                color: isSelected ? AppColors.background : AppColors.neonCyan,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            selected: isSelected,
                            selectedColor: AppColors.neonCyan,
                            backgroundColor: AppColors.surfaceLight,
                            onSelected: (_) {
                              context.read<CpuTweaksCubit>().setGovernor(gov);
                            },
                          );
                        }).toList(),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 20),

                Text('SETPROP CPU TWEAKS', style: AppTypography.titleSection),
                const SizedBox(height: 12),
                ...TweakConstants.cpuTweaks.map((tweak) {
                  final isEnabled = state.tweakStates[tweak.key] ?? false;
                  return TweakToggleTile(
                    tweak: tweak,
                    isEnabled: isEnabled,
                    onChanged: (val) {
                      context.read<CpuTweaksCubit>().toggleTweak(tweak, val);
                    },
                  );
                }).toList(),
              ],
            ),
          );
        },
      ),
    );
  }
}
