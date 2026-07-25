import '../../domain/entities/cpu_tweak.dart';
import '../../domain/repositories/cpu_tweaks_repository.dart';
import '../datasources/cpu_tweaks_datasource.dart';

/// Concrete implementation of [CpuTweaksRepository] backed by
/// [CpuTweaksDatasource].
class CpuTweaksRepositoryImpl implements CpuTweaksRepository {
  final CpuTweaksDatasource datasource;

  CpuTweaksRepositoryImpl({required this.datasource});

  @override
  Future<List<CpuTweak>> getCpuTweaks() async => datasource.getCpuTweaks();

  @override
  Future<bool> applyTweak(CpuTweak tweak) => datasource.applyTweak(tweak);

  @override
  Future<bool> revertTweak(CpuTweak tweak) => datasource.revertTweak(tweak);
}
