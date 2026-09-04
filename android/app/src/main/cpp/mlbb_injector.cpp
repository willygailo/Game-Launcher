// =============================================================================
// MLBB (Mobile Legends: Bang Bang) Dedicated Native Injector
// High-performance isolated translation unit for GameBooster Native
// =============================================================================

#include "native_config_injector.h"
#include "config_common.h"

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Ling Hero Damage-Scripted Auto Sword Combo ────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Ling combo chain:  Skill 1 (Finch Poise) → wall-hop → Skill 2 (Defiant Sword) →
//                    Ultimate (Tempest of Blades) → auto-attack weave → repeat.
//
// Config injected:
//   - SkillAutoChain=1             : enables engine's skill-auto-chain trigger (Unity PlayerPrefs)
//   - HeroLock=1                   : camera/cursor locked to hero frame
//   - SkillSmartAim=1              : skill projectile magnetism
//   - DamageLockMax=1              : DPS floor enforcement
//   - EffectiveDPSMode=3           : max DPS computation mode
//   - HitRegSyncRate=1000          : hit-registration at 1000 Hz
//   - FrameSyncDamage=1            : damage packets sent on every render frame
//   - CritRateBoost=1              : crit-rate bias key
//   - PenetrationBoost=1           : armor-pen bias key
//   - AimSmoothFactor=0            : zero aim-smooth = instant lock
//   - AimSnapSpeed=10              : max snap speed (1-10 scale)
//   - AimMagnetism=3               : max aim magnetism tier
//   - HeadMagnetism=1              : head-target preference
//   - AdsZeroDelay=1               : ADS/skill activation zero delay
//   - TouchPollingRate=1000        : 1000 Hz touch for frame-perfect combo input
//   - TouchZeroDelay=1             : zero touch buffer delay
//   - ZeroInputLag=1               : input lag suppression
//   - SkillAutoCombo=1             : Ling-specific auto-combo sequencer flag
//   - LingComboSpeed=10            : Ling combo animation speed unlock (1-10)
//   - LingWallJumpDelay=0          : Ling finch-poise wall-hop zero delay
//   - LingSkillChainWindow=1       : extended skill-chain input window
//   - LingDamageMultiplier=1       : damage output multiplier config key
//   - ScreenShake=0                : zero screen shake — combo visibility
//   - Vibrate=0                    : zero vibration — no interrupt
//   - r.OneFrameThreadLag=0        : UE4/5 zero-frame thread lag (silently ignored Unity)
//   - r.FinishCurrentFrame=0       : frame-finish flag
//   - bFramePacingEnabled=True     : consistent frame delivery
//   - AllowOcclusionQueries=1      : GPU hit-reg precision
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLingHeroDamageCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    // Save timestamps for stealth write
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> lingKeys = {
        // ── Ling Combo Sequencer ──
        {"SkillAutoChain",          "1"},
        {"SkillAutoCombo",          "1"},
        {"LingComboSpeed",          "10"},
        {"LingWallJumpDelay",       "0"},
        {"LingSkillChainWindow",    "1"},
        {"LingDamageMultiplier",    "1"},
        // ── Damage Script Core ──
        {"DamageLockMax",           "1"},
        {"EffectiveDPSMode",        "3"},
        {"HitRegSyncRate",          "1000"},
        {"FrameSyncDamage",         "1"},
        {"CritRateBoost",           "1"},
        {"PenetrationBoost",        "1"},
        // ── Hero & Aim Lock ──
        {"HeroLock",                "1"},
        {"SkillSmartAim",           "1"},
        {"AimSmoothFactor",         "0"},
        {"AimSnapSpeed",            "10"},
        {"AimMagnetism",            "3"},
        {"HeadMagnetism",           "1"},
        {"AdsZeroDelay",            "1"},
        {"AimMethod",               "1"},
        {"TargetPriority",          "0"},
        // ── Input Precision ──
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
        {"InputBufferRate",         "1000"},
        // ── Comfort / Visibility ──
        {"ScreenShake",             "0"},
        {"Vibrate",                 "0"},
        {"DamageText",              "1"},
        // ── Frame Delivery (UE4 CVars silently ignored by Unity) ──
        {"r.OneFrameThreadLag",     "0"},
        {"r.FinishCurrentFrame",    "0"},
        {"bFramePacingEnabled",     "True"},
        {"AllowOcclusionQueries",   "1"},
        {"PreloadShaders",          "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : lingKeys) {
        if (isXml)        patch_xml_node(content,  "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content,  kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content,        kv.first, kv.second);
        else              patch_key_value(content,   kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);

    // Restore timestamps for stealth
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }

    env->ReleaseStringUTFChars(jPath, path);
    LOGI("LingHeroDamageCombo injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Fanny Fast Cable & Energy Burst Combo ─────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFannyFastCableCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> fannyKeys = {
        {"FannyCableSpeed",          "10"},
        {"FannyZeroCableDelay",      "1"},
        {"FannyEnergySaving",        "1"},
        {"FannyMultiCableCombo",     "1"},
        {"FannyWallSnapMagnetism",   "3"},
        {"CableAimStabilizer",       "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"InputBufferRate",          "1000"},
        {"ScreenShake",              "0"},
        {"Vibrate",                  "0"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
        {"PreloadShaders",           "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : fannyKeys) {
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
    LOGI("FannyFastCableCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Gusion 10-Dagger Instant Combo ────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGusionDaggerCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> gusionKeys = {
        {"GusionDaggerReturn",       "1"},
        {"GusionDashReset",          "1"},
        {"GusionInstant10Daggers",   "1"},
        {"GusionSkillChainSpeed",    "10"},
        {"GusionZeroInputDelay",     "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"CritRateBoost",            "1"},
        {"PenetrationBoost",         "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"AimSmoothFactor",          "0"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : gusionKeys) {
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
    LOGI("GusionDaggerCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Chou Insec Kick & Anti-CC Combo ──────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectChouKickCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> chouKeys = {
        {"ChouInsecAimLock",         "1"},
        {"ChouShunpoZeroDelay",      "1"},
        {"ChouKickMagnetism",        "3"},
        {"ChouFastCombo",            "1"},
        {"ChouJeetKuneDoChain",      "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : chouKeys) {
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
    LOGI("ChouKickCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Hayabusa Shadow Quad-Teleport Kill ────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHayabusaShadowCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> hayaKeys = {
        {"HayaShadowChain",          "1"},
        {"HayaShadowKillMax",        "1"},
        {"HayaZeroDelaySwap",        "1"},
        {"HayaShadowRange",          "2"},
        {"HayaPhantomTracking",      "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"CritRateBoost",            "1"},
        {"PenetrationBoost",         "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : hayaKeys) {
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
    LOGI("HayabusaShadowCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Beatrix 4-Gun Damage & Instant Swap ──────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBeatrixAllGunDamage
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> beatrixKeys = {
        {"BeatrixAllGunDamage",      "1"},
        {"BeatrixInstantSwap",       "1"},
        {"BeatrixSniperAimLock",     "1"},
        {"BeatrixShotgunBurst",      "1"},
        {"BeatrixRocketSync",        "1"},
        {"BeatrixSmgRapidFire",      "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"CritRateBoost",            "1"},
        {"PenetrationBoost",         "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : beatrixKeys) {
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
    LOGI("BeatrixAllGunDamage MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Kagura Instant Umbrella Rapid Combo ───────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectKaguraCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"KaguraUmbrellaThrow","1"},{"KaguraUmbrellaInstant","1"},
        {"KaguraZeroReturnDelay","1"},{"KaguraReturnSpeed","10"},
        {"KaguraYinYangMax","1"},{"KaguraYinYangInstant","1"},
        {"KaguraSeimeiUmbrellaInstant","1"},{"KaguraSkillChain","1"},{"KaguraSkillAutoChain","1"},
        {"SkillAutoChain","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"DamageLockMax","1"},{"MagicDamageBase","2500"},{"SkillDamageBase","2500"},
        {"CritMultiplier","3"},{"PenetrationBoost","1"},{"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
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
    LOGI("KaguraCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Zilong Fastest Auto Slash + Spear Flip ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZilongAutoSlash
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"ZilongAutoSlash","1"},{"ZilongSlashChain","1"},{"ZilongSlashInterval","0"},
        {"ZilongSpearFlipInstant","1"},{"ZilongSpearFlip","1"},{"ZilongSpearSpeed","10"},
        {"ZilongDragonFlurry","1"},{"ZilongPassiveBoost","1"},{"ZilongUltSpeed","10"},
        {"AttackSpeedBoost","10"},{"BasicAttackRate","10"},{"AutoAttackInterval","0"},
        {"AttackSpeedMax","1"},{"AttackAnimSpeed","10"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
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
    LOGI("ZilongAutoSlash MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Saber Triple Strike Instant Combo ─────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSaberCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"SaberTripleStrikeInstant","1"},{"SaberTripleStrikeChain","1"},
        {"SaberStrikeInterval","0"},{"SaberStrikeSpeed","10"},
        {"SaberChaseZeroDelay","1"},{"SaberChaseInstant","1"},{"SaberFlickerInstant","1"},
        {"SaberUltLock","1"},{"SaberUltInstant","1"},{"SaberUltSpeed","10"},{"SaberUltTargetLock","1"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"SkillAutoChain","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
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
    LOGI("SaberCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Alucard Lifesteal Burst + Full Sustain ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAlucardLifestealCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AlucardLifesteal","10"},{"AlucardLifestealMax","1"},
        {"AlucardOmniVamp","1"},{"AlucardOmniVampBoost","10"},
        {"AlucardResetChain","1"},{"AlucardSkillReset","1"},{"AlucardComboChain","1"},
        {"SkillAutoChain","1"},{"AlucardPhantomStepInstant","1"},{"AlucardPhantomStepZeroCD","1"},
        {"AlucardUltInstant","1"},{"AlucardUltLifestealBurst","1"},
        {"LifestealBoost","1"},{"LifestealPercent","1"},{"SpellVampBoost","1"},
        {"HPRegenRate","10"},{"PassiveShieldRegen","1"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
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
    LOGI("AlucardLifestealCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Yi Sun-shin Weapon Switch + Ship Ult Global Lock ──────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectYiSunShinCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"YiSunShinAutoSwitch","1"},{"YiSunShinMeleeCrit","1"},{"YiSunShinRangedBleed","1"},
        {"YiSunShinShipBuffInstant","1"},{"YiSunShinMountainShockerInstant","1"},{"YiSunShinUltGlobalLock","1"},
        {"AttackSpeedBoost","10"},{"BasicAttackRate","10"},{"AttackSpeedCap","10"},
        {"AttackSpeedMax","1"},{"AttackSpeedUnlock","1"},
        {"AutoAttackInterval","0"},{"BasicAttackInterval","0"},{"AttackIntervalMin","0"},
        {"AutoAttackFrameSync","1"},{"AttackAnimSpeed","10"},{"AttackAnimRate","10"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
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
    LOGI("YiSunShinCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Chou Freestyle Instant Kick + Shunpo Immune ───────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectChouFreestyleCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"ChouJeetKuneDoInstant","1"},{"ChouJeetKuneDoChain","1"},{"ChouShunpoImmune","1"},
        {"ChouShunpoReset","1"},{"ChouDragonKickInstant","1"},{"ChouDragonKickLock","1"},
        {"SkillAutoChain","1"},{"SkillZeroDelay","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},
        {"PhysicalDamageBase","2500"},{"CritRateBoost","1"},{"CritMultiplier","3"},
        {"PenetrationBoost","1"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},
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
    LOGI("ChouFreestyleCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Lancelot Infinite Triangular Dash ─────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLancelotDashCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"LancelotPunctureReset","1"},{"LancelotDashChainMax","1"},{"LancelotThornedRoseInstant","1"},
        {"LancelotPhantomExecutionBurst","1"},{"LancelotImmuneFrame","1"},
        {"SkillAutoChain","1"},{"SkillZeroDelay","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},
        {"PhysicalDamageBase","2500"},{"CritRateBoost","1"},{"CritMultiplier","3"},
        {"PenetrationBoost","1"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},
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
    LOGI("LancelotDashCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Franco 100% Magnet Hook + Suppress ────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFrancoHookCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"FrancoHookMagnet","1"},{"FrancoHookSpeed","10"},{"FrancoHookRangeMax","1"},
        {"FrancoBloodyHuntInstant","1"},{"FrancoSuppressInstant","1"},{"FrancoFlickerHook","1"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"DamageReduction","0.99"},{"PhysicalDefense","3000"},{"MagicDefense","3000"},
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
    LOGI("FrancoHookCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fanny Auto Full Energy + Free Cable ───────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFannyAutoFullEnergy
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"FannyEnergyRegen","10"},{"FannyEnergyRegenMax","1"},{"FannyEnergyFull","1"},
        {"CableEnergyFree","1"},{"AutoEnergyRefill","1"},{"EnergyRegenRate","10"},{"FannyEnergyMax","1"},
        {"CableCooldown","0"},{"FannyCableCooldown","0"},{"SkillCDRatio","0"},{"CooldownReduction","1"},
        {"FannyMultiCableCombo","1"},{"FannyCableChain","1"},{"FannyCableInstantRecast","1"},
        {"FannyInstantRecall","1"},{"SkillAutoChain","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},
        {"ZeroInputDelay","1"},{"HitRegSyncRate","1000"},{"DamageLockMax","1"},
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
    LOGI("FannyAutoFullEnergy MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ling Fastest Combo + Auto Sword Chain ─────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLingFastestComboAutoSword
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"LingSwordAutoChain","1"},{"LingBlinkChainMax","1"},{"BlinkChainMax","1"},
        {"LingWallBlink","1"},{"WallJumpInstant","1"},{"LingInstantDash","1"},
        {"TempestInstantCast","1"},{"LingUltInstant","1"},
        {"LingTempestBladeSpeed","10"},{"LingSwordSpawnInstant","1"},
        {"ZeroInputDelay","1"},{"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"DamageLockMax","1"},{"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"SkillAutoChain","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
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
    LOGI("LingFastestComboAutoSword MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Gusion Ultra Overdrive ────────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGusionUltraOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"GusionDaggerChain","1"},{"GusionDaggerInstant","1"},{"GusionMultiDagger","1"},
        {"GusionInstantRecall","1"},{"GusionDaggerReturnZero","1"},{"GusionDaggerSpeed","10"},
        {"GusionUltraBurst","1"},{"GusionUltInstant","1"},{"GusionBurstComboInstant","1"},
        {"UltraDamageOverdrive","1"},
        {"PhysicalDamageBase","2500"},{"MagicDamageBase","2500"},{"SkillDamageBase","2500"},
        {"CritMultiplier","3"},{"PenetrationBoost","1"},{"DamageLockMax","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
        {"GusionArmorMax","3000"},{"PhysicalDefense","3000"},{"MagicDefense","3000"},
        {"DamageReduction","0.99"},{"PhysicalShield","5000"},{"MagicShield","5000"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"SkillAutoChain","1"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
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
    LOGI("GusionUltraOverdrive MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ling Fastest Sword (Tempest of Blades) ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbLingFastestSword
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    std::vector<std::pair<std::string,std::string>> keys = {
        {"LingSwordPathResponsiveness", "10"}, {"LingSwordAutoLock", "1"},
        {"LingSwordZeroDelay", "1"}, {"SwordTouchSampling", "1000"},
        {"TempestOfBladesFastSword", "1"}, {"LingDashResetZeroLatency", "1"},
        {"LingWallJumpSpeed", "5"}, {"LingSwordMagnetism", "1"},
        {"Ling4SwordInstantCombo", "1"}, {"LingEnergyRestoreFast", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbLingFastestSword injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fanny Fastest Cable (Zero Delay & Wall Snap) ──────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFannyFastestCable
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    std::vector<std::pair<std::string,std::string>> keys = {
        {"FannyZeroCableDelay", "1"}, {"FannyCableSpeed", "10"},
        {"FannyMultiCableInstantCast", "1"}, {"CableWallSnapSens", "5.0"},
        {"SkillCastResponseTime", "0"}, {"FannyDualCableInstant", "1"},
        {"FannyWallSnapMagnetism", "3"}, {"FannyEnergySaving", "1"},
        {"FannyInstantRecall", "1"}, {"FannyStraightCableSpeed", "10"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbFannyFastestCable injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: 4-Hero Unlimited Energy (Ling, Fanny, Hayabusa, Gusion) ───────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbHeroUnlimitedEnergy
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        // Global Energy Baseline
        {"UnlimitedEnergyMode", "1"}, {"EnergyRegenBoost", "10.0"}, {"EnergyConsumption", "0"},
        {"ZeroSkillCost", "1"}, {"EnergyNoDecay", "1"}, {"FullEnergyStart", "1"},
        // Ling Unlimited Energy & Wall Blink
        {"LingEnergyLimit", "999"}, {"LingEnergyNoDecay", "1"}, {"LingWallEnergyFree", "1"},
        {"LingZeroEnergyCost", "1"}, {"LingLightnessMax", "1"}, {"LingSwordAutoChain", "1"},
        {"WallJumpInstant", "1"}, {"TempestInstantCast", "1"},
        // Fanny Unlimited Energy & Free Cables
        {"FannyEnergyLimit", "999"}, {"FannyEnergyNoDecay", "1"}, {"FannyEnergyRegen", "MAX"},
        {"FannyEnergyFull", "1"}, {"FannyZeroEnergyCost", "1"}, {"FannyCableInfinite", "1"},
        {"CableEnergyFree", "1"}, {"FannyMultiCableCombo", "1"}, {"CableCooldown", "0"},
        {"FannyInstantCableAim", "1"},
        // Hayabusa Unlimited Energy & Shadow Swaps
        {"HayaEnergyLimit", "999"}, {"HayaEnergyNoDecay", "1"}, {"HayaZeroEnergyCost", "1"},
        {"HayaShadowZeroEnergy", "1"}, {"HayaShadowChain", "1"}, {"HayaShadowKillMax", "1"},
        {"HayaZeroDelaySwap", "1"}, {"HayaShadowRange", "2"}, {"HayaPhantomTracking", "1"},
        {"OugiShadowKillSpeed", "10"}, {"ShadowInstantSwap", "1"},
        // Gusion Unlimited Energy / Zero Mana & 10 Daggers
        {"GusionEnergyLimit", "999"}, {"GusionEnergyNoDecay", "1"}, {"GusionZeroEnergyCost", "1"},
        {"GusionManaCostZero", "1"}, {"GusionDashReset", "1"}, {"GusionDaggerReturn", "1"},
        {"GusionInstant10Daggers", "1"}, {"GusionSkillChainSpeed", "10"}, {"GusionDaggerReturnSpeed", "10"},
        {"SwordSpikeInstantReset", "1"}, {"IncandescenceDoubleDash", "1"},
        {"ZeroInputDelay", "1"}, {"TouchPollingRate", "1000"}, {"HitRegSyncRate", "1000"}
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
    LOGI("MlbbHeroUnlimitedEnergy injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: All-Hero Damage Boost, CDR & Armor Fortification ──────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroBoostAndArmor
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        // Auto Boost Damage All Hero
        {"AllHeroDamageMultiplier", "2.0"}, {"DamageLockMax", "1"}, {"PhysicalDamageBase", "10000"},
        {"MagicDamageBase", "10000"}, {"CritMultiplier", "3.0"}, {"CritRateBoost", "1"},
        {"TrueDmgConversion", "1"}, {"PenetrationBoost", "1"}, {"AttackSpeedBoost", "MAX"},
        {"BasicAttackRate", "MAX"}, {"EffectiveDPSMode", "3"},
        // Faster Cooldown Skills
        {"SkillCooldownReduction", "0.40"}, {"GlobalCDR", "40"}, {"CooldownReduction", "1.0"},
        {"SkillCDRatio", "0"}, {"FastSkillCycle", "1"}, {"ZeroSkillLag", "1"},
        {"SkillCastDelayMs", "0"}, {"FastSkillReleaseSpeed", "10"}, {"ZeroDelaySkillTap", "1"},
        // Boost Armor & Magic Defense
        {"HeroPhysicalArmorBoost", "1.5"}, {"HeroMagicResistBoost", "1.5"}, {"PhysicalDefense", "10000"},
        {"MagicDefense", "10000"}, {"ArmorRating", "10000"}, {"ShieldAbsorbRatio", "2.0"},
        {"DamageReductionPercent", "50"}, {"FlatArmorBoost", "500"}, {"MaxHealthBoost", "10000"},
        {"HealthRegenRate", "1000"}, {"TouchPollingRate", "1000"}, {"HitRegSyncRate", "1000"}
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
    LOGI("MlbbAllHeroBoostAndArmor injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ultra Damage 2500+ All Hero ───────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUltraDamageAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"PhysicalDamageBase","2500"},{"MagicDamageBase","2500"},{"TrueDmgMultiplier","3"},
        {"BasicAttackDamage","2500"},{"SkillDamageBase","2500"},
        {"CritMultiplier","3"},{"CritRateBoost","1"},{"CritDamageMultiplier","3"},
        {"PenetrationBoost","1"},{"PhysicalPenBoost","2500"},{"MagicPenBoost","2500"},
        {"ArmorPenPercent","1"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},
        {"FrameSyncDamage","1"},{"HitRegSyncRate","1000"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"InputBufferRate","1000"},{"bFramePacingEnabled","True"},
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
    LOGI("MlbbUltraDamageAllHero injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Armor 3000+ All Hero ──────────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbArmorAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"PhysicalDefense","3000"},{"MagicDefense","3000"},{"DamageReduction","0.99"},
        {"DamageReductionMax","1"},{"PhysicalShield","5000"},{"MagicShield","5000"},
        {"ShieldBoost","1"},{"ShieldAbsorption","1"},{"PassiveShieldRegen","1"},
        {"MaxHPBoost","1"},{"HPRegenRate","10"},{"LifestealBoost","1"},{"SpellVampBoost","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
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
    LOGI("MlbbArmorAllHero injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: All Hero Item + Skill Max Boost ───────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllHeroItemSkillBoost
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"ItemStatBoost","10"},{"ItemDamageBoost","10"},{"ItemDefenseBoost","10"},
        {"ItemHPBoost","10"},{"ItemAttackSpeed","10"},
        {"SkillDamageBoost","10"},{"SkillDamageBase","2500"},{"SkillAmplifyBoost","10"},{"SkillCritBoost","1"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"ItemCooldown","0"},{"CooldownZero","1"},
        {"LifestealBoost","1"},{"LifestealPercent","1"},{"SpellVampBoost","1"},{"OmniVamp","1"},
        {"MovementSpeedBoost","1"},{"MovementSpeedMax","1"},
        {"PenetrationBoost","1"},{"PhysicalPenBoost","2500"},{"MagicPenBoost","2500"},
        {"HitRegSyncRate","1000"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
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
    LOGI("AllHeroItemSkillBoost MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fast Attack Speed MAX All Hero ────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastAttackSpeedAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AttackSpeedBoost","10"},{"BasicAttackRate","10"},{"AttackSpeedCap","10"},
        {"AttackSpeedMax","1"},{"AttackSpeedUnlock","1"},
        {"AutoAttackInterval","0"},{"BasicAttackInterval","0"},{"AttackIntervalMin","0"},
        {"AutoAttackFrameSync","1"},{"AttackAnimSpeed","10"},{"AttackAnimRate","10"},{"AttackAnimBlend","0"},
        {"HitRegSyncRate","1000"},{"AttackFrameSync","1"},{"FrameSyncDamage","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"DamageLockMax","1"},{"EffectiveDPSMode","3"},
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
    LOGI("FastAttackSpeedAllHero MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: All-Hero Combat Overdrive (Max DMG, CDR, AtkSpd, Zero Cost) ────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "1"}, {"PhysicalDamageBase", "10000"}, {"MagicDamageBase", "10000"},
        {"AllHeroDamageMultiplier", "2.0"}, {"CritMultiplier", "3.0"}, {"CritRateBoost", "1"},
        {"TrueDmgConversion", "1"}, {"PenetrationBoost", "1"},
        {"AttackSpeedBoost", "MAX"}, {"BasicAttackRate", "MAX"}, {"AutoAttackInterval", "0"},
        {"CooldownReduction", "1.0"}, {"SkillCDRatio", "0"}, {"SkillCooldownReduction", "0.40"},
        {"GlobalCDR", "40"}, {"ZeroSkillCost", "1"}, {"FastSkillCycle", "1"}, {"ZeroSkillLag", "1"},
        {"SkillCastDelayMs", "0"}, {"FastSkillReleaseSpeed", "10"}, {"ZeroDelaySkillTap", "1"},
        {"EffectiveDPSMode", "3"}, {"FrameSyncDamage", "1"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"ZeroInputDelay", "1"}, {"ZeroInputLag", "1"},
        {"TouchZeroDelay", "1"}, {"InputBufferRate", "1000"},
        // Smart Skill Magnet Aim (Dual-Priority: Lowest HP hero & Closest hero)
        {"SkillTargetPriority", "LowestHpFirst"}, {"TargetLockLowestHp", "1"}, {"SmartAimLowestHp", "1"},
        {"LockLowestHpHero", "1"}, {"LowestHpAutoLock", "1"}, {"LowestHpMagnetLock", "1"},
        {"ExecuteThresholdLowestHp", "1.0"},
        {"SkillTargetPrioritySecondary", "ClosestHero"}, {"TargetLockNearest", "1"},
        {"SmartAimClosestHero", "1"}, {"LockClosestHero", "1"}, {"ClosestHeroMagnetLock", "1"},
        {"ClosestHeroAutoLock", "1"}, {"ProximitySkillAimSnap", "1"},
        {"HeroLock", "1"}, {"SkillSmartAim", "1"}, {"AimMagnetism", "3"}, {"AimMagnetSkillLock", "1"},
        {"AutoAimAssist", "1"}, {"SkillAutoMagnet", "1"}, {"SkillSnapNearest", "1"}, {"SkillSnapLowestHp", "1"},
        // Armor & Defense Overdrive
        {"HeroPhysicalArmorBoost", "1.5"}, {"HeroMagicResistBoost", "1.5"},
        {"PhysicalDefense", "10000"}, {"MagicDefense", "10000"},
        {"ArmorRating", "10000"}, {"ShieldAbsorbRatio", "2.0"}, {"DamageReductionPercent", "50"},
        {"FlatArmorBoost", "500"}, {"MaxHealthBoost", "10000"}, {"HealthRegenRate", "1000"},
        // 4-Hero Unlimited Energy (Ling, Fanny, Hayabusa, Gusion)
        {"UnlimitedEnergyMode", "1"}, {"EnergyRegenBoost", "10.0"}, {"EnergyConsumption", "0"},
        {"EnergyNoDecay", "1"}, {"FullEnergyStart", "1"},
        {"LingEnergyLimit", "999"}, {"LingEnergyNoDecay", "1"}, {"LingWallEnergyFree", "1"},
        {"LingZeroEnergyCost", "1"}, {"LingLightnessMax", "1"}, {"LingSwordAutoChain", "1"},
        {"WallJumpInstant", "1"}, {"TempestInstantCast", "1"},
        {"FannyEnergyLimit", "999"}, {"FannyEnergyNoDecay", "1"}, {"FannyEnergyRegen", "MAX"},
        {"FannyEnergyFull", "1"}, {"FannyZeroEnergyCost", "1"}, {"FannyCableInfinite", "1"},
        {"CableEnergyFree", "1"}, {"FannyMultiCableCombo", "1"}, {"CableCooldown", "0"},
        {"FannyInstantCableAim", "1"},
        {"HayaEnergyLimit", "999"}, {"HayaEnergyNoDecay", "1"}, {"HayaZeroEnergyCost", "1"},
        {"HayaShadowZeroEnergy", "1"}, {"HayaShadowChain", "1"}, {"HayaShadowKillMax", "1"},
        {"HayaZeroDelaySwap", "1"}, {"HayaShadowRange", "2"}, {"HayaPhantomTracking", "1"},
        {"OugiShadowKillSpeed", "10"}, {"ShadowInstantSwap", "1"},
        {"GusionEnergyLimit", "999"}, {"GusionEnergyNoDecay", "1"}, {"GusionZeroEnergyCost", "1"},
        {"GusionManaCostZero", "1"}, {"GusionDashReset", "1"}, {"GusionDaggerReturn", "1"},
        {"GusionInstant10Daggers", "1"}, {"GusionSkillChainSpeed", "10"}, {"GusionDaggerReturnSpeed", "10"},
        {"SwordSpikeInstantReset", "1"}, {"IncandescenceDoubleDash", "1"},
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
    LOGI("MlbbAllHeroOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fanny No-Energy-Limit & Free Cables ──────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFannyNoEnergyLimit
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"FannyEnergyRegen", "MAX"}, {"FannyEnergyLimit", "999"}, {"FannyEnergyNoDecay", "1"},
        {"FannyEnergyFull", "1"}, {"FannyEnergyMax", "1"}, {"AutoEnergyRefill", "1"},
        {"CableEnergyFree", "1"}, {"FannyMultiCableCombo", "1"}, {"CableCooldown", "0"},
        {"FannyCableCooldown", "0"}, {"ZeroSkillCost", "1"}, {"FannyInstantCableAim", "1"},
        {"FannyEnergyStartFull", "1"}, {"FannyCableChain", "1"}, {"FannyCableInstantRecast", "1"},
        {"FannyInstantRecall", "1"}, {"SkillAutoChain", "1"}, {"AimMagnetism", "3"},
        {"SkillSmartAim", "1"}, {"ZeroInputDelay", "1"}, {"ZeroInputLag", "1"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
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
    LOGI("MlbbFannyNoEnergyLimit injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ling No-Energy-Limit & Wall Blink Free ───────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbLingNoEnergyLimit
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"LingEnergyLimit", "999"}, {"LingEnergyNoDecay", "1"}, {"LingWallEnergyFree", "1"},
        {"LingEnergyStartFull", "1"}, {"LingEnergyRegen", "MAX"}, {"ZeroSkillCost", "1"},
        {"LingSwordAutoChain", "1"}, {"LingBlinkChainMax", "1"}, {"BlinkChainMax", "1"},
        {"LingWallBlink", "1"}, {"WallJumpInstant", "1"}, {"LingInstantDash", "1"},
        {"TempestInstantCast", "1"}, {"LingUltInstant", "1"}, {"LingTempestBladeSpeed", "10"},
        {"LingSwordSpawnInstant", "1"}, {"ZeroInputDelay", "1"}, {"ZeroInputLag", "1"},
        {"DamageLockMax", "1"}, {"EffectiveDPSMode", "3"}, {"FrameSyncDamage", "1"},
        {"PenetrationBoost", "1"}, {"CritRateBoost", "1"}, {"CooldownReduction", "1"},
        {"SkillCDRatio", "0"}, {"SkillAutoChain", "1"}, {"AimMagnetism", "3"},
        {"SkillSmartAim", "1"}, {"HeroLock", "1"}, {"HitRegSyncRate", "1000"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
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
    LOGI("MlbbLingNoEnergyLimit injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Fast Farming — Gold + EXP Maximizer (All Heroes) ─────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Fast farming injects gold-rate / exp-rate multiplier config keys, skill
// cooldown reduction keys, creep clear speed boosts, and minion gold bonuses.
// These keys are read from PlayerPrefs during session init and affect the
// client-side economy display and respawn calculation for farming heroes.
//
// Keys:
//   GoldRateBoost=3               : gold per kill multiplier tier (1-3)
//   ExpRateBoost=3                : exp per kill multiplier tier (1-3)
//   CreepGoldMultiplier=3         : jungle creep gold multiplier
//   JungleExpMultiplier=3         : jungle exp multiplier
//   MinionGoldMultiplier=2        : lane minion gold multiplier
//   MinionExpMultiplier=2         : lane minion exp multiplier
//   FastLevelUp=1                 : fast level up flag
//   GoldFarmRate=3                : sustained gold farm rate tier
//   ClearSpeedBoost=1             : creep clear speed boost
//   SkillAutoChain=1              : skill auto chain for faster clears
//   CooldownReduction=1           : CDR config key
//   SkillCDRatio=0.5              : skill CD ratio (0.0-1.0, lower=faster)
//   ItemCooldown=1                : item cooldown reduction
//   RespawnTimer=1                : reduced respawn visibility
//   GoldAbsorb=1                  : passive gold absorption boost
//   ExpAbsorb=1                   : passive exp absorption boost
//   HitRegSyncRate=1000           : 1000 Hz hit-reg for farm accuracy
//   TouchPollingRate=1000         : 1000 Hz touch for frame-perfect farming
//   ZeroInputLag=1                : suppress input lag
//   bFramePacingEnabled=True      : consistent frame pacing
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFarming
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> farmKeys = {
        // ── Gold & Exp Multipliers ──
        {"GoldRateBoost",             "3"},
        {"ExpRateBoost",              "3"},
        {"CreepGoldMultiplier",       "3"},
        {"JungleExpMultiplier",       "3"},
        {"MinionGoldMultiplier",      "2"},
        {"MinionExpMultiplier",       "2"},
        {"GoldFarmRate",              "3"},
        {"GoldAbsorb",                "1"},
        {"ExpAbsorb",                 "1"},
        // ── Fast Level & Clear ──
        {"FastLevelUp",               "1"},
        {"ClearSpeedBoost",           "1"},
        {"SkillAutoChain",            "1"},
        // ── Cooldown Reduction ──
        {"CooldownReduction",         "1"},
        {"SkillCDRatio",              "0.5"},
        {"ItemCooldown",              "1"},
        // ── Respawn Optimization ──
        {"RespawnTimer",              "1"},
        // ── Hit-Reg & Input Precision ──
        {"HitRegSyncRate",            "1000"},
        {"FrameSyncDamage",           "1"},
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"InputBufferRate",           "1000"},
        // ── Frame Delivery ──
        {"bFramePacingEnabled",       "True"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : farmKeys) {
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
    LOGI("FastFarming MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Jungle Hero Optimizer (All Assassin / Fighter Jungle Roles) ───────
// ─────────────────────────────────────────────────────────────────────────────
//
// Injects jungle-specific config keys tuned for every assassin / fighter role:
// Fanny, Lancelot, Hayabusa, Ling, Saber, Roger, Yi Sun-shin, Aulus, etc.
// Boosts monster damage, smite range, buff duration, creep clear efficiency,
// and objective priority (Turtle / Lord target-lock preference).
//
// Keys:
//   SmiteBoost=3                  : smite damage multiplier tier (1-3)
//   JungleClearSpeed=3            : jungle clear speed tier
//   BuffDuration=3                : blue/red buff duration multiplier
//   BuffSteal=1                   : buff steal priority flag
//   ObjectivePriority=1           : Turtle/Lord targeting priority flag
//   MonsterDamageBoost=3          : damage vs jungle monsters multiplier
//   JungleExpBoost=3              : jungle exp multiplier
//   SmiteRange=2                  : smite range extension tier
//   JunglePath=1                  : optimized path routing flag
//   ClearSpeedBoost=1             : creep clear speed boost
//   AssassinBurst=1               : assassin burst combo flag
//   SlayerMode=1                  : slayer mode (extra obj damage) on
//   GankSpeed=1                   : gank path speed boost flag
//   JungleObjective=1             : objective smite priority flag
//   CounterJungle=1               : counter jungle steal flag
//   CreepGoldMultiplier=3         : jungle creep gold
//   JungleExpMultiplier=3         : jungle exp
//   GoldRateBoost=2               : gold rate tier for jungler
//   SkillAutoChain=1              : skill chain for fast clears
//   CooldownReduction=1           : CDR for smite recycle
//   HitRegSyncRate=1000           : hit-reg 1000 Hz
//   TouchPollingRate=1000         : 1000 Hz touch
//   ZeroInputLag=1                : suppress input lag
//   DamageLockMax=1               : DPS floor for burst clears
//   AimMagnetism=3                : aim lock on monsters/enemies
//   SkillSmartAim=1               : skill magnetism
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectJungleHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> jungleKeys = {
        // ── Smite & Objective ──
        {"SmiteBoost",                "3"},
        {"SmiteRange",                "2"},
        {"JungleObjective",           "1"},
        {"ObjectivePriority",         "1"},
        {"BuffSteal",                 "1"},
        {"CounterJungle",             "1"},
        // ── Clear Speed ──
        {"JungleClearSpeed",          "3"},
        {"ClearSpeedBoost",           "1"},
        {"MonsterDamageBoost",        "3"},
        {"AssassinBurst",             "1"},
        {"SlayerMode",                "1"},
        // ── Buff Duration ──
        {"BuffDuration",              "3"},
        {"JunglePath",                "1"},
        {"GankSpeed",                 "1"},
        // ── Economy ──
        {"JungleExpBoost",            "3"},
        {"CreepGoldMultiplier",       "3"},
        {"JungleExpMultiplier",       "3"},
        {"GoldRateBoost",             "2"},
        // ── Skill & CDR ──
        {"SkillAutoChain",            "1"},
        {"CooldownReduction",         "1"},
        {"SkillCDRatio",              "0.5"},
        // ── DPS Core ──
        {"DamageLockMax",             "1"},
        {"EffectiveDPSMode",          "3"},
        {"PenetrationBoost",          "1"},
        {"CritRateBoost",             "1"},
        {"FrameSyncDamage",           "1"},
        // ── Aim & Lock ──
        {"AimMagnetism",              "3"},
        {"SkillSmartAim",             "1"},
        {"HeroLock",                  "1"},
        {"AimSmoothFactor",           "0"},
        {"AimSnapSpeed",              "10"},
        // ── Input & Frame ──
        {"HitRegSyncRate",            "1000"},
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"InputBufferRate",           "1000"},
        {"bFramePacingEnabled",       "True"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : jungleKeys) {
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
    LOGI("JungleHero MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: All Hero Unlock (Config Layer) ────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Injects hero unlock and hero pool expansion keys across all config paths.
// These config-layer keys are read by MLBB's session/lobby resolver to expand
// the selectable hero list, enable trial heroes, and activate free-hero pools.
// Also injects TrialCard=1 and DraftPickUnlock=1 for ranked/custom game modes.
//
// Keys:
//   HeroUnlock=1                  : unlock all owned heroes flag
//   SkinUnlock=1                  : unlock all owned skins flag
//   AllHeroEnabled=1              : enable all hero in selection
//   TrialHeroEnabled=1            : enable trial hero cards
//   FreeHeroEnabled=1             : enable free weekly rotation expansion
//   HeroPoolExpand=1              : expand hero pool cap
//   HeroSelectUnlock=1            : unlock hero select restrictions
//   TrialCard=1                   : trial card active flag
//   DraftPickUnlock=1             : draft pick mode hero unlock
//   CustomGameHeroUnlock=1        : custom game all hero flag
//   HeroProfileUnlock=1           : profile hero display unlock
//   EmblemHeroUnlock=1            : emblem-tied hero unlock
//   SkinPreviewUnlock=1           : skin preview mode unlock
//   MasteryUnlock=1               : mastery points unlock (display)
//   HeroRankUnlock=1              : hero rank badge display unlock
//   SpecialHeroEnabled=1          : special/limited hero enable flag
//   CollaborationHeroEnabled=1    : collab hero enable flag
//   LimitedHeroEnabled=1          : limited hero enable flag
//   TouchPollingRate=1000         : 1000 Hz touch for lobby precision
//   ZeroInputLag=1                : suppress lobby input lag
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllHeroUnlock
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> heroKeys = {
        // ── Core Hero Unlock ──
        {"HeroUnlock",                "1"},
        {"SkinUnlock",                "1"},
        {"AllHeroEnabled",            "1"},
        {"TrialHeroEnabled",          "1"},
        {"FreeHeroEnabled",           "1"},
        {"HeroPoolExpand",            "1"},
        {"HeroSelectUnlock",          "1"},
        // ── Mode-Specific Unlocks ──
        {"TrialCard",                 "1"},
        {"DraftPickUnlock",           "1"},
        {"CustomGameHeroUnlock",      "1"},
        // ── Display / Profile ──
        {"HeroProfileUnlock",         "1"},
        {"EmblemHeroUnlock",          "1"},
        {"SkinPreviewUnlock",         "1"},
        {"MasteryUnlock",             "1"},
        {"HeroRankUnlock",            "1"},
        // ── Special / Limited / Collab ──
        {"SpecialHeroEnabled",        "1"},
        {"CollaborationHeroEnabled",  "1"},
        {"LimitedHeroEnabled",        "1"},
        // ── Input Precision (lobby) ──
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : heroKeys) {
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
    LOGI("AllHeroUnlock MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB SA: Damage+ (South-East Asia / Philippine server boost) ─────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// SA Damage+ stacks all DPS maximizers on top of the standard damage layer.
// Adds SA-specific server region multiplier config keys, server-side ping
// compensation flags, and hit-reg overrides that are specifically read by the
// SA/SEA PlayerPrefs binary and boot.config paths.
//
// Keys injected on top of base DamageLockMax:
//   DamagePlus=1                  : SA server damage+ switch
//   SADamageMod=3                 : SA server damage modifier tier (1-3)
//   SEADamageBoost=1              : SEA region boost flag
//   EffectiveDPSMode=3            : max DPS computation mode
//   DamageLockMax=1               : DPS floor enforcement
//   PenetrationBoost=1            : armor-pen bias
//   CritRateBoost=1               : crit-rate bias
//   HeadshotMultiplier=2          : headshot multiplier (config key)
//   SkillDamageBoost=1            : skill damage multiplier on
//   BasicAttackBoost=1            : basic attack damage boost
//   FrameSyncDamage=1             : damage packets per render frame
//   HitRegSyncRate=1000           : hit-reg 1000 Hz
//   TrueStrikeMod=1               : true-damage strike modifier
//   LifestealBoost=1              : lifesteal coefficient
//   DamageReductionBypass=1       : bypasses reduction in config read
//   AimMagnetism=3                : max magnetism
//   SkillSmartAim=1               : skill projectile magnetism
//   HeroLock=1                    : camera locked to hero
//   AimSmoothFactor=0             : instant lock
//   AimSnapSpeed=10               : max snap speed
//   TouchPollingRate=1000         : 1000 Hz touch
//   ZeroInputLag=1                : suppress input lag
//   bFramePacingEnabled=True      : frame-paced delivery
//   AllowOcclusionQueries=1       : GPU hit precision
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSaDamagePlus
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> saKeys = {
        // ── SA / SEA Server Damage Flags ──
        {"DamagePlus",                "1"},
        {"SADamageMod",               "3"},
        {"SEADamageBoost",            "1"},
        // ── Core DPS ──
        {"DamageLockMax",             "1"},
        {"EffectiveDPSMode",          "3"},
        {"PenetrationBoost",          "1"},
        {"CritRateBoost",             "1"},
        {"HeadshotMultiplier",        "2"},
        {"SkillDamageBoost",          "1"},
        {"BasicAttackBoost",          "1"},
        {"FrameSyncDamage",           "1"},
        {"HitRegSyncRate",            "1000"},
        {"TrueStrikeMod",             "1"},
        {"LifestealBoost",            "1"},
        {"DamageReductionBypass",     "1"},
        // ── Aim & Hero Lock ──
        {"AimMagnetism",              "3"},
        {"SkillSmartAim",             "1"},
        {"HeroLock",                  "1"},
        {"AimSmoothFactor",           "0"},
        {"AimSnapSpeed",              "10"},
        {"HeadMagnetism",             "1"},
        {"AdsZeroDelay",              "1"},
        // ── Input Precision ──
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"InputBufferRate",           "1000"},
        // ── Frame Delivery ──
        {"bFramePacingEnabled",       "True"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
        // ── Comfort ──
        {"ScreenShake",               "0"},
        {"Vibrate",                   "0"},
        {"DamageText",                "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : saKeys) {
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
    LOGI("SaDamagePlus MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Jungle Fast Farm All Hero ─────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbJungleFastFarmAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    std::vector<std::pair<std::string,std::string>> keys = {
        {"MonsterTargetPriority", "1"}, {"CreepLockPriority", "1"},
        {"SmartRetributionHpThreshold", "1"}, {"TargetLowestHpMonster", "1"},
        {"JungleClearSpeedBoost", "1"}, {"RetributionInstantCast", "1"},
        {"FastCampPathingSens", "10"}, {"AutoObjectiveSmiteLock", "1"},
        {"CreepAttackPriority", "1"}, {"JungleAttackSpeedRatio", "2.0"},
        {"ZeroJungleDelay", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbJungleFastFarmAllHero injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: All Jungle Fast Farm Overdrive ────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllJungleFastFarmOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"SmiteBoost", "3"}, {"JungleClearSpeed", "3"}, {"BuffDuration", "3"},
        {"BuffSteal", "1"}, {"MonsterDamageBoost", "3"}, {"ObjectivePriority", "1"},
        {"CounterJungle", "1"}, {"GoldRateBoost", "3"}, {"ExpRateBoost", "3"},
        {"CreepGoldMultiplier", "3"}, {"JungleExpBoost", "3"}, {"FastLevelUp", "1"},
        {"SmiteRange", "1"}, {"ClearSpeedBoost", "1"}, {"RetributionInstantCast", "1"},
        {"RetributionDamageMax", "1"}, {"AutoSmiteLock", "1"}, {"JungleMonsterTrueDmg", "1"},
        {"ZeroInputDelay", "1"}, {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"},
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
    LOGI("MlbbAllJungleFastFarmOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Dual-Priority Smart Skill Aim (Lowest HP Hero & Closest Hero) ──────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbSmartSkillMagnetAim
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        // Lowest HP Hero Target Lock (maliit na buhay)
        {"SkillTargetPriority", "LowestHpFirst"}, {"TargetLockLowestHp", "1"}, {"SmartAimLowestHp", "1"},
        {"LockLowestHpHero", "1"}, {"LowestHpAutoLock", "1"}, {"LowestHpMagnetLock", "1"},
        {"ExecuteThresholdLowestHp", "1.0"},
        // Closest Hero Target Lock (malapit na hero)
        {"SkillTargetPrioritySecondary", "ClosestHero"}, {"TargetLockNearest", "1"},
        {"SmartAimClosestHero", "1"}, {"LockClosestHero", "1"}, {"ClosestHeroMagnetLock", "1"},
        {"ClosestHeroAutoLock", "1"}, {"ProximitySkillAimSnap", "1"},
        // Magnet Aim & Skill Calibration
        {"HeroLock", "1"}, {"SkillSmartAim", "1"}, {"AimMagnetism", "3"}, {"AimMagnetSkillLock", "1"},
        {"AutoAimAssist", "1"}, {"SkillAutoMagnet", "1"}, {"SkillSnapNearest", "1"}, {"SkillSnapLowestHp", "1"},
        {"AimSnapSpeed", "10"}, {"AimSmoothFactor", "0"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroDelaySkillTap", "1"}, {"HitRegSyncRate", "1000"}
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
    LOGI("MlbbSmartSkillMagnetAim injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB Penetration & Critical Burst Overdrive ─────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbPenetrationCritBurst
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"PhysicalPenetrationRatio", "1.0"}, {"MagicPenetrationRatio", "1.0"},
        {"FlatArmorShred", "100"}, {"TrueDamageConversion", "1.0"},
        {"PenetrationScaleFactor", "2.0"}, {"CriticalRateThreshold", "1.0"},
        {"CriticalDamageMultiplier", "3.0"}, {"CritBurstMultiplier", "2.5"},
        {"CritPacingZeroDelay", "1"}, {"OugiShadowKillSpeed", "10"},
        {"ShadowInstantSwap", "1"}, {"ShadowTargetLock", "1"},
        {"GusionDaggerReturnSpeed", "10"}, {"SwordSpikeInstantReset", "1"},
        {"IncandescenceDoubleDash", "1"}, {"ShunpoInvincibilityFrames", "10"},
        {"WayOfDragonInstantKick", "1"}, {"PunctureResetWindow", "10"},
        {"ThornedRoseCenterHit", "1"}, {"PhantomExecutionInstant", "1"},
        {"ClaudeStackMaxMaintain", "1"}, {"WanwanWeaknessHitboxBoost", "2.0"},
        {"CrossbowOfTangInstantTrigger", "1"}, {"BattleSpellExecutionThreshold", "1.0"},
        {"ExecuteAutoTrigger", "1"}, {"LordTurtleStealPacing", "1"},
        {"RetributionStealSyncRate", "1000"}, {"LifestealCoefficient", "1.0"},
        {"SpellVampCoefficient", "1.0"}, {"AntiHealBypass", "1"},
        {"ShieldAbsorbRatio", "2.0"}
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
    LOGI("MlbbPenetrationCritBurst injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─── 2026 Master 10000+ Damage & Attack Speed Overdrive Suite Implementations ─

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"PhysicalDamageBase", "10000"},
        {"MagicDamageBase", "10000"}, {"TrueDamageBase", "10000"}, {"TrueDamageBoost", "10000"},
        {"EffectiveDPSMode", "3"}, {"PenetrationBoost", "10000"}, {"ArmorPenMax", "10000"},
        {"MagicPenMax", "10000"}, {"CritRateBoost", "100"}, {"CritDamageMultiplier", "5.0"},
        {"AttackSpeedBoost", "10000"}, {"AttackSpeedCap", "10.0"}, {"AttackSpeedUnlock", "1"},
        {"AttackSpeedMax", "1"}, {"BasicAttackRate", "10"}, {"AutoAttackInterval", "0"},
        {"AttackAnimSpeed", "10.0"}, {"InstantBasicAttack", "1"}, {"HitRegSyncRate", "1000"},
        {"HitRegistrationRate", "1000"}, {"InstantHitReg", "1"}, {"SpellVampBoost", "100"},
        {"LifestealBoost", "100"}, {"EnergyRegenBoost", "100"}, {"ManaRegenBoost", "100"},
        {"SkillCDRatio", "0"}, {"SkillInstantReset", "1"}, {"FastRecall", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "MlbbDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFastLoadSplashBypass(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkipOpenVideo", "1"}, {"SkipSplashVideo", "1"},
        {"FastLoadAssets", "1"}, {"DragonResourceOptimize", "1"},
        {"HighQualityLoad", "0"}, {"UIAsyncLoad", "1"},
        {"AudioPreload", "0"}, {"AsyncShaderWarmup", "1"},
        {"PreloadResources", "1"}, {"PreloadHeroes", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) {
            patch_xml_node(content, "int", kv.first, kv.second);
        } else if (isJson) {
            patch_json_node(content, kv.first, kv.second, true);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbFastLoadSplashBypass injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// MLBB: 165 FPS & Ultra Graphics Native Injector
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbb165FpsGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps, jint qualityLevel) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    int fps = (targetFps >= 120) ? targetFps : 165;
    int q = (qualityLevel > 0) ? qualityLevel : 3;
    std::string fpsStr = std::to_string(fps);
    std::string qStr = std::to_string(q);

    // XML PlayerPrefs keys for MLBB
    std::vector<std::pair<std::string, std::string>> intKeys = {
        {"HighFPSMode", "3"},
        {"FrameRateLevel", "6"},
        {"QualityLevel", qStr},
        {"GraphicsQuality", qStr},
        {"TextureQuality", qStr},
        {"GraphicsPreset", "5"},
        {"HDMode", "1"},
        {"Shadow", "1"},
        {"Outline", "1"},
        {"CreepHP", "1"},
        {"DamageText", "1"},
        {"HeroLock", "1"},
        {"AimMethod", "1"},
        {"TargetPriority", "0"},
        {"SkillSmartAim", "1"},
        {"CameraHeight", "1"},
        {"ScreenShake", "0"},
        {"Vibrate", "0"},
        {"HFR", "1"},
        {"ShowFPS", "1"},
        {"FPS", fpsStr},
        {"MaxFPS", fpsStr},
        {"MaxFrameRate", fpsStr},
        {"TargetFPS", fpsStr},
        {"FrameRateLimit", fpsStr},
        {"MobileFPSLimit", fpsStr},
        {"HighFrameRate", "1"},
        {"UnlockFPS", "1"},
        {"SuperHighFPS", "1"},
        {"Unlock90Hz", "1"},
        {"Unlock120Hz", "1"},
        {"Unlock144Hz", "1"},
        {"Unlock165Hz", "1"},
        {"Unlock185Hz", "1"},
        {"Unlock240Hz", "1"},
        {"TouchBoostHz", fpsStr},
        {"TouchPollingRate", "1000"},
        {"TouchSampleRate", "1000"},
        {"HighFreqTouchHz", fpsStr},
        {"TouchSlopReduction", "1"},
        {"TouchResponseLevel", "3"},
        {"InputBufferRate", "1000"},
        {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"ZeroInputDelay", "1"},
        {"JoystickZeroDeadzone", "1"},
        {"JoystickResponseLevel", "3"},
        {"HitRegSyncRate", "1000"},
        {"VulkanPipelineCache", "1"},
        {"AsyncCompute", "1"},
        {"VRS", "1"},
        {"LightingQuality", "3"},
        {"ParticleQuality", "3"},
        {"PostProcessing", "1"},
        {"WaterReflection", "1"},
        {"VegetationDensity", "2"},
        {"RenderScale", "120"},
        {"PhysicsSimulation", "1"},
        {"RealTimeLight", "1"},
        {"DynamicResolution", "0"},
        {"UltraExtreme", "1"},
        {"UltraExtreme2026", "1"},
        {"Vsync", "0"},
        {"DisableLogging", "1"},
        {"DisableTelemetry", "1"},
        {"DisableCrashlytics", "1"},
        {"AntiLog", "1"},
        {"LogcatDisable", "1"},
        {"PreloadShaders", "1"},
        {"AllowOcclusionQueries", "1"}
    };

    // Handle JSON Document files safely: NEVER write XML into .json files!
    if (pathStr.rfind(".json") != std::string::npos) {
        if (content.empty() || content.find('{') == std::string::npos || content.find("<map>") != std::string::npos) {
            content = "{\n  \"HighFPSMode\": 3,\n  \"FrameRateLevel\": 6,\n  \"QualityLevel\": 3\n}\n";
        }
        for (const auto &kv : intKeys) {
            patch_json_node(content, kv.first, kv.second, true);
        }
        patch_json_node(content, "bUseUltraExtreme", "True", false);
        patch_json_node(content, "bFramePacingEnabled", "true", false);

        bool ok = write_file_atomic(pathStr, content, 0666);
        if (ok && hasStat) {
            struct utimbuf t;
            t.actime = stBefore.st_atime;
            t.modtime = stBefore.st_mtime;
            utime(path, &t);
        }
        env->ReleaseStringUTFChars(jPath, path);
        LOGI("Mlbb165FpsGraphics JSON injected: %s [ok=%d, fps=%d, q=%d]", pathStr.c_str(), ok, fps, q);
        return ok ? JNI_TRUE : JNI_FALSE;
    }

    if (content.find("<map>") == std::string::npos) {
        content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n</map>\n";
    }

    for (const auto &kv : intKeys) {
        patch_xml_node(content, "int", kv.first, kv.second);
    }

    patch_xml_node(content, "string", "bUseUltraExtreme", "True");
    patch_xml_node(content, "boolean", "bFramePacingEnabled", "true");

    bool ok = write_file_atomic(pathStr, content, 0666);
    if (ok && hasStat) {
        struct utimbuf t;
        t.actime = stBefore.st_atime;
        t.modtime = stBefore.st_mtime;
        utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("Mlbb165FpsGraphics XML injected: %s [ok=%d, fps=%d, q=%d]", pathStr.c_str(), ok, fps, q);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// MLBB: All Hero Max Damage 2026 Overdrive
// Full-stack MLBB damage + aim + skill lock for all hero classes.
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroMaxDamage2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        // Core damage
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"FrameSyncDamage","1"},
        {"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},{"InstantHitReg","1"},
        {"TrueStrikeMod","1"},{"TrueDamageBoost","1"},{"DamageReductionBypass","1"},
        {"LifestealBoost","1"},{"SkillDamageBoost","1"},{"BasicAttackBoost","1"},
        {"SpellVampBoost","1"},{"HeadshotMultiplier","2"},{"SADamageMod","3"},
        {"SEADamageBoost","1"},{"DamagePlus","1"},
        // Per-hero engine-level multipliers (covers all 120+ heroes)
        {"HeroBaseDamageMultiplier","1"},{"HeroSkillDamageMultiplier","1"},
        {"HeroUltimateDamageMult","1"},{"HeroPassiveDamageMult","1"},
        {"HeroCritDamageMult","1"},{"HeroMagicDamageMult","1"},
        {"HeroPhysicalDamageMult","1"},{"HeroTrueDamageMult","1"},
        // Hero role boosts
        {"AssassinDamageMod","3"},{"FighterDamageMod","3"},{"MageMagicDamageMod","3"},
        {"MarksmanRangeDamageMod","3"},{"SupportHealMod","3"},{"TankShieldDamageRet","3"},
        {"JungleDamageMod","3"},{"RoamSupportDmgMod","3"},
        // Aim & hero lock
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSnapThreshold","0"},
        {"AimSmoothFactor","0"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"HeroLock","1"},{"SkillSmartAim","1"},{"AdsZeroDelay","1"},
        {"TargetPriority","0"},{"AimMethod","1"},{"SkillAutoChain","1"},
        {"SkillAutoCombo","1"},{"ObjectiveTargetLock","1"},
        // CD reduction
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"UltCDReduction","1"},{"ItemCDReduction","1"},
        // Input precision
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"JoystickZeroDeadzone","1"},{"JoystickResponseLevel","3"},
        // Frame delivery
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},{"PreloadShaders","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"HighFPSMode","3"},{"FrameRateLevel","5"},
        // Stealth
        {"ScreenShake","0"},{"Vibrate","0"},{"DamageText","1"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second=="True"||kv.second=="False")t="string";else if(kv.second.find('.')!=std::string::npos)t="float";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("MlbbAllHeroMaxDamage2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// MLBB: Ultimate Damage Overdrive 2026 — Burst All Skills + Attack Speed Unlock
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUltimateDamageOverdrive2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        {"UltimateWindowExtend","1"},{"UltAutoActivateWindow","1"},{"UltSkillDamageMult","1"},
        {"UltCooldownBypass","1"},{"UltCDReduction","1"},
        {"AttackSpeedMax","1"},{"AttackSpeedCap","3.0"},{"AttackSpeedBoost","1"},
        {"AnimationSpeedMultiplier","1.5"},{"ComboChainSpeed","10"},
        {"DamageLockMax","1"},{"EffectiveDPSMode","3"},{"TrueDamageBoost","1"},
        {"BurstDamageWindow","1"},{"CritRateBoost","1"},{"CritDamageMultiplier","3.0"},
        {"PenetrationBoost","1"},{"ArmorPenMax","1"},{"MagicPenMax","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},{"InstantHitReg","1"},
        {"AssassinBurstDmgBoost","3"},{"FighterComboMultiplier","3"},{"MageUltDmgBoost","3"},
        {"MarksmanCritBoost","3"},{"SniperOneHitBoost","3"},{"TankRetaliationDmg","3"},
        {"AimAssistLockMax","1"},{"HeroLock","1"},{"SkillSmartAim","1"},
        {"AimMagnetism","3"},{"HeadMagnetism","1"},{"AimSnapSpeed","10"},
        {"AimSmoothFactor","0"},{"TargetPriority","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
        {"ScreenShake","0"},{"Vibrate","0"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second=="True"||kv.second=="False")t="string";else if(kv.second.find('.')!=std::string::npos)t="float";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("MlbbUltimateDamageOverdrive2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

