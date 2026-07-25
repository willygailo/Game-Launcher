import 'package:equatable/equatable.dart';

class GameProfile extends Equatable {
  final String name;
  final String iconName;
  final Map<String, String> tweaks;

  const GameProfile({
    required this.name,
    required this.iconName,
    required this.tweaks,
  });

  @override
  List<Object?> get props => [name, iconName, tweaks];
}

class ProfilesState extends Equatable {
  final List<GameProfile> profiles;
  final String? activeProfileName;

  const ProfilesState({
    required this.profiles,
    this.activeProfileName,
  });

  ProfilesState copyWith({
    List<GameProfile>? profiles,
    String? activeProfileName,
  }) {
    return ProfilesState(
      profiles: profiles ?? this.profiles,
      activeProfileName: activeProfileName ?? this.activeProfileName,
    );
  }

  @override
  List<Object?> get props => [profiles, activeProfileName];
}
