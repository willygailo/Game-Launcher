import '../repositories/permissions_repository.dart';

class RequestStoragePermission {
  final PermissionsRepository repository;
  const RequestStoragePermission(this.repository);
  Future<bool> call() => repository.requestStoragePermission();
}
