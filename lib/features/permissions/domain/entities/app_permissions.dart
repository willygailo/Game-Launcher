import 'package:equatable/equatable.dart';

enum ExecutionMode { root, shizuku, readOnly }

/// Entity representing the current permission, root, and Shizuku status.
class AppPermissions extends Equatable {
  final bool isRooted;
  final bool hasRootAccess;
  final bool isShizukuAvailable;
  final bool hasShizukuAccess;
  final bool hasStorageAccess;
  final ExecutionMode executionMode;

  const AppPermissions({
    required this.isRooted,
    required this.hasRootAccess,
    required this.isShizukuAvailable,
    required this.hasShizukuAccess,
    required this.hasStorageAccess,
    required this.executionMode,
  });

  @override
  List<Object?> get props => [
        isRooted,
        hasRootAccess,
        isShizukuAvailable,
        hasShizukuAccess,
        hasStorageAccess,
        executionMode,
      ];
}
