// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Arabic (`ar`).
class AppLocalizationsAr extends AppLocalizations {
  AppLocalizationsAr([String locale = 'ar']) : super(locale);

  @override
  String get appTitle => 'مساحة الألعاب';

  @override
  String get dashboard => 'لوحة التحكم';

  @override
  String get metrics => 'المقاييس';

  @override
  String get quickBoost => 'تعزيز سريع';

  @override
  String get cpuTweaks => 'تحسينات المعالج';

  @override
  String get gpuTweaks => 'تحسينات كارت الشاشة';

  @override
  String get touchTweaks => 'حساسية اللمس';

  @override
  String get networkTweaks => 'الشبكة وزمن الاستجابة';

  @override
  String get performance => 'الأداء';

  @override
  String get profiles => 'ملفات الألعاب';

  @override
  String get permissions => 'الأذونات';

  @override
  String get settings => 'الإعدادات';

  @override
  String get rootStatus => 'حالة صلاحيات الروت';

  @override
  String get rootGranted => 'تم منح صلاحية الروت';

  @override
  String get rootDenied => 'بدون روت (وضع العرض فقط)';

  @override
  String get deviceInfo => 'مواصفات الجهاز';

  @override
  String get manufacturer => 'الشركة المصنعة';

  @override
  String get chipset => 'المعالج الرئيسي';

  @override
  String get cpuCores => 'أنوية المعالج';

  @override
  String get ramTotal => 'إجمالي الذاكرة العشوائية';

  @override
  String get activeTweaks => 'التحسينات النشطة';

  @override
  String get applyAllTweaks => 'تطبيق تحسينات الألعاب';

  @override
  String get resetTweaks => 'إعادة الإعدادات الافتراضية';

  @override
  String get language => 'اللغة';

  @override
  String get theme => 'المظهر';

  @override
  String get cpuOptimizations => 'تحسينات المعالج';

  @override
  String get cpuGovernorMode => 'وضع حاكم المعالج';

  @override
  String get cpuGovernorDesc =>
      'اختر وضع الحاكم لتحديد كيفية تغيير تردد المعالج تحت الحمل.';

  @override
  String get setpropCpuTweaks => 'تحسينات SETPROP للمعالج';

  @override
  String get gpuGraphicsTweaks => 'تحسينات كارت الشاشة والرسوميات';

  @override
  String get surfaceFlingerComposition => 'تكوين SURFACEFLINGER';

  @override
  String get surfaceFlingerDesc =>
      'فرض تكوين كارت الشاشة لتخفيف العبء عن المعالج.';

  @override
  String get setpropGpuTweaks => 'تحسينات SETPROP لكارت الشاشة';

  @override
  String get touchResponsiveness => 'استجابة اللمس';

  @override
  String get touchSamplingRate => 'معدل استجابة اللمس';

  @override
  String samplingRateLabel(Object rate) {
    return 'معدل الاستجابة: $rate هرتز';
  }

  @override
  String get latencyTestPad => 'لوحة اختبار الاستجابة (اضغط هنا)';

  @override
  String get tapAnywhere => 'اضغط في أي مكان داخل الصندوق';

  @override
  String registeredTaps(Object count) {
    return 'الضغطات المسجلة: $count';
  }

  @override
  String lastPos(Object x, Object y) {
    return 'آخر موضع: ($x, $y)';
  }

  @override
  String get setpropTouchTweaks => 'تحسينات SETPROP لحساسية اللمس';

  @override
  String get networkLatency => 'الشبكة وزمن الاستجابة';

  @override
  String get gamingDnsSelector => 'محدد محول خادم DNS للألعاب';

  @override
  String activeResolver(Object dns) {
    return 'المحول النشط: $dns';
  }

  @override
  String get setpropNetworkTweaks => 'تحسينات SETPROP لشبكة الاتصال';

  @override
  String get systemPerformanceTweaks => 'تحسينات أداء النظام';

  @override
  String get systemDalvikTweaks => 'تحسينات النظام والـ Dalvik VM';

  @override
  String get gameTweakProfiles => 'ملفات تحسينات الألعاب';

  @override
  String exportedMagisk(Object path) {
    return 'تم تصدير ملف Magisk إلى:\n$path';
  }

  @override
  String get failedExportMagisk => 'فشل تصدير ملف Magisk.';

  @override
  String get active => 'نشط';

  @override
  String containsTweaks(Object count) {
    return 'يحتوي على $count من إعدادات setprop';
  }

  @override
  String get applyProfile => 'تطبيق الملف الشخصي';

  @override
  String get profileApplied => 'تم تطبيق الملف الشخصي';

  @override
  String get exportMagiskZip => 'تصدير ملف Magisk';

  @override
  String get systemPermissions => 'أذونات النظام';

  @override
  String get rootAccessSu => 'صلاحية الروت (SU)';

  @override
  String get rootGrantedDesc => 'ممنوحة - تم فتح تحسينات setprop';

  @override
  String get rootDeniedDesc => 'مرفوضة - يعمل في وضع القراءة فقط';

  @override
  String get requestRootAccess => 'طلب صلاحيات الروت';

  @override
  String get cpuLoad => 'حمل المعالج';

  @override
  String get ramUsage => 'استهلاك الذاكرة';

  @override
  String get batteryPercent => 'البطارية %';

  @override
  String get optimizationModules => 'وحدات التحسين';

  @override
  String errorLoadingDevice(Object message) {
    return 'خطأ في تحميل معلومات الجهاز: $message';
  }

  @override
  String get cores => 'أنوية';

  @override
  String get appVersion => 'GAME SPACE PRO v1.0.0';

  @override
  String get supportedDevices =>
      'يدعم Infinix, Tecno, Samsung, Xiaomi, Realme, Pixel (MediaTek, Unisoc, Snapdragon, Exynos, Tensor).';
}
