import 'package:equatable/equatable.dart';
import '../../domain/entities/device_info.dart';

abstract class HomeState extends Equatable {
  const HomeState();

  @override
  List<Object?> get props => [];
}

class HomeInitial extends HomeState {}

class HomeLoading extends HomeState {}

class HomeLoaded extends HomeState {
  final DeviceInfoEntity deviceInfo;
  final int activeTweaksCount;
  final bool isBoosting;

  const HomeLoaded({
    required this.deviceInfo,
    required this.activeTweaksCount,
    this.isBoosting = false,
  });

  HomeLoaded copyWith({
    DeviceInfoEntity? deviceInfo,
    int? activeTweaksCount,
    bool? isBoosting,
  }) {
    return HomeLoaded(
      deviceInfo: deviceInfo ?? this.deviceInfo,
      activeTweaksCount: activeTweaksCount ?? this.activeTweaksCount,
      isBoosting: isBoosting ?? this.isBoosting,
    );
  }

  @override
  List<Object?> get props => [deviceInfo, activeTweaksCount, isBoosting];
}

class HomeError extends HomeState {
  final String message;

  const HomeError(this.message);

  @override
  List<Object?> get props => [message];
}
