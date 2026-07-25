import '../entities/gpu_tweak.dart';
import '../repositories/gpu_tweaks_repository.dart';

class GetGpuTweaks {
  final GpuTweaksRepository repository;
  const GetGpuTweaks(this.repository);
  Future<List<GpuTweak>> call() => repository.getGpuTweaks();
}
