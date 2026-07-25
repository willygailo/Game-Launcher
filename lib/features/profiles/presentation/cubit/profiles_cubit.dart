import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/platform/root_command_service.dart';
import 'profiles_state.dart';

class ProfilesCubit extends Cubit<ProfilesState> {
  final RootCommandService rootCommandService;

  ProfilesCubit(this.rootCommandService)
      : super(const ProfilesState(
          profiles: [
            GameProfile(
              name: 'PUBG Mobile Extreme',
              iconName: 'sports_esports',
              tweaks: {
                'debug.composition.type': 'gpu',
                'windowsmgr.max_events_per_sec': '300',
                'ro.min_pointer_dur': '8',
              },
            ),
            GameProfile(
              name: 'Free Fire Ultra Smooth',
              iconName: 'fireplace',
              tweaks: {
                'debug.sf.hw': '1',
                'video.accelerate.hw': '1',
                'wifi.supplicant_scan_interval': '180',
              },
            ),
            GameProfile(
              name: 'Genshin Impact Max FPS',
              iconName: 'auto_awesome',
              tweaks: {
                'hw3d.force': '1',
                'debug.gr.swapinterval': '0',
                'debug.rs.max-threads': '8',
              },
            ),
          ],
        ));

  Future<void> activateProfile(GameProfile profile) async {
    emit(state.copyWith(activeProfileName: profile.name));
    await rootCommandService.executeBatchTweaks(profile.tweaks);
  }

  void addProfile(GameProfile newProfile) {
    final updatedList = List<GameProfile>.from(state.profiles)..add(newProfile);
    emit(state.copyWith(profiles: updatedList));
  }
}
