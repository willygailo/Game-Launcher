// =============================================================================
// Universal Combat & Cross-Game Overdrive Injector
// High-performance isolated translation unit for GameBooster Native
// =============================================================================

#include "native_config_injector.h"
#include "config_common.h"

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat mult, jfloat hsMult, jint crit) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string multStr = std::to_string(mult > 0 ? mult : 1.5f);
    std::string hsStr = std::to_string(hsMult > 0 ? hsMult : 2.0f);
    std::string critStr = std::to_string(crit > 0 ? crit : 100);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageBoost", "1"}, {"DamageMultiplier", multStr}, {"DamageLockMax", "1"},
        {"HeadshotMultiplier", hsStr}, {"CritRateBoost", critStr}, {"EffectiveDPSMode", "3"},
        {"PenetrationBoost", "1"}, {"TrueDamageBoost", "1"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"FrameSyncDamage", "1"}, {"TouchPollingRate", "1000"},
        {"r.PUBGDamageLockMax", "1"}, {"r.PUBGDamageBoost", "1"}, {"r.PUBGHeadshotMultiplier", hsStr}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "DamageBoost");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat recoilScale, jint stability) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ZeroRecoil", "1"}, {"RecoilScale", "0"}, {"VerticalRecoilScale", "0"},
        {"HorizontalRecoilScale", "0"}, {"RecoilPatternScale", "0"}, {"WeaponSpread", "0"},
        {"WeaponSway", "0"}, {"BulletSpreadScale", "0"}, {"SpreadDecayRate", "10"},
        {"MuzzleSpread", "0"}, {"MovingSpreadFactor", "0"}, {"RecoilControlAssist", "1"},
        {"r.WeaponRecoilScale", "0"}, {"r.VerticalRecoilScale", "0"}, {"r.HorizontalRecoilScale", "0"},
        {"r.RecoilPatternScale", "0"}, {"r.WeaponSpread", "0"}, {"r.WeaponSway", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ZeroRecoil");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *env, jclass, jstring jPath, jint strength, jint precision) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimAssistEnabled", "1"}, {"AimAssistStrength", "100"}, {"AimMagnetism", "3"},
        {"AimAssistLockMax", "1"}, {"HeadMagnetism", "1"}, {"HeadBoneAimPriority", "1"},
        {"AimSnapSpeed", "10"}, {"AimSnapThreshold", "0"}, {"AimSmoothFactor", "0"},
        {"AdsZeroDelay", "1"}, {"PredictiveAim", "1"}, {"HeroLock", "1"}, {"SkillSmartAim", "1"},
        {"r.AimAssistEnabled", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"},
        {"r.AimSnapThreshold", "0"}, {"r.HeadBoneAimPriority", "1"}, {"r.PredictiveAim", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "AimAssist");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *env, jclass, jstring jPath, jfloat trackingStrength, jfloat hitboxMultiplier) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string hbStr = std::to_string(hitboxMultiplier > 0 ? hitboxMultiplier : 3.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"TrackingBullet", "1"}, {"TrackingStrength", "1000"}, {"BulletMagnetism", "1"},
        {"HitboxMultiplier", hbStr}, {"HitboxScale", hbStr}, {"BulletVelocityComp", "1"},
        {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"}, {"HitRegistrationRate", "1000"},
        {"ZeroBulletDrop", "1"}, {"BulletDropComp", "1"}, {"WeaponSpread", "0"},
        {"BulletSpreadScale", "0"}, {"MuzzleVelocityFactor", "1.0"}, {"TrueDamageBoost", "1"},
        {"r.PUBGBulletVelocityCompensation", "1"}, {"r.PUBGInstantHitReg", "1"},
        {"r.BulletSpreadScale", "0"}, {"r.WeaponSpread", "0"}, {"r.MuzzleVelocityFactor", "1.0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "TrackingBullet");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *env, jclass, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string defStr = std::to_string(defBoost > 0 ? defBoost : 3000.0f);
    std::string redStr = std::to_string(dmgReduction > 0 ? dmgReduction : 0.99f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ArmorDefBoost", "1"}, {"ArmorDefense", defStr}, {"PhysicalDefense", defStr},
        {"MagicDefense", defStr}, {"DamageReduction", redStr}, {"DamageReductionBypassImmune", "1"},
        {"ShieldMultiplier", "3.0"}, {"LifestealBoost", "1"}, {"SpellVampBoost", "1"},
        {"bFramePacingEnabled", "True"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ArmorDef");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSpeedBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat speedMultiplier, jfloat sprintBoost) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string spdStr = std::to_string(speedMultiplier > 0 ? speedMultiplier : 1.5f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"MovementSpeedBoost", "1"}, {"SpeedMultiplier", spdStr}, {"SprintSpeedMax", "1"},
        {"SlideDistanceMax", "1"}, {"SlideSpeedBoost", "1"}, {"JumpHeightBoost", "1"},
        {"FastTacticalSprint", "1"}, {"SprintDelayZero", "1"}, {"JoystickZeroDeadzone", "1"},
        {"JoystickResponseLevel", "3"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "SpeedBoost");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroDamage1000
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat, jint, jint) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "1"}, {"DamageBoost", "1"}, {"EffectiveDPSMode", "3"},
        {"HeroBaseDamageMultiplier", "1"}, {"HeroSkillDamageMultiplier", "1"},
        {"HeroUltimateDamageMult", "1"}, {"HeroPassiveDamageMult", "1"},
        {"HeroCritDamageMult", "1"}, {"HeroMagicDamageMult", "1"}, {"HeroPhysicalDamageMult", "1"},
        {"HeroTrueDamageMult", "1"}, {"PenetrationBoost", "1"}, {"CritRateBoost", "1"},
        {"CritDamageMultiplier", "3.0"}, {"HeadshotMultiplier", "2.0"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"HitRegistrationRate", "1000"}, {"FrameSyncDamage", "1"},
        {"TrueDamageBoost", "1"}, {"DamageReductionBypass", "1"}, {"SpellVampBoost", "1"},
        {"LifestealBoost", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"r.PUBGDamageLockMax", "1"}, {"r.PUBGDamageBoost", "1"}, {"r.PUBGTrueDamageMod", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "HeroDamage1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat, jint) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ScopeZeroRecoil", "1"}, {"RecoilScale", "0"}, {"VerticalRecoilScale", "0"},
        {"HorizontalRecoilScale", "0"}, {"RecoilPatternScale", "0"}, {"WeaponSpread", "0"},
        {"WeaponSway", "0"}, {"BulletSpreadScale", "0"}, {"Scope2xStabilizer", "1"},
        {"Scope3xStabilizer", "1"}, {"Scope4xStabilizer", "1"}, {"Scope6xStabilizer", "1"},
        {"Scope8xStabilizer", "1"}, {"ScopeZeroSway", "1"}, {"ScopeBreathingDamp", "1"},
        {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"r.WeaponRecoilScale", "0"}, {"r.WeaponSpread", "0"}, {"r.WeaponSway", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ScopeZeroRecoil");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist1000
  (JNIEnv *env, jclass, jstring jPath, jint strength, jfloat precision) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimAssistLockMax", "1"}, {"AimAssistEnabled", "1"}, {"AimAssistStrength", "1000"},
        {"AimMagnetism", "1000"}, {"AimMagnetismLevel", "10"}, {"LockOnRange", "1.0"},
        {"AimSnapSpeed", "10"}, {"AimSnapThreshold", "0"}, {"AimSmoothFactor", "0"},
        {"AimStabilizer", "1"}, {"HeadMagnetism", "1"}, {"HeadBoneAimPriority", "1"},
        {"AdsZeroDelay", "1"}, {"PredictiveAim", "1"}, {"HeroLock", "1"}, {"SkillSmartAim", "1"},
        {"TargetPriority", "0"}, {"AimMethod", "1"}, {"WeaponSway", "0"}, {"WeaponSpread", "0"},
        {"WeaponRecoilScale", "0"}, {"TouchPollingRate", "1000"}, {"TouchSampleRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"GyroSampleRate", "1000"},
        {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"}, {"GyroLatencyMode", "0"},
        {"GyroSensitivityRatio", "2.5"},
        {"r.AimAssistEnabled", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"},
        {"r.AimSnapThreshold", "0"}, {"r.HeadBoneAimPriority", "1"}, {"r.PredictiveAim", "1"},
        {"r.GyroSampleRate", "1000"}, {"r.GyroZeroDelay", "1"}, {"r.GyroStabilization", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "AimAssist1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet1000
  (JNIEnv *env, jclass, jstring jPath, jfloat trackingStrength, jfloat hitboxMultiplier) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string hbStr = std::to_string(hitboxMultiplier > 0 ? hitboxMultiplier : 3.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"TrackingBullet", "1"}, {"TrackingBulletLockMax", "1000"}, {"TrackingStrength", "1000"},
        {"BulletTrackingStrength", "1000"}, {"HitboxMultiplier", hbStr}, {"HitboxScale", hbStr},
        {"BulletMagnetism", "1"}, {"BulletVelocityComp", "1"}, {"BulletVelocityBoost", "1"},
        {"MuzzleVelocityFactor", "1.0"}, {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"},
        {"HitRegistrationRate", "1000"}, {"FrameSyncDamage", "1"}, {"ZeroBulletDrop", "1"},
        {"BulletDropComp", "1"}, {"WeaponSpread", "0"}, {"BulletSpreadScale", "0"},
        {"MuzzleSpread", "0"}, {"MovingSpreadFactor", "0"}, {"TrueDamageBoost", "1"},
        {"PenetrationBoost", "1"}, {"DamageReductionBypass", "1"}, {"DamageLockMax", "1"},
        {"EffectiveDPSMode", "3"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"r.PUBGBulletVelocityCompensation", "1"}, {"r.PUBGInstantHitReg", "1"},
        {"r.PUBGTrueDamageMod", "1"}, {"r.BulletSpreadScale", "0"}, {"r.WeaponSpread", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "TrackingBullet1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef1000
  (JNIEnv *env, jclass, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string defStr = std::to_string(defBoost > 0 ? defBoost : 3000.0f);
    std::string redStr = std::to_string(dmgReduction > 0 ? dmgReduction : 0.99f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ArmorDefBoost", "1"}, {"ArmorDef1000", "1"}, {"ArmorDefense", defStr},
        {"PhysicalDefense", defStr}, {"MagicDefense", defStr}, {"DamageReduction", redStr},
        {"DamageReductionBypassImmune", "1"}, {"ShieldMultiplier", "3.0"}, {"LifestealBoost", "1"},
        {"SpellVampBoost", "1"}, {"RetaliationDamage", "3"}, {"bFramePacingEnabled", "True"},
        {"ZeroInputLag", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ArmorDef1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectShield1500
  (JNIEnv *env, jclass, jstring jPath, jfloat shieldMult, jfloat defBoost) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string shStr = std::to_string(shieldMult > 0 ? shieldMult : 3.0f);
    std::string defStr = std::to_string(defBoost > 0 ? defBoost : 3000.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"Shield1500", "1"}, {"ShieldMultiplier", shStr}, {"ShieldScale", shStr},
        {"PhysicalDefense", defStr}, {"MagicDefense", defStr}, {"DamageReduction", "0.99"},
        {"ShieldRegenRate", "10"}, {"ImmunityShield", "1"}, {"TankRetaliationDmg", "3"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "Shield1500");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDroneView
  (JNIEnv *env, jclass, jstring jPath, jint fov, jint height) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string fovStr = std::to_string(fov > 0 ? fov : 180);
    std::string htStr = std::to_string(height > 0 ? height : 180);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DroneView", "1"}, {"DroneFOV", fovStr}, {"MaxFOV", fovStr}, {"FieldOfView", fovStr},
        {"CameraHeight", htStr}, {"CameraDistance", htStr}, {"WideCameraAngle", "1"},
        {"MapVisibilityRange", "2.0"}, {"FogOfWarBypass", "1"}, {"AllowOcclusionQueries", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "DroneView");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimHeadLock
  (JNIEnv *env, jclass, jstring jPath, jfloat headMagnetism, jint snapSpeed) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string magStr = std::to_string(headMagnetism > 0 ? headMagnetism : 1.0f);
    std::string spdStr = std::to_string(snapSpeed > 0 ? snapSpeed : 10);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimHeadLock", "1"}, {"HeadMagnetism", magStr}, {"HeadBoneAimPriority", "1"},
        {"BoneIndex", "0"}, {"AimSnapSpeed", spdStr}, {"AimSnapThreshold", "0"},
        {"AimSmoothFactor", "0"}, {"AdsZeroDelay", "1"}, {"PredictiveAim", "1"},
        {"AimAssistStrength", "1000"}, {"AimMagnetism", "1000"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"GyroSampleRate", "1000"},
        {"r.HeadBoneAimPriority", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "AimHeadLock");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraDamageOverdrive
  (JNIEnv *env, jclass, jstring jPath, jfloat damageScale, jfloat critMultiplier, jfloat trueDamage) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string dmgStr = std::to_string(damageScale > 0 ? damageScale : 1.5f);
    std::string critStr = std::to_string(critMultiplier > 0 ? critMultiplier : 3.0f);
    std::string trueStr = std::to_string(trueDamage > 0 ? trueDamage : 1.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"UltraDamageOverdrive", "1"}, {"DamageLockMax", "1"}, {"DamageBoost", "1"},
        {"DamageScale", dmgStr}, {"CritDamageMultiplier", critStr}, {"CritRateBoost", "1"},
        {"TrueDamageBoost", trueStr}, {"EffectiveDPSMode", "3"}, {"PenetrationBoost", "1"},
        {"ArmorPenMax", "1"}, {"MagicPenMax", "1"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"HitRegistrationRate", "1000"}, {"FrameSyncDamage", "1"},
        {"HeadshotMultiplier", "2.0"}, {"DamageReductionBypass", "1"}, {"BurstDamageWindow", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"r.PUBGDamageLockMax", "1"}, {"r.PUBGDamageBoost", "1"}, {"r.PUBGTrueDamageMod", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "UltraDamageOverdrive");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroAimLock
  (JNIEnv *env, jclass, jstring jPath, jint targetPriority, jfloat lockDistance) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string prioStr = std::to_string(targetPriority >= 0 ? targetPriority : 0);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"HeroAimLock", "1"}, {"HeroLock", "1"}, {"SkillSmartAim", "1"},
        {"TargetPriority", prioStr}, {"AimMethod", "1"}, {"AimAssistStrength", "1000"},
        {"AimMagnetism", "1000"}, {"AimSnapSpeed", "10"}, {"AimSnapThreshold", "0"},
        {"HeadMagnetism", "1"}, {"HeadBoneAimPriority", "1"}, {"AdsZeroDelay", "1"},
        {"PredictiveAim", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"}, {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"},
        {"r.AimAssistEnabled", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "HeroAimLock");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── Universal / MLBB: Critical Burst Overdrive ─────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCriticalBurstOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> critKeys = {
        {"CriticalBurstOverdrive",   "1"},
        {"CritRateBoost",            "1"},
        {"CritMultiplier",           "2.5"},
        {"TrueDamagePenetration",    "3"},
        {"AntiArmorBypass",          "1"},
        {"PhysicalPenetration",      "1"},
        {"MagicPenetration",         "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"HeadshotMultiplier",       "2"},
        {"TrueStrikeMod",            "1"},
        {"LifestealBoost",           "1"},
        {"DamageReductionBypass",    "1"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
        {"PreloadShaders",           "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : critKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CriticalBurstOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── All-Gun Weapon Category Calibration ─────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllGunWeaponCalibration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> gunKeys = {
        // ── Assault Rifles (AR) ──
        {"AR_RecoilZero",             "1"},
        {"AR_SpreadZero",             "1"},
        {"AR_BulletVelocityBoost",    "1"},
        {"AR_AimMagnetism",           "3"},
        {"AR_MaxDamageLock",          "1"},
        {"AR_HeadMagnetism",          "1"},
        {"AR_AccuracyMax",            "1"},
        {"AR_EffectiveRangeMax",      "1"},
        {"AR_ZeroDeadzone",           "1"},
        {"AR_SprayPatternRecovery",   "10"},
        // ── Submachine Guns (SMG) ──
        {"SMG_HipfireBurst",          "1"},
        {"SMG_ZeroRecoil",            "1"},
        {"SMG_RapidFireHitReg",       "1000"},
        {"SMG_AimMagnetism",          "3"},
        {"SMG_SprayControlMax",       "1"},
        {"SMG_DamageMultiplier",      "1.0"},
        {"SMG_BulletVelocityMax",     "1"},
        {"SMG_ZeroSpread",            "1"},
        // ── Sniper Rifles ──
        {"Sniper_ZeroSway",           "1"},
        {"Sniper_QuickScopeZeroDelay","1"},
        {"Sniper_BulletDropComp",     "1"},
        {"Sniper_HeadshotLock",       "1"},
        {"Sniper_InstantHitReg",      "1"},
        {"Sniper_ScopeStabilizer",    "1"},
        {"Sniper_BreathHoldZero",     "1"},
        {"Sniper_MaxDamageLock",      "1"},
        // ── Designated Marksman Rifles (DMR) ──
        {"DMR_RapidTapSync",          "1000"},
        {"DMR_VerticalKickDamp",      "1"},
        {"DMR_RecoilRecovery",        "10"},
        {"DMR_ZeroSway",              "1"},
        {"DMR_ZeroDelay",             "1"},
        {"DMR_AimMagnetism",          "3"},
        {"DMR_MaxDPSLock",            "1"},
        // ── Shotguns ──
        {"Shotgun_TightPelletSpread", "1"},
        {"Shotgun_ZeroPelletRNG",     "1"},
        {"Shotgun_HitSync",           "1000"},
        {"Shotgun_PelletConcentration","1.0"},
        {"Shotgun_RangeBoost",        "1"},
        {"Shotgun_MaxDamageBurst",    "1"},
        // ── Light Machine Guns (LMG) ──
        {"LMG_ContinuousFireStability","1"},
        {"LMG_OverheatReduction",     "1"},
        {"LMG_RecoilCeiling",         "0"},
        {"LMG_ZeroBloom",             "1"},
        {"LMG_SpreadCap",             "0"},
        {"LMG_AimLock",               "1"},
        {"LMG_HitRegSync",            "1000"},
        // ── Pistols ──
        {"Pistol_TriggerZeroDeadzone", "1"},
        {"Pistol_RapidTapBoost",      "1"},
        {"Pistol_RecoilDamp",         "1"},
        {"Pistol_AimMagnetism",       "3"},
        {"Pistol_ZeroDelay",          "1"},
        // ── Universal Gun Physics & 2026 Engine Overdrive ──
        {"WeaponSpread",              "0"},
        {"WeaponSway",                "0"},
        {"WeaponRecoilScale",         "0"},
        {"RecoilPatternScale",        "0"},
        {"VerticalRecoilScale",       "0"},
        {"HorizontalRecoilScale",     "0"},
        {"BulletSpreadScale",         "0"},
        {"MuzzleVelocityFactor",      "1.0"},
        {"BulletVelocityComp",        "1"},
        {"PredictiveAim",             "1"},
        {"HitRegSyncRate",            "1000"},
        {"HitRegistrationRate",       "1000"},
        {"DamageLockMax",             "1"},
        {"DamageBoost",               "1"},
        {"EffectiveDPSMode",          "3"},
        {"PenetrationBoost",          "1"},
        {"CritRateBoost",             "1"},
        {"FrameSyncDamage",           "1"},
        {"AimAssistLockMax",          "1"},
        {"AimAssistEnabled",          "1"},
        {"AimAssistStrength",         "100"},
        {"AimMagnetism",              "3"},
        {"AimSnapSpeed",              "10"},
        {"AimSnapThreshold",          "0"},
        {"AimStabilizer",             "1"},
        {"HeadMagnetism",             "1"},
        {"HeadBoneAimPriority",       "1"},
        {"AdsZeroDelay",              "1"},
        {"AimSmoothFactor",           "0"},
        {"TouchPollingRate",          "1000"},
        {"TouchSampleRate",           "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"GyroSampleRate",            "1000"},
        {"GyroZeroDelay",             "1"},
        {"GyroStabilization",         "1"},
        {"GyroLatencyMode",           "0"},
        {"GyroSensitivityRatio",      "2.5"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
        {"r.VSync",                   "0"},
        {"bFramePacingEnabled",       "1"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"}
    };

    if (isCvar) {
        for (const auto& kv : gunKeys) {
            patch_cvar(content, "r." + kv.first, kv.second);
            patch_cvar(content, kv.first, kv.second);
        }
    } else if (isXml) {
        for (const auto& kv : gunKeys) {
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            patch_xml_node(content, tag, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : gunKeys) {
            bool isNum = (!kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-'));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else {
        for (const auto& kv : gunKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("AllGunWeaponCalibration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── All-Scope Optics Mastery Calibration ────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllScopeMasteryCalibration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> scopeMasteryKeys = {
        // ── No-Scope & Hipfire ──
        {"NoScopeTouchRate",         "1000"},
        {"HipfireDeadzone",          "0"},
        {"HipfireSensitivityBoost",  "1.2"},
        {"HipfireSpread",            "0"},
        {"HipfireAimLock",           "1"},
        // ── Iron Sight & Canted Sight ──
        {"IronSightSensitivity",     "1.0"},
        {"CantedSightZeroDelay",     "1"},
        {"CantedSightSensitivity",   "1.0"},
        // ── Red Dot & Holographic ──
        {"RedDotSensScale",          "1.0"},
        {"RedDotAimLock",            "1"},
        {"HoloSensScale",            "1.0"},
        {"HoloZeroDeadzone",         "1"},
        // ── Mid-Range Optical Scopes (2x, 3x, 4x) ──
        {"Scope2xSensitivity",       "1.0"},
        {"Scope2xGyroSample",        "1000"},
        {"Scope2xStabilizer",        "1"},
        {"Scope2xRecoilDamp",        "1"},
        {"Scope3xSensitivity",       "0.90"},
        {"Scope3xGyroStabilization", "1"},
        {"Scope3xRecoilDamp",        "1"},
        {"Scope3xDriftCancel",       "1"},
        {"Scope4xSensitivity",       "0.85"},
        {"Scope4xGyroStabilization", "1"},
        {"Scope4xStabilizer",        "1"},
        {"Scope4xZeroSway",          "1"},
        // ── Long-Range High Power Scopes (6x, 8x) ──
        {"Scope6xSensitivity",       "0.75"},
        {"Scope6xMicroDamping",      "1"},
        {"Scope6xStabilizer",        "1"},
        {"Scope6xGyro1000Hz",        "1"},
        {"Scope8xSensitivity",       "0.65"},
        {"Scope8xPrecisionFilter",   "1"},
        {"Scope8xStabilizer",        "1"},
        {"Scope8xGyro1000Hz",        "1"},
        {"Scope8xZeroBreathing",     "1"},
        // ── Thermal & Specialized Scopes ──
        {"ThermalScopeTracking",     "1"},
        {"ThermalHitboxGlow",        "1"},
        {"NightVisionClarity",       "1"},
        // ── Core Gyro & AimBot Magnetism ──
        {"AimAssistLockMax",         "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"AimSmoothFactor",          "0"},
        {"HeadMagnetism",            "1"},
        {"AdsZeroDelay",             "1"},
        {"GyroSampleRate",           "1000"},
        {"GyroZeroDelay",            "1"},
        {"GyroStabilization",        "1"},
        {"GyroLatencyMode",          "0"},
        {"GyroSmoothFactor",         "1"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
    };

    if (isCvar) {
        for (const auto& kv : scopeMasteryKeys) {
            patch_cvar(content, "r." + kv.first, kv.second);
            patch_cvar(content, kv.first, kv.second);
        }
    } else if (isXml) {
        for (const auto& kv : scopeMasteryKeys) {
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            patch_xml_node(content, tag, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : scopeMasteryKeys) {
            bool isNum = (!kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-'));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else {
        for (const auto& kv : scopeMasteryKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("AllScopeMasteryCalibration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastReloadQuickSwap
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"FastReload", "1"}, {"ReloadSpeedMultiplier", "10.0"}, {"ReloadDurationReduction", "0.99"},
        {"InstantChambering", "1"}, {"QuickSwap", "1"}, {"WeaponSwapZeroDelay", "1"},
        {"HolsterSpeedBoost", "10.0"}, {"DrawSpeedBoost", "10.0"}, {"SprintToFireZeroDelay", "1"},
        {"AdsZeroDelay", "1"}, {"TouchPollingRate", "1000"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastReloadQuickSwap");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWallPiercingArmorShredder
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"WallPiercing", "1"}, {"MaterialPenetrationMax", "1"}, {"CoverPenetrationMultiplier", "5.0"},
        {"ArmorShredder", "1"}, {"ArmorReductionMultiplier", "1.0"}, {"DamageReductionBypass", "1"},
        {"TrueDamagePenetration", "10000"}, {"ShieldPiercing", "1"}, {"HitRegSyncRate", "1000"},
        {"ZeroBulletDrop", "1"}, {"r.PUBGTrueDamageMod", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "WallPiercingArmorShredder");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroPingNetworkOverclock
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ZeroPingNetwork", "1"}, {"NetworkJitterBufferZero", "1"}, {"ClientTickRate", "128"},
        {"ServerPacketSyncRate", "1000"}, {"InterpolationDelay", "0"}, {"ExtrapolationSmoothing", "1"},
        {"PacketLossComp", "1"}, {"HitRegSyncRate", "1000"}, {"InstantHitReg", "1"},
        {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ZeroPingNetworkOverclock");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtreme240FpsGraphics
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"TargetFPS", "240"}, {"MaxFrameRate", "240"}, {"FrameRateLimit", "240"},
        {"FrameRateLevel", "10"}, {"HighFPSMode", "4"}, {"Unlock240Hz", "1"},
        {"Unlock185Hz", "1"}, {"Unlock165Hz", "1"}, {"Unlock144Hz", "1"},
        {"Vsync", "0"}, {"bFramePacingEnabled", "1"}, {"ResolutionScale", "140"},
        {"HDR10Plus", "1"}, {"UltraExtreme2026", "1"}, {"VulkanPipelineCache", "1"},
        {"AsyncCompute", "1"}, {"VRS", "1"}, {"PreloadShaders", "1"},
        {"r.PUBGDeviceFPS", "10"}, {"r.PUBGMaxFPS", "240"}, {"r.VSync", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "UltraExtreme240FpsGraphics");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamage", "10000"},
        {"PhysicalDamageBase", "10000"}, {"MagicDamageBase", "10000"}, {"TrueDamageBase", "10000"},
        {"AttackSpeedBoost", "10000"}, {"AttackSpeedCap", "10.0"}, {"AttackSpeedMax", "1"},
        {"FireRateMultiplier", "10.0"}, {"FireRateBoost", "10000"}, {"PenetrationBoost", "10000"},
        {"CritRateBoost", "100"}, {"CritDamageMultiplier", "5.0"}, {"HitRegSyncRate", "1000"},
        {"InstantHitReg", "1"}, {"ZeroRecoil", "1"}, {"ZeroSpread", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "UniversalDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// 2026 SKILL ECONOMY OVERDRIVE SUITE
// Fast Cooldown · Fast Full Mana · Fast Full Energy · Fast HP Regen
// Fast Stamina/Fury · Zero Skill Cost · Max Ult Charge · Master Suite
// ─────────────────────────────────────────────────────────────────────────────

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown
  (JNIEnv *env, jclass, jstring jPath, jfloat cdrRatio) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    // cdrRatio: 0.001 = near-instant cooldown (0.1% of normal)
    char cdrBuf[32]; snprintf(cdrBuf, sizeof(cdrBuf), "%.4f", (float)cdrRatio);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkillCDR",               cdrBuf},
        {"CooldownMultiplier",     cdrBuf},
        {"SkillCooldown",          "0.0"},
        {"SkillCooldownReduction", "1.0"},
        {"ItemCooldown",           "0.0"},
        {"ItemActiveCooldown",     "0.0"},
        {"PassiveCDReduction",     "1.0"},
        {"GlobalCooldown",         cdrBuf},
        {"UltCooldownReduction",   "1.0"},
        {"HeroUltCD",              cdrBuf},
        {"PassiveCooldown",        "0.0"},
        {"CastTimeReduction",      "1.0"},
        {"TalentCooldown",         "0.0"},
        {"EquipmentCooldown",      "0.0"},
        // MLBB keys
        {"SkillCD",                "0"},
        {"SP_SkillCooldown",       "0.0"},
        // CODM keys
        {"OperatorSkillCooldown",  cdrBuf},
        {"TacticalCooldown",       cdrBuf},
        {"LethalCooldown",         cdrBuf},
        {"FieldUpgradeCooldown",   cdrBuf},
        // PUBGM keys
        {"VehicleCooldown",        "0.0"},
        {"AdrenalineCooldown",     "0.0"},
        // Legacy & general speed keys
        {"CooldownReduction",      "1"},
        {"SkillCDRatio",           "0"},
        {"SkillInstantReset",      "1"},
        {"FastCooldownMax",        "1"},
        {"EnergyRegenBoost",       "10"},
        {"ManaRegenBoost",         "10"},
        {"ZeroInputLag",           "1"},
        {"TouchPollingRate",       "1000"},
        {"TouchZeroDelay",         "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastCooldown2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastCooldown2026 injected: %s [cdr=%.4f ok=%d]", pathStr.c_str(), (float)cdrRatio, ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullMana
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"ManaRegen",          "10000"},
        {"MaxMana",            "99999"},
        {"StartMana",          "99999"},
        {"ManaCostMultiplier", "0.0"},
        {"SkillManaCost",      "0"},
        {"UltManaCost",        "0"},
        {"ManaPerSecond",      "10000"},
        {"ManaRestoreRate",    "10000"},
        {"InstantManaFull",    "1"},
        {"ManaOnKill",         "10000"},
        {"ManaOnHit",          "10000"},
        // MLBB mana keys
        {"HeroMaxMana",        "99999"},
        {"ManaGain",           "10000"},
        {"SP_MaxMana",         "99999"},
        {"SP_ManaRegen",       "10000"},
        // HoYo/Genshin keys
        {"SkillEnergyCost",    "0"},
        {"EnergyRecharge",     "10000"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastFullMana2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastFullMana2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullEnergy
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"EnergyRegen",          "10000"},
        {"MaxEnergy",            "99999"},
        {"StartEnergy",          "99999"},
        {"EnergyCostMultiplier", "0.0"},
        {"SkillEnergyCost",      "0"},
        {"UltEnergyCost",        "0"},
        {"EnergyPerSecond",      "10000"},
        {"EnergyRestoreRate",    "10000"},
        {"InstantEnergyFull",    "1"},
        {"EnergyOnKill",         "10000"},
        {"EnergyOnHit",          "10000"},
        // MLBB/HOK energy keys
        {"SP_MaxEnergy",         "99999"},
        {"SP_EnergyRegen",       "10000"},
        {"EnergyGain",           "10000"},
        // Farlight/ArenaBreakout shield-energy
        {"ShieldEnergyRegen",    "10000"},
        {"MaxShieldEnergy",      "99999"},
        // PUBGM adrenaline / boost energy
        {"BoostEnergyRegen",     "10000"},
        {"AdrenalineEffect",     "10000"},
        {"BoostDecayRate",       "0.0"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastFullEnergy2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastFullEnergy2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastHpRegen
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"HpRegen",              "10000"},
        {"HpRegenPerSecond",     "10000"},
        {"PassiveRegenTick",     "10000"},
        {"RegenDelay",           "0.0"},
        {"HealMultiplier",       "10.0"},
        {"LifeStealRate",        "1.0"},
        {"LifeStealMax",         "1.0"},
        {"BloodthirstRate",      "10000"},
        {"VampirismRate",        "10000"},
        {"HpOnKill",             "10000"},
        {"HpOnHit",              "10000"},
        {"SelfHealMultiplier",   "10.0"},
        {"OutOfCombatRegen",     "10000"},
        // MLBB hero HP regen
        {"HeroHPRegen",          "10000"},
        {"SP_HpRegen",           "10000"},
        // PUBGM health bar regen
        {"HealthRegen",          "10000"},
        {"AutoHealRate",         "10000"},
        // CODM operator passive regen
        {"OperatorHealRate",     "10000"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastHpRegen2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastHpRegen2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastStaminaFuryRegen
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"StaminaRegen",       "10000"},
        {"MaxStamina",         "99999"},
        {"StaminaDecayRate",   "0.0"},
        {"SprintStaminaCost",  "0.0"},
        {"DodgeCost",          "0.0"},
        {"JumpCost",           "0.0"},
        {"FuryRegen",          "10000"},
        {"MaxFury",            "99999"},
        {"FuryDecay",          "0.0"},
        {"RageRegen",          "10000"},
        {"MaxRage",            "99999"},
        {"RageDecay",          "0.0"},
        {"MomentumRegen",      "10000"},
        {"FlowRegen",          "10000"},
        // MLBB fighter rage/fury
        {"SP_FuryRegen",       "10000"},
        {"HeroFuryMax",        "99999"},
        // PUBGM sprint stamina
        {"SprintDuration",     "99999"},
        {"SprintRecovery",     "10000"},
        // CODM operator charge
        {"OperatorCharge",     "10000"},
        {"ChargeDecayRate",    "0.0"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastStaminaFuryRegen2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastStaminaFuryRegen2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroSkillCost
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkillCost",         "0"},
        {"UltCost",           "0"},
        {"AbilityCost",       "0"},
        {"SpellCost",         "0"},
        {"ThrowableCost",     "0"},
        {"ConsumableCost",    "0"},
        {"AmmoConsumption",   "0"},
        {"ResourceCost",      "0"},
        {"StaminaCost",       "0"},
        {"EnergyCost",        "0"},
        {"ManaCost",          "0"},
        {"FuryCost",          "0"},
        {"RageCost",          "0"},
        {"HeatCost",          "0"},
        // MLBB zero skill mana cost
        {"SP_SkillManaCost",  "0"},
        {"SP_UltManaCost",    "0"},
        // CODM zero costs
        {"OperatorSkillCost", "0"},
        {"TacticalCost",      "0"},
        {"LethalCost",        "0"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ZeroSkillCost2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("ZeroSkillCost2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMaxUltCharge
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"UltChargeRate",       "10000"},
        {"UltChargeMax",        "99999"},
        {"StartUltCharge",      "99999"},
        {"UltPassiveRegen",     "10000"},
        {"SuperChargeRate",     "10000"},
        {"UltReadyInstant",     "1"},
        {"ChargeDecayRate",     "0.0"},
        {"UltChargeOnKill",     "10000"},
        {"UltChargeOnHit",      "10000"},
        {"UltChargeOnDamage",   "10000"},
        // MLBB ult recharge
        {"HeroUltRecharge",     "10000"},
        {"SP_UltChargeRate",    "10000"},
        // CODM scorestreak charge
        {"ScorestreakCharge",   "10000"},
        {"KillstreakCharge",    "10000"},
        // HOK/WildRift ult charge
        {"AbilityChargeRate",   "10000"},
        {"UltimateChargeRate",  "10000"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "MaxUltCharge2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MaxUltCharge2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─── Master One-Shot Skill Economy Injector ──────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSkillEconomyMasterSuite
  (JNIEnv *env, jclass cls, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    // Re-use path string across all sub-injectors
    bool r1 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown(env, cls, jPath, 0.001f);
    bool r2 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullMana(env, cls, jPath);
    bool r3 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullEnergy(env, cls, jPath);
    bool r4 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastHpRegen(env, cls, jPath);
    bool r5 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastStaminaFuryRegen(env, cls, jPath);
    bool r6 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroSkillCost(env, cls, jPath);
    bool r7 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMaxUltCharge(env, cls, jPath);
    bool anyOk = r1 || r2 || r3 || r4 || r5 || r6 || r7;
    LOGI("SkillEconomyMasterSuite2026: CDR=%d Mana=%d Energy=%d HP=%d Stamina=%d ZeroCost=%d Ult=%d",
         r1,r2,r3,r4,r5,r6,r7);
    return anyOk ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Fast Loot & Instant Weapon Swap ─────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastLootAndWeaponSwap
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"AutoPickUpSpeed", "100"}, {"bAutoPickUpSpeedPriority", "True"},
        {"PickUpSearchRadius", "2500"}, {"QuickLootThreshold", "0"},
        {"LootBoxRenderPriority", "1"}, {"WeaponSwitchZeroDelay", "1"},
        {"QuickDrawLatencyReduction", "0"}, {"bFastEquipEnabled", "True"},
        {"InstantLootResponse", "1"}, {"FastWeaponSwapSpeedMultiplier", "2.0"},
        {"ZeroLootDelay", "1"}, {"bFastItemPickup", "True"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastLootAndWeaponSwap injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Instant Sprint Turbo ────────────────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectInstantSprintTurbo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SprintSensitivity", "100"}, {"JoystickDeadZone", "0"},
        {"SprintDelayZero", "1"}, {"InstantSprintThreshold", "0.01"},
        {"FastSprintResponse", "1"}, {"SprintForwardDeadzone", "0"},
        {"ZeroSprintTransitionLag", "1"}, {"bInstantSprintActive", "True"},
        {"TouchAnalogSensitivity", "2.0"}, {"RunLockZeroLatency", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("InstantSprintTurbo injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Multi-Range Headshot Precision Calibration ──────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMultiRangeHeadshotCalibration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"CloseRangeAimSensMultiplier", "1.5"}, {"HipFireFovStability", "1"},
        {"TouchZeroFriction", "1"}, {"MidRangeRecoilStability", "1"},
        {"AimFovSmoothCurve", "0"}, {"CrosshairSwayElimination", "1"},
        {"LongRangeMicroAimPrecision", "1000"}, {"SniperScopeZeroLatency", "1"},
        {"SteadyAimFovLock", "1"}, {"SubPixelAimCalibration", "1"},
        {"GyroMicroSensitivityBoost", "1.4"}, {"HeadshotHitboxAimMagnetism", "1"},
        {"DynamicAimAcceleration", "0"}, {"bMultiRangeHeadshotEnabled", "True"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MultiRangeHeadshotCalibration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal Zero-Delay Skill Tap & Combo All Hero ─────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalZeroDelaySkillTapAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkillQueueInstant", "1"}, {"SmartSkillCastZeroDelay", "1"},
        {"AutoAttackAnimationCancel", "1"}, {"ComboChainBufferMs", "0"},
        {"TouchSamplingRate", "1000"}, {"ZeroDelaySkillTap", "1"},
        {"InstantSkillCancelThreshold", "0"}, {"HeroTargetLockPriority", "1"},
        {"FastSkillReleaseSpeed", "10"}, {"InputQueueBypass", "1"},
        {"bZeroLatencyInput", "True"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("UniversalZeroDelaySkillTapAllHero injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal Fast Loot, Auto Pick-Up Guns & Fast Sprint ─────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastLootAndSprint
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"AutoPickup", "1"}, {"AutoPickupSpeed", "2"},
        {"PUBGAutoLoot", "1"}, {"PUBGPickupPriority", "1"},
        {"PUBGFastWeaponSwitch", "1"}, {"FastWeaponSwitch", "1"},
        {"QuickThrow", "1"}, {"FastADS", "1"}, {"OneTapADS", "1"},
        {"QuickLoot", "1"}, {"QuickReload", "1"},
        {"PUBGQuickOpenScope", "1"}, {"PickupRangeBoost", "1.5"},
        {"LootResponseTime", "0"}, {"AutoSprint", "1"},
        {"bSprintAlways", "True"}, {"SprintSensitivity", "100"},
        {"MovementDeadzone", "0"}, {"FastSlide", "1"},
        {"SlideDelayMs", "0"}, {"SprintAcceleration", "10"},
        {"JoyStickDeadzone", "0"}, {"TouchResponseSprint", "1000"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastLootAndSprint injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal Combat Mechanics & True Damage Overdrive ──────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalCombatMechanicsOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"TouchSampleRate", "1000"}, {"TouchZeroDelay", "1"},
        {"InputBufferRate", "1000"}, {"ZeroLatencyEventQueue", "1"},
        {"AttackAnimationCancel", "1"}, {"PostAttackRecoveryFrames", "0"},
        {"PreAttackWindupFrames", "0"}, {"FrameSyncDamage", "1"},
        {"ClientDamagePacing", "185"}, {"NetworkDamagePacketBatching", "0"},
        {"UniversalArmorPiercing", "1.0"}, {"TrueDamageMode", "1"},
        {"EffectiveDPSMultiplier", "3.0"}
    };
    for (const auto& kv : keys) {
        if (isXml) {
            std::string t = "int";
            if (kv.second.find('.') != std::string::npos) t = "float";
            patch_xml_node(content, t, kv.first, kv.second);
        } else if (isJson) {
            bool n = !kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-');
            patch_json_node(content, kv.first, kv.second, n);
        } else if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("UniversalCombatMechanicsOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalFastLoadTurbo(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"FastLoad", "1"}, {"SkipSplash", "1"},
        {"SkipIntro", "1"}, {"AsyncLoadingThread", "1"},
        {"ShaderPrewarmAsync", "1"}, {"TextureStreamingBufferMB", "512"},
        {"MultiThreadedAssetLoading", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) {
            patch_xml_node(content, "int", kv.first, kv.second);
        } else if (isJson) {
            patch_json_node(content, kv.first, kv.second, true);
        } else if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("UniversalFastLoadTurbo injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal: Tiered No-Scope Auto Headshot (20m, 40m, 50m, 100m - All Guns)
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoScopeTieredHeadshotAllGun
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"NoScopeHeadshot20m", "1"}, {"AimMagnetism20m", "3"}, {"HipfireLock20m", "1"}, {"NoScopeAimLock20m", "1"}, {"NoScopeSpread20m", "0"}, {"CQBAutoHeadshot20m", "1"},
        {"NoScopeHeadshot40m", "1"}, {"AimMagnetism40m", "3"}, {"HipfireLock40m", "1"}, {"NoScopeAimLock40m", "1"}, {"NoScopeSpread40m", "0"}, {"CloseRangeHeadshot40m", "1"},
        {"NoScopeHeadshot50m", "1"}, {"AimMagnetism50m", "3"}, {"HipfireLock50m", "1"}, {"NoScopeAimLock50m", "1"}, {"NoScopeSpread50m", "0"}, {"MidRangeNoScope50m", "1"},
        {"NoScopeHeadshot100m", "1"}, {"AimMagnetism100m", "3"}, {"HipfireLock100m", "1"}, {"NoScopeAimLock100m", "1"}, {"NoScopeSpread100m", "0"}, {"ExtremeNoScope100m", "1"},
        {"AllGunNoScopeHeadshot", "1"}, {"NoScopeHeadLock", "1"}, {"NoScopeAimMagnetism", "3"}, {"NoScopeCrosshairAccuracy", "1.0"},
        {"HipfireMagnetism", "3"}, {"HipfireHeadLock", "1"}, {"CrosshairTightness", "1.0"}, {"NoScopeRecoilZero", "1"},
        {"WeaponSpreadScale", "0"}, {"AutoHeadshotBurst", "3"}, {"Auto3BulletHeadshot", "1"}, {"HitRegSyncRate", "1000"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("NoScopeTieredHeadshotAllGun injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal: Tiered Scope-On Auto Headshot (100m, 200m, 300m, 400m - Rifles)
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectRifleScopeTieredHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"RifleScopeHeadshot100m", "1"}, {"RifleScopeMagnetism100m", "3"}, {"AimSnapHead100m", "1"}, {"Scope1xHeadLock", "1"}, {"Scope1xAimMagnetism", "3"}, {"ScopeRedDotHeadLock", "1"}, {"ScopeHoloHeadLock", "1"},
        {"RifleScopeHeadshot200m", "1"}, {"RifleScopeMagnetism200m", "3"}, {"AimSnapHead200m", "1"}, {"Scope2xHeadLock", "1"}, {"Scope3xHeadLock", "1"}, {"Scope2xZeroRecoil", "1"}, {"Scope3xZeroRecoil", "1"}, {"PredictiveAim200m", "1"},
        {"RifleScopeHeadshot300m", "1"}, {"RifleScopeMagnetism300m", "3"}, {"AimSnapHead300m", "1"}, {"Scope4xHeadLock", "1"}, {"Scope6xHeadLock", "1"}, {"BulletDropComp300m", "1"}, {"ZeroBreathSway300m", "1"},
        {"RifleScopeHeadshot400m", "1"}, {"RifleScopeMagnetism400m", "3"}, {"AimSnapHead400m", "1"}, {"Scope8xLongRangeHeadLock", "1"}, {"BulletDropComp400m", "1"}, {"TargetLeadComp400m", "1"}, {"ExtremeRangeHeadLock400m", "1"}, {"ZeroMicroJitter400m", "1"},
        {"AllRifleAutoHeadshot", "1"}, {"RifleZeroRecoil", "1"}, {"RifleZeroSpread", "1"}, {"RifleScopeAimMagnetism", "3"},
        {"BulletTrackingEnemy", "1"}, {"ZeroADSDelay", "1"}, {"GyroStabilization", "1"}, {"GyroSampleRate", "1000"}, {"HitRegSyncRate", "1000"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("RifleScopeTieredHeadshot injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

