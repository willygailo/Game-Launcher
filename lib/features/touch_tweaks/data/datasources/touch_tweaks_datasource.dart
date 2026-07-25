import '../../../../core/constants/tweak_constants.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/touch_tweak.dart';

class TouchTweaksDatasource {
  final RootCommandService rootCommandService;
  TouchTweaksDatasource({required this.rootCommandService});

  List<TouchTweak> getTouchTweaks() {
    return TweakConstants.touchTweaks
        .where((t) => !t.isReadOnly)
        .map(
          (t) => TouchTweak(
            key: t.key,
            value: t.tweakValue,
            description: t.description,
          ),
        )
        .toList();
  }

  Future<bool> applyTweak(TouchTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, tweak.value);

  Future<bool> revertTweak(TouchTweak tweak) =>
      rootCommandService.setSystemProperty(tweak.key, '');
}
