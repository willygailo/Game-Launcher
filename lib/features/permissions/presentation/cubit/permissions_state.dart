import 'package:equatable/equatable.dart';
import '../../domain/entities/app_permissions.dart';

class PermissionsState extends Equatable {
  final bool isRootGranted;
  final bool isShizukuAvailable;
  final bool isShizukuGranted;
  final bool isWriteSettingsGranted;
  final ExecutionMode executionMode;

  const PermissionsState({
    required this.isRootGranted,
    required this.isShizukuAvailable,
    required this.isShizukuGranted,
    required this.isWriteSettingsGranted,
    required this.executionMode,
  });

  PermissionsState copyWith({
    bool? isRootGranted,
    bool? isShizukuAvailable,
    bool? isShizukuGranted,
    bool? isWriteSettingsGranted,
    ExecutionMode? executionMode,
  }) {
    return PermissionsState(
      isRootGranted: isRootGranted ?? this.isRootGranted,
      isShizukuAvailable: isShizukuAvailable ?? this.isShizukuAvailable,
      isShizukuGranted: isShizukuGranted ?? this.isShizukuGranted,
      isWriteSettingsGranted:
          isWriteSettingsGranted ?? this.isWriteSettingsGranted,
      executionMode: executionMode ?? this.executionMode,
    );
  }

  @override
  List<Object?> get props => [
        isRootGranted,
        isShizukuAvailable,
        isShizukuGranted,
        isWriteSettingsGranted,
        executionMode,
      ];
}
