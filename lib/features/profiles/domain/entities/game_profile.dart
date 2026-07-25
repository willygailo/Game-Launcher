import 'package:equatable/equatable.dart';

/// Entity representing a saved performance profile (e.g. Gaming, Battery, Balanced).
class GameProfile extends Equatable {
  final String id;
  final String name;
  final String description;
  final Map<String, String> tweaks; // key → value map of setprop commands
  final bool isActive;

  const GameProfile({
    required this.id,
    required this.name,
    required this.description,
    required this.tweaks,
    this.isActive = false,
  });

  GameProfile copyWith({
    String? id,
    String? name,
    String? description,
    Map<String, String>? tweaks,
    bool? isActive,
  }) {
    return GameProfile(
      id: id ?? this.id,
      name: name ?? this.name,
      description: description ?? this.description,
      tweaks: tweaks ?? this.tweaks,
      isActive: isActive ?? this.isActive,
    );
  }

  @override
  List<Object?> get props => [id, name, description, tweaks, isActive];
}
