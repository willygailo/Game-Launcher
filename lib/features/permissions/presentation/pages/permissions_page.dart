import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/neon_button.dart';
import '../../domain/entities/app_permissions.dart';
import 'cubit/permissions_cubit.dart';
import 'cubit/permissions_state.dart';

class PermissionsPage extends StatefulWidget {
  const PermissionsPage({Key? key}) : super(key: key);

  @override
  State<PermissionsPage> createState() => _PermissionsPageState();
}

class _PermissionsPageState extends State<PermissionsPage> {
  @override
  void initState() {
    super.initState();
    context.read<PermissionsCubit>().checkPermissions();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.systemPermissions),
      ),
      body: BlocBuilder<PermissionsCubit, PermissionsState>(
        builder: (context, state) {
          final modeStr = state.executionMode == ExecutionMode.root
              ? 'ROOT (su)'
              : state.executionMode == ExecutionMode.shizuku
                  ? 'SHIZUKU ADB'
                  : 'READ-ONLY MODE';

          final modeColor = state.executionMode == ExecutionMode.root
              ? AppColors.neonGreen
              : state.executionMode == ExecutionMode.shizuku
                  ? AppColors.neonCyan
                  : AppColors.warning;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Active Engine Banner
                GlassmorphicCard(
                  borderColor: modeColor,
                  child: Row(
                    children: [
                      Icon(Icons.bolt, color: modeColor, size: 36),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('ACTIVE EXECUTION ENGINE', style: AppTypography.caption),
                            Text(
                              modeStr,
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

                // Root Access Card
                GlassmorphicCard(
                  borderColor: state.isRootGranted ? AppColors.neonGreen : AppColors.glassBorder,
                  child: Column(
                    children: [
                      ListTile(
                        leading: Icon(
                          state.isRootGranted ? Icons.security : Icons.shield_outlined,
                          color: state.isRootGranted ? AppColors.neonGreen : AppColors.warning,
                          size: 32,
                        ),
                        title: Text(l10n.rootAccessSu, style: AppTypography.bodyLarge),
                        subtitle: Text(
                          state.isRootGranted ? l10n.rootGrantedDesc : l10n.rootDeniedDesc,
                          style: AppTypography.bodyMedium,
                        ),
                      ),
                      const SizedBox(height: 12),
                      SizedBox(
                        width: double.infinity,
                        child: NeonButton(
                          label: l10n.requestRootAccess,
                          onPressed: () {
                            context.read<PermissionsCubit>().requestRoot();
                          },
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // Shizuku ADB Card for Non-Rooted Devices
                GlassmorphicCard(
                  borderColor: state.isShizukuGranted ? AppColors.neonCyan : AppColors.glassBorder,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      ListTile(
                        leading: Icon(
                          state.isShizukuGranted ? Icons.verified : Icons.phonelink_setup,
                          color: state.isShizukuGranted ? AppColors.neonCyan : AppColors.warning,
                          size: 32,
                        ),
                        title: const Text('Shizuku App Connection', style: AppTypography.bodyLarge),
                        subtitle: Text(
                          state.isShizukuGranted
                              ? 'Shizuku connected! Full 120Hz/144Hz & Graphics unlocked directly without root.'
                              : state.isShizukuAvailable
                                  ? 'Shizuku app ready! Tap to authorize 1-tap connection (no manual ADB steps needed).'
                                  : 'Open Shizuku app to enable direct non-rooted tweaking.',
                          style: AppTypography.bodyMedium,
                        ),
                      ),
                      const SizedBox(height: 12),
                      SizedBox(
                        width: double.infinity,
                        child: NeonButton(
                          label: state.isShizukuGranted ? 'SHIZUKU ACTIVE' : 'REQUEST SHIZUKU PERMISSION',
                          gradient: AppColors.primaryGradient,
                          onPressed: () {
                            context.read<PermissionsCubit>().requestShizuku();
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
