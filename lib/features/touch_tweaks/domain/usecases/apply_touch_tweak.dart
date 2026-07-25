import '../entities/touch_tweak.dart';
import '../repositories/touch_tweaks_repository.dart';

class ApplyTouchTweak {
  final TouchTweaksRepository repository;
  const ApplyTouchTweak(this.repository);
  Future<bool> call(TouchTweak tweak) => repository.applyTweak(tweak);
}
