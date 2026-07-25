import '../../domain/entities/gpu_tweak.dart';
import '../../domain/repositories/gpu_tweaks_repository.dart';
import '../datasources/gpu_tweaks_datasource.dart';

class GpuTweaksRepositoryImpl implements GpuTweaksRepository {
  final GpuTweaksDatasource datasource;
  GpuTweaksRepositoryImpl({required this.datasource});

  @override
  Future<List<GpuTweak>> getGpuTweaks() async => datasource.getGpuTweaks();

  @override
  Future<bool> applyTweak(GpuTweak tweak) => datasource.applyTweak(tweak);

  @override
  Future<bool> revertTweak(GpuTweak tweak) => datasource.revertTweak(tweak);
}
