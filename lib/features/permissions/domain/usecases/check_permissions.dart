import '../entities/app_permissions.dart';
import '../repositories/permissions_repository.dart';

class CheckPermissions {
  final PermissionsRepository repository;
  const CheckPermissions(this.repository);
  Future<AppPermissions> call() => repository.checkPermissions();
}
