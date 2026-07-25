import '../../../../core/platform/permission_service.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../domain/entities/app_permissions.dart';

class PermissionsDatasource {
  final PermissionService permissionService;
  final RootCommandService rootCommandService;

  PermissionsDatasource({
    required this.permissionService,
    required this.rootCommandService,
  });

  Future<AppPermissions> checkPermissions() async {
    final hasRoot = await rootCommandService.isRootAvailable();
    final hasStorage = await permissionService.hasStoragePermission();
    return AppPermissions(
      isRooted: hasRoot,
      hasRootAccess: hasRoot,
      hasStorageAccess: hasStorage,
    );
  }

  Future<bool> requestStoragePermission() =>
      permissionService.requestStoragePermission();
}

