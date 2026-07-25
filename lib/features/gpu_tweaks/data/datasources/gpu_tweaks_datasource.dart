import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/gpu_tweak.dart';

class GpuTweaksDatasource {
  final RootCommandService rootCommandService;
  GpuTweaksDatasource({required this.rootCommandService});

  List<GpuTweak> getGpuTweaks() {
    return TweakConstants.gpuTweaks
        .where((t) => !t.isReadOnly)
        .map(
          (t) => GpuTweak(
            key: t.key,
            value: t.tweakValue,
            description: t.description,
          ),
        )
        .toList();
  }

  Future<bool> applyTweak(GpuTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, tweak.value);

  Future<bool> revertTweak(GpuTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, '');
}
