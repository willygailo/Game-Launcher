import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/entities/game_profile.dart' as domain;
import '../../domain/usecases/activate_profile.dart';
import '../../domain/usecases/get_profiles.dart';
import '../../../../features/profiles/data/repositories/profiles_repository_impl.dart';
import 'profiles_state.dart';

// Default presets seeded into SharedPreferences on first launch.
const _defaultPresets = [
  (
    id: 'pubg',
    name: 'PUBG Mobile / 3D FPS Extreme',
    icon: 'sports_esports',
    tweaks: {
      'debug.composition.type': 'gpu',
      'windowsmgr.max_events_per_sec': '300',
      'debug.sf.hw': '1',
    },
  ),
  (
    id: 'games_2d',
    name: '2D & Pixel Games Ultra Smooth',
    icon: 'videogame_asset',
    tweaks: {
      'windowsmgr.max_events_per_sec': '300',
      'debug.sf.hw': '1',
      'persist.sys.scrollingcache': '3',
      'wifi.supplicant_scan_interval': '180',
      'debug.egl.hw': '1',
    },
  ),
  (
    id: 'freefire',
    name: 'Free Fire Ultra Smooth',
    icon: 'fireplace',
    tweaks: {
      'debug.sf.hw': '1',
      'video.accelerate.hw': '1',
      'wifi.supplicant_scan_interval': '180',
    },
  ),
  (
    id: 'genshin',
    name: 'Genshin Impact Max Performance',
    icon: 'auto_awesome',
    tweaks: {
      'hw3d.force': '1',
      'debug.gr.swapinterval': '0',
      'debug.rs.max-threads': '8',
    },
  ),
];

class ProfilesCubit extends Cubit<ProfilesState> {
  final GetProfiles _getProfiles;
  final ActivateProfile _activateProfile;
  final ProfilesRepositoryImpl _repo;

  ProfilesCubit({
    required GetProfiles getProfiles,
    required ActivateProfile activateProfile,
    required ProfilesRepositoryImpl repo,
  })  : _getProfiles = getProfiles,
        _activateProfile = activateProfile,
        _repo = repo,
        super(const ProfilesState(profiles: [], activeProfileName: null));

  Future<void> loadProfiles() async {
    List<domain.GameProfile> stored = await _getProfiles();

    // Seed defaults on first launch or add missing presets
    if (stored.isEmpty) {
      for (final p in _defaultPresets) {
        final profile = domain.GameProfile(
          id: p.id,
          name: p.name,
          description: p.icon,
          tweaks: p.tweaks,
        );
        await _repo.saveProfile(profile);
      }
      stored = await _getProfiles();
    }

    final presState = stored
        .map((p) => GameProfile(
              name: p.name,
              iconName: p.description, // description carries icon name
              tweaks: p.tweaks,
            ))
        .toList();

    final active = stored.where((p) => p.isActive).firstOrNull;
    emit(ProfilesState(
      profiles: presState,
      activeProfileName: active?.name,
    ));
  }

  Future<void> activateProfile(GameProfile profile) async {
    emit(state.copyWith(activeProfileName: profile.name));
    final domainProfile = domain.GameProfile(
      id: profile.name.toLowerCase().replaceAll(' ', '_'),
      name: profile.name,
      description: profile.iconName,
      tweaks: profile.tweaks,
      isActive: true,
    );
    await _activateProfile(domainProfile);
  }

  Future<void> addProfile(GameProfile newProfile) async {
    final domainProfile = domain.GameProfile(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      name: newProfile.name,
      description: newProfile.iconName,
      tweaks: newProfile.tweaks,
    );
    await _repo.saveProfile(domainProfile);
    final updatedList = List<GameProfile>.from(state.profiles)..add(newProfile);
    emit(state.copyWith(profiles: updatedList));
  }
}
