// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get appTitle => 'GAME SPACE';

  @override
  String get dashboard => 'Panel Principal';

  @override
  String get metrics => 'Métricas';

  @override
  String get quickBoost => 'IMPULSO RÁPIDO';

  @override
  String get cpuTweaks => 'Ajustes de CPU';

  @override
  String get gpuTweaks => 'Ajustes de GPU';

  @override
  String get touchTweaks => 'Sensibilidad Táctil';

  @override
  String get networkTweaks => 'Red y Latencia';

  @override
  String get performance => 'Rendimiento';

  @override
  String get profiles => 'Perfiles de Juego';

  @override
  String get permissions => 'Permisos';

  @override
  String get settings => 'Ajustes';

  @override
  String get rootStatus => 'Estado de Acceso Root';

  @override
  String get rootGranted => 'ROOT CONCEDIDO';

  @override
  String get rootDenied => 'SIN ROOT (MODO INFO)';

  @override
  String get deviceInfo => 'Especificaciones del Dispositivo';

  @override
  String get manufacturer => 'Fabricante';

  @override
  String get chipset => 'Procesador';

  @override
  String get cpuCores => 'Núcleos CPU';

  @override
  String get ramTotal => 'RAM Total';

  @override
  String get activeTweaks => 'Ajustes Activos';

  @override
  String get applyAllTweaks => 'Aplicar Ajustes';

  @override
  String get resetTweaks => 'Restablecer';

  @override
  String get language => 'Idioma';

  @override
  String get theme => 'Tema';

  @override
  String get cpuOptimizations => 'OPTIMIZACIONES DE CPU';

  @override
  String get cpuGovernorMode => 'MODO GOVERNOR DE CPU';

  @override
  String get cpuGovernorDesc =>
      'Seleccione el modo governor para definir cómo escala la frecuencia de la CPU.';

  @override
  String get setpropCpuTweaks => 'AJUSTES SETPROP DE CPU';

  @override
  String get gpuGraphicsTweaks => 'AJUSTES DE GPU Y GRÁFICOS';

  @override
  String get surfaceFlingerComposition => 'COMPOSICIÓN SURFACEFLINGER';

  @override
  String get surfaceFlingerDesc =>
      'Forzar composición por GPU para liberar la carga del procesador.';

  @override
  String get setpropGpuTweaks => 'AJUSTES SETPROP DE GPU';

  @override
  String get touchResponsiveness => 'RESPUESTA TÁCTIL';

  @override
  String get touchSamplingRate => 'DESLIZADOR DE TASA DE MUESTREO';

  @override
  String samplingRateLabel(Object rate) {
    return 'Tasa de Muestreo: $rate Hz';
  }

  @override
  String get latencyTestPad => 'PRUEBA DE LATENCIA (TOCA AQUÍ)';

  @override
  String get tapAnywhere => 'Toca en cualquier parte del recuadro';

  @override
  String registeredTaps(Object count) {
    return 'Toques Registrados: $count';
  }

  @override
  String lastPos(Object x, Object y) {
    return 'Última Pos: ($x, $y)';
  }

  @override
  String get setpropTouchTweaks => 'AJUSTES SETPROP TÁCTILES';

  @override
  String get networkLatency => 'RED Y LATENCIA';

  @override
  String get gamingDnsSelector => 'SELECTOR DE RESOLVEDOR DNS PARA JUEGOS';

  @override
  String activeResolver(Object dns) {
    return 'Resolvedor Activo: $dns';
  }

  @override
  String get setpropNetworkTweaks => 'AJUSTES SETPROP DE RED';

  @override
  String get systemPerformanceTweaks => 'AJUSTES DE RENDIMIENTO DEL SISTEMA';

  @override
  String get systemDalvikTweaks => 'AJUSTES DE SISTEMA Y DALVIK VM';

  @override
  String get gameTweakProfiles => 'PERFILES DE AJUSTES DE JUEGO';

  @override
  String exportedMagisk(Object path) {
    return 'Zip de Magisk exportado a:\n$path';
  }

  @override
  String get failedExportMagisk => 'Error al exportar Zip de Magisk.';

  @override
  String get active => 'ACTIVO';

  @override
  String containsTweaks(Object count) {
    return 'Contiene $count configuraciones setprop';
  }

  @override
  String get applyProfile => 'APLICAR PERFIL';

  @override
  String get profileApplied => 'PERFIL APLICADO';

  @override
  String get exportMagiskZip => 'Exportar Zip de Magisk';

  @override
  String get systemPermissions => 'PERMISOS DEL SISTEMA';

  @override
  String get rootAccessSu => 'Acceso Root (binario SU)';

  @override
  String get rootGrantedDesc => 'Concedido - Ajustes setprop desbloqueados';

  @override
  String get rootDeniedDesc => 'Denegado - Ejecutando en Modo Lectura/Info';

  @override
  String get requestRootAccess => 'SOLICITAR ACCESO ROOT';

  @override
  String get cpuLoad => 'Carga de CPU';

  @override
  String get ramUsage => 'Uso de RAM';

  @override
  String get batteryPercent => 'Batería %';

  @override
  String get optimizationModules => 'MÓDULOS DE OPTIMIZACIÓN';

  @override
  String errorLoadingDevice(Object message) {
    return 'Error al cargar datos del dispositivo: $message';
  }

  @override
  String get cores => 'Núcleos';

  @override
  String get appVersion => 'GAME SPACE PRO v1.0.0';

  @override
  String get supportedDevices =>
      'Compatible con Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).';
}
