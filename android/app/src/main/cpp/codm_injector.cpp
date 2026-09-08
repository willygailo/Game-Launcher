// =============================================================================
// CODM (Call of Duty Mobile) Dedicated Native Injector
// High-performance isolated translation unit for GameBooster Native
// =============================================================================

#include "native_config_injector.h"
#include "config_common.h"

// ─────────────────────────────────────────────────────────────────────────────
// ─── CODM: No Recoil + No Spread + AimBot Precision ─────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// CODM runs on a custom engine (modified UE4). Config keys work across
// its UserSetting.json, PlayerPrefs.xml, and GraphicsSettings.ini.
//
// Config injected:
//   RecoilScale=0              : zero all recoil
//   VerticalRecoilScale=0      : zero vertical kick
//   HorizontalRecoilScale=0    : zero horizontal kick
//   RecoilPatternScale=0       : zero recoil pattern
//   WeaponSpread=0             : zero bullet spread cone
//   WeaponSway=0               : zero weapon idle sway
//   BulletSpreadScale=0        : zero spread scalar
//   SpreadDecayRate=10         : spread recovers instantly
//   AimAssistEnabled=1         : aim assist on
//   AimAssistStrength=100      : max strength
//   AimMagnetism=3             : max magnetism tier
//   HeadMagnetism=1            : head-bone preference
//   AimSnapSpeed=10            : max snap speed
//   AimSmoothFactor=0          : zero smooth = instant lock
//   AdsZeroDelay=1             : ADS instant
//   Scope2xStabilizer=1        : 2x scope recoil damping
//   Scope4xStabilizer=1        : 4x scope recoil damping
//   Scope8xStabilizer=1        : 8x scope recoil damping
//   GyroSampleRate=1000        : 1000 Hz gyro
//   GyroZeroDelay=1            : zero gyro delay
//   GyroStabilization=1        : gyro stabilization
//   GyroLatencyMode=0          : raw gyro
//   TouchPollingRate=1000      : 1000 Hz touch
//   TouchZeroDelay=1           : zero touch buffer
//   ZeroInputLag=1             : input lag suppression
//   HitRegSyncRate=1000        : hit-reg 1000 Hz
//   bFramePacingEnabled=True   : frame-paced delivery
//   AllowOcclusionQueries=1    : GPU hit-reg precision
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoRecoilNoSpread
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> codmKeys = {
        // ── Zero Recoil ──
        {"RecoilScale",             "0"},
        {"VerticalRecoilScale",     "0"},
        {"HorizontalRecoilScale",   "0"},
        {"RecoilPatternScale",      "0"},
        {"RecoilMultiplier",        "0"},
        // ── Zero Spread ──
        {"WeaponSpread",            "0"},
        {"WeaponSway",              "0"},
        {"BulletSpreadScale",       "0"},
        {"SpreadDecayRate",         "10"},
        {"MuzzleSpread",            "0"},
        {"MovingSpreadFactor",      "0"},
        {"JumpSpreadFactor",        "0"},
        // ── AimBot Precision ──
        {"AimAssistEnabled",        "1"},
        {"AimAssistStrength",       "100"},
        {"AimMagnetism",            "3"},
        {"HeadMagnetism",           "1"},
        {"AimSnapSpeed",            "10"},
        {"AimSmoothFactor",         "0"},
        {"AimSnapThreshold",        "0"},
        {"AdsZeroDelay",            "1"},
        {"PredictiveAim",           "1"},
        {"HeadBoneAimPriority",     "1"},
        // ── Scope Stabilizers ──
        {"Scope2xStabilizer",       "1"},
        {"Scope4xStabilizer",       "1"},
        {"Scope6xStabilizer",       "1"},
        {"Scope8xStabilizer",       "1"},
        {"ScopeBreathingDamp",      "1"},
        {"ScopeSwayDamp",           "1"},
        // ── Gyro 1000Hz ──
        {"GyroSampleRate",          "1000"},
        {"GyroZeroDelay",           "1"},
        {"GyroStabilization",       "1"},
        {"GyroLatencyMode",         "0"},
        {"GyroSmoothFactor",        "1"},
        // ── Touch Precision ──
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
        {"InputBufferRate",         "1000"},
        {"TouchStabilization",      "1"},
        {"JoystickZeroDeadzone",    "1"},
        {"JoystickResponseLevel",   "3"},
        // ── Hit-Reg ──
        {"HitRegSyncRate",          "1000"},
        {"bFramePacingEnabled",     "True"},
        {"AllowOcclusionQueries",   "1"},
        {"PreloadShaders",          "1"},
        {"r.OneFrameThreadLag",     "0"},
        {"r.FinishCurrentFrame",    "0"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : codmKeys) {
        if (isXml) {
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            else if (kv.second == "True" || kv.second == "False") tag = "string";
            patch_xml_node(content, tag, kv.first, kv.second);
        } else if (isJson) {
            bool isNum = (!kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-'));
            patch_json_node(content, kv.first, kv.second, isNum);
        } else if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
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
    LOGI("NoRecoilNoSpread CODM injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: No-Scope Hipfire Aimbot ───────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmNoScopeAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"HipfireSpread","0"},{"HipfireSpreadScale","0"},{"AR_SpreadZero","1"},
        {"NoScope50mOnly","1"},{"NoScopeAimbotMaxRange","50"},{"NoScopeMaxRange","50"},
        {"NoScope50mHeadLock","1"},{"NoScopeHeadshot50m","1"},{"NoScopeAimLock50m","1"},
        {"NoScopeSpread50m","0"},{"NoScopeAimMagnetism50m","3"},{"NoScopeCrosshairAccuracy50m","1.0"},
        {"HipfireLock50m","1"},{"HipfireHeadLock50m","1"},{"CQBAutoHeadshot50m","1"},
        {"ExtremeHipfireLock50m","1"},{"NoScopeMagicBullet50m","1"},
        {"CQBEnemySnap360","1"},{"HipfireHeadTrackingMax","1"},{"Shotgun_ZeroPelletRNG","1"},
        {"ShotgunPelletSpreadScale","0"},{"ShotgunTightChokeMod","1"},{"Shotgun_TightPelletSpread","1"},
        {"Shotgun_MaxDamageBurst","1"},{"Shotgun_HitSync","1000"},
        {"SlideHipfireAccuracy","1.0"},{"JumpHipfireSpread","0"},{"SlideCancelHipfireZeroDelay","1"},
        {"NoScopeMagicBulletRadius","50"},{"CQBMagicBulletBend","1"},
        {"CloseQuartersAimSnap","1"},{"HipfireInstantTargetLock","1"},{"LaserHipfireAccuracy","1.0"},
        {"AR_HipfireLock","1"},{"AR_BulletVelocityBoost","1"},
        {"SMG_HipfireBurst","1"},{"SMG_ZeroSpread","1"},{"SMG_RapidFireHitReg","1000"},
        {"Sniper_HipfireAccuracy","1.0"},{"Sniper_QuickScopeZeroDelay","1"},
        {"DMR_HipfireAccuracy","1.0"},{"DMR_RapidTapSync","1000"},
        {"LMG_RecoilCeiling","0"},{"LMG_ContinuousFireStability","1"},
        {"Pistol_TriggerZeroDeadzone","1"},{"Pistol_RapidTapBoost","1"},{"Pistol_AimMagnetism","3"},
        {"PenetrationDamageLoss","0"},{"ArmorPiercingOverdrive","1"},{"VestDamageBypass","1"},
        {"WeaponSpread","0"},{"BulletSpreadScale","0"},{"NoSpreadHipfire","1"},
        {"HeadMagnetism","1"},{"HeadBonePriority","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"AimAssistLockMax","1"},{"HipfireHeadLock","1"},{"CodmNoScopeAimbot","1"},
        {"NoScopeAutoAimbot","1000"},{"NoScopeAimbotOverdrive1000","1"},
        {"NoScopeDamageBoost","1000"},{"NoScopeDamageMultiplier","1000"},
        {"NoScopeTrueDamage","1000"},{"NoScopeHeadshotDamage","1000"},
        {"NoScopeDamageLock","1000"},{"DamageLockMax","1000"},
        {"DamageBoost","1000"},{"TrueDamageMod","1000"},
        {"InstantHitDamage","1000"},{"HipfireDamageBoost","1000"},
        {"NoScopeAimbotMaxRangeOverdrive","1000"},
        {"InstantAimSnap","1"},{"WeaponSway","0"},{"SwayAmplitude","0"},{"BreathSway","0"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmNoScopeAimbot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: All-Scope Aimbot (All Optic Classes) ──────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AdsZeroDelay","1"},{"AdsTransitionTime","0"},{"AdsSnapEnable","1"},{"CodmAdsZeroDelay","1"},
        {"InstantADSSnap","1"},{"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"AllScopeHeadshotPriority","1"},{"ScopeAimbotEnable","1"},
        {"AllScopeAutoAimbot","1000"},{"ScopeAimbotTier500m","1"},
        {"ScopeAimbotTier750m","1"},{"ScopeAimbotTier1000m","1"},
        {"ScopeAimRangeMax","1000"},{"AllScopeAimSnap","1000"},
        {"AllScopeAimMagnetism","1000"},{"ScopePredictiveAim","1000"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},{"AimAssistLockMax","1"},
        {"PredictiveAim","1"},{"ScopePredictiveAim","1"},{"ScopeTargetLeadFactor","1.0"},
        {"IronSightSensitivity","1.0"},{"IronSightZeroDelay","1"},{"CantedSightZeroDelay","1"},{"CantedSightSensitivity","1.0"},
        {"RedDotAimLock","1"},{"RedDotHeadMagnet","3"},{"RedDotSensScale","1.0"},
        {"HoloZeroDeadzone","1"},{"HoloSensScale","1.0"},
        {"TacticalScopeHeadLock","1"},{"Scope1_5xHeadLock","1"},
        {"Scope2xHeadLock","1"},{"Scope2xSensitivity","1.0"},
        {"Scope3xHeadLock","1"},{"Scope3xSensitivity","0.90"},
        {"Scope4xHeadLock","1"},{"Scope4xSensitivity","0.85"},
        {"Scope4_4xHeadLock","1"},
        {"OWCTacticalSnap","1"},{"RTGPrecisionScopeLock","1"},{"MIPCustomOpticAimLock","1"},
        {"Scope6xHeadLock","1"},{"Scope6xSensitivity","0.75"},
        {"Scope8xHeadLock","1"},{"Scope8xSensitivity","0.65"},
        {"SniperScopeHeadLock","1"},
        {"ThermalHeadshotLock","1"},{"ThermalClarityOverdrive","1"},{"ThermalScopeTracking","1"},{"ThermalHitboxGlow","1"},{"NightVisionClarity","1"},
        {"HoldBreathInfinite","1"},{"ScopeZeroBreathing","1"},{"ScopeBreathHoldTime","0"},
        {"WeaponSway","0"},{"BreathSway","0"},{"MicroJitterCancel","1"},{"ScopeEyeReliefClear","1"},
        {"ScopeFovStabilization","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"HitRegSyncRate","1000"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmAllScopeAimbot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: Long-Range Headshot Lock (Sniper/Marksman) ────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmLongRangeHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"BulletDropComp","1"},{"Sniper_BulletDropComp","1"},
        {"MuzzleVelocityFactor","1.0"},{"BulletVelocityBoost","1"},
        {"HoldBreathCancel","1"},{"BreathHoldTime","0"},{"BreathSway","0"},
        {"Scope8xZeroBreathing","1"},{"Scope6xZeroBreathing","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"Sniper_HeadshotLock","1"},{"CodmLongRangeHeadshotLock","1"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"MicroJitterCancel","1"},{"Scope6xMicroDamping","1"},{"Scope8xPrecisionFilter","1"},
        {"Sniper_ZeroSway","1"},{"GyroSampleRate","1000"},
        {"Scope6xGyro1000Hz","1"},{"Scope8xGyro1000Hz","1"},
        {"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmLongRangeHeadshot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: Mid-Range Auto Headshot (AR/SMG) ──────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMidRangeHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"PredictiveAim","1"},{"TargetPrediction","1"},{"TargetLeadCompensation","1"},
        {"FrameSyncHeadshot","1"},{"CodmMidRangeHeadshotLock","1"},{"HeadshotLockMidRange","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"VerticalRecoilScale","0"},{"VerticalKickDamp","1"},
        {"AR_RecoilZero","1"},{"SMG_ZeroRecoil","1"},{"SMG_RapidFireHitReg","1000"},
        {"RecoilPatternScale","0"},{"HorizontalRecoilScale","0"},
        {"Scope2xHeadLock","1"},{"Scope2xRecoilDamp","1"},
        {"Scope3xHeadLock","1"},{"Scope3xDriftCancel","1"},
        {"Scope4xHeadLock","1"},{"Scope4xZeroSway","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmMidRangeHeadshot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: Fast Fire Rate + Operator Attack Speed ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastAttackSpeed
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"WeaponFireRate","10"},{"FireRateBoost","10"},{"FireIntervalMin","0"},
        {"FullAutoFrameSync","1"},{"ADSFireInterval","0"},{"FireRateUnlockMax","1"},{"CodmFireRateMax","1"},
        {"OperatorSkillAttackRate","10"},{"OperatorCooldownMin","0"},
        {"OperatorSkillCastSpeed","10"},{"OperatorSkillFrameSync","1"},
        {"MeleePunchSpeed","10"},{"MeleeAttackRate","10"},{"MeleeIntervalMin","0"},{"CodmMeleeSpeedMax","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},{"AttackFrameSync","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"bFramePacingEnabled","True"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmFastAttackSpeed injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM BSA Removal & Infinite Range Overdrive ─────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmBsaRemovalRangeOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"BulletSpreadAccuracy", "0.0"}, {"ADSBulletSpreadDecay", "0.0"},
        {"HipfireBloom", "0.0"}, {"InitialBulletSpread", "0.0"},
        {"DamageRangeFalloff", "0.0"}, {"DamageRangeMultiplier", "3.0"},
        {"MinDamageMultiplier", "1.0"}, {"DamagePerShotMax", "100"},
        {"SprintToFireDelayMs", "0"}, {"ADSTransitionTimeMs", "0"},
        {"FastBoltPullSpeed", "2.0"}, {"QuickDrawFactor", "2.0"},
        {"HitFlinchScale", "0.0"}, {"FlinchRecoveryRate", "10.0"},
        {"ScreenShakeScale", "0.0"}, {"QuickScopeAccuracyThreshold", "1.0"},
        {"BlankScopeAccuracy", "1.0"}, {"SniperADSIdleSway", "0.0"},
        {"OneShotKillHitbox", "1"}, {"ShotgunDamagePerPellet", "50"},
        {"PelletSpreadADS", "0.0"}, {"PumpActionCycleMs", "0"}
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
    LOGI("CodmBsaRemovalRangeOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Tiered All-Scope Auto 3-Bullet Headshot (100m - 400m) ─────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeTieredHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        // ─── Tiered No-Scope Auto Headshot (20m, 40m, 50m, 100m - All Guns) ─────
        {"NoScopeHeadshot20m", "1"}, {"AimMagnetism20m", "3"}, {"HipfireLock20m", "1"}, {"NoScopeAimLock20m", "1"}, {"NoScopeSpread20m", "0"}, {"CQBAutoHeadshot20m", "1"},
        {"NoScopeHeadshot40m", "1"}, {"AimMagnetism40m", "3"}, {"HipfireLock40m", "1"}, {"NoScopeAimLock40m", "1"}, {"NoScopeSpread40m", "0"}, {"CloseRangeHeadshot40m", "1"},
        {"NoScopeHeadshot50m", "1"}, {"AimMagnetism50m", "3"}, {"HipfireLock50m", "1"}, {"NoScopeAimLock50m", "1"}, {"NoScopeSpread50m", "0"}, {"MidRangeNoScope50m", "1"},
        {"NoScopeHeadshot100m", "1"}, {"AimMagnetism100m", "3"}, {"HipfireLock100m", "1"}, {"NoScopeAimLock100m", "1"}, {"NoScopeSpread100m", "0"}, {"ExtremeNoScope100m", "1"},
        {"AllGunNoScopeHeadshot", "1"}, {"NoScopeHeadLock", "1"}, {"NoScopeAimMagnetism", "3"}, {"NoScopeCrosshairAccuracy", "1.0"},
        {"HipfireMagnetism", "3"}, {"HeadBoneLock", "1"}, {"InstantAimSnap", "1"}, {"TrackingBullet", "1"},
        // ─── Tiered Scope-On Auto Headshot (100m, 200m, 300m, 400m - All Rifles) ─
        {"RifleScopeHeadshot100m", "1"}, {"RifleScopeMagnetism100m", "3"}, {"AimSnapHead100m", "1"}, {"Scope1xHeadLock", "1"}, {"Scope1xAimMagnetism", "3"},
        {"RifleScopeHeadshot200m", "1"}, {"RifleScopeMagnetism200m", "3"}, {"AimSnapHead200m", "1"}, {"MidScopeRecoilZero", "1"}, {"ARSMGHeadLock", "1"}, {"ScopeAimMag", "3"}, {"GyroMidStabilize", "1"}, {"PredictiveAim200m", "1"},
        {"RifleScopeHeadshot300m", "1"}, {"RifleScopeMagnetism300m", "3"}, {"AimSnapHead300m", "1"}, {"SniperMarkHeadLock", "1"}, {"BulletDropCompensation", "1"}, {"ZeroHoldBreath", "1"}, {"LRScopeAimLock", "1"}, {"BulletDropComp300m", "1"},
        {"RifleScopeHeadshot400m", "1"}, {"RifleScopeMagnetism400m", "3"}, {"AimSnapHead400m", "1"}, {"SniperBlankScope", "1"}, {"HitscanLRLock", "1"}, {"ZeroMicroJitter", "1"}, {"UltraRangeHeadLock", "1"}, {"LongRangePrecision400m", "1"},
        {"AllRifleAutoHeadshot", "1"}, {"RifleZeroRecoil", "1"}, {"RifleZeroSpread", "1"}, {"RifleScopeAimMagnetism", "3"},
        // Global CODM gunplay, burst, BSA removal & gyro stabilization
        {"AutoHeadshotBurst", "3"}, {"Auto3BulletHeadshot", "1"}, {"HeadshotBurstCount", "3"},
        {"BSARemoval", "1"}, {"WeaponSpread", "0"}, {"RecoilScale", "0"}, {"ZeroRecoil", "1"},
        {"LessRecoil", "1"}, {"BulletSpreadAccuracy", "0"}, {"BulletTrackingEnemy", "1"},
        {"DamageRangeFalloffBypass", "1"}, {"ZeroFlinch", "1"}, {"FlinchMultiplier", "0"},
        {"DamageLockMax", "1"}, {"ArmorRating", "10000"}, {"PhysicalDefense", "10000"},
        {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"},
        {"GyroMicroSmoothing", "1"}, {"GyroAimAssistLock", "1"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"ZeroInputDelay", "1"},
        {"ADSInstantTransition", "1"}, {"SprintToFireDelay", "0"},
        {"bFramePacingEnabled", "True"}, {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
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
    LOGI("CodmAllScopeTieredHeadshot injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamage", "10000"},
        {"DPSOverdrive", "10000"}, {"FireRateMultiplier", "10.0"}, {"CycleTimeReduction", "1.0"},
        {"BoltActionSpeedBoost", "5.0"}, {"ADSFireRateSync", "1000"}, {"TriggerZeroDeadzone", "1"},
        {"HitRegSyncRate", "1000"}, {"InstantHitReg", "1"}, {"PenetrationBoost", "10000"},
        {"TrueDamageBoost", "10000"}, {"ZeroRecoil", "1"}, {"RecoilScale", "0"},
        {"WeaponSpread", "0"}, {"SlideCancelInstant", "1"}, {"QuickDrawZeroDelay", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "CodmDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// CODM: 165 FPS & Ultra Graphics Native Injector
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodm165FpsGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps, jint qualityLevel) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    int fps = (targetFps >= 120) ? targetFps : 165;
    int q = (qualityLevel > 0) ? qualityLevel : 4;
    std::string fpsStr = std::to_string(fps);
    std::string qStr = std::to_string(q);

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isControlsIni = (pathStr.rfind("ControlsSettings.ini") != std::string::npos);
    bool isHwJson = (pathStr.rfind("HardwareProfile.json") != std::string::npos);

    if (isHwJson) {
        std::vector<std::pair<std::string, std::string>> hwKeys = {
            {"DeviceModel", "SM-S948B"}, {"DeviceBrand", "samsung"}, {"Manufacturer", "samsung"},
            {"GPURenderer", "Adreno (TM) 840"}, {"GPUVendor", "Qualcomm"}, {"SoCModel", "SM8850-AB"},
            {"SoCManufacturer", "Qualcomm"}, {"CPUCores", "8"}, {"RAMTotalMB", "16384"},
            {"MaxFrameRate", fpsStr}, {"TargetFPS", fpsStr}, {"FPSLimit", fpsStr},
            {"FrameRateLimit", fpsStr}, {"MobileFPSLimit", fpsStr}, {"FrameRateLevel", "9"},
            {"GraphicQuality", qStr}, {"UnlockUltraHighFPS", "true"}, {"Unlock185Hz", "true"},
            {"Unlock165Hz", "true"}, {"Unlock144Hz", "true"}, {"Unlock120Hz", "true"},
            {"VulkanSupport", "true"}
        };
        for (const auto& kv : hwKeys) {
            bool isNum = (kv.second == "true" || kv.second == "false" ||
                          (kv.second.find_first_not_of("0123456789.") == std::string::npos));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else if (isControlsIni) {
        std::vector<std::pair<std::string, std::string>> ctrlKeys = {
            {"TouchBoostHz", fpsStr}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
            {"GyroSampleRate", "1000"}, {"GyroSensitivityRatio", "2.5"}, {"GyroZeroDelay", "1"},
            {"GyroSmoothFactor", "1"}, {"GyroStabilization", "1"}, {"JoystickZeroDeadzone", "1"},
            {"JoystickResponseLevel", "3"}, {"ZeroInputLag", "1"}
        };
        for (const auto& kv : ctrlKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else {
        std::vector<std::pair<std::string, std::string>> keys = {
            {"MaxFrameRate", fpsStr}, {"TargetFPS", fpsStr}, {"FPSLimit", fpsStr},
            {"FrameRateLimit", fpsStr}, {"MobileFPSLimit", fpsStr}, {"FrameRateLevel", "9"},
            {"GraphicQuality", qStr}, {"TextureQuality", qStr}, {"ShadowQuality", "0"},
            {"ShadowResolution", "512"}, {"AntiAliasingQuality", "0"}, {"BloomQuality", "0"},
            {"MaxAnisotropy", "1"}, {"HDRMode", "1"}, {"HDR10Plus", "1"}, {"HDRColorMode", "2"},
            {"UltraHDMode", "1"}, {"SuperResolution", "0"}, {"ResolutionScale", "100"},
            {"UltraExtreme", "1"}, {"bUseUltraExtreme", "True"}, {"bFramePacingEnabled", "True"},
            {"Vsync", "0"}, {"Unlock120Hz", "1"}, {"Unlock144Hz", "1"},
            {"Unlock165Hz", "1"}, {"Unlock185Hz", "1"}, {"Unlock240Hz", "1"},
            {"Unlock144FPS", "1"}, {"Unlock165FPS", "1"}, {"Unlock185FPS", "1"},
            {"Ultra144FPS", "1"}, {"Ultra165FPS", "1"},
            {"TouchBoostHz", fpsStr}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
            {"GyroSampleRate", "1000"}, {"PreloadShaders", "1"}, {"VulkanPipelineCache", "1"},
            {"AsyncCompute", "1"}, {"VRS", "1"}, {"AllowOcclusionQueries", "1"}
        };
        for (const auto& kv : keys) {
            if (isXml) {
                std::string t = "int";
                if (kv.second.find('.') != std::string::npos) t = "float";
                else if (kv.second == "True" || kv.second == "False") t = "string";
                patch_xml_node(content, t, kv.first, kv.second);
            } else if (isJson) {
                bool isNum = (kv.second == "True" || kv.second == "False" ||
                              (kv.second.find_first_not_of("0123456789.") == std::string::npos));
                patch_json_node(content, kv.first, kv.second, isNum);
            } else {
                patch_key_value(content, kv.first, kv.second);
            }
        }
    }

    bool ok = write_file_atomic(pathStr, content, 0666);
    if (ok && hasStat) {
        struct utimbuf t;
        t.actime = stBefore.st_atime;
        t.modtime = stBefore.st_mtime;
        utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("Codm165FpsGraphics injected: %s [ok=%d, fps=%d, q=%d]", pathStr.c_str(), ok, fps, q);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// CODM: Max Damage All Weapon 2026 — all weapon classes full stack
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMaxDamageAllWeapon2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("GraphicsSettings.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"HeadshotMultiplier","2"},
        {"PenetrationDamageLoss","0"},{"ArmorPiercingOverdrive","1"},{"VestDamageBypass","1"},
        {"BSARemoval","1"},{"ZeroDamageDropoff","1"},{"DamageRangeFalloffBypass","1"},
        {"TrueDamageBoost","1"},{"FrameSyncDamage","1"},
        {"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},{"InstantHitReg","1"},
        {"BulletVelocityComp","1"},{"MuzzleVelocityFactor","1.0"},
        {"AR_MaxDamageLock","1"},{"AR_RecoilZero","1"},{"AR_SpreadZero","1"},
        {"AR_AimMagnetism","3"},{"AR_AccuracyMax","1"},{"AR_BulletVelocityBoost","1"},
        {"SMG_ZeroRecoil","1"},{"SMG_ZeroSpread","1"},{"SMG_RapidFireHitReg","1000"},{"SMG_DamageMultiplier","1.0"},
        {"Sniper_ZeroSway","1"},{"Sniper_HeadshotLock","1"},{"Sniper_InstantHitReg","1"},
        {"Sniper_MaxDamageLock","1"},{"Sniper_BulletDropComp","1"},
        {"DMR_ZeroSway","1"},{"DMR_MaxDPSLock","1"},{"DMR_RecoilRecovery","10"},
        {"Shotgun_ZeroPelletRNG","1"},{"Shotgun_MaxDamageBurst","1"},{"Shotgun_HitSync","1000"},{"Shotgun_TightPelletSpread","1"},
        {"LMG_ZeroBloom","1"},{"LMG_AimLock","1"},{"LMG_HitRegSync","1000"},{"LMG_RecoilCeiling","0"},
        {"Pistol_AimMagnetism","3"},{"Pistol_ZeroDelay","1"},{"Pistol_RecoilDamp","1"},
        {"RecoilScale","0"},{"VerticalRecoilScale","0"},{"HorizontalRecoilScale","0"},
        {"RecoilPatternScale","0"},{"RecoilMultiplier","0"},
        {"WeaponSpread","0"},{"WeaponSway","0"},{"BulletSpreadScale","0"},
        {"SpreadDecayRate","10"},{"MuzzleSpread","0"},{"MovingSpreadFactor","0"},{"JumpSpreadFactor","0"},
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"AimSnapSpeed","10"},{"AimSnapThreshold","0"},{"AimSmoothFactor","0"},
        {"AdsZeroDelay","1"},{"PredictiveAim","1"},
        {"Scope2xStabilizer","1"},{"Scope4xStabilizer","1"},{"Scope6xStabilizer","1"},
        {"Scope8xStabilizer","1"},{"ScopeBreathingDamp","1"},{"ScopeSwayDamp","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"JoystickZeroDeadzone","1"},{"JoystickResponseLevel","3"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},{"PreloadShaders","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"FrameRateLevel","9"},{"ResolutionScale","100"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmMaxDamageAllWeapon2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// CODM: Ultra Config Cheat 2026 — Full Stack: Damage + Aim + Speed + Graphics
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmUltraConfigCheat2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("GraphicsSettings.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        // Damage
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"HeadshotMultiplier","2"},
        {"TrueDamageBoost","1"},{"FrameSyncDamage","1"},
        {"HitRegSyncRate","1000"},{"InstantHitReg","1"},
        // Aim
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"AimSnapSpeed","10"},{"AimSnapThreshold","0"},{"AimSmoothFactor","0"},
        {"AdsZeroDelay","1"},{"PredictiveAim","1"},
        // No recoil / no spread
        {"RecoilScale","0"},{"VerticalRecoilScale","0"},{"HorizontalRecoilScale","0"},
        {"RecoilPatternScale","0"},{"RecoilMultiplier","0"},
        {"WeaponSpread","0"},{"WeaponSway","0"},{"BulletSpreadScale","0"},
        {"SpreadDecayRate","10"},{"MuzzleSpread","0"},
        {"MovingSpreadFactor","0"},{"JumpSpreadFactor","0"},
        // Speed & movement
        {"MovementSpeedBoost","1"},{"SprintSpeedMax","1"},{"SlideDistanceMax","1"},
        {"SlideSpeedBoost","1"},{"JumpHeightBoost","1"},
        // Graphics unlock 2026
        {"FrameRateLevel","9"},{"ResolutionScale","100"},
        {"HDR10Plus","1"},{"UltraExtreme2026","1"},
        {"VulkanPipelineCache","1"},{"AsyncCompute","1"},{"VRS","1"},
        {"PreloadShaders","1"},{"bPreloadShaders","True"},
        {"ShaderPrecompile","1"},{"ShaderWarmupAtLaunch","1"},
        // Gyro & touch
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"JoystickZeroDeadzone","1"},{"JoystickResponseLevel","3"},
        // Frame & engine
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"ScreenShake","0"},{"Vibrate","0"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmUltraConfigCheat2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastLoadShaderBypass(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"FastLoad", "1"}, {"SkipIntroMovie", "1"},
        {"AsyncAssetLoading", "1"}, {"TextureStreamBufferSize", "512"},
        {"MaxAsyncLoadingTasks", "8"}, {"PreloadWeaponModels", "0"},
        {"ShaderPrewarmAtStartup", "0"}, {"FastShaderWarmup", "1"},
        {"LoadBalanceMode", "1"}
    };
    for (const auto& kv : keys) {
        if (isJson) {
            patch_json_node(content, kv.first, kv.second, true);
        } else if (isXml) {
            patch_xml_node(content, "int", kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmFastLoadShaderBypass injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// CODM: Auto Head 5-Bullet Aim Assist Lock & Target Scope Tracking (50m-300m)
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAutoHead5BulletAimLock(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ─── 5-Bullet Auto Headshot Burst Lock ───
        {"Auto5BulletHeadshot",       "1"},
        {"HeadshotBurstCount",        "5"},
        {"AutoHeadshotBurst",         "5"},
        {"FiveBulletHeadLock",        "1"},
        {"BurstFireRateLock",         "5"},
        {"HeadBoneLock",              "1"},
        {"InstantAimSnap",            "1"},

        // ─── No-Scope Auto Headshot & CQB Tracking (50m Only) ───
        {"NoScope50mOnly",            "1"},
        {"NoScopeAimbotMaxRange",     "50"},
        {"NoScopeMaxRange",           "50"},
        {"NoScope50mHeadLock",        "1"},
        {"NoScopeHeadshot50m",        "1"},
        {"NoScopeAimLock50m",         "1"},
        {"NoScopeSpread50m",          "0"},
        {"NoScopeAimMagnetism50m",    "3"},
        {"HipfireLock50m",            "1"},
        {"HipfireHeadLock50m",        "1"},
        {"CQBAutoHeadshot50m",        "1"},
        {"NoScopeMagicBullet50m",     "1"},
        {"ScopeLock50m",              "1"},
        {"AimTrackingLock50m",        "1"},
        {"AimMagnetism50m",           "3"},

        // ─── May-Scope Auto Headshot: 100m Tier (Tactical / Red Dot / Holo) ───
        {"MayScope100m",              "1"},
        {"ScopeAimbotTier100m",       "1"},
        {"ScopeLock100m",             "1"},
        {"AimTrackingLock100m",       "1"},
        {"Scope1xHeadLock100m",       "1"},
        {"RedDotAimLock100m",         "1"},
        {"HoloZeroDeadzone100m",      "1"},
        {"ScopeAimMag100m",           "3"},
        {"AimMagnetism100m",          "3"},
        {"AimSnapHead100m",           "1"},

        // ─── May-Scope Auto Headshot: 150m Tier (2x / 3x Scope) ───
        {"MayScope150m",              "1"},
        {"ScopeAimbotTier150m",       "1"},
        {"ScopeLock150m",             "1"},
        {"AimTrackingLock150m",       "1"},
        {"Scope2xHeadLock150m",       "1"},
        {"Scope3xHeadLock150m",       "1"},
        {"MidRangeScopeSnap150m",     "1"},
        {"ScopeAimMag150m",           "3"},
        {"GyroMidStabilize150m",      "1"},
        {"AimMagnetism150m",          "3"},

        // ─── May-Scope Auto Headshot: 200m Tier (3x / 4x AR & Marksman) ───
        {"MayScope200m",              "1"},
        {"ScopeAimbotTier200m",       "1"},
        {"ScopeLock200m",             "1"},
        {"AimTrackingLock200m",       "1"},
        {"ARSMGHeadLock200m",         "1"},
        {"Scope4xHeadLock200m",       "1"},
        {"PredictiveAim200m",         "1"},
        {"ZeroRecoil200m",            "1"},
        {"ScopeAimMag200m",           "3"},
        {"AimMagnetism200m",          "3"},

        // ─── May-Scope Auto Headshot: 250m Tier (4.4x / 6x Marksman & Sniper) ───
        {"MayScope250m",              "1"},
        {"ScopeAimbotTier250m",       "1"},
        {"ScopeLock250m",             "1"},
        {"AimTrackingLock250m",       "1"},
        {"Scope4xHeadLock250m",       "1"},
        {"Scope6xHeadLock250m",       "1"},
        {"SniperMarkHeadLock250m",    "1"},
        {"BulletDropComp250m",        "1"},
        {"TargetLeadComp250m",        "1"},
        {"ScopeAimMag250m",           "3"},
        {"AimMagnetism250m",          "3"},

        // ─── May-Scope Auto Headshot: 300m Tier (6x / 8x Sniper Scope) ───
        {"MayScope300m",              "1"},
        {"ScopeAimbotTier300m",       "1"},
        {"ScopeLock300m",             "1"},
        {"AimTrackingLock300m",       "1"},
        {"SniperScopeLock300m",       "1"},
        {"Scope6xHeadLock300m",       "1"},
        {"Scope8xHeadLock300m",       "1"},
        {"BulletDropComp300m",        "1"},
        {"ZeroHoldBreath300m",        "1"},
        {"ScopeAimMag300m",           "3"},
        {"AimMagnetism300m",          "3"},

        // ─── May-Scope Auto Headshot: 350m Tier (Extreme Long-Range Hitscan) ───
        {"MayScope350m",              "1"},
        {"ScopeAimbotTier350m",       "1"},
        {"ScopeLock350m",             "1"},
        {"AimTrackingLock350m",       "1"},
        {"Scope8xHeadLock350m",       "1"},
        {"HitscanLRLock350m",         "1"},
        {"UltraRangeHeadLock350m",    "1"},
        {"LongRangePrecision350m",    "1"},
        {"BulletDropComp350m",        "1"},
        {"ZeroMicroJitter350m",       "1"},
        {"ScopeAimMag350m",           "3"},
        {"AimMagnetism350m",          "3"},

        // ─── Magic Bullet Overdrive Keys ───
        {"NoScopeMagicBullet",        "1"},
        {"MagicBulletFollowEnemy",    "1"},
        {"BulletMagnetEnemy",         "1"},
        {"ScopedMagicBullet",         "1"},

        // ─── Armor Boost & Damage Overdrive (Easy Kill Enemy) ───
        {"BSARemoval",                "1"},
        {"BulletSpreadAccuracy",      "0"},
        {"RecoilScale",               "0"},
        {"ZeroRecoil",                "1"},
        {"LessRecoil",                "1"},
        {"WeaponSpread",              "0"},
        {"DamageLockMax",             "10000"},
        {"DamageBoost",               "10000"},
        {"PhysicalDefense",           "10000"},
        {"ArmorRating",               "10000"},
        {"DamageReduction",           "0.90"},
        {"TrueDamageMod",             "1"},
        {"InstantHitReg",             "1"},
        {"HitRegSyncRate",            "1000"},
        {"BulletTrackingEnemy",       "1"},
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputDelay",            "1"},
        {"GyroSampleRate",            "1000"},
        {"bFramePacingEnabled",       "True"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"}
    };

    for (const auto &kv : keys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf t;
        t.actime = stBefore.st_atime;
        t.modtime = stBefore.st_mtime;
        utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmAutoHead5BulletAimLock injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Distance-Tiered Auto-Aimbot & Magic Bullet 5-Head (2026) ──────────
// No-Scope: strictly <= 50m CQB lock + zero hipfire spread + magic bullet bend.
// May-Scope: 100m, 150m, 200m, 250m, 300m, 350m tiered scope aimbot tracking.
// Magic Bullet: predictive path bending + target enemy tracking on both modes.
// 5-Head Auto: 5-bullet burst headshot lock across all gun classes.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmDistanceTieredAimbot(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ─── Master Feature Switches ───
        {"DistanceTieredAimbot",        "1"},
        {"CodmDistanceTieredAimbotV2",  "1"},
        {"NoScope50mOnly",              "1"},
        {"MayScopeMultiDistance",       "1"},

        // ─── No-Scope Auto-Aimbot: Strictly 50m Only ───
        {"NoScopeAimbotMaxRange",       "50"},
        {"NoScopeMaxRange",             "50"},
        {"NoScope50mHeadLock",          "1"},
        {"NoScopeHeadshot50m",          "1"},
        {"NoScopeAimLock50m",           "1"},
        {"NoScopeSpread50m",            "0"},
        {"NoScopeAimMagnetism50m",      "3"},
        {"NoScopeCrosshairAccuracy50m", "1.0"},
        {"HipfireLock50m",              "1"},
        {"HipfireHeadLock50m",          "1"},
        {"CQBAutoHeadshot50m",          "1"},
        {"ExtremeHipfireLock50m",       "1"},
        {"NoScopeMagicBullet50m",       "1"},

        // ─── May-Scope Auto-Aimbot: 100m Tier (Tactical / Red Dot / Holo) ───
        {"MayScope100m",                "1"},
        {"ScopeAimbotTier100m",         "1"},
        {"ScopeLock100m",               "1"},
        {"AimTrackingLock100m",         "1"},
        {"Scope1xHeadLock100m",         "1"},
        {"RedDotAimLock100m",           "1"},
        {"HoloZeroDeadzone100m",        "1"},
        {"ScopeAimMag100m",             "3"},
        {"AimMagnetism100m",            "3"},
        {"AimSnapHead100m",             "1"},

        // ─── May-Scope Auto-Aimbot: 150m Tier (2x / 3x Scope) ───
        {"MayScope150m",                "1"},
        {"ScopeAimbotTier150m",         "1"},
        {"ScopeLock150m",               "1"},
        {"AimTrackingLock150m",         "1"},
        {"Scope2xHeadLock150m",         "1"},
        {"Scope3xHeadLock150m",         "1"},
        {"ScopeAimMag150m",             "3"},
        {"AimMagnetism150m",            "3"},
        {"MidRangeScopeSnap150m",       "1"},
        {"GyroMidStabilize150m",        "1"},
        {"PredictiveAim150m",           "1"},

        // ─── May-Scope Auto-Aimbot: 200m Tier (3x / 4x Scope AR & Marksman) ───
        {"MayScope200m",                "1"},
        {"ScopeAimbotTier200m",         "1"},
        {"ScopeLock200m",               "1"},
        {"AimTrackingLock200m",         "1"},
        {"ARSMGHeadLock200m",           "1"},
        {"Scope4xHeadLock200m",         "1"},
        {"PredictiveAim200m",           "1"},
        {"ZeroRecoil200m",              "1"},
        {"ScopeAimMag200m",             "3"},
        {"AimMagnetism200m",            "3"},

        // ─── May-Scope Auto-Aimbot: 250m Tier (4.4x / 6x Marksman & Sniper) ───
        {"MayScope250m",                "1"},
        {"ScopeAimbotTier250m",         "1"},
        {"ScopeLock250m",               "1"},
        {"AimTrackingLock250m",         "1"},
        {"Scope4xHeadLock250m",         "1"},
        {"Scope6xHeadLock250m",         "1"},
        {"SniperMarkHeadLock250m",      "1"},
        {"BulletDropComp250m",          "1"},
        {"TargetLeadComp250m",          "1"},
        {"ScopeAimMag250m",             "3"},
        {"AimMagnetism250m",            "3"},

        // ─── May-Scope Auto-Aimbot: 300m Tier (6x / 8x Sniper Scope) ───
        {"MayScope300m",                "1"},
        {"ScopeAimbotTier300m",         "1"},
        {"ScopeLock300m",               "1"},
        {"AimTrackingLock300m",         "1"},
        {"SniperScopeLock300m",         "1"},
        {"Scope6xHeadLock300m",         "1"},
        {"Scope8xHeadLock300m",         "1"},
        {"BulletDropComp300m",          "1"},
        {"ZeroHoldBreath300m",          "1"},
        {"ScopeAimMag300m",             "3"},
        {"AimMagnetism300m",            "3"},

        // ─── May-Scope Auto-Aimbot: 350m Tier (Extreme Long-Range Hitscan Sniper) ───
        {"MayScope350m",                "1"},
        {"ScopeAimbotTier350m",         "1"},
        {"ScopeLock350m",               "1"},
        {"AimTrackingLock350m",         "1"},
        {"Scope8xHeadLock350m",         "1"},
        {"HitscanLRLock350m",           "1"},
        {"UltraRangeHeadLock350m",      "1"},
        {"LongRangePrecision350m",      "1"},
        {"BulletDropComp350m",          "1"},
        {"ZeroMicroJitter350m",         "1"},
        {"ScopeAimMag350m",             "3"},
        {"AimMagnetism350m",            "3"},

        // ─── May-Scope Auto-Aimbot: 500m, 750m, 1000m Tiers ───
        {"MayScope500m",                "1"},
        {"ScopeAimbotTier500m",         "1"},
        {"ScopeLock500m",               "1"},
        {"AimTrackingLock500m",         "1"},
        {"UltraRangeHeadLock500m",      "1"},
        {"MayScope750m",                "1"},
        {"ScopeAimbotTier750m",         "1"},
        {"ScopeLock750m",               "1"},
        {"AimTrackingLock750m",         "1"},
        {"UltraRangeHeadLock750m",      "1"},
        {"MayScope1000m",               "1"},
        {"ScopeAimbotTier1000m",        "1"},
        {"ScopeLock1000m",              "1"},
        {"AimTrackingLock1000m",        "1"},
        {"UltraRangeHeadLock1000m",     "1"},
        {"AllScopeAutoAimbot",          "1000"},
        {"AllScopeAimSnap",             "1000"},
        {"AllScopeAimMagnetism",        "1000"},
        {"NoScopeAutoAimbot",           "1000"},
        {"NoScopeDamageBoost",          "1000"},
        {"NoScopeDamageMultiplier",     "1000"},
        {"NoScopeTrueDamage",           "1000"},
        {"DamageLockMax",               "1000"},
        {"DamageBoost",                 "1000"},
        {"InstantHitDamage",            "1000"},
        {"MagicBullet1000",             "1"},
        {"MagicBulletMultiplier",       "1000"},
        {"MagicBulletDamage",           "1000"},
        {"MagicBulletAttractStrength",  "1000"},
        {"BulletMagnetEnemyOverdrive",  "1000"},
        {"MagicBulletHeadAttractFactor1000","1000.0"},

        // ─── Magic Bullet Overdrive: No-Scope <=50m & May-Scope 100m-350m ───
        {"MagicBulletEnabled",          "1"},
        {"NoScopeMagicBullet",          "1"},
        {"MagicBulletFollowEnemy",      "1"},
        {"BulletMagnetEnemy",           "1"},
        {"BulletTrackingEnemy",         "1"},
        {"TrackingBullet",              "1"},
        {"TrackingBulletVelocity",      "1"},
        {"ScopedMagicBullet",           "1"},
        {"InstantHitReg",               "1"},
        {"HitRegSyncRate",              "1000"},
        {"BulletVelocityComp",          "1"},

        // ─── 5-Head Auto Headshot Burst Lock ───
        {"Auto5BulletHeadshot",         "1"},
        {"HeadshotBurstCount",          "5"},
        {"FiveBulletHeadLock",          "1"},
        {"BurstFireRateLock",           "5"},
        {"AutoHeadshotBurst",           "5"},
        {"HeadBoneLock",                "1"},
        {"HeadBoneAimPriority",         "1"},
        {"HeadMagnetism",               "1"},
        {"InstantAimSnap",              "1"},
        {"AimSnapSpeed",                "10"},
        {"AimSnapThreshold",            "0"},
        {"AimSmoothFactor",             "0"},

        // ─── BSA Removal & Zero Recoil ───
        {"BSARemoval",                  "1"},
        {"BulletSpreadAccuracy",        "0"},
        {"RecoilScale",                 "0"},
        {"ZeroRecoil",                  "1"},
        {"LessRecoil",                  "1"},
        {"WeaponSpread",                "0"},
        {"BulletSpreadScale",           "0"},
        {"HipfireSpread",               "0"},
        {"InitialBulletSpread",         "0.0"},
        {"DamageRangeFalloffBypass",    "1"},

        // ─── Gyro & Input Overclock ───
        {"GyroSampleRate",              "1000"},
        {"GyroZeroDelay",               "1"},
        {"GyroStabilization",           "1"},
        {"GyroLatencyMode",             "0"},
        {"TouchPollingRate",            "1000"},
        {"TouchZeroDelay",              "1"},
        {"ZeroInputLag",                "1"},
        {"ZeroInputDelay",              "1"},
        {"bFramePacingEnabled",         "True"},
        {"r.OneFrameThreadLag",         "0"},
        {"r.FinishCurrentFrame",        "0"}
    };

    for (const auto &kv : keys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmDistanceTieredAimbot injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Scope Target Tracking + Magic Bullet 5-Head (2026) ────────────────
// ADSScopeHeadLock follows enemy head on scope-on. No-scope magic bullet bend.
// 5-bullet burst headshot lock. BSA removal. Zero spread all states.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmScopeTargetTrackingMagicBullet5Head(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ── Scope Target Tracking ──
        {"ScopeOnEnemyTrack",          "1"},
        {"ScopeTrackEnemyHead",        "1"},
        {"ADSScopeHeadLock",           "1"},
        {"ScopedMagnetism",            "3"},
        {"AimTrackingWhenScoped",      "1"},
        {"CodmScopedAutoHeadLock",     "1"},
        // ── No-Scope Magic Bullet ──
        {"NoScopeMagicBullet",         "1"},
        {"MagicBulletFollowEnemy",     "1"},
        {"BulletMagnetEnemy",          "1"},
        // ── 5-Bullet Headshot Burst Lock ──
        {"Auto5BulletHeadshot",        "1"},
        {"HeadshotBurstCount",         "5"},
        {"FiveBulletHeadLock",         "1"},
        {"HeadBoneLock",               "1"},
        {"HeadMagnetism",              "1"},
        {"InstantAimSnap",             "1"},
        // ── No Spread / BSA Removal ──
        {"WeaponSpread",               "0"},
        {"BulletSpreadScale",          "0"},
        {"BSARemoval",                 "1"},
        {"HipfireSpread",              "0"},
        {"MovingSpreadFactor",         "0"},
        {"JumpSpreadFactor",           "0"},
        // ── Gyro + Hit Reg ──
        {"GyroSampleRate",             "1000"},
        {"GyroZeroDelay",              "1"},
        {"HitRegSyncRate",             "1000"},
        {"TouchPollingRate",           "1000"},
    };

    for (const auto &kv : keys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmScopeTargetTrackingMagicBullet5Head injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Fast Reload + Fast HP Regen + Fast Gun Switch + Fast Run (2026) ───
// Zero reload, max HP instant regen, zero swap times, slide cancel instant,
// sprint speed boost.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastReloadHpRegenGunSwitchRun(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ── Fast Reload ──
        {"ReloadTimeMin",              "0"},
        {"FastReload",                 "1"},
        {"InstantReload",              "1"},
        {"FastBoltPullSpeed",          "10.0"},
        {"PumpActionCycleMs",          "0"},
        {"MagChangeTimeMs",            "0"},
        {"QuickReloadFactor",          "10.0"},
        // ── Fast HP Regen ──
        {"HpRegenRate",                "10"},
        {"HpRegenDelay",               "0"},
        {"FastHpRegen",                "1"},
        {"HealthRegenMax",             "1"},
        {"HealingRate",                "10"},
        {"HealingSpeedMax",            "1"},
        {"HpRegenCooldown",            "0"},
        // ── Fast Gun Switch / Weapon Swap ──
        {"WeaponSwapTime",             "0"},
        {"HolsterTime",                "0"},
        {"DrawTime",                   "0"},
        {"FastGunSwitch",              "1"},
        {"QuickDrawFactor",            "10.0"},
        {"QuickDrawZeroDelay",         "1"},
        {"SprintToFireDelayMs",        "0"},
        {"ADSTransitionTimeMs",        "0"},
        {"SlideCancelInstant",         "1"},
        // ── Fast Run / Sprint ──
        {"SprintSpeedMax",             "1"},
        {"MovementSpeedBoost",         "1"},
        {"SprintSpeedBoost",           "10.0"},
        {"SlideDistanceMax",           "1"},
        {"SlideSpeedBoost",            "1"},
        {"FastSlide",                  "1"},
        // ── Engine Sync ──
        {"TouchPollingRate",           "1000"},
        {"ZeroInputLag",               "1"},
        {"r.OneFrameThreadLag",        "0"},
    };

    for (const auto &kv : keys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmFastReloadHpRegenGunSwitchRun injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Magic Bullet Overdrive (Predictive Trajectory Bending & Hitscan) ──
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMagicBulletOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ─── Magic Bullet Core Bending & Attraction ───
        {"MagicBulletEnabled",           "1"},
        {"MagicBulletFollowEnemy",       "1"},
        {"BulletMagnetEnemy",            "1"},
        {"BulletTrackingEnemy",          "1"},
        {"TrackingBullet",               "1"},
        {"TrackingBulletVelocity",       "1"},
        {"MagicBullet1000",              "1"},
        {"MagicBulletMultiplier",        "1000"},
        {"MagicBulletDamage",            "1000"},
        {"MagicBulletAttractStrength",   "1000"},
        {"BulletMagnetEnemyOverdrive",   "1000"},
        {"MagicBulletHeadAttractFactor", "1000.0"},
        {"TrackingBulletVelocity1000",   "1000"},
        {"MagicBulletCurveAngle",        "90"},
        {"MagicBulletCurveRate",         "0.98"},
        {"MagicBulletHeadAttractFactor", "1.0"},
        {"BulletTrajectoryMagnet",       "1"},
        {"NoScopeMagicBullet",           "1"},
        {"ScopedMagicBullet",            "1"},
        {"NoScopeMagicBullet50m",        "1"},
        {"CQBMagicBulletBend",           "1"},

        // ─── BSA (Bullet Spread Accuracy) Removal & Laser Precision ───
        {"BSARemoval",                   "1"},
        {"BulletSpreadAccuracy",         "0"},
        {"BulletSpreadScale",            "0"},
        {"WeaponSpread",                 "0"},
        {"InitialBulletSpread",          "0.0"},
        {"SpreadPerShot",                "0"},
        {"MaxBulletSpread",              "0"},
        {"HipfireSpread",                "0"},
        {"HipfireSpreadScale",           "0"},

        // ─── Wall Piercing & Penetration Damage Retention ───
        {"WallPiercingMod",              "1"},
        {"PenetrationDamageLoss",        "0"},
        {"MaterialPenetrationMax",       "1"},
        {"ArmorPiercingOverdrive",       "1"},
        {"DamageRangeFalloffBypass",     "1"},
        {"TrueDamageMod",                "1"},

        // ─── Hitscan Simulation & Hit Registration Synchronization ───
        {"HitscanSimulation",            "1"},
        {"InstantHitReg",                "1"},
        {"HitRegSyncRate",               "1000"},
        {"BulletVelocityComp",           "1"},
        {"MuzzleVelocityFactor",         "2.0"},
        {"ZeroBulletDrop",               "1"},
        {"BulletDropComp",               "1"},
        {"FrameSyncDamage",              "1"},

        // ─── Auto 5-Headshot Lock Synergy ───
        {"Auto5BulletHeadshot",          "1"},
        {"HeadshotBurstCount",           "5"},
        {"FiveBulletHeadLock",           "1"},
        {"BurstFireRateLock",            "5"},
        {"HeadBoneLock",                 "1"},
        {"HeadBoneAimPriority",          "1"},
        {"HeadMagnetism",                "1"},

        // ─── Input & Engine Timing ───
        {"ZeroInputLag",                 "1"},
        {"ZeroInputDelay",               "1"},
        {"TouchPollingRate",             "1000"},
        {"GyroSampleRate",               "1000"},
        {"bFramePacingEnabled",          "True"},
        {"r.OneFrameThreadLag",          "0"},
        {"r.FinishCurrentFrame",         "0"}
    };

    for (const auto &kv : keys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf t;
        t.actime = stBefore.st_atime;
        t.modtime = stBefore.st_mtime;
        utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmMagicBulletOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Full Overdrive 2026 — Master Combo (all above in one pass) ────────
// Calls CodmScopeTargetTrackingMagicBullet5Head + CodmFastReloadHpRegenGunSwitchRun
// + CodmMagicBulletOverdrive + existing CodmAutoHead5BulletAimLock in one atomic sweep.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFullOverdrive2026(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    env->ReleaseStringUTFChars(jPath, path);

    bool r0 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmDistanceTieredAimbot(env, nullptr, jPath);
    bool r1 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmScopeTargetTrackingMagicBullet5Head(env, nullptr, jPath);
    bool r2 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastReloadHpRegenGunSwitchRun(env, nullptr, jPath);
    bool r3 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAutoHead5BulletAimLock(env, nullptr, jPath);
    bool r4 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMagicBulletOverdrive(env, nullptr, jPath);
    LOGI("CodmFullOverdrive2026 injected [tiered=%d scope=%d fast=%d lock=%d magic=%d]", r0, r1, r2, r3, r4);
    return (r0 || r1 || r2 || r3 || r4) ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Bullet Tracking + 5-Burst Headshot Overdrive (NEW 2026) ────────────
// Injects: BulletTrackingMode, BulletBodyHit, BulletHeadshotPriority,
//          BulletBurst5, Bullet5BurstDamage=1000, BulletBurstHeadshot=1000,
//          BulletBendFactor=1000, BulletTravelTime=0, MagicBulletBend=1.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmBulletTracking5Burst(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"BulletTrackingMode",      "1"},
        {"BulletBodyHit",           "1"},
        {"BulletHeadshotPriority",  "1"},
        {"BulletBurst5",            "1"},
        {"Bullet5BurstDamage",      "1000"},
        {"BulletBurstHeadshot",     "1000"},
        {"BulletBendFactor",        "1000"},
        {"BulletPredictTrajectory", "1"},
        {"InstantBulletDrop",       "0"},
        {"BulletTravelTime",        "0"},
        {"BulletMagicBend",         "1"},
        {"MultiTargetTracking",     "1"},
        {"BulletMagnetStrength",    "1000"},
        {"AimBoneTarget",           "0"},
        {"HeadBoneAttractFactor",   "1000"},
        {"BulletAutoCorrect",       "1"},
        {"MagicBulletBend",         "1"},
        {"BulletTrackingOverdrive", "1000"},
        {"HeadshotBurstMultiplier", "1000"},
        {"Bullet5HeadshotLock",     "1"},
        {"BulletBodyDamageBoost",   "1000"},
        {"BulletHeadshotDamage",    "1000"},
        {"InstantHitReg",           "1"},
        {"HitRegSyncRate",          "1000"},
        {"FrameSyncDamage",         "1"},
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
    };
    for (const auto &kv : keys) {
        if (isXml)       patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else             patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmBulletTracking5Burst injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Damage Boost + Armor Bypass Overdrive (NEW 2026) ──────────────────
// Injects: TrueDamage=1000, DamageMultiplier=1000, ArmorPiercing=1,
//          VestBypass=1, HelmetBypass=1, LimbDamageBoost=1000,
//          FleshDamageBoost=1000, HeadshotDamage=1000, CritDamageBoost=1000.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmDamageBoostArmor(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"TrueDamage",              "1000"},
        {"DamageMultiplier",        "1000"},
        {"ArmorPiercing",           "1"},
        {"VestBypass",              "1"},
        {"HelmetBypass",            "1"},
        {"LimbDamageBoost",         "1000"},
        {"FleshDamageBoost",        "1000"},
        {"HeadshotDamage",          "1000"},
        {"ArmorPenetration",        "1000"},
        {"PhysicalPenetration",     "1000"},
        {"DefenseBypass",           "1"},
        {"ShieldBreaker",           "1"},
        {"CritRateBoost",           "1"},
        {"CritDamageBoost",         "1000"},
        {"DamageLockMax",           "1000"},
        {"EffectiveDPSMode",        "3"},
        {"HitRegSyncRate",          "1000"},
        {"FrameSyncDamage",         "1"},
        {"BulletBodyDamageBoost",   "1000"},
        {"BulletHeadshotBoost",     "1000"},
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
    };
    for (const auto &kv : keys) {
        if (isXml)       patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else             patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmDamageBoostArmor injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: All-Scope Aimbot Overdrive (NEW 2026) ─────────────────────────────
// Injects per-scope aim-assist + head-lock + bullet tracking for every scope
// tier (iron sights, 1x, 2x, 3x, 4x, 6x, 8x, 10x/sniper). Also enables
// sticky aim, global head magnetism, and zero-delay ADS.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeAimbotOverdrive(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        // Global aim assist
        {"AimAssistEnabled",        "1"},
        {"AimAssistStrength",       "1000"},
        {"AimMagnetism",            "1000"},
        {"HeadMagnetism",           "1"},
        {"StickyAim",               "1"},
        {"AimRetention",            "1000"},
        {"AimBoneTarget",           "0"},
        {"AimSnapSpeed",            "10"},
        {"AimSmoothFactor",         "0"},
        {"AimSnapThreshold",        "0"},
        {"AdsZeroDelay",            "1"},
        {"AllScopeHeadLock",        "1"},
        {"AllScopeAimSnap",         "1000"},
        {"AllScopeAimMagnetism",    "1000"},
        {"ScopeAimbotEnable",       "1"},
        {"MultiTargetTracking",     "1"},
        {"PredictiveAim",           "1"},
        // Iron sights
        {"IronSightHeadLock",       "1"}, {"IronSightAimAssist","1000"}, {"IronSightBulletTracking","1"},
        // 1x
        {"Scope1xHeadLock",         "1"}, {"Scope1xAimAssist","1000"}, {"Scope1xBulletTracking","1"},
        {"Scope1xRecoilDamp",       "1"}, {"Scope1xDriftCancel","1"},
        // 2x
        {"Scope2xHeadLock",         "1"}, {"Scope2xAimAssist","1000"}, {"Scope2xBulletTracking","1"},
        {"Scope2xRecoilDamp",       "1"}, {"Scope2xDriftCancel","1"},
        // 3x
        {"Scope3xHeadLock",         "1"}, {"Scope3xAimAssist","1000"}, {"Scope3xBulletTracking","1"},
        {"Scope3xRecoilDamp",       "1"}, {"Scope3xDriftCancel","1"},
        // 4x
        {"Scope4xHeadLock",         "1"}, {"Scope4xAimAssist","1000"}, {"Scope4xBulletTracking","1"},
        {"Scope4xZeroSway",         "1"}, {"Scope4xDriftCancel","1"},
        // 6x
        {"Scope6xHeadLock",         "1"}, {"Scope6xAimAssist","1000"}, {"Scope6xBulletTracking","1"},
        {"Scope6xZeroSway",         "1"}, {"Scope6xDriftCancel","1"},
        // 8x
        {"Scope8xHeadLock",         "1"}, {"Scope8xAimAssist","1000"}, {"Scope8xBulletTracking","1"},
        {"Scope8xZeroSway",         "1"}, {"Scope8xDriftCancel","1"},
        // 10x / Sniper
        {"Scope10xHeadLock",        "1"}, {"Scope10xAimAssist","1000"}, {"Scope10xBulletTracking","1"},
        {"Scope10xZeroSway",        "1"}, {"SniperHeadLock","1"}, {"SniperNoBreath","1"},
        // Gyro + touch
        {"GyroSampleRate",          "1000"},
        {"GyroZeroDelay",           "1"},
        {"GyroStabilization",       "1"},
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
        {"HitRegSyncRate",          "1000"},
        {"FrameSyncDamage",         "1"},
    };
    for (const auto &kv : keys) {
        if (isXml)       patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else             patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmAllScopeAimbotOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}



