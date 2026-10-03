// language: C++, file: combat_system_jni.cpp, runtime: C++17/20, target: Android JNI (GameBooster Native)
#include "combat_system.hpp"
#include <jni.h>
#include <vector>
#include <memory>
#include <mutex>

namespace {
    combat::CooldownManager g_cooldown_mgr;
    std::mutex g_cooldown_mutex;
}

extern "C" {

JNIEXPORT jfloat JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeCalculateCombatDamage
  (JNIEnv*, jclass, jfloat rawDamage, jint damageType, jfloat armor, jboolean isCrit, jfloat critMult, jboolean dodgeRoll) {
    combat::DamagePacket packet;
    packet.raw_amount = rawDamage;
    packet.type = static_cast<combat::DamageType>(damageType);
    packet.is_crit = (isCrit == JNI_TRUE);
    packet.crit_multiplier = critMult > 0.0f ? critMult : 1.5f;

    combat::Stat defense(armor);
    combat::DamageResult result = combat::CalculateDamage(packet, defense, dodgeRoll == JNI_TRUE);
    return static_cast<jfloat>(result.final_damage);
}

JNIEXPORT jint JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeEvaluateAimAssistTarget
  (JNIEnv* env, jclass, jfloat originX, jfloat originY, jfloat originZ,
   jfloat fwdX, jfloat fwdY, jfloat fwdZ,
   jfloatArray enemyPositions, jint enemyCount, jfloat maxRange, jfloat coneAngleDegrees) {
    if (!enemyPositions || enemyCount <= 0) {
        return -1;
    }

    jsize len = env->GetArrayLength(enemyPositions);
    if (len < enemyCount * 3) {
        return -1;
    }

    jfloat* elements = env->GetFloatArrayElements(enemyPositions, nullptr);
    if (!elements) {
        return -1;
    }

    combat::Vector3 origin{originX, originY, originZ};
    combat::Vector3 forward{fwdX, fwdY, fwdZ};
    forward = forward.Normalized();

    std::vector<combat::TargetEntity> enemies;
    enemies.reserve(enemyCount);

    for (int i = 0; i < enemyCount; ++i) {
        combat::TargetEntity ent;
        ent.id = static_cast<uint32_t>(i);
        ent.position = {elements[i * 3 + 0], elements[i * 3 + 1], elements[i * 3 + 2]};
        ent.is_alive = true;
        enemies.push_back(ent);
    }

    env->ReleaseFloatArrayElements(enemyPositions, elements, JNI_ABORT);

    const combat::TargetEntity* best = combat::GetBestTarget(origin, forward, enemies, maxRange, coneAngleDegrees);
    return best ? static_cast<jint>(best->id) : -1;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeCombatCooldownTrigger
  (JNIEnv*, jclass, jint actionId, jfloat durationSeconds) {
    std::lock_guard<std::mutex> lock(g_cooldown_mutex);
    g_cooldown_mgr.Trigger(static_cast<uint32_t>(actionId), durationSeconds);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeCombatCooldownIsReady
  (JNIEnv*, jclass, jint actionId) {
    std::lock_guard<std::mutex> lock(g_cooldown_mutex);
    return g_cooldown_mgr.IsReady(static_cast<uint32_t>(actionId)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jfloat JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeCombatCooldownRemaining
  (JNIEnv*, jclass, jint actionId) {
    std::lock_guard<std::mutex> lock(g_cooldown_mutex);
    return static_cast<jfloat>(g_cooldown_mgr.GetRemaining(static_cast<uint32_t>(actionId)));
}

} // extern "C"
