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

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeEvaluateMovingAimAssistVector
  (JNIEnv* env, jclass, jfloat originX, jfloat originY, jfloat originZ,
   jfloat fwdX, jfloat fwdY, jfloat fwdZ,
   jfloatArray enemyPositions, jfloatArray enemyVelocities, jint enemyCount,
   jfloat maxRange, jfloat coneAngleDegrees, jfloat bulletSpeed, jfloat latencySeconds, jfloat strengthMultiplier,
   jfloatArray outResult) {
    if (!enemyPositions || enemyCount <= 0 || !outResult) return JNI_FALSE;

    jsize posLen = env->GetArrayLength(enemyPositions);
    if (posLen < enemyCount * 3) return JNI_FALSE;

    jfloat* posElem = env->GetFloatArrayElements(enemyPositions, nullptr);
    if (!posElem) return JNI_FALSE;

    jfloat* velElem = (enemyVelocities && env->GetArrayLength(enemyVelocities) >= enemyCount * 3)
                      ? env->GetFloatArrayElements(enemyVelocities, nullptr) : nullptr;

    combat::Vector3 origin{originX, originY, originZ};
    combat::Vector3 forward{fwdX, fwdY, fwdZ};
    forward = forward.Normalized();

    std::vector<combat::TargetEntity> enemies;
    enemies.reserve(enemyCount);

    for (int i = 0; i < enemyCount; ++i) {
        combat::TargetEntity ent;
        ent.id = static_cast<uint32_t>(i);
        ent.position = {posElem[i * 3 + 0], posElem[i * 3 + 1], posElem[i * 3 + 2]};
        if (velElem) {
            ent.velocity = {velElem[i * 3 + 0], velElem[i * 3 + 1], velElem[i * 3 + 2]};
        }
        ent.is_alive = true;
        enemies.push_back(ent);
    }

    env->ReleaseFloatArrayElements(enemyPositions, posElem, JNI_ABORT);
    if (velElem) {
        env->ReleaseFloatArrayElements(enemyVelocities, velElem, JNI_ABORT);
    }

    combat::AimAssistResult res = combat::EvaluateMovingAimAssist(
        origin, forward, enemies, maxRange, coneAngleDegrees, bulletSpeed, latencySeconds, strengthMultiplier
    );

    if (res.is_locked) {
        jfloat buffer[7] = {
            static_cast<jfloat>(res.target_id),
            res.predicted_pos.x, res.predicted_pos.y, res.predicted_pos.z,
            res.delta_pitch, res.delta_yaw,
            res.magnetic_pull
        };
        env->SetFloatArrayRegion(outResult, 0, 7, buffer);
        return JNI_TRUE;
    }

    return JNI_FALSE;
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
