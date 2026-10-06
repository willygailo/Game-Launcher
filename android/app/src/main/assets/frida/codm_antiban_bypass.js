/**
 * CODM Deep Anti-Ban & Anticheat Cloak Script (Frida)
 * Targets:
 *  - Tencent ACE (libanort.so, libanogs.so)
 *  - CrashSight (libCrashSight.so, libCrashSightPlugin.so)
 *  - Security Anti-Fraud (libsaf.so)
 *  - SSL / Telemetry Pinning
 */

'use strict';

console.log('[AntiBan-CODM] Initializing Anticheat Suppression Suite...');

// ─── 1. SSL Unpinning & Telemetry Host Redirection ───────────────────────────
Java.perform(() => {
    try {
        const CertificatePinner = Java.use('okhttp3.CertificatePinner');
        CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(host, certs) {
            // Nullify SSL check
            return;
        };
        console.log('[AntiBan-CODM] OkHttp3 CertificatePinner unpinned');
    } catch(e) {}

    try {
        const TrustManagerImpl = Java.use('com.android.org.conscrypt.TrustManagerImpl');
        TrustManagerImpl.verifyChain.implementation = function(chain, authType, host, clientAuth, ocsp, tls) {
            return chain;
        };
        console.log('[AntiBan-CODM] Conscrypt TrustManager unpinned');
    } catch(e) {}
});

// Helper: Module watcher
function onModuleLoaded(moduleName, callback) {
    const mod = Process.findModuleByName(moduleName);
    if (mod) {
        callback(mod);
    } else {
        const timer = setInterval(() => {
            const m = Process.findModuleByName(moduleName);
            if (m) {
                clearInterval(timer);
                callback(m);
            }
        }, 300);
    }
}

// ─── 2. Tencent ACE (libanort.so & libanogs.so) Watchdog Suppression ─────────
onModuleLoaded('libanort.so', (mod) => {
    console.log('[AntiBan-CODM] libanort.so detected @ ' + mod.base);

    // Enumerate exports to hook detection and telemetry dispatch
    mod.enumerateExports().forEach(exp => {
        const name = exp.name.toLowerCase();
        if (name.includes('report') || name.includes('check') || name.includes('detect') || name.includes('heartbeat')) {
            try {
                Interceptor.replace(exp.address, new NativeCallback(() => {
                    return 0; // return SUCCESS or NO-OP
                }, 'int', []));
                console.log('[AntiBan-CODM] Hooked libanort export: ' + exp.name);
            } catch(e) {}
        }
    });
});

onModuleLoaded('libanogs.so', (mod) => {
    console.log('[AntiBan-CODM] libanogs.so detected @ ' + mod.base);

    mod.enumerateExports().forEach(exp => {
        const name = exp.name.toLowerCase();
        if (name.includes('tick') || name.includes('scan') || name.includes('verify')) {
            try {
                Interceptor.replace(exp.address, new NativeCallback(() => {
                    return 0;
                }, 'int', []));
                console.log('[AntiBan-CODM] Hooked libanogs export: ' + exp.name);
            } catch(e) {}
        }
    });
});

// ─── 3. CrashSight Suppression ───────────────────────────────────────────────
onModuleLoaded('libCrashSight.so', (mod) => {
    console.log('[AntiBan-CODM] libCrashSight.so detected @ ' + mod.base);

    mod.enumerateExports().forEach(exp => {
        const name = exp.name.toLowerCase();
        if (name.includes('crash') || name.includes('report') || name.includes('upload')) {
            try {
                Interceptor.replace(exp.address, new NativeCallback(() => {
                    return 0;
                }, 'int', []));
            } catch(e) {}
        }
    });
});

// ─── 4. Memory Ptrace / Debugger Trap Shield ────────────────────────────────
Interceptor.attach(Module.findExportByName(null, 'ptrace'), {
    onEnter(args) {
        const request = args[0].toInt32();
        // PTRACE_TRACEME = 0
        if (request === 0) {
            console.log('[AntiBan-CODM] Suppressed PTRACE_TRACEME anti-debug check');
            this.fake = true;
        }
    },
    onLeave(retval) {
        if (this.fake) {
            retval.replace(ptr(0));
        }
    }
});
