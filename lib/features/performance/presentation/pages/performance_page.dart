import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/tweak_toggle_tile.dart';
import 'cubit/performance_cubit.dart';
import 'cubit/performance_state.dart';

class PerformancePage extends StatelessWidget {
  const PerformancePage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.systemPerformanceTweaks),
      ),
      body: BlocBuilder<PerformanceCubit, PerformanceState>(
        builder: (context, state) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(l10n.systemDalvikTweaks, style: AppTypography.titleSection),
                const SizedBox(height: 12),
                ...TweakConstants.systemTweaks.map((tweak) {
                  final isEnabled = state.tweakStates[tweak.key] ?? false;
                  return TweakToggleTile(
                    tweak: tweak,
                    isEnabled: isEnabled,
                    onChanged: (val) {
                      context.read<PerformanceCubit>().toggleTweak(tweak, val);
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
