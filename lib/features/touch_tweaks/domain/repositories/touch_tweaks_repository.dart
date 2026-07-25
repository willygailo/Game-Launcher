import '../entities/touch_tweak.dart';

abstract class TouchTweaksRepository {
  Future<List<TouchTweak>> getTouchTweaks();
  Future<bool> applyTweak(TouchTweak tweak);
  Future<bool> revertTweak(TouchTweak tweak);
}
