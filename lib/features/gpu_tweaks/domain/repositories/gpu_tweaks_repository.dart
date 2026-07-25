import '../entities/gpu_tweak.dart';

abstract class GpuTweaksRepository {
  Future<List<GpuTweak>> getGpuTweaks();
  Future<bool> applyTweak(GpuTweak tweak);
  Future<bool> revertTweak(GpuTweak tweak);
}
