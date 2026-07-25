import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import '../../../../core/widgets/tweak_toggle_tile.dart';
import 'cubit/network_tweaks_cubit.dart';
import 'cubit/network_tweaks_state.dart';

class NetworkTweaksPage extends StatelessWidget {
  const NetworkTweaksPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('NETWORK & LATENCY'),
      ),
      body: BlocBuilder<NetworkTweaksCubit, NetworkTweaksState>(
        builder: (context, state) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // DNS Optimizer Card
                GlassmorphicCard(
                  borderColor: AppColors.neonGreen,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('GAMING DNS RESOLVER SELECTOR', style: AppTypography.titleSection),
                      const SizedBox(height: 8),
                      Text(
                        'Active Resolver: ${state.activeDns}',
                        style: AppTypography.bodyMedium.copyWith(color: AppColors.neonGreen),
                      ),
                      const SizedBox(height: 12),
                      Wrap(
                        spacing: 8,
                        children: [
                          _buildDnsChip(context, state, 'Google DNS', '8.8.8.8', '8.8.4.4'),
                          _buildDnsChip(context, state, 'Cloudflare (1.1.1.1)', '1.1.1.1', '1.0.0.1'),
                          _buildDnsChip(context, state, 'AdGuard Gaming', '94.140.14.14', '94.140.15.15'),
                          _buildDnsChip(context, state, 'OpenDNS', '208.67.222.222', '208.67.220.220'),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 20),

                Text('SETPROP NETWORK LATENCY TWEAKS', style: AppTypography.titleSection),
                const SizedBox(height: 12),
                ...TweakConstants.networkTweaks.map((tweak) {
                  final isEnabled = state.tweakStates[tweak.key] ?? false;
                  return TweakToggleTile(
                    tweak: tweak,
                    isEnabled: isEnabled,
                    onChanged: (val) {
                      context.read<NetworkTweaksCubit>().toggleTweak(tweak, val);
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

  Widget _buildDnsChip(BuildContext context, NetworkTweaksState state, String name, String p1, String p2) {
    final isSelected = state.activeDns.contains(name);
    return ChoiceChip(
      label: Text(
        name,
        style: TextStyle(
          color: isSelected ? AppColors.background : AppColors.neonGreen,
          fontWeight: FontWeight.bold,
        ),
      ),
      selected: isSelected,
      selectedColor: AppColors.neonGreen,
      backgroundColor: AppColors.surfaceLight,
      onSelected: (_) {
        context.read<NetworkTweaksCubit>().setDns(name, p1, p2);
      },
    );
  }
}
