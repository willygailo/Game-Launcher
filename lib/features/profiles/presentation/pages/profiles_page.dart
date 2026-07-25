import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/neon_button.dart';
import 'cubit/profiles_cubit.dart';
import 'cubit/profiles_state.dart';

import '../../../../core/platform/magisk_exporter_service.dart';
import '../../../../injection.dart';

class ProfilesPage extends StatefulWidget {
  const ProfilesPage({Key? key}) : super(key: key);

  @override
  State<ProfilesPage> createState() => _ProfilesPageState();
}

class _ProfilesPageState extends State<ProfilesPage> {
  @override
  void initState() {
    super.initState();
    context.read<ProfilesCubit>().loadProfiles();
  }

  Future<void> _exportMagisk(BuildContext context, GameProfile profile) async {
    final service = sl<MagiskExporterService>();
    final path = await service.exportMagiskModule(
      moduleName: profile.name.replaceAll(' ', '_'),
      tweaks: profile.tweaks,
    );

    if (!mounted) return;
    if (path != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Exported Magisk Zip to:\n$path'),
          backgroundColor: AppColors.neonGreen,
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Failed to export Magisk zip.'),
          backgroundColor: AppColors.error,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('GAME TWEAK PROFILES'),
      ),
      body: BlocBuilder<ProfilesCubit, ProfilesState>(
        builder: (context, state) {
          if (state.profiles.isEmpty) {
            return const Center(
              child: CircularProgressIndicator(color: AppColors.neonCyan),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(16.0),
            itemCount: state.profiles.length,
            itemBuilder: (context, index) {
              final profile = state.profiles[index];
              final isActive = state.activeProfileName == profile.name;

              return Padding(
                padding: const EdgeInsets.only(bottom: 12.0),
                child: GlassmorphicCard(
                  borderColor: isActive ? AppColors.neonGreen : AppColors.glassBorder,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(Icons.sports_esports, color: isActive ? AppColors.neonGreen : AppColors.neonCyan, size: 28),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(profile.name, style: AppTypography.titleSection.copyWith(fontSize: 16)),
                          ),
                          if (isActive)
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                color: AppColors.neonGreen.withOpacity(0.2),
                                borderRadius: BorderRadius.circular(4),
                                border: Border.all(color: AppColors.neonGreen),
                              ),
                              child: Text(
                                'ACTIVE',
                                style: AppTypography.caption.copyWith(color: AppColors.neonGreen, fontWeight: FontWeight.bold),
                              ),
                            ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Contains ${profile.tweaks.length} setprop configurations',
                        style: AppTypography.bodyMedium,
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          Expanded(
                            child: NeonButton(
                              label: isActive ? 'PROFILE APPLIED' : 'APPLY PROFILE',
                              gradient: isActive ? AppColors.primaryGradient : AppColors.boostGradient,
                              onPressed: () {
                                context.read<ProfilesCubit>().activateProfile(profile);
                              },
                            ),
                          ),
                          const SizedBox(width: 8),
                          IconButton(
                            icon: const Icon(Icons.archive, color: AppColors.neonCyan),
                            tooltip: 'Export Magisk Zip',
                            onPressed: () => _exportMagisk(context, profile),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}

