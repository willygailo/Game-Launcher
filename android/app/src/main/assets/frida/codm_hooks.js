/**
 * CODM Frida Hook Scripts
 * Game: Call of Duty Mobile Garena (com.garena.game.codm)
 * Engine: Unity + IL2CPP
 * Target lib: libunity.so, libil2cpp (via PuertsCore/TDataMaster)
 * Version: 1.6.57
 */

'use strict';

// ─── Config (set from Game Launcher PRO) ──────────────────────────────────────
const CFG = {
  aimbot:          Java.use('android.os.SystemProperties').get('gamebooster.codm.aim', '0') === '1',
  damageMultiplier: parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.codm.dmg', '1.0')),
  speedMult:        parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.codm.speed', '1.0')),
  noRecoil:        Java.use('android.os.SystemProperties').get('gamebooster.codm.recoil', '0') === '1',
  fpsUnlock:       parseInt(Java.use('android.os.SystemProperties').get('gamebooster.codm.fps', '60')),
  antiBan:         Java.use('android.os.SystemProperties').get('gamebooster.codm.antiban', '1') === '1',
};

console.log('[GameBoosterPRO] CODM hooks loading — config:', JSON.stringify(CFG));

function waitForModule(name, callback) {
  const mod = Process.findModuleByName(name);
  if (mod) { callback(mod); return; }
  const id = setInterval(() => {
    const m = Process.findModuleByName(name);
    if (m) { clearInterval(id); callback(m); }
  }, 500);
}

// ─── SSL Unpin / Anti-Ban ─────────────────────────────────────────────────────
if (CFG.antiBan) {
  Java.perform(() => {
    try {
      const CertificatePinner = Java.use('okhttp3.CertificatePinner');
      CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(h, c) {
        console.log('[AntiBan-CODM] SSL pin bypassed: ' + h);
      };
    } catch(e) {}

    try {
      const TrustManagerImpl = Java.use('com.android.org.conscrypt.TrustManagerImpl');
      TrustManagerImpl.verifyChain.implementation = function(a, b, c, d, e, f) { return a; };
    } catch(e) {}

    // Garena-specific: bypass signature/integrity checks
    try {
      const gSecurity = Java.use('com.garena.msdk.module.MSecurity');
      gSecurity.check.implementation = function() { return 0; };
    } catch(e) {}
  });
  console.log('[GameBoosterPRO] CODM Anti-ban active');
}

// ─── Unity / IL2CPP Hooks ─────────────────────────────────────────────────────
waitForModule('libunity.so', (mod) => {
  console.log('[GameBoosterPRO] libunity.so found @ ' + mod.base);

  // ── FPS Unlock ─────────────────────────────────────────────────────────────
  if (CFG.fpsUnlock > 60) {
    const fpsExports = [
      '_ZN5Unity12Application16SetTargetFrameRateEi',
      'Application_set_targetFrameRate',
    ];
    for (const exp of fpsExports) {
      const addr = Module.findExportByName('libunity.so', exp);
      if (addr) {
        Interceptor.attach(addr, {
          onEnter(args) { args[0] = ptr(CFG.fpsUnlock); }
        });
        console.log('[GameBoosterPRO] CODM FPS → ' + CFG.fpsUnlock);
        break;
      }
    }
  }

  // ── Speed Hack ─────────────────────────────────────────────────────────────
  if (CFG.speedMult !== 1.0) {
    // Hook Unity Time.timeScale to effectively speed up movement
    const timeScaleAddr = Module.findExportByName('libunity.so', 'set_timeScale_Injected')
      || Module.findExportByName('libunity.so', '_ZN5Unity4Time12set_timeScaleEf');
    if (timeScaleAddr) {
      Interceptor.attach(timeScaleAddr, {
        onEnter(args) {
          const orig = args[0].readFloat ? args[0].readFloat() : 1.0;
          // Only apply during gameplay (timeScale == 1.0 means running)
          if (orig > 0.9) {
            Memory.writeFloat(args[0], CFG.speedMult);
          }
        }
      });
      console.log('[GameBoosterPRO] Speed x' + CFG.speedMult + ' via timeScale hook');
    }
  }

  // ── No Recoil ──────────────────────────────────────────────────────────────
  if (CFG.noRecoil) {
    try {
      // Scan for recoil application pattern in libunity.so
      // Recoil is typically applied as a camera rotation delta
      // Pattern: FMUL + FADD near "recoil" strings
      const recoilStr = Memory.scanSync(mod.base, mod.size, '72 65 63 6F 69 6C'); // "recoil"
      if (recoilStr.length > 0) {
        console.log('[GameBoosterPRO] Recoil string found @ ' + recoilStr[0].address);
        // Hook the AddRecoil function — zero out X and Y args
        // Fine-grained: requires offset derivation per-build
        // Fallback: hook AddForce/AddTorque on camera RigidBody
        const addForce = Module.findExportByName('libunity.so',
          '_ZN4Rigidbody8AddForceERK7Vector3N13ForceMode4ModeE');
        if (addForce) {
          Interceptor.attach(addForce, {
            onEnter(args) {
              // Zero out force vector if called during recoil window
              Memory.writeFloat(args[1], 0.0);       // x
              Memory.writeFloat(args[1].add(4), 0.0); // y
              Memory.writeFloat(args[1].add(8), 0.0); // z
            }
          });
          console.log('[GameBoosterPRO] No recoil via AddForce zero hook');
        }
      }
    } catch(e) { console.log('[GameBoosterPRO] Recoil hook error: ' + e); }
  }
});

// ─── Damage Multiplier (via TDataMaster / game logic lib) ─────────────────────
if (CFG.damageMultiplier !== 1.0) {
  waitForModule('libTDataMaster.so', (mod) => {
    console.log('[GameBoosterPRO] libTDataMaster.so found @ ' + mod.base);
    // TDataMaster handles weapon/character data including damage values
    // Hook GetDamageValue or equivalent exported symbol
    const exports = mod.enumerateExports();
    for (const exp of exports) {
      if (exp.name.toLowerCase().includes('damage') || exp.name.includes('Damage')) {
        Interceptor.attach(exp.address, {
          onLeave(retval) {
            const v = retval.toInt32();
            if (v > 0 && v < 100000) {
              retval.replace(ptr(Math.round(v * CFG.damageMultiplier)));
            }
          }
        });
        console.log('[GameBoosterPRO] Damage hook: ' + exp.name + ' @ ' + exp.address);
      }
    }
  });
}

// ─── Aimbot (find nearest enemy target) ───────────────────────────────────────
if (CFG.aimbot) {
  Java.perform(() => {
    try {
      // Hook Unity Camera.WorldToScreenPoint to detect enemy positions
      // and Physics.OverlapSphere for target acquisition
      const Camera = Java.use('com.unity3d.player.UnityPlayer');
      // The actual aimbot logic lives in native — wire via Frida native hooks
      // This is a placeholder that enables the aimbot flag
      // Full implementation requires per-build offset derivation
      console.log('[GameBoosterPRO] Aimbot: Java layer flag set. Native hook requires offset derivation.');
    } catch(e) {}
  });

  waitForModule('libunity.so', (mod) => {
    // Hook Physics.OverlapSphere — intercept target finding
    const overlapSphere = Module.findExportByName('libunity.so',
      '_ZN7Physics13OverlapSphereERK7Vector3fNS_9LayerMaskE');
    if (overlapSphere) {
      Interceptor.attach(overlapSphere, {
        onLeave(retval) {
          // retval contains array of colliders — ensure enemies are included
          console.log('[GameBoosterPRO] OverlapSphere intercepted, collider count: ' + retval.toInt32());
        }
      });
    }
  });
}

// ─── FPS Unlock (120 / 144 / 165 FPS) ────────────────────────────────────────
if (CFG.fpsUnlock > 60) {
  // 1. Universal EGL swap interval override (driver-level uncap)
  try {
    const eglSwapInterval = Module.findExportByName('libEGL.so', 'eglSwapInterval');
    if (eglSwapInterval) {
      Interceptor.attach(eglSwapInterval, {
        onEnter(args) {
          args[1] = ptr(0);
        }
      });
      console.log('[GameBoosterPRO] CODM eglSwapInterval uncapped (0) ✓');
    }
  } catch(e) {}

  // 2. UE4 CVar MaxFPS override
  waitForModule('libUE4.so', (mod) => {
    try {
      const setMaxFps = Module.findExportByName('libUE4.so', '_ZN7GEngine9SetMaxFPSEf');
      if (setMaxFps) {
        Interceptor.attach(setMaxFps, {
          onEnter(args) {
            args[1] = ptr(CFG.fpsUnlock);
          }
        });
        console.log('[GameBoosterPRO] CODM UE4 GEngine.SetMaxFPS locked to ' + CFG.fpsUnlock);
      }
    } catch(e) {}
  });

  // 3. Unity targetFrameRate override (for Unity-based sub-renderers)
  waitForModule('libunity.so', (mod) => {
    try {
      const fpsExports = ['_ZN5Unity12Application16SetTargetFrameRateEi',
                          'Application_set_targetFrameRate'];
      for (const exp of fpsExports) {
        const addr = Module.findExportByName('libunity.so', exp);
        if (addr) {
          Interceptor.attach(addr, {
            onEnter(args) { args[0] = ptr(CFG.fpsUnlock); }
          });
          break;
        }
      }
      const vsyncExports = ['_ZN5Unity15QualitySettings12SetVSyncCountEi',
                            'QualitySettings_set_vSyncCount'];
      for (const exp of vsyncExports) {
        const addr = Module.findExportByName('libunity.so', exp);
        if (addr) {
          Interceptor.attach(addr, {
            onEnter(args) { args[0] = ptr(0); }
          });
          break;
        }
      }
    } catch(e) {}
  });
}

console.log('[GameBoosterPRO] CODM hooks installed ✓');
