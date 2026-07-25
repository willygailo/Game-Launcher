import '../entities/gpu_tweak.dart';
import '../repositories/gpu_tweaks_repository.dart';

class ApplyGpuTweak {
  final GpuTweaksRepository repository;
  const ApplyGpuTweak(this.repository);
  Future<bool> call(GpuTweak tweak) => repository.applyTweak(tweak);
}
