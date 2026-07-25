import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/cpu_tweak.dart';

/// Datasource that reads CPU tweak definitions from [TweakConstants]
/// and executes them through [RootCommandService].
class CpuTweaksDatasource {
  final RootCommandService rootCommandService;

  CpuTweaksDatasource({required this.rootCommandService});

  List<CpuTweak> getCpuTweaks() {
    return TweakConstants.cpuTweaks
        .where((t) => !t.isReadOnly)
        .map(
          (t) => CpuTweak(
            key: t.key,
            value: t.tweakValue,
            description: t.description,
          ),
        )
        .toList();
  }

  Future<bool> applyTweak(CpuTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, tweak.value);

  Future<bool> revertTweak(CpuTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, '');
}
