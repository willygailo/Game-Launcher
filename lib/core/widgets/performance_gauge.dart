import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

class PerformanceGauge extends StatelessWidget {
  final double value; // 0.0 to 100.0
  final String title;
  final String unit;

  const PerformanceGauge({
    Key? key,
    required this.value,
    required this.title,
    this.unit = '%',
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final clampedValue = value.clamp(0.0, 100.0);
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        SizedBox(
          width: 100,
          height: 100,
          child: Stack(
            alignment: Alignment.center,
            children: [
              CircularProgressIndicator(
                value: clampedValue / 100.0,
                strokeWidth: 8,
                backgroundColor: AppColors.surfaceLight,
                valueColor: AlwaysStoppedAnimation<Color>(
                  clampedValue > 80
                      ? AppColors.error
                      : (clampedValue > 50 ? AppColors.warning : AppColors.neonCyan),
                ),
              ),
              Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    '${clampedValue.toInt()}$unit',
                    style: AppTypography.titleSection.copyWith(fontSize: 20),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 8),
        Text(title, style: AppTypography.bodyMedium),
      ],
    );
  }
}
