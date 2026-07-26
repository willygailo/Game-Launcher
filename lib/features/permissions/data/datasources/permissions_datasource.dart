import '../../../../core/platform/permission_service.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../../../core/platform/shizuku_service.dart';
import '../../domain/entities/app_permissions.dart';

class PermissionsDatasource {
  final PermissionService permissionService;
  final RootCommandService rootCommandService;
  final ShizukuService shizukuService;

  PermissionsDatasource({
    required this.permissionService,
    required this.rootCommandService,
    required this.shizukuService,
  });

  Future<AppPermissions> checkPermissions() async {
    final hasRoot = await rootCommandService.isRootAvailable();
    final isShizukuAvail = await shizukuService.isShizukuAvailable();
    final hasShizukuAccess = isShizukuAvail && await shizukuService.isPermissionGranted();
    final hasStorage = await permissionService.hasStoragePermission();

    ExecutionMode mode = ExecutionMode.readOnly;
    if (hasRoot) {
      mode = ExecutionMode.root;
    } else if (hasShizukuAccess) {
      mode = ExecutionMode.shizuku;
    }

    return AppPermissions(
      isRooted: hasRoot,
      hasRootAccess: hasRoot,
      isShizukuAvailable: isShizukuAvail,
      hasShizukuAccess: hasShizukuAccess,
      hasStorageAccess: hasStorage,
      executionMode: mode,
    );
  }

  Future<bool> requestStoragePermission() =>
      permissionService.requestStoragePermission();

  Future<bool> requestShizukuPermission() =>
      shizukuService.requestPermission();
}
