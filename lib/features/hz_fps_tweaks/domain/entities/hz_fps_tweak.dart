import 'package:equatable/equatable.dart';

class HzFpsInfo extends Equatable {
  final double currentHz;
  final List<double> supportedRates;
  final double selectedHz;
  final bool isThermalBypassActive;
  final bool isGameModeActive;

  const HzFpsInfo({
    required this.currentHz,
    required this.supportedRates,
    required this.selectedHz,
    required this.isThermalBypassActive,
    required this.isGameModeActive,
  });

  @override
  List<Object?> get props => [
        currentHz,
        supportedRates,
        selectedHz,
        isThermalBypassActive,
        isGameModeActive,
      ];
}
