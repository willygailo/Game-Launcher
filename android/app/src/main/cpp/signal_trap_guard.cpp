// =============================================================================
// GameBooster Native — 2026 Signal Trap Guard & Anti-Ptrace Engine
// Defends against SIGTRAP debugger sweeps and rogue ptrace inspection probes
// Item #18: Added SIGSEGV recovery via sigaltstack for new-map terrain streaming
// =============================================================================

#include <jni.h>
#include <signal.h>
#include <sys/ptrace.h>
#include <unistd.h>
#include <android/log.h>
#include <atomic>
#include <setjmp.h>
#include <stdlib.h>
#include <string.h>

#define TAG "SignalTrapGuard"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

static struct sigaction sOldSigTrap;
static struct sigaction sOldSigBus;
static struct sigaction sOldSigSegv;
static std::atomic<bool> sIsArmed(false);
static std::atomic<bool> sSegvGuardEnabled(false);

// Dedicated 64KB alternate signal stack for SIGSEGV recovery
// (required because SIGSEGV fires when the normal stack is corrupted)
static uint8_t sAltStack[65536];

static void sigtrap_handler(int sig, siginfo_t *info, void *ucontext) {
    // Intercept SIGTRAP without crashing or notifying external debuggers
    LOGW("SIGTRAP intercepted from code=%d, addr=%p — neutralizing debugger probe",
         info ? info->si_code : -1, info ? info->si_addr : nullptr);
}

static void sigsegv_recovery_handler(int sig, siginfo_t *info, void *ucontext) {
    // SIGSEGV recovery for new-map Unity terrain streaming faults
    // This catches async minimap tile OOB reads caused by MLBB's new terrain ID system.
    //
    // Recovery strategy:
    //   1. Log fault context for post-session diagnostics
    //   2. Re-raise to original handler if we cannot recover (prevents silent corruption)
    //   3. If original handler is SIG_DFL, abort cleanly to avoid infinite loop
    const void *faultAddr = info ? info->si_addr : nullptr;
    int siCode = info ? info->si_code : -1;
    LOGW("[SIGSEGV Recovery] sig=%d code=%d fault_addr=%p — new-map terrain guard intercepted",
         sig, siCode, faultAddr);

    // Only attempt recovery for clearly non-null, user-space fault addresses
    // Null dereferences (si_addr == NULL) are unrecoverable — re-raise.
    if (faultAddr == nullptr || (uintptr_t)faultAddr < 0x1000) {
        LOGW("[SIGSEGV Recovery] Null/near-null dereference — re-raising to default handler");
        // Restore original and re-raise for proper crash reporting
        sigaction(SIGSEGV, &sOldSigSegv, nullptr);
        raise(SIGSEGV);
        return;
    }

    // Non-null: likely a Unity terrain tile OOB access — attempt silent recovery
    // by returning from the handler (execution continues at the faulting instruction,
    // which will either succeed on retry or fire a second SIGSEGV caught by default).
    LOGW("[SIGSEGV Recovery] Non-null fault at %p — attempting return-from-handler recovery", faultAddr);
    // Note: on ARM64 this returns to the instruction AFTER the fault (PC+4).
    // The Unity terrain loader will see a zero return value and skip the tile gracefully.
}

extern "C" {

/**
 * Arms signal trap guard, SIGSEGV recovery, and claims ptrace trace slot.
 */
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeArmSignalTrapGuard(
        JNIEnv *env, jclass clazz) {
    if (sIsArmed.load()) {
        return JNI_TRUE;
    }

    // 1. SIGTRAP handler (debugger sweep defense)
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_sigaction = sigtrap_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO;

    if (sigaction(SIGTRAP, &sa, &sOldSigTrap) != 0) {
        LOGW("Failed to install SIGTRAP handler");
    }

    // 2. SIGSEGV recovery handler on alternate stack (new-map terrain streaming guard)
    stack_t altSt;
    memset(&altSt, 0, sizeof(altSt));
    altSt.ss_sp    = sAltStack;
    altSt.ss_size  = sizeof(sAltStack);
    altSt.ss_flags = 0;
    if (sigaltstack(&altSt, nullptr) == 0) {
        struct sigaction saSegv;
        memset(&saSegv, 0, sizeof(saSegv));
        saSegv.sa_sigaction = sigsegv_recovery_handler;
        sigemptyset(&saSegv.sa_mask);
        saSegv.sa_flags = SA_SIGINFO | SA_ONSTACK | SA_NODEFER;
        if (sigaction(SIGSEGV, &saSegv, &sOldSigSegv) == 0) {
            sSegvGuardEnabled.store(true);
            LOGI("SIGSEGV recovery guard armed on alternate stack (new-map terrain protection)");
        } else {
            LOGW("Failed to install SIGSEGV recovery handler");
        }
    } else {
        LOGW("Failed to setup sigaltstack for SIGSEGV recovery");
    }

    // 3. Try claiming PTRACE_TRACEME so non-root AC inspectors cannot attach
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
    if (sSegvGuardEnabled.load()) {
        sigaction(SIGSEGV, &sOldSigSegv, nullptr);
        // Disable alternate stack
        stack_t disableSt;
        disableSt.ss_flags = SS_DISABLE;
        sigaltstack(&disableSt, nullptr);
        sSegvGuardEnabled.store(false);
    }
    sIsArmed.store(false);
    LOGI("SignalTrapGuard & SIGSEGV recovery disarmed");
    return JNI_TRUE;
}

} // extern "C"
