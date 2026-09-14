// =============================================================================
// Other Games (Free Fire, Blood Strike, Delta Force, HOK, Wild Rift) Injector
// High-performance isolated translation unit for GameBooster Native
// =============================================================================

#include "native_config_injector.h"
#include "config_common.h"

// =============================================================================
// ─── Free Fire: Auto Drag Headshot Magnetism + Zero Bloom ─────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireAutoHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"DragHeadshotAssist","1"},{"HeadshotSensitivityMultiplier","3.0"},{"CrosshairBloom","0"},
        {"SpreadZero","1"},{"RecoilControlAssist","1"},{"HeadMagnetism","1"},
        {"AimBoneTarget","0"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},
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
    LOGI("FreeFireAutoHeadshot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── Free Fire: Instant 360 Gloo Wall + Fast Reload ──────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireFastGlooWall
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"InstantGlooWall","1"},{"GlooWallDeployDelay","0"},{"FastGlooCrouch","1"},
        {"ReloadSpeedBoost","10"},{"WeaponSwitchZeroDelay","1"},{"SprintDelayZero","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
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
    LOGI("FreeFireFastGlooWall injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"GunDamageMultiplier", "10000"},
        {"FireRateOverclock", "10000"}, {"FireRateBoost", "10.0"}, {"AutoHeadshotDamage", "10000"},
        {"HeadshotMultiplier", "5.0"}, {"QuickShotZeroDelay", "1"}, {"GlooWallDeploySpeed", "10.0"},
        {"HitRegSyncRate", "1000"}, {"InstantHitReg", "1"}, {"ZeroSpread", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FreeFireDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Blood Strike: Zero Recoil + Slide Cancel ────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeZeroRecoil
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"RecoilCompFactor","0"},{"SpreadZero","1"},{"SlideCancelSync","1"},
        {"FastTacticalSprint","1"},{"HipfireSpread","0"},{"HeadMagnetism","1"},
        {"AimBoneTarget","0"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"HitRegSyncRate","1000"},{"ZeroInputLag","1"},{"FrameSyncDamage","1"},
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
    LOGI("BloodStrikeZeroRecoil injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── Delta Force: Precision Aim + Bullet Drop Calculator ─────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForcePrecisionAim
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"BulletDropComp","1"},{"MuzzleVelocityFactor","1.0"},{"ZeroSwaySniper","1"},
        {"ThermalScopeLock","1"},{"HeadBonePriority","1"},{"AimMagnetism","3"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"HitRegSyncRate","1000"},
        {"ZeroInputLag","1"},{"FrameSyncDamage","1"},
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
    LOGI("DeltaForcePrecisionAim injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── HOK: Auto Smite Objective + Predictive Skill Aim ────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHokAutoSmiteObjective
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"HokSmiteObjectivePriority","1"},{"HokSmiteStealMax","1"},{"HokPredictiveSkillAim","1"},
        {"SkillSmartAim","1"},{"HeroLock","1"},{"ObjectiveTargetLock","1"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"HitRegSyncRate","1000"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
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
    LOGI("HokAutoSmiteObjective injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHokDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"PhysicalPowerBase", "10000"},
        {"MagicPowerBase", "10000"}, {"AttackSpeedCap", "10.0"}, {"AttackSpeedBoost", "10000"},
        {"AutoAttackInterval", "0"}, {"AttackAnimSpeed", "10.0"}, {"AutoSmiteDamage", "10000"},
        {"InstantBasicAttack", "1"}, {"HitRegSyncRate", "1000"}, {"SpellVampBoost", "100"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "HokDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWildRiftDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"AttackDamageBase", "10000"},
        {"AbilityPowerBase", "10000"}, {"AttackSpeedMaxRatio", "10.0"}, {"AttackSpeedBoost", "10000"},
        {"AutoAttackWindupZero", "1"}, {"TrueDamageMultiplier", "10000"}, {"CritRateBoost", "100"},
        {"InstantBasicAttack", "1"}, {"HitRegSyncRate", "1000"}, {"OmnivampBoost", "100"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "WildRiftDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Blood Strike: Slide Cancel & Fast Sprint Overdrive ──────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeSlideCancelOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"SlideCancelSync", "1"}, {"FastTacticalSprint", "1"}, {"SlideDistanceMax", "1"},
        {"SprintToFireDelay", "0"}, {"WeaponSwitchZeroDelay", "1"}, {"ReloadCancelFast", "1"},
        {"ZeroRecoil", "1"}, {"SpreadZero", "1"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"HitRegSyncRate", "1000"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "BloodStrikeSlideCancelOverdrive");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Delta Force: UE5 Nanite Shader Pre-Cache & Ballistic Trajectory Lock ────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForceNaniteShaderPrewarm
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"BulletDropComp", "1"}, {"MuzzleVelocityFactor", "1.0"}, {"ZeroSwaySniper", "1"},
        {"ThermalScopeLock", "1"}, {"NaniteShaderPreCache", "1"}, {"PreloadShaders", "1"},
        {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}, {"bFramePacingEnabled", "True"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"HitRegSyncRate", "1000"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "DeltaForceNaniteShaderPrewarm");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Arena Breakout: Thermal Clarity & Footstep Audio Visualizer ESP ─────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArenaBreakoutThermalFootstepAudio
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ThermalContrastBoost", "1"}, {"FootstepSoundVFXBoost", "1"}, {"ZeroSwaySniper", "1"},
        {"StaminaDrainReduction", "1"}, {"BulletDropComp", "1"}, {"HitRegSyncRate", "1000"},
        {"InstantHitReg", "1"}, {"TouchPollingRate", "1000"}, {"ZeroInputLag", "1"},
        {"AllowOcclusionQueries", "1"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ArenaBreakoutThermalFootstepAudio");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Valorant Mobile: Counter-Strafe Zero Deadzone & Head-Level Crosshair ───
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectValorantCounterStrafeAimLock
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"CounterStrafeDeadzone", "0"}, {"FirstBulletAccuracy", "1.0"}, {"CrosshairHeadLevelLock", "1"},
        {"MovingSpreadFactor", "0"}, {"ZeroRecoil", "1"}, {"HeadMagnetism", "1"},
        {"AimSnapSpeed", "10"}, {"AimSmoothFactor", "0"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"HitRegSyncRate", "1000"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ValorantCounterStrafeAimLock");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Farlight 84: Jetpack Zero Cooldown & Air-Dash Distance Max ──────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFarlightJetpackZeroCooldown
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"JetpackCooldownDelay", "0"}, {"AirDashDistanceMax", "1"}, {"RapidReload", "1"},
        {"AimAssistStrength", "100"}, {"AimMagnetism", "3"}, {"ZeroRecoil", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"HitRegSyncRate", "1000"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FarlightJetpackZeroCooldown");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Standoff 2: 128-Tick Rate Network Emulation & Zero Spread ───────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectStandoff2Tick128ZeroSpread
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"cl_updaterate", "128"}, {"cl_cmdrate", "128"}, {"rate", "786432"},
        {"ZeroRecoil", "1"}, {"SpreadZero", "1"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"HitRegSyncRate", "1000"},
        {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "Standoff2Tick128ZeroSpread");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Blood Strike: Damage 10000 & Attack Speed Max Overdrive ─────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamage", "10000"},
        {"FleshDamageMultiplier", "3.0"}, {"ArmorDamageMultiplier", "3.0"}, {"VestDamageBypass", "1"},
        {"PelletDamageFull", "1"}, {"ShotgunDamagePerPellet", "100"}, {"HeadshotMultiplier", "5.0"},
        {"FireRateOverclock", "10000"}, {"FireRateBoost", "10.0"}, {"FastTacticalSprint", "1"},
        {"SlideCancelSync", "1"}, {"ZeroRecoil", "1"}, {"SpreadZero", "1"},
        {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "BloodStrikeDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Delta Force: Damage 10000 & Sniper Lethality Overdrive ──────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForceDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamageBoost", "10000"},
        {"SniperHeadshotDamage", "999"}, {"OneShotKillHitbox", "1"}, {"ArmorPenetrationTier6", "1"},
        {"BulletDropComp", "1"}, {"MuzzleVelocityFactor", "2.0"}, {"ZeroSwaySniper", "1"},
        {"FrameSyncDamage", "1"}, {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"},
        {"ZeroRecoil", "1"}, {"SpreadZero", "1"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "DeltaForceDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Arena Breakout: Damage 10000 & Armor Penetration Max ────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArenaBreakoutDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"ArmorPiercingTier6", "1"},
        {"ArmorDamageMultiplier", "3.0"}, {"LimbDamageMultiplier", "2.5"}, {"FleshDamageMultiplier", "3.0"},
        {"SniperOneShotKill", "1"}, {"ZeroSwaySniper", "1"}, {"BulletDropComp", "1"},
        {"ThermalContrastBoost", "1"}, {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"},
        {"ZeroRecoil", "1"}, {"SpreadZero", "1"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ArenaBreakoutDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Valorant Mobile: Damage 10000 & Headshot 1-Tap Lethality ────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectValorantDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"HeadshotMultiplier", "5.0"},
        {"OneTapHeadshot", "1"}, {"FirstBulletAccuracy", "1.0"}, {"CounterStrafeDeadzone", "0"},
        {"CrosshairHeadLevelLock", "1"}, {"MovingSpreadFactor", "0"}, {"ZeroRecoil", "1"},
        {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ValorantDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Farlight 84: Damage 10000 & Air-Dash Rapid Combat Overdrive ─────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFarlightDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"GunDamageMultiplier", "10000"},
        {"FireRateOverclock", "10000"}, {"AirDashBulletSync", "1"}, {"JetpackCooldownDelay", "0"},
        {"RapidReload", "1"}, {"HeadshotMultiplier", "4.0"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"ZeroRecoil", "1"}, {"SpreadZero", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FarlightDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Standoff 2: Damage 10000 & 128-Tick Instant Hit Registration ────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectStandoff2Damage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"HeadshotDamageMultiplier", "5.0"},
        {"cl_updaterate", "128"}, {"cl_cmdrate", "128"}, {"rate", "786432"},
        {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"}, {"ZeroRecoil", "1"},
        {"SpreadZero", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "Standoff2Damage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Genshin Impact: Damage 10000 & Elemental Reaction Burst Max ─────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGenshinDamage10000ElementalBurstMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"ElementalDamageMultiplier", "10000"},
        {"PhysicalDamageBase", "10000"}, {"CritRateBoost", "100"}, {"CritDamageMultiplier", "10.0"},
        {"ElementalMasteryBoost", "10000"}, {"EnergyRechargeMax", "1"}, {"BurstCooldownZero", "1"},
        {"InstantSkillCast", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"}, {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "GenshinDamage10000ElementalBurstMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Roblox: Damage 10000 & Physics Tick Overclock ───────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectRobloxDamage10000Max
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamageBoost", "10000"},
        {"PhysicsTickRate", "1000"}, {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"},
        {"ZeroInputLag", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "RobloxDamage10000Max");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CarX: Torque & Horsepower 10000 Drift Overdrive ─────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCarXTorqueHorsepower10000Max
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"TorqueMultiplier", "10.0"}, {"HorsepowerBoost", "10000"}, {"TurboBoostMax", "1"},
        {"TireGripSlipOptimization", "1"}, {"SteeringAngleMax", "70"}, {"ZeroSteeringLag", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"bFramePacingEnabled", "True"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "CarXTorqueHorsepower10000Max");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

