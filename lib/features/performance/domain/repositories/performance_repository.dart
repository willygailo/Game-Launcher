import '../entities/performance_metrics.dart';

abstract class PerformanceRepository {
  /// Reads live device metrics from the native layer.
  Future<PerformanceMetrics> getPerformanceMetrics();
}
