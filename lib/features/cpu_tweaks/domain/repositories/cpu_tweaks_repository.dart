import '../entities/cpu_tweak.dart';

/// Abstract repository contract for CPU tweak operations.
abstract class CpuTweaksRepository {
  /// Returns all predefined CPU tweaks for the current chipset.
  Future<List<CpuTweak>> getCpuTweaks();

  /// Applies the given [tweak] via root shell.
  Future<bool> applyTweak(CpuTweak tweak);

  /// Reverts the given [tweak] to its default value.
  Future<bool> revertTweak(CpuTweak tweak);
}
