import '../../domain/entities/app_permissions.dart';
import '../../domain/repositories/permissions_repository.dart';
import '../datasources/permissions_datasource.dart';

class PermissionsRepositoryImpl implements PermissionsRepository {
  final PermissionsDatasource datasource;
  PermissionsRepositoryImpl({required this.datasource});

  @override
  Future<AppPermissions> checkPermissions() => datasource.checkPermissions();

  @override
  Future<bool> requestStoragePermission() =>
      datasource.requestStoragePermission();

  @override
  Future<bool> requestShizukuPermission() =>
      datasource.requestShizukuPermission();
}
