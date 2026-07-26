import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/tweak_toggle_tile.dart';
import 'cubit/gpu_tweaks_cubit.dart';
import 'cubit/gpu_tweaks_state.dart';

class GpuTweaksPage extends StatelessWidget {
  const GpuTweaksPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.gpuGraphicsTweaks),
      ),
      body: BlocBuilder<GpuTweaksCubit, GpuTweaksState>(
        builder: (context, state) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Render Composition Selector Card
                GlassmorphicCard(
                  borderColor: AppColors.neonPurple,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(l10n.surfaceFlingerComposition, style: AppTypography.titleSection),
                      const SizedBox(height: 8),
                      Text(
                        l10n.surfaceFlingerDesc,
                        style: AppTypography.bodyMedium,
                      ),
                      const SizedBox(height: 12),
                      Wrap(
                        spacing: 8,
                        children: ['gpu', 'c2d', 'dyn', 'mdp'].map((mode) {
                          final isSelected = state.compositionType == mode;
                          return ChoiceChip(
                            label: Text(
                              mode.toUpperCase(),
                              style: TextStyle(
                                color: isSelected ? AppColors.background : AppColors.neonPurple,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            selected: isSelected,
                            selectedColor: AppColors.neonPurple,
                            backgroundColor: AppColors.surfaceLight,
                            onSelected: (_) {
                              context.read<GpuTweaksCubit>().setComposition(mode);
                            },
                          );
                        }).toList(),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 20),

                Text(l10n.setpropGpuTweaks, style: AppTypography.titleSection),
                const SizedBox(height: 12),
                ...TweakConstants.gpuTweaks.map((tweak) {
                  final isEnabled = state.tweakStates[tweak.key] ?? false;
                  return TweakToggleTile(
                    tweak: tweak,
                    isEnabled: isEnabled,
                    onChanged: (val) {
                      context.read<GpuTweaksCubit>().toggleTweak(tweak, val);
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
