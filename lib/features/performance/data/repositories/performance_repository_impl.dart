import '../../domain/entities/performance_metrics.dart';
import '../../domain/repositories/performance_repository.dart';
import '../datasources/performance_datasource.dart';

class PerformanceRepositoryImpl implements PerformanceRepository {
  final PerformanceDatasource datasource;
  PerformanceRepositoryImpl({required this.datasource});

  @override
  Future<PerformanceMetrics> getPerformanceMetrics() =>
      datasource.getPerformanceMetrics();
}
