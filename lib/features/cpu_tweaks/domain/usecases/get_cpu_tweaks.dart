import '../entities/cpu_tweak.dart';
import '../repositories/cpu_tweaks_repository.dart';

/// Use case: Fetch all available CPU tweaks.
class GetCpuTweaks {
  final CpuTweaksRepository repository;

  const GetCpuTweaks(this.repository);

  Future<List<CpuTweak>> call() => repository.getCpuTweaks();
}
