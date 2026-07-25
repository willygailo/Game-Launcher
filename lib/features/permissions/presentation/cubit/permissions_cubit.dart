import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/platform/permission_service.dart';
import 'permissions_state.dart';

class PermissionsCubit extends Cubit<PermissionsState> {
  final PermissionService permissionService;

  PermissionsCubit(this.permissionService)
      : super(const PermissionsState(
          isRootGranted: false,
          isWriteSettingsGranted: true,
        ));

  Future<void> checkPermissions() async {
    final hasRoot = await permissionService.checkRootPermission();
    emit(state.copyWith(isRootGranted: hasRoot));
  }

  Future<void> requestRoot() async {
    final granted = await permissionService.requestRootPermission();
    emit(state.copyWith(isRootGranted: granted));
  }
}
