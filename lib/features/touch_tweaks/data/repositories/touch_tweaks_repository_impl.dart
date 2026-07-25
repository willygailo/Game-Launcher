import '../../domain/entities/touch_tweak.dart';
import '../../domain/repositories/touch_tweaks_repository.dart';
import '../datasources/touch_tweaks_datasource.dart';

class TouchTweaksRepositoryImpl implements TouchTweaksRepository {
  final TouchTweaksDatasource datasource;
  TouchTweaksRepositoryImpl({required this.datasource});

  @override
  Future<List<TouchTweak>> getTouchTweaks() async =>
      datasource.getTouchTweaks();

  @override
  Future<bool> applyTweak(TouchTweak tweak) => datasource.applyTweak(tweak);

  @override
  Future<bool> revertTweak(TouchTweak tweak) => datasource.revertTweak(tweak);
}
