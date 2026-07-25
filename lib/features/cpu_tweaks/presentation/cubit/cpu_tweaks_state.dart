import 'package:equatable/equatable.dart';

class CpuTweaksState extends Equatable {
  final Map<String, bool> tweakStates;
  final String governor;

  const CpuTweaksState({
    required this.tweakStates,
    this.governor = 'performance',
  });

  CpuTweaksState copyWith({
    Map<String, bool>? tweakStates,
    String? governor,
  }) {
    return CpuTweaksState(
      tweakStates: tweakStates ?? this.tweakStates,
      governor: governor ?? this.governor,
    );
  }

  @override
  List<Object?> get props => [tweakStates, governor];
}
