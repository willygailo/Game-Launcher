import '../entities/app_permissions.dart';

abstract class PermissionsRepository {
  Future<AppPermissions> checkPermissions();
  Future<bool> requestStoragePermission();
}
