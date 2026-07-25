import 'package:equatable/equatable.dart';

class PermissionsState extends Equatable {
  final bool isRootGranted;
  final bool isWriteSettingsGranted;

  const PermissionsState({
    required this.isRootGranted,
    required this.isWriteSettingsGranted,
  });

  PermissionsState copyWith({
    bool? isRootGranted,
    bool? isWriteSettingsGranted,
  }) {
    return PermissionsState(
      isRootGranted: isRootGranted ?? this.isRootGranted,
      isWriteSettingsGranted: isWriteSettingsGranted ?? this.isWriteSettingsGranted,
    );
  }

  @override
  List<Object?> get props => [isRootGranted, isWriteSettingsGranted];
}
