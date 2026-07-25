import 'package:flutter/material.dart';
import '../constants/tweak_constants.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';
import 'glassmorphic_card.dart';

class TweakToggleTile extends StatelessWidget {
  final TweakItem tweak;
  final bool isEnabled;
  final ValueChanged<bool> onChanged;

  const TweakToggleTile({
    Key? key,
    required this.tweak,
    required this.isEnabled,
    required this.onChanged,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: GlassmorphicCard(
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
        borderColor: isEnabled ? AppColors.neonCyan : AppColors.glassBorder,
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(tweak.title, style: AppTypography.bodyLarge),
                      const SizedBox(width: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: AppColors.neonPurple.withOpacity(0.2),
                          borderRadius: BorderRadius.circular(4),
                          border: Border.all(color: AppColors.neonPurple, width: 0.8),
                        ),
                        child: Text(
                          tweak.category,
                          style: AppTypography.caption.copyWith(
                            color: AppColors.neonPurple,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(tweak.description, style: AppTypography.bodyMedium),
                  const SizedBox(height: 4),
                  Text(
                    'Prop: ${tweak.key} = ${isEnabled ? tweak.tweakValue : tweak.defaultValue}',
                    style: AppTypography.caption.copyWith(fontFamily: 'monospace'),
                  ),
                ],
              ),
            ),
            Switch(
              value: isEnabled,
              onChanged: onChanged,
            ),
          ],
        ),
      ),
    );
  }
}
