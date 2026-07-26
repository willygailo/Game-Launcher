import 'package:flutter_test/flutter_test.dart';
import 'package:game_space/features/hz_fps_tweaks/presentation/cubit/hz_fps_state.dart';

void main() {
  group('HzFpsState', () {
    test('HzFpsLoaded copyWith creates new instance with updated properties', () {
      const loaded = HzFpsLoaded(
        currentHz: 60.0,
        supportedRates: [60.0, 90.0, 120.0],
        activeHz: 60.0,
        isThermalBypassActive: false,
      );

      final updated = loaded.copyWith(
        activeHz: 120.0,
        isThermalBypassActive: true,
        message: 'Locked to 120Hz',
      );

      expect(updated.activeHz, equals(120.0));
      expect(updated.isThermalBypassActive, isTrue);
      expect(updated.message, equals('Locked to 120Hz'));
      expect(updated.currentHz, equals(60.0));
    });
  });
}
