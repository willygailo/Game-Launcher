import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

import '../../domain/entities/game_profile.dart';

const _kProfilesKey = 'game_space_profiles';

/// Persists and retrieves [GameProfile] objects using SharedPreferences.
class ProfilesDatasource {
  final SharedPreferences prefs;

  ProfilesDatasource({required this.prefs});

  List<GameProfile> getProfiles() {
    final raw = prefs.getStringList(_kProfilesKey) ?? [];
    return raw.map((json) => _fromJson(jsonDecode(json))).toList();
  }

  Future<bool> saveProfile(GameProfile profile) async {
    final profiles = getProfiles();
    final index = profiles.indexWhere((p) => p.id == profile.id);
    if (index >= 0) {
      profiles[index] = profile;
    } else {
      profiles.add(profile);
    }
    return prefs.setStringList(
      _kProfilesKey,
      profiles.map((p) => jsonEncode(_toJson(p))).toList(),
    );
  }

  Future<bool> deleteProfile(String profileId) async {
    final profiles = getProfiles()..removeWhere((p) => p.id == profileId);
    return prefs.setStringList(
      _kProfilesKey,
      profiles.map((p) => jsonEncode(_toJson(p))).toList(),
    );
  }

  Future<bool> setActiveBootTweaks(Map<String, String> tweaks) async {
    return prefs.setString('active_boot_tweaks', jsonEncode(tweaks));
  }

  GameProfile _fromJson(Map<String, dynamic> json) {
    return GameProfile(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String,
      tweaks: Map<String, String>.from(json['tweaks'] as Map),
      isActive: json['isActive'] as bool? ?? false,
    );
  }

  Map<String, dynamic> _toJson(GameProfile p) => {
    'id': p.id,
    'name': p.name,
    'description': p.description,
    'tweaks': p.tweaks,
    'isActive': p.isActive,
  };
}

