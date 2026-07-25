import 'package:equatable/equatable.dart';

class GpuTweaksState extends Equatable {
  final Map<String, bool> tweakStates;
  final String compositionType;

  const GpuTweaksState({
    required this.tweakStates,
    this.compositionType = 'gpu',
  });

  GpuTweaksState copyWith({
    Map<String, bool>? tweakStates,
    String? compositionType,
  }) {
    return GpuTweaksState(
      tweakStates: tweakStates ?? this.tweakStates,
      compositionType: compositionType ?? this.compositionType,
    );
  }

  @override
  List<Object?> get props => [tweakStates, compositionType];
}
