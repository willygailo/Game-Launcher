import 'package:equatable/equatable.dart';

/// Entity representing a touch sampling rate / sensitivity tweak.
class TouchTweak extends Equatable {
  final String key;
  final String value;
  final String description;
  final bool isEnabled;

  const TouchTweak({
    required this.key,
    required this.value,
    required this.description,
    this.isEnabled = false,
  });

  TouchTweak copyWith({
    String? key,
    String? value,
    String? description,
    bool? isEnabled,
  }) {
    return TouchTweak(
      key: key ?? this.key,
      value: value ?? this.value,
      description: description ?? this.description,
      isEnabled: isEnabled ?? this.isEnabled,
    );
  }

  @override
  List<Object?> get props => [key, value, description, isEnabled];
}
