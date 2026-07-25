import 'package:equatable/equatable.dart';

/// Entity representing a single network latency / throughput tweak.
class NetworkTweak extends Equatable {
  final String key;
  final String value;
  final String description;
  final bool isEnabled;

  const NetworkTweak({
    required this.key,
    required this.value,
    required this.description,
    this.isEnabled = false,
  });

  NetworkTweak copyWith({
    String? key,
    String? value,
    String? description,
    bool? isEnabled,
  }) {
    return NetworkTweak(
      key: key ?? this.key,
      value: value ?? this.value,
      description: description ?? this.description,
      isEnabled: isEnabled ?? this.isEnabled,
    );
  }

  @override
  List<Object?> get props => [key, value, description, isEnabled];
}
