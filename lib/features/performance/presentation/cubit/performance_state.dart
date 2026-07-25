import 'package:equatable/equatable.dart';

class PerformanceState extends Equatable {
  final Map<String, bool> tweakStates;

  const PerformanceState({required this.tweakStates});

  PerformanceState copyWith({Map<String, bool>? tweakStates}) {
    return PerformanceState(tweakStates: tweakStates ?? this.tweakStates);
  }

  @override
  List<Object?> get props => [tweakStates];
}
