import '../entities/cpu_tweak.dart';
import '../repositories/cpu_tweaks_repository.dart';

/// Use case: Apply a single CPU tweak via root shell.
class ApplyCpuTweak {
  final CpuTweaksRepository repository;

  const ApplyCpuTweak(this.repository);

  Future<bool> call(CpuTweak tweak) => repository.applyTweak(tweak);
}
