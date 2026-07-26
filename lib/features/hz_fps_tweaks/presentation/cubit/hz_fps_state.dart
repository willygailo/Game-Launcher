import 'package:equatable/equatable.dart';

abstract class HzFpsState extends Equatable {
  const HzFpsState();

  @override
  List<Object?> get props => [];
}

class HzFpsInitial extends HzFpsState {}

class HzFpsLoading extends HzFpsState {}

class HzFpsLoaded extends HzFpsState {
  final double currentHz;
  final List<double> supportedRates;
  final double activeHz;
  final bool isThermalBypassActive;
  final bool isGameModeActive;
  final bool isApplying;
  final String? message;

  const HzFpsLoaded({
    required this.currentHz,
    required this.supportedRates,
    required this.activeHz,
    this.isThermalBypassActive = false,
    this.isGameModeActive = false,
    this.isApplying = false,
    this.message,
  });

  HzFpsLoaded copyWith({
    double? currentHz,
    List<double>? supportedRates,
    double? activeHz,
    bool? isThermalBypassActive,
    bool? isGameModeActive,
    bool? isApplying,
    String? message,
  }) {
    return HzFpsLoaded(
      currentHz: currentHz ?? this.currentHz,
      supportedRates: supportedRates ?? this.supportedRates,
      activeHz: activeHz ?? this.activeHz,
      isThermalBypassActive:
          isThermalBypassActive ?? this.isThermalBypassActive,
      isGameModeActive: isGameModeActive ?? this.isGameModeActive,
      isApplying: isApplying ?? this.isApplying,
      message: message,
    );
  }

  @override
  List<Object?> get props => [
        currentHz,
        supportedRates,
        activeHz,
        isThermalBypassActive,
        isGameModeActive,
        isApplying,
        message,
      ];
}

class HzFpsError extends HzFpsState {
  final String message;
  const HzFpsError(this.message);

  @override
  List<Object?> get props => [message];
}
