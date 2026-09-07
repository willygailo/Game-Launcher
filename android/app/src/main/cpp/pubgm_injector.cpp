// =============================================================================
// PUBGM (PUBG Mobile) Dedicated Native Injector
// High-performance isolated translation unit for GameBooster Native
// =============================================================================

#include "native_config_injector.h"
#include "config_common.h"

// ─────────────────────────────────────────────────────────────────────────────
// ─── PUBGM: Magic Bullet Aimbot + No Recoil ──────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// "Magic bullet" in config terms = maximum bullet-velocity compensation + predictive
// aim CVars + gyro 1000Hz tracking + zero weapon sway + zero spread config keys.
//
// Config injected (UE4 CVar format for UserCustom.ini):
//   r.PUBGBulletVelocityCompensation=1  : bullet drop / travel-time compensation
//   r.PredictiveAim=1                   : predictive aim-ahead calculation
//   r.AimAssistEnabled=1                : aim assist master switch
//   r.AimAssistStrength=100             : max strength (0-100 scale)
//   r.AimSnapThreshold=0                : snap to target with zero dead zone
//   r.AimMagnetism=3                    : max magnetism tier
//   r.HeadBoneAimPriority=1             : prefer head bone target
//   r.WeaponSpread=0                    : zero horizontal/vertical spread
//   r.WeaponSway=0                      : zero weapon idle sway
//   r.WeaponRecoilScale=0               : zero recoil scale factor
//   r.RecoilPatternScale=0              : zero recoil pattern
//   r.VerticalRecoilScale=0             : zero vertical kick
//   r.HorizontalRecoilScale=0           : zero horizontal kick
//   r.BulletSpreadScale=0               : zero spread cone
//   r.MuzzleVelocityFactor=1.0          : 100% bullet velocity (no penalty)
//   r.GyroSampleRate=1000               : 1000 Hz gyro
//   r.GyroSensitivityRatio=2.5          : gyro sensitivity multiplier
//   r.GyroZeroDelay=1                   : gyro zero delay
//   r.GyroLatencyMode=0                 : raw gyro (no smoothing)
//   r.GyroStabilization=1               : gyro stabilization on
//   r.GyroSmoothFactor=1                : gyro smooth factor on
//   TouchPollingRate=1000               : 1000 Hz touch
//   r.OneFrameThreadLag=0               : zero render thread lag
//   r.FinishCurrentFrame=0              : immediate frame finish
//   bFramePacingEnabled=True            : consistent frame delivery
//   AllowOcclusionQueries=1             : occlusion-based hit detection
//   HitRegSyncRate=1000                 : hit-reg 1000 Hz
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMagicBulletAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    // CVar format for UserCustom.ini; fallback plain-key for non-UE4
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    // ── CVar-style keys (UE4 / PUBGM) ──
    std::vector<std::pair<std::string, std::string>> cvarKeys = {
        {"r.PUBGBulletVelocityCompensation", "1"},
        {"r.PredictiveAim",                  "1"},
        {"r.AimAssistEnabled",               "1"},
        {"r.AimAssistStrength",              "100"},
        {"r.AimSnapThreshold",               "0"},
        {"r.AimMagnetism",                   "3"},
        {"r.HeadBoneAimPriority",            "1"},
        {"r.WeaponSpread",                   "0"},
        {"r.WeaponSway",                     "0"},
        {"r.WeaponRecoilScale",              "0"},
        {"r.RecoilPatternScale",             "0"},
        {"r.VerticalRecoilScale",            "0"},
        {"r.HorizontalRecoilScale",          "0"},
        {"r.BulletSpreadScale",              "0"},
        {"r.MuzzleVelocityFactor",           "1.0"},
        {"r.GyroSampleRate",                 "1000"},
        {"r.GyroSensitivityRatio",           "2.5"},
        {"r.GyroZeroDelay",                  "1"},
        {"r.GyroLatencyMode",                "0"},
        {"r.GyroStabilization",              "1"},
        {"r.GyroSmoothFactor",               "1"},
        {"r.OneFrameThreadLag",              "0"},
        {"r.FinishCurrentFrame",             "0"},
        {"r.VSync",                          "0"},
        {"r.AllowOcclusionQueries",          "1"},
    };

    // ── Plain / INI / JSON / XML keys ──
    std::vector<std::pair<std::string, std::string>> plainKeys = {
        {"WeaponSpread",            "0"},
        {"WeaponSway",              "0"},
        {"WeaponRecoilScale",       "0"},
        {"RecoilPatternScale",      "0"},
        {"VerticalRecoilScale",     "0"},
        {"HorizontalRecoilScale",   "0"},
        {"BulletSpreadScale",       "0"},
        {"MuzzleVelocityFactor",    "1.0"},
        {"BulletVelocityComp",      "1"},
        {"PredictiveAim",           "1"},
        {"AimAssistEnabled",        "1"},
        {"AimAssistStrength",       "100"},
        {"AimSnapThreshold",        "0"},
        {"AimMagnetism",            "3"},
        {"HeadMagnetism",           "1"},
        {"AimSmoothFactor",         "0"},
        {"AimSnapSpeed",            "10"},
        {"AdsZeroDelay",            "1"},
        {"GyroSampleRate",          "1000"},
        {"GyroSensitivityRatio",    "2.5"},
        {"GyroZeroDelay",           "1"},
        {"GyroLatencyMode",         "0"},
        {"GyroStabilization",       "1"},
        {"GyroSmoothFactor",        "1"},
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
        {"HitRegSyncRate",          "1000"},
        {"InputBufferRate",         "1000"},
        {"bFramePacingEnabled",     "True"},
        {"AllowOcclusionQueries",   "1"},
        {"PreloadShaders",          "1"},
    };

    if (isCvar) {
        // Inject as UE4 +CVars= lines
        for (const auto& kv : cvarKeys) {
            patch_cvar(content, kv.first, kv.second);
        }
        for (const auto& kv : plainKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else if (isXml) {
        for (const auto& kv : plainKeys) {
            // Determine tag type: floats/decimals → float, booleans → string, else int
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            else if (kv.second == "True" || kv.second == "False") tag = "string";
            patch_xml_node(content, tag, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : plainKeys) {
            bool isNum = (!kv.second.empty() && (isdigit(kv.second[0]) || kv.second[0] == '-' || kv.second[0] == '.'));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else {
        for (const auto& kv : cvarKeys) {
            patch_cvar(content, kv.first, kv.second);
        }
        for (const auto& kv : plainKeys) {
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
    LOGI("MagicBulletAimbot PUBGM injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: No-Scope Hipfire Aimbot ──────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoScopeAimbot
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
    std::vector<std::pair<std::string, std::string>> keys = {
        {"HipfireSpread","0"},{"HipfireSpreadScale","0"},{"WeaponSpread","0"},
        {"BulletSpreadScale","0"},{"NoSpreadHipfire","1"},{"HipfireAimLock","1"},
        {"NoScope50mOnly","1"},{"NoScopeAimbotMaxRange","50"},{"NoScopeMaxRange","50"},
        {"NoScope50mHeadLock","1"},{"NoScopeHeadshot50m","1"},{"NoScopeAimLock50m","1"},
        {"NoScopeSpread50m","0"},{"NoScopeMagnetism50m","3"},{"NoScopeCrosshairAccuracy50m","1.0"},
        {"HipfireLock50m","1"},{"HipfireHeadLock50m","1"},{"HipfireMagnetism50m","3"},
        {"CQBAutoHeadshot50m","1"},{"ExtremeHipfireLock50m","1"},{"NoScopeMagicBullet50m","1"},
        {"WeaponSway","0"},{"SwayAmplitude","0"},{"SwayFrequency","0"},
        {"BreathSway","0"},{"HeadMagnetism","1"},{"HeadBonePriority","1"},
        {"AimBoneTarget","0"},{"AimMagnetism","3"},{"AimSnapSpeed","10"},
        {"AimSmoothFactor","0"},{"AimAssistLockMax","1"},{"NoScopeAimbot","1"},
        {"HipfireAimbotEnable","1"},{"HipfireHeadLock","1"},{"InstantAimSnap","1"},
        {"AimDeadzone","0"},{"GyroSampleRate","1000"},{"GyroZeroDelay","1"},
        {"GyroStabilization","1"},{"GyroLatencyMode","0"},{"TouchPollingRate","1000"},
        {"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"HitRegSyncRate","1000"},
        {"FrameSyncDamage","1"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for (const auto& kv : keys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime=stBefore.st_atime; t.modtime=stBefore.st_mtime; utime(path,&t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("NoScopeAimbot PUBGM injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: All-Scope Aimbot (2x-8x) ────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllScopeAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AdsZeroDelay","1"},{"AdsSnapEnable","1"},{"AdsTransitionTime","0"},
        {"HeadMagnetism","1"},{"HeadBonePriority","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"AimAssistLockMax","1"},{"AllScopeHeadLock","1"},{"ScopeAimbotEnable","1"},
        {"PredictiveAim","1"},{"WeaponSway","0"},{"BreathSway","0"},{"SwayAmplitude","0"},
        {"MicroJitterCancel","1"},
        {"Scope2xGyroSample","1000"},{"Scope2xHeadLock","1"},{"Scope2xZeroSway","1"},{"Scope2xAimSnap","10"},
        {"Scope3xGyroStabilization","1"},{"Scope3xHeadLock","1"},{"Scope3xZeroSway","1"},{"Scope3xAimSnap","10"},
        {"Scope4xGyroStabilization","1"},{"Scope4xHeadLock","1"},{"Scope4xZeroSway","1"},{"Scope4xAimSnap","10"},
        {"Scope6xGyro1000Hz","1"},{"Scope6xHeadLock","1"},{"Scope6xMicroDamping","1"},{"Scope6xAimSnap","10"},
        {"Scope8xGyro1000Hz","1"},{"Scope8xHeadLock","1"},{"Scope8xPrecisionFilter","1"},
        {"Scope8xZeroBreathing","1"},{"Scope8xAimSnap","10"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"HitRegSyncRate","1000"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for (const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("AllScopeAimbot PUBGM injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Long-Range Scope Headshot (6x/8x) ────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLongRangeScopeHeadshot
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
        {"BulletVelocityFactor","1.0"},{"MuzzleVelocityFactor","1.0"},
        {"HoldBreathCancel","1"},{"BreathHoldTime","0"},{"BreathSway","0"},
        {"Scope8xZeroBreathing","1"},{"Scope6xZeroBreathing","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"LongRangeHeadshotLock","1"},{"Sniper_HeadshotLock","1"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"MicroJitterCancel","1"},
        {"Scope6xGyro1000Hz","1"},{"Scope6xMicroDamping","1"},{"Scope6xStabilizer","1"},{"Scope6xPrecisionFilter","1"},
        {"Scope8xGyro1000Hz","1"},{"Scope8xPrecisionFilter","1"},{"Scope8xStabilizer","1"},{"Scope8xMicroJitterCancel","1"},
        {"ThermalScopeTracking","1"},{"ThermalHitboxGlow","1"},
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
    LOGI("LongRangeScopeHeadshot PUBGM injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Mid-Range Auto Headshot (2x/3x/4x) ───────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMidRangeAutoHeadshot
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
        {"FrameSyncHeadshot","1"},{"HeadshotLockMidRange","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"VerticalRecoilScale","0"},{"VerticalKickDamp","1"},{"RecoilPatternScale","0"},{"WeaponRecoilScale","0"},
        {"Scope2xGyroSample","1000"},{"Scope2xHeadLock","1"},{"Scope2xRecoilDamp","1"},
        {"Scope3xGyroStabilization","1"},{"Scope3xHeadLock","1"},{"Scope3xRecoilDamp","1"},{"Scope3xDriftCancel","1"},
        {"Scope4xGyroStabilization","1"},{"Scope4xHeadLock","1"},{"Scope4xZeroSway","1"},
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
    LOGI("MidRangeAutoHeadshot PUBGM injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Fast Attack Speed (Fire Rate + Melee Max) ────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastAttackSpeed
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
        {"FullAutoFrameSync","1"},{"AutoFireSync","1"},{"ADSFireInterval","0"},{"FireRateUnlockMax","1"},
        {"MeleePunchSpeed","10"},{"MeleeAttackRate","10"},{"MeleeIntervalMin","0"},{"PubgmMeleeSpeedMax","1"},
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
    LOGI("PubgmFastAttackSpeed injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── PUBGM Ballistics Velocity & Armor Penetration Overdrive ─────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmBallisticsVelocityPenetration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> cv = {
        {"r.PUBGMuzzleVelocityBoost", "2.0"}, {"r.PUBGBulletFlightTimeZero", "1"},
        {"r.PUBGHitScanSimulation", "1"}, {"r.PUBGArmorPenetrationLevel3", "1.0"},
        {"r.PUBGHelmetPenetrationLevel3", "1.0"}, {"r.PUBGLimbDamageMultiplier", "1.5"},
        {"r.PUBGFleshDamageMultiplier", "2.0"}, {"r.PUBGVestDamageBypass", "1"},
        {"r.PUBGShotgunPelletSpread", "0.0"}, {"r.PUBGChokeTightness", "1.0"},
        {"r.PUBGSniperHeadshotDamage", "300"}, {"r.PUBGCameraShakeIntensity", "0.0"},
        {"r.PUBGScopeVisualBob", "0.0"}, {"r.PUBGVehicleDamageMultiplier", "2.5"}
    };
    std::vector<std::pair<std::string,std::string>> pl = {
        {"MuzzleVelocityBoost", "2.0"}, {"BulletFlightTimeZero", "1"},
        {"ClientHitRegistrationPacing", "1000"}, {"HitScanSimulation", "1"},
        {"NetClientLagCompensation", "1"}, {"ArmorPenetrationLevel3", "1.0"},
        {"HelmetPenetrationLevel3", "1.0"}, {"LimbDamageMultiplier", "1.5"},
        {"FleshDamageMultiplier", "2.0"}, {"VestDamageBypass", "1"},
        {"ShotgunPelletSpread", "0.0"}, {"ChokeTightness", "1.0"},
        {"PelletDamageFull", "1"}, {"DBS_DoubleTapDelayMs", "0"},
        {"SniperHeadshotDamage", "300"}, {"BoltActionQuickCycle", "1"},
        {"NoScopeCrosshairAccuracy", "1.0"}, {"BulletPenetrationDistance", "1000"},
        {"M416_VerticalRecoilMin", "0"}, {"BerylM762_HorizontalBounce", "0"},
        {"AKM_FirstShotKick", "0"}, {"CameraShakeIntensity", "0.0"},
        {"ScopeVisualBob", "0.0"}, {"VehicleDamageMultiplier", "2.5"},
        {"VehicleOccupantPenetration", "1"}
    };
    if (isCvar) {
        for (const auto& kv : cv) patch_cvar(content, kv.first, kv.second);
        for (const auto& kv : pl) patch_key_value(content, kv.first, kv.second);
    } else if (isXml) {
        for (const auto& kv : pl) {
            std::string t = "int";
            if (kv.second.find('.') != std::string::npos) t = "float";
            else if (kv.second == "True" || kv.second == "False") t = "string";
            patch_xml_node(content, t, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : pl) {
            bool n = !kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-');
            patch_json_node(content, kv.first, kv.second, n);
        }
    } else {
        for (const auto& kv : pl) patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("PubgmBallisticsVelocityPenetration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// PUBGM: 165 FPS & HDR Graphics Native Injector
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgm165FpsGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps, jint qualityLevel) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    int fps = targetFps;
    if (fps <= 120) fps = 120;
    else if (fps <= 144) fps = 144;
    else if (fps <= 165) fps = 165;
    else fps = 185;

    // FpsUnlockTier standard: Level 7 = 120fps, Level 8 = 144fps, Level 9 = 165fps, Level 10 = 185fps
    int effectiveLevel = 7;
    if (fps == 120) effectiveLevel = 7;
    else if (fps == 144) effectiveLevel = 8;
    else if (fps == 165) effectiveLevel = 9;
    else if (fps >= 185) effectiveLevel = 10;

    int q = (qualityLevel > 0) ? qualityLevel : 4; // 4 = HDR
    std::string fpsStr = std::to_string(fps);
    std::string effLvlStr = std::to_string(effectiveLevel);
    std::string qStr = std::to_string(q);

    // 1. Binary GVAS savegame (Active.sav / ActiveShadow.sav)
    if (pathStr.rfind(".sav") != std::string::npos || pathStr.find("Active") != std::string::npos) {
        std::ifstream file(pathStr, std::ios::binary);
        if (!file.is_open()) {
            env->ReleaseStringUTFChars(jPath, path);
            return JNI_FALSE;
        }
        std::vector<uint8_t> data((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
        file.close();

        if (data.empty()) {
            env->ReleaseStringUTFChars(jPath, path);
            return JNI_FALSE;
        }

        bool mod = false;
        mod |= patch_gvas_int_property_cpp(data, "FPSLevel", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "BattleFPS", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "LobbyFPS", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "MainCityFPS", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "HighFPSMode", 3);
        mod |= patch_gvas_int_property_cpp(data, "BattleRenderQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "LobbyRenderQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "MainCityRenderQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "GraphicQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "ArtQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "MobileHDRMode", 1);
        mod |= patch_gvas_int_property_cpp(data, "ShadowQuality", 3);
        mod |= patch_gvas_int_property_cpp(data, "ShadowSwitch", 1);
        mod |= patch_gvas_int_property_cpp(data, "AutoChangeQuality", 0);

        std::string tmpPath = pathStr + ".tmp";
        std::ofstream out(tmpPath, std::ios::binary | std::ios::trunc);
        if (!out.is_open()) {
            env->ReleaseStringUTFChars(jPath, path);
            return JNI_FALSE;
        }
        out.write(reinterpret_cast<const char*>(data.data()), data.size());
        out.close();
        chmod(tmpPath.c_str(), 0666);
        bool ok = (rename(tmpPath.c_str(), pathStr.c_str()) == 0);
        if (ok && hasStat) {
            struct utimbuf t;
            t.actime = stBefore.st_atime;
            t.modtime = stBefore.st_mtime;
            utime(path, &t);
        }
        env->ReleaseStringUTFChars(jPath, path);
        LOGI("PubgmActiveSav 165 FPS & HDR injected: %s [ok=%d]", pathStr.c_str(), ok);
        return ok ? JNI_TRUE : JNI_FALSE;
    }

    // 2. INI / XML Text configs
    std::string content = read_file_posix(pathStr);

    if (pathStr.rfind(".xml") != std::string::npos) {
        if (content.find("<map>") == std::string::npos) {
            content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n</map>\n";
        }
        patch_xml_node(content, "int", "FPS", fpsStr);
        patch_xml_node(content, "int", "FrameRateLevel", effLvlStr);
        patch_xml_node(content, "int", "GraphicQuality", qStr);
        patch_xml_node(content, "int", "MobileHDRMode", "1");
        patch_xml_node(content, "int", "HighFPSMode", "3");
        patch_xml_node(content, "int", "Unlock165Hz", "1");
        patch_xml_node(content, "int", "Unlock165FPS", "1");
    } else if (pathStr.find("EnjoyCJZC.ini") != std::string::npos || pathStr.find("EnjoyCJ.ini") != std::string::npos) {
        // EnjoyCJZC UserSetting section
        std::vector<std::pair<std::string, std::string>> enjoyKeys = {
            {"FrameRateLevel", effLvlStr}, {"BattleFPS", effLvlStr}, {"LobbyFPS", effLvlStr},
            {"FPS", fpsStr}, {"MaxFPS", fpsStr}, {"TargetFPS", fpsStr}, {"FrameRateLimit", fpsStr},
            {"MobileFPSLimit", fpsStr}, {"GraphicQuality", qStr}, {"ArtQuality", qStr},
            {"ShadowQuality", "0"}, {"MobileHDRMode", "1"}, {"HighFPSMode", "3"},
            {"bUseHDRMode", "True"}, {"bUseUltraExtreme", "True"}, {"bFramePacingEnabled", "True"},
            {"UnlockFPS", "1"}, {"Unlock120Hz", "1"}, {"Unlock144Hz", "1"}, {"Unlock165Hz", "1"},
            {"Unlock185Hz", "1"}, {"Unlock240Hz", "1"}, {"Unlock165FPS", "1"}, {"Ultra165FPS", "1"},
            {"+CVars=r.PUBGDeviceFPS", effLvlStr}, {"+CVars=r.PUBGTargetFPS", fpsStr},
            {"+CVars=r.MobileHDR", "1"}, {"+CVars=r.PUBGHDRMode", "1"}
        };
        for (const auto &kv : enjoyKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else if (pathStr.find("GameUserSettings.ini") != std::string::npos) {
        std::vector<std::pair<std::string, std::string>> gusKeys = {
            {"FrameRateLimit", fpsStr + ".000000"}, {"bUseVSync", "False"},
            {"bUseDynamicResolution", "False"}, {"ResolutionSizeX", "2400"},
            {"ResolutionSizeY", "1080"}, {"LastUserConfirmedResolutionSizeX", "2400"},
            {"LastUserConfirmedResolutionSizeY", "1080"}
        };
        for (const auto &kv : gusKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else {
        // UserCustom.ini / DeviceProfile.ini / Quality.ini
        std::vector<std::pair<std::string, std::string>> ue4Keys = {
            {"+CVars=r.PUBGDeviceFPS", effLvlStr},
            {"+CVars=r.PUBGDeviceFPSPolicy", "1"},
            {"+CVars=r.DefaultDeviceFPS", effLvlStr},
            {"+CVars=r.UserFPSSetting", effLvlStr},
            {"+CVars=r.PUBGTargetFPS", fpsStr},
            {"+CVars=r.PUBGMaxFPS", fpsStr},
            {"+CVars=r.PUBGFrameRateLimit", fpsStr},
            {"+CVars=r.FrameRateLimit", fpsStr},
            {"+CVars=r.MobileFPSLimit", fpsStr},
            {"+CVars=r.Vsync", "0"},
            {"+CVars=r.Unlock120Hz", "1"},
            {"+CVars=r.Unlock144Hz", "1"},
            {"+CVars=r.Unlock165Hz", "1"},
            {"+CVars=r.Unlock185Hz", "1"},
            {"+CVars=r.Unlock240Hz", "1"},
            {"+CVars=r.TouchBoostHz", fpsStr},
            {"+CVars=r.MobileTouchBoostRate", fpsStr},
            {"+CVars=r.FramePacing", "1"},
            {"+CVars=r.MobileHDR", "1"},
            {"+CVars=r.PUBGHDRMode", "1"},
            {"+CVars=r.PUBGQualityLevel", qStr},
            {"+CVars=r.PUBGSDKQualityLevel", qStr},
            {"+CVars=r.UserQualitySetting", qStr},
            {"+CVars=r.ShadowQuality", "3"},
            {"+CVars=r.PostProcessAAQuality", "3"},
            {"+CVars=r.Tonemapper.Quality", "4"},
            {"+CVars=r.MobileContentScaleFactor", "1.0"},
            {"+CVars=r.MaxAnisotropy", "16"},
            {"+CVars=r.TemporalAA.Upscale", "1"},
            {"+CVars=r.AllowOcclusionQueries", "1"},
            {"+CVars=r.Vulkan.Enable", "1"},
            {"+CVars=r.Vulkan.PipelineCache", "1"},
            {"+CVars=r.AsyncCompute", "1"},
            {"+CVars=r.VRS.Enable", "1"},
            {"FPS", fpsStr},
            {"MaxFPS", fpsStr},
            {"TargetFPS", fpsStr},
            {"FrameRateLimit", fpsStr},
            {"MobileFPSLimit", fpsStr},
            {"FrameRateLevel", effLvlStr},
            {"GraphicQuality", qStr},
            {"ArtQuality", qStr},
            {"UnlockFPS", "1"},
            {"Unlock165FPS", "1"},
            {"Ultra165FPS", "1"},
            {"HighFPSMode", "3"},
            {"HDRMode", "1"},
            {"TouchBoostHz", fpsStr},
            {"TouchPollingRate", "1000"}
        };
        for (const auto &kv : ue4Keys) {
            patch_key_value(content, kv.first, kv.second);
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
    LOGI("Pubgm165FpsGraphics injected: %s [ok=%d, fps=%d, q=%d]", pathStr.c_str(), ok, fps, q);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamageBoost", "10000"},
        {"BulletVelocityBoost", "10000"}, {"MuzzleVelocityFactor", "10.0"}, {"FireRateBoost", "10000"},
        {"FireRateMultiplier", "10.0"}, {"RapidFireHitReg", "1000"}, {"TriggerZeroDelay", "1"},
        {"BurstIntervalZero", "1"}, {"HitboxMultiplier", "5.0"}, {"HitboxScale", "5.0"},
        {"BulletMagnetism", "1"}, {"BulletVelocityComp", "1"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"ZeroBulletDrop", "1"}, {"BulletDropComp", "1"},
        {"TrueDamageBoost", "10000"}, {"PenetrationBoost", "10000"}, {"ZeroRecoil", "1"},
        {"RecoilScale", "0"}, {"WeaponSpread", "0"}, {"BulletSpreadScale", "0"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"r.PUBGDamageBoost", "10000"}, {"r.PUBGFireRateBoost", "10000"},
        {"r.PUBGBulletVelocityCompensation", "1"}, {"r.PUBGInstantHitReg", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "PubgmDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Tiered All-Scope Auto 3-Bullet Headshot (100m - 400m) ─────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAllScopeTieredHeadshot
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
        {"HipfireMagnetism", "3"}, {"HipfireHeadLock", "1"}, {"CrosshairTightness", "1.0"}, {"NoScopeRecoilZero", "1"},
        // ─── Tiered Scope-On Auto Headshot (100m, 200m, 300m, 400m - All Rifles) ─
        {"RifleScopeHeadshot100m", "1"}, {"RifleScopeMagnetism100m", "3"}, {"AimSnapHead100m", "1"}, {"Scope1xHeadLock", "1"}, {"Scope1xAimMagnetism", "3"}, {"ScopeRedDotHeadLock", "1"}, {"ScopeHoloHeadLock", "1"},
        {"RifleScopeHeadshot200m", "1"}, {"RifleScopeMagnetism200m", "3"}, {"AimSnapHead200m", "1"}, {"Scope2xHeadLock", "1"}, {"Scope3xHeadLock", "1"}, {"Scope2xZeroRecoil", "1"}, {"Scope3xZeroRecoil", "1"}, {"PredictiveAim200m", "1"},
        {"RifleScopeHeadshot300m", "1"}, {"RifleScopeMagnetism300m", "3"}, {"AimSnapHead300m", "1"}, {"Scope4xHeadLock", "1"}, {"Scope6xHeadLock", "1"}, {"BulletDropComp300m", "1"}, {"ZeroBreathSway300m", "1"},
        {"RifleScopeHeadshot400m", "1"}, {"RifleScopeMagnetism400m", "3"}, {"AimSnapHead400m", "1"}, {"Scope8xLongRangeHeadLock", "1"}, {"BulletDropComp400m", "1"}, {"TargetLeadComp400m", "1"}, {"ExtremeRangeHeadLock400m", "1"}, {"ZeroMicroJitter400m", "1"},
        {"AllRifleAutoHeadshot", "1"}, {"RifleZeroRecoil", "1"}, {"RifleZeroSpread", "1"}, {"RifleScopeAimMagnetism", "3"},
        // Global PUBG gunplay, burst & gyro stabilization
        {"AutoHeadshotBurst", "3"}, {"Auto3BulletHeadshot", "1"}, {"HeadshotBurstCount", "3"},
        {"WeaponRecoilScale", "0"}, {"WeaponSpreadScale", "0"}, {"RecoilZero", "1"},
        {"LessRecoil", "1"}, {"RecoilCompFactor", "0"}, {"ZeroHorizontalRecoil", "1"},
        {"BulletTrackingEnemy", "1"}, {"TrackingBullet", "1"}, {"BulletTracking", "1"},
        {"EnemyTrackingLock", "1"}, {"TrackingBulletVelocity", "1"},
        {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"},
        {"GyroMicroSmoothing", "1"}, {"GyroAimAssistLock", "1"},
        {"DamageLockMax", "1"}, {"PhysicalDefense", "10000"}, {"ArmorRating", "10000"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"ZeroInputDelay", "1"},
        {"SniperSnapSpeed", "10"}, {"AimSnapSpeed", "10"}, {"ZeroADSDelay", "1"},
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
    LOGI("PubgmAllScopeTieredHeadshot injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// PUBGM: All Weapon Max Damage 2026 — UE4 CVars + Plain INI full stack
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAllWeaponMaxDamage2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos
        ||pathStr.rfind("UserCustom.ini")!=std::string::npos
        ||pathStr.rfind("EnjoyCJZC.ini")!=std::string::npos
        ||pathStr.rfind("EnjoyCJ.ini")!=std::string::npos);
    // UE4 CVar keys
    std::vector<std::pair<std::string,std::string>> cv={
        {"r.PUBGDamageLockMax","1"},{"r.PUBGDamageBoost","1"},{"r.PUBGEffectiveDPSMode","3"},
        {"r.PUBGBulletVelocityCompensation","1"},{"r.PUBGInstantHitReg","1"},
        {"r.PUBGPenetrationBoost","1"},{"r.PUBGCritRateBoost","1"},
        {"r.PUBGHeadshotMultiplier","2.0"},{"r.PUBGTrueDamageMod","1"},
        {"r.WeaponRecoilScale","0"},{"r.VerticalRecoilScale","0"},{"r.HorizontalRecoilScale","0"},
        {"r.RecoilPatternScale","0"},{"r.WeaponSpread","0"},{"r.WeaponSway","0"},
        {"r.BulletSpreadScale","0"},{"r.MuzzleVelocityFactor","1.0"},
        {"r.AimAssistEnabled","1"},{"r.AimAssistStrength","100"},{"r.AimMagnetism","3"},
        {"r.AimSnapThreshold","0"},{"r.HeadBoneAimPriority","1"},{"r.PredictiveAim","1"},
        {"r.GyroSampleRate","1000"},{"r.GyroSensitivityRatio","2.5"},{"r.GyroZeroDelay","1"},
        {"r.GyroLatencyMode","0"},{"r.GyroStabilization","1"},{"r.GyroSmoothFactor","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"r.AllowOcclusionQueries","1"},{"r.PUBGDeviceFPS","7"},
        {"r.PUBGMaxFPS","165"},{"r.ResolutionScale","100"},
    };
    // Plain keys for XML/JSON/INI
    std::vector<std::pair<std::string,std::string>> pl={
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"HeadshotMultiplier","2"},
        {"FrameSyncDamage","1"},{"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},
        {"InstantHitReg","1"},{"BulletVelocityComp","1"},{"TrueDamageMod","1"},
        {"AR_RecoilZero","1"},{"AR_SpreadZero","1"},{"AR_MaxDamageLock","1"},{"AR_AimMagnetism","3"},
        {"SMG_ZeroRecoil","1"},{"SMG_ZeroSpread","1"},{"SMG_RapidFireHitReg","1000"},
        {"Sniper_ZeroSway","1"},{"Sniper_HeadshotLock","1"},{"Sniper_BulletDropComp","1"},
        {"Sniper_InstantHitReg","1"},{"Sniper_MaxDamageLock","1"},
        {"DMR_ZeroSway","1"},{"DMR_MaxDPSLock","1"},
        {"Shotgun_ZeroPelletRNG","1"},{"Shotgun_MaxDamageBurst","1"},
        {"LMG_ZeroBloom","1"},{"LMG_AimLock","1"},{"LMG_HitRegSync","1000"},
        {"Pistol_AimMagnetism","3"},{"Pistol_ZeroDelay","1"},
        {"WeaponSpread","0"},{"WeaponSway","0"},{"WeaponRecoilScale","0"},
        {"RecoilPatternScale","0"},{"VerticalRecoilScale","0"},{"HorizontalRecoilScale","0"},
        {"BulletSpreadScale","0"},{"MuzzleVelocityFactor","1.0"},
        {"MuzzleSpread","0"},{"MovingSpreadFactor","0"},
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSnapThreshold","0"},
        {"AimSmoothFactor","0"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"AdsZeroDelay","1"},{"PredictiveAim","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},{"PreloadShaders","1"},
        {"FrameRateLevel","7"},{"ResolutionScale","100"},{"HighFPSMode","3"},
    };
    if(isCvar){for(const auto& kv:cv)patch_cvar(content,kv.first,kv.second);for(const auto& kv:pl)patch_key_value(content,kv.first,kv.second);}
    else if(isXml){for(const auto& kv:pl){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}}
    else if(isJson){for(const auto& kv:pl){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}}
    else{for(const auto& kv:cv)patch_cvar(content,kv.first,kv.second);for(const auto& kv:pl)patch_key_value(content,kv.first,kv.second);}
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("PubgmAllWeaponMaxDamage2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// PUBGM: Ultra Aimbot 2026 — Head Lock + Bullet Magnetism + 1000Hz Gyro
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmUltraAimbot2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        {"r.AimAssistEnabled","1"},{"r.AimAssistStrength","100"},{"r.AimMagnetism","3"},
        {"r.AimSnapThreshold","0"},{"r.HeadBoneAimPriority","1"},{"r.PredictiveAim","1"},
        {"r.PUBGBulletVelocityCompensation","1"},
        {"r.GyroSampleRate","1000"},{"r.GyroSensitivityRatio","2.5"},{"r.GyroZeroDelay","1"},
        {"r.GyroLatencyMode","0"},{"r.GyroStabilization","1"},{"r.GyroSmoothFactor","1"},
        {"r.WeaponSpread","0"},{"r.WeaponSway","0"},{"r.WeaponRecoilScale","0"},
        {"r.RecoilPatternScale","0"},{"r.VerticalRecoilScale","0"},{"r.HorizontalRecoilScale","0"},
        {"r.BulletSpreadScale","0"},{"r.MuzzleVelocityFactor","1.0"},
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"LockOnRange","1.0"},{"AimSnapSpeed","10"},
        {"AimSnapThreshold","0"},{"AimStabilizer","1"},{"HeadMagnetism","1"},
        {"HeadBoneAimPriority","1"},{"AdsZeroDelay","1"},{"AimSmoothFactor","0"},
        {"PredictiveAim","1"},{"BulletVelocityComp","1"},{"BulletMagnetism","1"},
        {"Scope2xSensitivity","1.0"},{"Scope2xStabilizer","1"},{"Scope2xRecoilDamp","1"},
        {"Scope3xSensitivity","0.90"},{"Scope3xStabilizer","1"},{"Scope3xRecoilDamp","1"},
        {"Scope4xSensitivity","0.85"},{"Scope4xStabilizer","1"},{"Scope4xZeroSway","1"},
        {"Scope6xSensitivity","0.75"},{"Scope6xStabilizer","1"},{"Scope6xGyro1000Hz","1"},
        {"Scope8xSensitivity","0.65"},{"Scope8xStabilizer","1"},{"Scope8xZeroBreathing","1"},
        {"ThermalScopeTracking","1"},{"ThermalHitboxGlow","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},{"InstantHitReg","1"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
    };
    for(const auto& kv:k){
        if(isCvar){patch_cvar(content,kv.first,kv.second);patch_key_value(content,kv.first,kv.second);}
        else if(isXml){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("PubgmUltraAimbot2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastLoadAsyncStreaming(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"s.AsyncLoadingThreadEnabled", "True"},
        {"s.AsyncLoadingTimeLimit", "10.0"},
        {"s.PriorityAsyncLoadingExtraTime", "20.0"},
        {"r.TextureStreaming", "1"},
        {"r.Streaming.PoolSize", "1024"},
        {"r.Streaming.UseBackgroundThreadPool", "1"},
        {"r.ShaderCompiler.CoreCount", "8"},
        {"r.ShaderPipelineCache.StartupMode", "3"},
        {"bSkipSplash", "True"},
        {"bSkipMovie", "True"},
        {"r.Streaming.HLODStrategy", "2"}
    };
    for (const auto& kv : keys) {
        if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("PubgmFastLoadAsyncStreaming injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// PUBGM: Auto Head 5-Bullet Aim Assist Lock & Target Scope Tracking (50m-300m)
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAutoHead5BulletAimLock(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ─── 5-Bullet Auto Headshot Burst Lock ───
        {"Auto5BulletHeadshot",       "1"},
        {"HeadshotBurstCount",        "5"},
        {"AutoHeadshotBurst",         "5"},
        {"FiveBulletHeadLock",        "1"},
        {"BurstFireRateLock",         "5"},
        {"AutoHeadshotLock",          "1"},
        {"HeadBoneAimPriority",       "1"},

        // ─── No-Scope Auto Headshot & CQB Tracking (50m Only) ───
        {"NoScope50mOnly",            "1"},
        {"NoScopeAimbotMaxRange",     "50"},
        {"NoScopeMaxRange",           "50"},
        {"NoScope50mHeadLock",        "1"},
        {"NoScopeHeadshot50m",        "1"},
        {"NoScopeAimLock50m",         "1"},
        {"NoScopeSpread50m",          "0"},
        {"NoScopeMagnetism50m",       "3"},
        {"HipfireLock50m",            "1"},
        {"HipfireHeadLock50m",        "1"},
        {"CQBAutoHeadshot50m",        "1"},
        {"NoScopeMagicBullet50m",     "1"},
        {"ScopeLock50m",              "1"},
        {"AimTrackingLock50m",        "1"},
        {"AimMagnetism50m",           "3"},

        // ─── May-Scope Auto Headshot: 100m Tier (1x / Red Dot / Holo) ───
        {"MayScope100m",              "1"},
        {"ScopeAimbotTier100m",       "1"},
        {"ScopeLock100m",             "1"},
        {"AimTrackingLock100m",       "1"},
        {"Scope1xHeadLock100m",       "1"},
        {"ScopeRedDotHeadLock100m",   "1"},
        {"ScopeHoloHeadLock100m",     "1"},
        {"ScopeAimMag100m",           "3"},
        {"AimMagnetism100m",          "3"},
        {"AimSnapHead100m",           "1"},

        // ─── May-Scope Auto Headshot: 150m Tier (2x / 3x Mid-Range) ───
        {"MayScope150m",              "1"},
        {"ScopeAimbotTier150m",       "1"},
        {"ScopeLock150m",             "1"},
        {"AimTrackingLock150m",       "1"},
        {"Scope2xHeadLock150m",       "1"},
        {"Scope3xHeadLock150m",       "1"},
        {"MidRangeSnap150m",          "1"},
        {"Scope3xGyroStabilize150m",  "1"},
        {"ScopeAimMag150m",           "3"},
        {"AimMagnetism150m",          "3"},

        // ─── May-Scope Auto Headshot: 200m Tier (3x / 4x AR & DMR) ───
        {"MayScope200m",              "1"},
        {"ScopeAimbotTier200m",       "1"},
        {"ScopeLock200m",             "1"},
        {"AimTrackingLock200m",       "1"},
        {"Scope4xHeadLock200m",       "1"},
        {"PredictiveAim200m",         "1"},
        {"ZeroSway200m",              "1"},
        {"Scope4xGyro1000Hz",         "1"},
        {"ScopeAimMag200m",           "3"},
        {"AimMagnetism200m",          "3"},

        // ─── May-Scope Auto Headshot: 250m Tier (4x / 6x Long DMR & Sniper) ───
        {"MayScope250m",              "1"},
        {"ScopeAimbotTier250m",       "1"},
        {"ScopeLock250m",             "1"},
        {"AimTrackingLock250m",       "1"},
        {"Scope4xHeadLock250m",       "1"},
        {"Scope6xHeadLock250m",       "1"},
        {"ScopeAimMag250m",           "3"},
        {"AimMagnetism250m",          "3"},
        {"PredictiveAim250m",         "1"},
        {"BulletDropComp250m",        "1"},
        {"TargetLeadComp250m",        "1"},

        // ─── May-Scope Auto Headshot: 300m Tier (6x / 8x Extreme Sniper) ───
        {"MayScope300m",              "1"},
        {"ScopeAimbotTier300m",       "1"},
        {"ScopeLock300m",             "1"},
        {"AimTrackingLock300m",       "1"},
        {"Scope6xHeadLock300m",       "1"},
        {"Scope8xLongRangeHeadLock300m", "1"},
        {"BulletDropComp300m",        "1"},
        {"TargetLeadComp300m",        "1"},
        {"ZeroBreathSway300m",        "1"},
        {"ScopeAimMag300m",           "3"},
        {"AimMagnetism300m",          "3"},

        // ─── May-Scope Auto Headshot: 350m Tier (8x Sniper Extreme) ───
        {"MayScope350m",              "1"},
        {"ScopeAimbotTier350m",       "1"},
        {"ScopeLock350m",             "1"},
        {"AimTrackingLock350m",       "1"},
        {"Scope8xHeadLock350m",       "1"},
        {"Scope8xLongRangeHeadLock350m","1"},
        {"Scope8xPrecisionFilter350m","1"},
        {"ExtremeRangeHeadLock350m",  "1"},
        {"BulletDropComp350m",        "1"},
        {"TargetLeadComp350m",        "1"},
        {"ZeroMicroJitter350m",       "1"},
        {"ScopeAimMag350m",           "3"},
        {"AimMagnetism350m",          "3"},

        // ─── Magic Bullet Overdrive Keys ───
        {"MagicBulletFollowEnemy",    "1"},
        {"BulletMagnetEnemy",         "1"},
        {"NoScopeMagicBullet",        "1"},
        {"ScopedMagicBullet",         "1"},

        // ─── Armor Boost & Damage Overdrive (Easy Kill Enemy) ───
        {"DamageLockMax",             "10000"},
        {"DamageBoost",               "10000"},
        {"PhysicalDefense",           "10000"},
        {"ArmorRating",               "10000"},
        {"DamageReduction",           "0.90"},
        {"TrueDamageMod",             "1"},
        {"InstantHitReg",             "1"},
        {"HitRegSyncRate",            "1000"},
        {"BulletTrackingEnemy",       "1"},
        {"WeaponRecoilScale",         "0"},
        {"WeaponSpreadScale",         "0"},
        {"RecoilZero",                "1"},
        {"LessRecoil",                "1"},
        {"TouchPollingRate",          "1000"},
        {"GyroSampleRate",            "1000"},
        {"ZeroInputDelay",            "1"},
        {"r.AimAssistStrength",       "100"},
        {"r.AimMagnetism",            "3"},
        {"r.PUBGHeadshotMultiplier",  "3.0"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
        {"bFramePacingEnabled",       "True"}
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
    LOGI("PubgmAutoHead5BulletAimLock injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Distance-Tiered Auto-Aimbot & Magic Bullet 5-Head (2026) ─────────
// No-Scope: strictly <= 50m CQB lock + zero hipfire spread + magic bullet bend.
// May-Scope: 100m, 150m, 200m, 250m, 300m, 350m tiered scope aimbot tracking.
// Magic Bullet: predictive path bending + target enemy tracking on both modes.
// 5-Head Auto: 5-bullet burst headshot lock across all gun classes.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmDistanceTieredAimbot(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ─── Master Feature Switches ───
        {"DistanceTieredAimbot",        "1"},
        {"PubgmDistanceTieredAimbotV2", "1"},
        {"NoScope50mOnly",              "1"},
        {"MayScopeMultiDistance",       "1"},

        // ─── No-Scope Auto-Aimbot: Strictly 50m Only ───
        {"NoScopeAimbotMaxRange",       "50"},
        {"NoScopeMaxRange",             "50"},
        {"NoScope50mHeadLock",          "1"},
        {"NoScopeHeadshot50m",          "1"},
        {"NoScopeAimLock50m",           "1"},
        {"NoScopeSpread50m",            "0"},
        {"NoScopeMagnetism50m",         "3"},
        {"NoScopeCrosshairAccuracy50m", "1.0"},
        {"HipfireLock50m",              "1"},
        {"HipfireHeadLock50m",          "1"},
        {"HipfireMagnetism50m",         "3"},
        {"CQBAutoHeadshot50m",          "1"},
        {"ExtremeHipfireLock50m",       "1"},
        {"NoScopeMagicBullet50m",       "1"},

        // ─── May-Scope Auto-Aimbot: 100m Tier (1x / Red Dot / Holo) ───
        {"MayScope100m",                "1"},
        {"ScopeAimbotTier100m",         "1"},
        {"ScopeLock100m",               "1"},
        {"AimTrackingLock100m",         "1"},
        {"Scope1xHeadLock100m",         "1"},
        {"ScopeRedDotHeadLock100m",     "1"},
        {"ScopeHoloHeadLock100m",       "1"},
        {"ScopeAimMag100m",             "3"},
        {"AimMagnetism100m",            "3"},
        {"AimSnapHead100m",             "1"},
        {"Scope1xZeroRecoil100m",       "1"},

        // ─── May-Scope Auto-Aimbot: 150m Tier (2x / 3x Mid-Range) ───
        {"MayScope150m",                "1"},
        {"ScopeAimbotTier150m",         "1"},
        {"ScopeLock150m",               "1"},
        {"AimTrackingLock150m",         "1"},
        {"Scope2xHeadLock150m",         "1"},
        {"Scope3xHeadLock150m",         "1"},
        {"ScopeAimMag150m",             "3"},
        {"AimMagnetism150m",            "3"},
        {"MidRangeSnap150m",            "1"},
        {"Scope3xGyroStabilize150m",    "1"},
        {"Scope2xZeroRecoil150m",       "1"},
        {"PredictiveAim150m",           "1"},

        // ─── May-Scope Auto-Aimbot: 200m Tier (3x / 4x AR & DMR) ───
        {"MayScope200m",                "1"},
        {"ScopeAimbotTier200m",         "1"},
        {"ScopeLock200m",               "1"},
        {"AimTrackingLock200m",         "1"},
        {"Scope3xHeadLock200m",         "1"},
        {"Scope4xHeadLock200m",         "1"},
        {"ScopeAimMag200m",             "3"},
        {"AimMagnetism200m",            "3"},
        {"PredictiveAim200m",           "1"},
        {"ZeroSway200m",                "1"},
        {"Scope4xGyro1000Hz200m",       "1"},
        {"Scope4xZeroRecoil200m",       "1"},

        // ─── May-Scope Auto-Aimbot: 250m Tier (4x / 6x Long DMR & Sniper) ───
        {"MayScope250m",                "1"},
        {"ScopeAimbotTier250m",         "1"},
        {"ScopeLock250m",               "1"},
        {"AimTrackingLock250m",         "1"},
        {"Scope4xHeadLock250m",         "1"},
        {"Scope6xHeadLock250m",         "1"},
        {"ScopeAimMag250m",             "3"},
        {"AimMagnetism250m",            "3"},
        {"PredictiveAim250m",           "1"},
        {"BulletDropComp250m",          "1"},
        {"TargetLeadComp250m",          "1"},
        {"Scope6xMicroDamping250m",     "1"},
        {"ZeroBreathSway250m",          "1"},

        // ─── May-Scope Auto-Aimbot: 300m Tier (6x / 8x Extreme Range) ───
        {"MayScope300m",                "1"},
        {"ScopeAimbotTier300m",         "1"},
        {"ScopeLock300m",               "1"},
        {"AimTrackingLock300m",         "1"},
        {"Scope6xHeadLock300m",         "1"},
        {"Scope8xLongRangeHeadLock300m","1"},
        {"ScopeAimMag300m",             "3"},
        {"AimMagnetism300m",            "3"},
        {"BulletDropComp300m",          "1"},
        {"TargetLeadComp300m",          "1"},
        {"ZeroBreathSway300m",          "1"},
        {"Scope8xGyro1000Hz300m",       "1"},

        // ─── May-Scope Auto-Aimbot: 350m Tier (8x Extreme Sniper Distance) ───
        {"MayScope350m",                "1"},
        {"ScopeAimbotTier350m",         "1"},
        {"ScopeLock350m",               "1"},
        {"AimTrackingLock350m",         "1"},
        {"Scope8xHeadLock350m",         "1"},
        {"Scope8xLongRangeHeadLock350m","1"},
        {"Scope8xPrecisionFilter350m",  "1"},
        {"ExtremeRangeHeadLock350m",    "1"},
        {"ScopeAimMag350m",             "3"},
        {"AimMagnetism350m",            "3"},
        {"BulletDropComp350m",          "1"},
        {"TargetLeadComp350m",          "1"},
        {"ZeroMicroJitter350m",         "1"},
        {"SniperInstantHitReg350m",     "1"},

        // ─── Magic Bullet Overdrive: No-Scope <=50m & May-Scope 100m-350m ───
        {"MagicBulletEnabled",          "1"},
        {"MagicBulletFollowEnemy",      "1"},
        {"BulletMagnetEnemy",           "1"},
        {"BulletFOVCorrection",         "1"},
        {"BulletTrackingEnemy",         "1"},
        {"TrackingBullet",              "1"},
        {"TrackingBulletVelocity",      "1"},
        {"NoScopeMagicBullet",          "1"},
        {"ScopedMagicBullet",           "1"},
        {"BulletTrajectoryMagnet",      "1"},
        {"HitscanSimulation",           "1"},
        {"InstantHitReg",               "1"},
        {"HitRegSyncRate",              "1000"},
        {"BulletVelocityComp",          "1"},
        {"MuzzleVelocityFactor",        "2.0"},

        // ─── 5-Head Auto Headshot Burst Lock ───
        {"Auto5BulletHeadshot",         "1"},
        {"HeadshotBurstCount",          "5"},
        {"FiveBulletHeadLock",          "1"},
        {"BurstFireRateLock",           "5"},
        {"AutoHeadshotBurst",           "5"},
        {"AutoHeadshotLock",            "1"},
        {"HeadBoneAimPriority",         "1"},
        {"HeadBoneLock",                "1"},
        {"HeadMagnetism",               "1"},
        {"AimSnapSpeed",                "10"},
        {"AimSnapThreshold",            "0"},
        {"AimSmoothFactor",             "0"},

        // ─── Zero Recoil & Zero Spread ───
        {"WeaponRecoilScale",           "0"},
        {"RecoilPatternScale",          "0"},
        {"VerticalRecoilScale",         "0"},
        {"HorizontalRecoilScale",       "0"},
        {"WeaponSpread",                "0"},
        {"WeaponSpreadScale",           "0"},
        {"BulletSpreadScale",           "0"},
        {"WeaponSway",                  "0"},
        {"BreathSway",                  "0"},
        {"MicroJitterCancel",           "1"},

        // ─── Gyro & Input Overclock ───
        {"GyroSampleRate",              "1000"},
        {"GyroZeroDelay",               "1"},
        {"GyroStabilization",           "1"},
        {"GyroLatencyMode",             "0"},
        {"TouchPollingRate",            "1000"},
        {"TouchZeroDelay",              "1"},
        {"ZeroInputLag",                "1"},
        {"ZeroInputDelay",              "1"},

        // ─── UE4 CVar Compatibility ───
        {"+CVars=r.PUBGBulletVelocityCompensation", "1"},
        {"+CVars=r.PredictiveAim",                  "1"},
        {"+CVars=r.AimAssistEnabled",               "1"},
        {"+CVars=r.AimAssistStrength",              "100"},
        {"+CVars=r.AimSnapThreshold",               "0"},
        {"+CVars=r.AimMagnetism",                   "3"},
        {"+CVars=r.HeadBoneAimPriority",            "1"},
        {"+CVars=r.WeaponSpread",                   "0"},
        {"+CVars=r.WeaponRecoilScale",              "0"},
        {"+CVars=r.VerticalRecoilScale",            "0"},
        {"+CVars=r.HorizontalRecoilScale",          "0"},
        {"+CVars=r.BulletSpreadScale",              "0"},
        {"+CVars=r.OneFrameThreadLag",              "0"},
        {"+CVars=r.FinishCurrentFrame",             "0"},
        {"+CVars=bFramePacingEnabled",              "True"}
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
    LOGI("PubgmDistanceTieredAimbot injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}


// =============================================================================
// ─── PUBGM: Scope Target Tracking + Magic Bullet 5-Head (2026) ───────────────
// Scope-on = enemy head auto-lock that follows. No-scope magic bullet FOV bend.
// 5-round burst headshot lock any gun any range. Zero spread all states.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmScopeTargetTrackingMagicBullet5Head(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ── Scope Target Tracking (scope-on = enemy head lock follows) ──
        {"ScopeOnEnemyTrack",         "1"},
        {"ScopeOnHeadLock",           "1"},
        {"ScopeTrackEnemy",           "1"},
        {"ScopeFollowEnemyHead",      "1"},
        {"AimTrackingWhenScoped",     "1"},
        {"ScopedAutoHeadLock",        "1"},
        {"ScopedMagnetism",           "3"},
        // ── No-Scope Magic Bullet (enemy-follow without scope) ──
        {"NoScopeMagicBullet",        "1"},
        {"MagicBulletFollowEnemy",    "1"},
        {"BulletFOVCorrection",       "1"},
        {"BulletMagnetEnemy",         "1"},
        {"HipfireAutoAimEnemy",       "1"},
        {"MagicBulletEnemy",          "1"},
        // ── 5-Bullet Headshot Burst Lock ──
        {"Auto5BulletHeadshot",       "1"},
        {"HeadshotBurstCount",        "5"},
        {"FiveBulletHeadLock",        "1"},
        {"BurstFireRateLock",         "5"},
        {"AutoHeadshotBurst",         "5"},
        {"AutoHeadshotLock",          "1"},
        {"HeadBoneAimPriority",       "1"},
        {"HeadBoneLock",              "1"},
        {"HeadMagnetism",             "1"},
        // ── No Spread (hip, ADS, moving, jumping) ──
        {"WeaponSpread",              "0"},
        {"BulletSpreadScale",         "0"},
        {"HipfireSpread",             "0"},
        {"MuzzleSpread",              "0"},
        {"MovingSpreadFactor",        "0"},
        {"JumpSpreadFactor",          "0"},
        {"WeaponSway",                "0"},
        // ── Gyro + Hit Reg ──
        {"GyroSampleRate",            "1000"},
        {"GyroZeroDelay",             "1"},
        {"TouchPollingRate",          "1000"},
        {"HitRegSyncRate",            "1000"},
        {"r.AimAssistStrength",       "100"},
        {"r.HeadBoneAimPriority",     "1"},
        {"r.PUBGBulletVelocityCompensation", "1"},
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
    LOGI("PubgmScopeTargetTrackingMagicBullet5Head injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Fast Reload + Fast HP Regen + Fast Gun Switch + Fast Run (2026) ──
// Zero reload/bolt cycle, max HP regen instant, zero swap/holster/draw time,
// sprint speed boost + sprint-to-fire delay = 0.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastReloadHpRegenGunSwitchRun(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        // ── Fast Reload ──
        {"ReloadTimeMin",             "0"},
        {"ReloadIntervalMin",         "0"},
        {"FastReload",                "1"},
        {"InstantReload",             "1"},
        {"BoltCycleTimeMin",          "0"},
        {"PumpActionCycleMs",         "0"},
        {"MagChangeTimeMs",           "0"},
        {"FastBoltPullSpeed",         "10.0"},
        {"QuickReloadFactor",         "10.0"},
        // ── Fast HP Regen ──
        {"HpRegenRate",               "10"},
        {"HpRegenDelay",              "0"},
        {"HealthRegenMax",            "1"},
        {"FastHpRegen",               "1"},
        {"HealingRate",               "10"},
        {"BleedOutPrevention",        "1"},
        {"MaxHpRegenSpeed",           "1"},
        {"HealingSpeedMax",           "1"},
        {"HealthRegenInterval",       "0"},
        {"HpRegenCooldown",           "0"},
        // ── Fast Gun Switch / Weapon Swap ──
        {"WeaponSwapTime",            "0"},
        {"HolsterTime",               "0"},
        {"DrawTime",                  "0"},
        {"FastGunSwitch",             "1"},
        {"WeaponSwitchDelay",         "0"},
        {"InstantWeaponSwap",         "1"},
        {"QuickDrawFactor",           "10.0"},
        {"QuickDrawZeroDelay",        "1"},
        {"SprintToFireDelayMs",       "0"},
        {"ADSTransitionTimeMs",       "0"},
        {"AdsZeroDelay",              "1"},
        // ── Fast Run / Sprint ──
        {"SprintSpeedMax",            "1"},
        {"MovementSpeedBoost",        "1"},
        {"SprintSpeedBoost",          "10.0"},
        {"FastRunBoost",              "1"},
        {"SprintToFireDelay",         "0"},
        {"SlideSpeedBoost",           "1"},
        {"FastSlide",                 "1"},
        {"RunAnimSpeed",              "10.0"},
        {"JogSpeed",                  "10.0"},
        {"MaxMoveSpeed",              "1"},
        // ── Engine Sync ──
        {"TouchPollingRate",          "1000"},
        {"ZeroInputLag",              "1"},
        {"r.OneFrameThreadLag",       "0"},
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
    LOGI("PubgmFastReloadHpRegenGunSwitchRun injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// PUBGM: iPad View Ultra FOV (105° Ultra-Wide Perspective & Zero Obstruction)
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmIpadViewFov105(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        {"r.DefaultFOV",              "105"},
        {"r.CameraFOV",               "105"},
        {"r.ThirdPersonFOV",          "105"},
        {"r.FirstPersonFOV",          "105"},
        {"r.CameraOffsetZ",           "15.0"},
        {"r.CameraDistance",          "260.0"},
        {"CameraFOVScale",            "1.25"},
        {"TPPCameraFOV",              "105"},
        {"FPPCameraFOV",              "105"},
        {"IpadViewMode",              "1"},
        {"IpadViewFOV",               "105"},
        {"WideFOVEnabled",            "1"},
        {"CameraPerspectiveZoom",     "0"}
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
    LOGI("PubgmIpadViewFov105 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// PUBGM: Footstep & 3D Spatial Audio Clarity (Unmuffled Walls & Gunshot Localization)
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFootstepAudioVisualizerClarity(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        {"au.Spatialization",         "1"},
        {"au.LowPassFilter",          "0"},
        {"au.MaxChannels",            "64"},
        {"au.DisableWallOcclusion",   "1"},
        {"SoundQuality",              "2"},
        {"AudioQualityLevel",         "2"},
        {"FootstepDistanceBoost",     "2.0"},
        {"FootstepVolumeMultiplier",  "1.5"},
        {"r.FootstepClarityEnhanced", "1"},
        {"r.GunshotLocalization3D",   "1"},
        {"r.AudioOcclusionScale",     "0.0"},
        {"r.SoundReverbEnabled",      "0"},
        {"SpatialAudio3D",            "1"},
        {"HighPrecisionAudio3D",      "1"}
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
    LOGI("PubgmFootstepAudioVisualizerClarity injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// PUBGM: Anti-Foliage / Low Grass & Fog Removal (Prone Player Visibility)
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmNoGrassFoliageClarity(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        {"foliage.DensityScale",      "0.1"},
        {"foliage.MinimumScreenSize", "0.2"},
        {"r.Grass.DensityScale",      "0.1"},
        {"r.Atmosphere",              "0"},
        {"r.Fog",                     "0"},
        {"r.VolumetricFog",           "0"},
        {"r.ViewDistanceScale",       "2.0"},
        {"r.ShadowQuality",           "0"},
        {"r.Shadow.DistanceScale",    "0.0"},
        {"r.DepthOfFieldQuality",     "0"},
        {"GrassRenderDistance",       "50"},
        {"FoliageCullDistanceScale",  "0.2"},
        {"ClearSightDistance",        "500"}
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
    LOGI("PubgmNoGrassFoliageClarity injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// PUBGM: Zero-Delay Gyroscope & 1000Hz Raw Touch Overclock
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmZeroDelayGyroRawInput1000Hz(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> keys = {
        {"r.GyroscopeDelay",          "0"},
        {"r.GyroLatencyCompensation", "0"},
        {"r.GyroPredictiveSmoothing", "0"},
        {"r.GyroSampleRate",          "1000"},
        {"r.GyroZeroDelay",           "1"},
        {"r.GyroStabilization",       "1"},
        {"r.GyroSensitivityRatio",    "3.0"},
        {"GyroSensorPollingRate",     "1000"},
        {"GyroRawDataMode",           "1"},
        {"TouchPollingRate",          "1000"},
        {"TouchResponseTimeMs",       "1"},
        {"ZeroTouchLatency",          "1"},
        {"Input.TouchDeadzone",       "0"},
        {"TouchPressureThreshold",    "0"},
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
        struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("PubgmZeroDelayGyroRawInput1000Hz injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Full Overdrive 2026 — Master Combo (all above in one pass) ───────
// Calls ScopeTargetTrackingMagicBullet5Head + FastReloadHpRegenGunSwitchRun
// + AutoHead5BulletAimLock + IpadView + FootstepAudio + NoGrass + Gyro1000Hz.
// =============================================================================
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFullOverdrive2026(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    env->ReleaseStringUTFChars(jPath, path);

    bool r0 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmDistanceTieredAimbot(env, nullptr, jPath);
    bool r1 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmScopeTargetTrackingMagicBullet5Head(env, nullptr, jPath);
    bool r2 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastReloadHpRegenGunSwitchRun(env, nullptr, jPath);
    bool r3 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAutoHead5BulletAimLock(env, nullptr, jPath);
    bool r4 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmIpadViewFov105(env, nullptr, jPath);
    bool r5 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFootstepAudioVisualizerClarity(env, nullptr, jPath);
    bool r6 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmNoGrassFoliageClarity(env, nullptr, jPath);
    bool r7 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmZeroDelayGyroRawInput1000Hz(env, nullptr, jPath);
    LOGI("PubgmFullOverdrive2026 injected [tiered=%d scope=%d fast=%d lock=%d ipad=%d audio=%d grass=%d gyro=%d]", r0, r1, r2, r3, r4, r5, r6, r7);
    return (r0 || r1 || r2 || r3 || r4 || r5 || r6 || r7) ? JNI_TRUE : JNI_FALSE;
}


