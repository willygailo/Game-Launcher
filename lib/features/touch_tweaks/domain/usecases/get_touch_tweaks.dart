import '../entities/touch_tweak.dart';
import '../repositories/touch_tweaks_repository.dart';

class GetTouchTweaks {
  final TouchTweaksRepository repository;
  const GetTouchTweaks(this.repository);
  Future<List<TouchTweak>> call() => repository.getTouchTweaks();
}
