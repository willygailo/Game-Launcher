import 'package:equatable/equatable.dart';

class TouchTweaksState extends Equatable {
  final Map<String, bool> tweakStates;
  final double samplingRate;

  const TouchTweaksState({
    required this.tweakStates,
    this.samplingRate = 300.0,
  });

  TouchTweaksState copyWith({
    Map<String, bool>? tweakStates,
    double? samplingRate,
  }) {
    return TouchTweaksState(
      tweakStates: tweakStates ?? this.tweakStates,
      samplingRate: samplingRate ?? this.samplingRate,
    );
  }

  @override
  List<Object?> get props => [tweakStates, samplingRate];
}
