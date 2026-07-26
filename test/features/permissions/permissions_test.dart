import 'package:flutter_test/flutter_test.dart';
import 'package:game_space/features/permissions/domain/entities/app_permissions.dart';
import 'package:game_space/features/permissions/presentation/cubit/permissions_state.dart';

void main() {
  group('AppPermissions Entity', () {
    test('supports value equality', () {
      const p1 = AppPermissions(
        isRooted: true,
        hasRootAccess: true,
        isShizukuAvailable: false,
        hasShizukuAccess: false,
        hasStorageAccess: true,
        executionMode: ExecutionMode.root,
      );

      const p2 = AppPermissions(
        isRooted: true,
        hasRootAccess: true,
        isShizukuAvailable: false,
        hasShizukuAccess: false,
        hasStorageAccess: true,
        executionMode: ExecutionMode.root,
      );

      expect(p1, equals(p2));
    });

    test('correctly identifies Shizuku mode', () {
      const p = AppPermissions(
        isRooted: false,
        hasRootAccess: false,
        isShizukuAvailable: true,
        hasShizukuAccess: true,
        hasStorageAccess: true,
        executionMode: ExecutionMode.shizuku,
      );

      expect(p.executionMode, equals(ExecutionMode.shizuku));
    });
  });

  group('PermissionsState', () {
    test('copyWith updates state correctly', () {
      const initial = PermissionsState(
        isRootGranted: false,
        isShizukuAvailable: true,
        isShizukuGranted: false,
        isWriteSettingsGranted: true,
        executionMode: ExecutionMode.readOnly,
      );

      final updated = initial.copyWith(
        isShizukuGranted: true,
        executionMode: ExecutionMode.shizuku,
      );

      expect(updated.isShizukuGranted, isTrue);
      expect(updated.executionMode, equals(ExecutionMode.shizuku));
      expect(updated.isRootGranted, isFalse);
    });
  });
}
