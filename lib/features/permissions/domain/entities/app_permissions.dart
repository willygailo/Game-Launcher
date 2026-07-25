import 'package:equatable/equatable.dart';

/// Entity representing the current permission / root-access status.
class AppPermissions extends Equatable {
  final bool isRooted;
  final bool hasRootAccess;
  final bool hasStorageAccess;

  const AppPermissions({
    required this.isRooted,
    required this.hasRootAccess,
    required this.hasStorageAccess,
  });

  @override
  List<Object?> get props => [isRooted, hasRootAccess, hasStorageAccess];
}
