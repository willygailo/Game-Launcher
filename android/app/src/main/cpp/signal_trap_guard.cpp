// =============================================================================
// GameBooster Native — 2026 Signal Trap Guard & Anti-Ptrace Engine
// Defends against SIGTRAP debugger sweeps and rogue ptrace inspection probes
// =============================================================================

#include <jni.h>
#include <signal.h>
#include <sys/ptrace.h>
#include <unistd.h>
#include <android/log.h>
#include <atomic>

#define TAG "SignalTrapGuard"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

static struct sigaction sOldSigTrap;
static struct sigaction sOldSigBus;
static std::atomic<bool> sIsArmed(false);

static void sigtrap_handler(int sig, siginfo_t *info, void *ucontext) {
    // Intercept SIGTRAP without crashing or notifying external debuggers
    LOGW("SIGTRAP intercepted from code=%d, addr=%p — neutralizing debugger probe",
         info ? info->si_code : -1, info ? info->si_addr : nullptr);
}

extern "C" {

/**
 * Arms signal trap guard and claims ptrace trace slot.
 */
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeArmSignalTrapGuard(
        JNIEnv *env, jclass clazz) {
    if (sIsArmed.load()) {
        return JNI_TRUE;
    }

    struct sigaction sa;
    sa.sa_sigaction = sigtrap_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;

    if (sigaction(SIGTRAP, &sa, &sOldSigTrap) != 0) {
        LOGW("Failed to install SIGTRAP handler");
    }

    // Try claiming PTRACE_TRACEME so non-root AC inspectors cannot attach
    long ptraceRes = ptrace(PTRACE_TRACEME, 0, 1, 0);
    LOGI("SignalTrapGuard armed successfully (ptrace_traceme: %ld)", ptraceRes);

    sIsArmed.store(true);
    return JNI_TRUE;
}

/**
 * Disarms signal trap guard and restores previous signal handlers.
 */
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeDisarmSignalTrapGuard(
        JNIEnv *env, jclass clazz) {
    if (!sIsArmed.load()) {
        return JNI_TRUE;
    }

    sigaction(SIGTRAP, &sOldSigTrap, nullptr);
    sIsArmed.store(false);
    LOGI("SignalTrapGuard disarmed");
    return JNI_TRUE;
}

} // extern "C"
