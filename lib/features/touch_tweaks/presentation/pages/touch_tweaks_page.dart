import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/tweak_toggle_tile.dart';
import 'cubit/touch_tweaks_cubit.dart';
import 'cubit/touch_tweaks_state.dart';

class TouchTweaksPage extends StatefulWidget {
  const TouchTweaksPage({Key? key}) : super(key: key);

  @override
  State<TouchTweaksPage> createState() => _TouchTweaksPageState();
}

class _TouchTweaksPageState extends State<TouchTweaksPage> {
  Offset? _touchPos;
  int _touchCount = 0;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.touchResponsiveness),
      ),
      body: BlocBuilder<TouchTweaksCubit, TouchTweaksState>(
        builder: (context, state) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Interactive Touch Test Card
                GlassmorphicCard(
                  borderColor: AppColors.neonPink,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(l10n.touchSamplingRate, style: AppTypography.titleSection),
                      const SizedBox(height: 8),
                      Text(
                        l10n.samplingRateLabel(state.samplingRate.toInt().toString()),
                        style: AppTypography.bodyLarge.copyWith(color: AppColors.neonPink),
                      ),
                      Slider(
                        value: state.samplingRate,
                        min: 60.0,
                        max: 360.0,
                        divisions: 10,
                        activeColor: AppColors.neonPink,
                        onChanged: (val) {
                          context.read<TouchTweaksCubit>().updateSamplingRate(val);
                        },
                      ),
                      const SizedBox(height: 12),

                      // Interactive Touch Test Box
                      Text(l10n.latencyTestPad, style: AppTypography.caption),
                      const SizedBox(height: 8),
                      GestureDetector(
                        onTapDown: (details) {
                          setState(() {
                            _touchPos = details.localPosition;
                            _touchCount++;
                          });
                        },
                        child: Container(
                          height: 120,
                          width: double.infinity,
                          decoration: BoxDecoration(
                            color: AppColors.surfaceLight,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: AppColors.neonPink.withOpacity(0.5)),
                          ),
                          child: Stack(
                            children: [
                              Center(
                                child: Text(
                                  _touchPos == null
                                      ? l10n.tapAnywhere
                                      : '${l10n.registeredTaps(_touchCount.toString())}\n${l10n.lastPos(_touchPos!.dx.toInt().toString(), _touchPos!.dy.toInt().toString())}',
                                  textAlign: TextAlign.center,
                                  style: AppTypography.bodyMedium,
                                ),
                              ),
                              if (_touchPos != null)
                                Positioned(
                                  left: _touchPos!.dx - 15,
                                  top: _touchPos!.dy - 15,
                                  child: Container(
                                    width: 30,
                                    height: 30,
                                    decoration: BoxDecoration(
                                      shape: BoxShape.circle,
                                      color: AppColors.neonPink.withOpacity(0.4),
                                      border: Border.all(color: AppColors.neonPink, width: 2),
                                    ),
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 20),

                Text(l10n.setpropTouchTweaks, style: AppTypography.titleSection),
                const SizedBox(height: 12),
                ...TweakConstants.touchTweaks.map((tweak) {
                  final isEnabled = state.tweakStates[tweak.key] ?? false;
                  return TweakToggleTile(
                    tweak: tweak,
                    isEnabled: isEnabled,
                    onChanged: (val) {
                      context.read<TouchTweaksCubit>().toggleTweak(tweak, val);
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
