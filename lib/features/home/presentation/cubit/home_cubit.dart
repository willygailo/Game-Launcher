import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/usecases/get_device_info.dart';
import '../../../../core/platform/root_command_service.dart';
import '../../../../core/constants/tweak_constants.dart';
import 'home_state.dart';

class HomeCubit extends Cubit<HomeState> {
  final GetDeviceInfo getDeviceInfo;
  final RootCommandService rootCommandService;

  HomeCubit({
    required this.getDeviceInfo,
    required this.rootCommandService,
  }) : super(HomeInitial());

  Future<void> loadDashboard() async {
    emit(HomeLoading());
    try {
      final info = await getDeviceInfo();
      emit(HomeLoaded(
        deviceInfo: info,
        activeTweaksCount: 0,
      ));
    } catch (e) {
      emit(HomeError(e.toString()));
    }
  }

  Future<void> triggerQuickBoost() async {
    if (state is HomeLoaded) {
      final currentState = state as HomeLoaded;
      emit(currentState.copyWith(isBoosting: true));

      // Build batch tweaks map from GPU, CPU, Touch, Network
      final Map<String, String> batchMap = {};
      for (var t in TweakConstants.gpuTweaks) {
        batchMap[t.key] = t.tweakValue;
      }
      for (var t in TweakConstants.touchTweaks) {
        batchMap[t.key] = t.tweakValue;
      }
      for (var t in TweakConstants.cpuTweaks) {
        batchMap[t.key] = t.tweakValue;
      }

      final appliedCount = await rootCommandService.executeBatchTweaks(batchMap);

      emit(currentState.copyWith(
        activeTweaksCount: appliedCount > 0 ? appliedCount : batchMap.length,
        isBoosting: false,
      ));
    }
  }
}
