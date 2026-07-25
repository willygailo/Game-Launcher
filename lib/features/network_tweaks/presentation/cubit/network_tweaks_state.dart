import 'package:equatable/equatable.dart';

class NetworkTweaksState extends Equatable {
  final Map<String, bool> tweakStates;
  final String activeDns;

  const NetworkTweaksState({
    required this.tweakStates,
    this.activeDns = '8.8.8.8 (Google)',
  });

  NetworkTweaksState copyWith({
    Map<String, bool>? tweakStates,
    String? activeDns,
  }) {
    return NetworkTweaksState(
      tweakStates: tweakStates ?? this.tweakStates,
      activeDns: activeDns ?? this.activeDns,
    );
  }

  @override
  List<Object?> get props => [tweakStates, activeDns];
}
