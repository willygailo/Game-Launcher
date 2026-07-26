import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/platform/permission_service.dart';
import '../../../../core/platform/shizuku_service.dart';
import '../../domain/entities/app_permissions.dart';
import 'permissions_state.dart';

class PermissionsCubit extends Cubit<PermissionsState> {
  final PermissionService permissionService;
  final ShizukuService shizukuService;

  PermissionsCubit({
    required this.permissionService,
    required this.shizukuService,
  }) : super(const PermissionsState(
          isRootGranted: false,
          isShizukuAvailable: false,
          isShizukuGranted: false,
          isWriteSettingsGranted: true,
          executionMode: ExecutionMode.readOnly,
        ));

  Future<void> checkPermissions() async {
    final hasRoot = await permissionService.checkRootPermission();
    final isShizukuAvail = await shizukuService.isShizukuAvailable();
    final isShizukuGranted =
        isShizukuAvail && await shizukuService.isPermissionGranted();

    ExecutionMode mode = ExecutionMode.readOnly;
    if (hasRoot) {
      mode = ExecutionMode.root;
    } else if (isShizukuGranted) {
      mode = ExecutionMode.shizuku;
    }

    emit(state.copyWith(
      isRootGranted: hasRoot,
      isShizukuAvailable: isShizukuAvail,
      isShizukuGranted: isShizukuGranted,
      executionMode: mode,
    ));
  }

  Future<void> requestRoot() async {
    final granted = await permissionService.requestRootPermission();
    await checkPermissions();
  }

  Future<void> requestShizuku() async {
    await shizukuService.requestPermission();
    await checkPermissions();
  }
}
