import '../entities/performance_metrics.dart';
import '../repositories/performance_repository.dart';

class GetPerformanceMetrics {
  final PerformanceRepository repository;
  const GetPerformanceMetrics(this.repository);
  Future<PerformanceMetrics> call() => repository.getPerformanceMetrics();
}
