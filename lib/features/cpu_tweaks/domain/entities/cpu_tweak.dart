import 'package:equatable/equatable.dart';

/// Entity representing a single CPU performance tweak property.
class CpuTweak extends Equatable {
  final String key;
  final String value;
  final String description;
  final bool isEnabled;

  const CpuTweak({
    required this.key,
    required this.value,
    required this.description,
    this.isEnabled = false,
  });

  CpuTweak copyWith({
    String? key,
    String? value,
    String? description,
    bool? isEnabled,
  }) {
    return CpuTweak(
      key: key ?? this.key,
      value: value ?? this.value,
      description: description ?? this.description,
      isEnabled: isEnabled ?? this.isEnabled,
    );
  }

  @override
  List<Object?> get props => [key, value, description, isEnabled];
}
