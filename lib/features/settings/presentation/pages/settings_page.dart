import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/widgets/glassmorphic_card.dart';
import 'cubit/settings_cubit.dart';
import 'cubit/settings_state.dart';

class SettingsPage extends StatelessWidget {
  const SettingsPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    final languages = [
      {'code': 'en', 'name': 'English'},
      {'code': 'fr', 'name': 'Français'},
      {'code': 'ar', 'name': 'العربية'},
      {'code': 'es', 'name': 'Español'},
      {'code': 'id', 'name': 'Bahasa Indonesia'},
      {'code': 'sw', 'name': 'Kiswahili'},
    ];

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.settings),
      ),
      body: BlocBuilder<SettingsCubit, SettingsState>(
        builder: (context, state) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Language Selection Card
                GlassmorphicCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.language, color: AppColors.neonCyan),
                          const SizedBox(width: 12),
                          Text(l10n.language, style: AppTypography.titleSection),
                        ],
                      ),
                      const SizedBox(height: 12),
                      DropdownButton<String>(
                        value: state.locale.languageCode,
                        isExpanded: true,
                        dropdownColor: AppColors.surface,
                        style: AppTypography.bodyLarge,
                        underline: Container(height: 1, color: AppColors.neonCyan),
                        items: languages.map((lang) {
                          return DropdownMenuItem<String>(
                            value: lang['code'],
                            child: Text(lang['name']!),
                          );
                        }).toList(),
                        onChanged: (val) {
                          if (val != null) {
                            context.read<SettingsCubit>().changeLanguage(val);
                          }
                        },
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // Theme Mode Card
                GlassmorphicCard(
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.dark_mode, color: AppColors.neonPurple),
                          const SizedBox(width: 12),
                          Text(l10n.theme, style: AppTypography.titleSection),
                        ],
                      ),
                      Switch(
                        value: state.themeMode == ThemeMode.dark,
                        onChanged: (isDark) {
                          context.read<SettingsCubit>().toggleTheme(isDark);
                        },
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // App Info Card
                GlassmorphicCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('GAME SPACE PRO v1.0.0', style: AppTypography.titleSection.copyWith(fontSize: 16)),
                      const SizedBox(height: 6),
                      Text(
                        'Supports Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).',
                        style: AppTypography.bodyMedium,
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
