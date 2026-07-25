import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/neon_button.dart';
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
    return Scaffold(
      appBar: AppBar(
        title: const Text('SYSTEM PERMISSIONS'),
      ),
      body: BlocBuilder<PermissionsCubit, PermissionsState>(
        builder: (context, state) {
          return Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                GlassmorphicCard(
                  borderColor: state.isRootGranted ? AppColors.neonGreen : AppColors.warning,
                  child: Column(
                    children: [
                      ListTile(
                        leading: Icon(
                          state.isRootGranted ? Icons.shield : Icons.warning_amber,
                          color: state.isRootGranted ? AppColors.neonGreen : AppColors.warning,
                          size: 32,
                        ),
                        title: Text('Root Access (SU binary)', style: AppTypography.bodyLarge),
                        subtitle: Text(
                          state.isRootGranted ? 'Granted - setprop tweaks unlocked' : 'Denied - Running in Read-only Info mode',
                          style: AppTypography.bodyMedium,
                        ),
                      ),
                      const SizedBox(height: 12),
                      SizedBox(
                        width: double.infinity,
                        child: NeonButton(
                          label: 'REQUEST ROOT ACCESS',
                          onPressed: () {
                            context.read<PermissionsCubit>().requestRoot();
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
