import 'package:equatable/equatable.dart';
import '../../domain/entities/performance_metrics.dart';

class PerformanceState extends Equatable {
  final Map<String, bool> tweakStates;
  final PerformanceMetrics? metrics;

  const PerformanceState({
    required this.tweakStates,
    this.metrics,
  });

  PerformanceState copyWith({
    Map<String, bool>? tweakStates,
    PerformanceMetrics? metrics,
  }) {
    return PerformanceState(
      tweakStates: tweakStates ?? this.tweakStates,
      metrics: metrics ?? this.metrics,
    );
  }

  @override
  List<Object?> get props => [tweakStates, metrics];
}

