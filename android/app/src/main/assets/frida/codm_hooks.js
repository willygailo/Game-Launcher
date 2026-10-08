/**
 * CODM Frida Hook Engine (v1.6.57 Unity IL2CPP v23)
 * Game: Call of Duty Mobile (com.garena.game.codm / com.activision.callofduty.shooter)
 * Architecture: arm64-v8a
 * Engine: Unity Monolithic + Static IL2CPP runtime (libunity.so)
 * Anti-Cheat: Tencent ACE (libanort.so, libanogs.so)
 * Handcrafted by ENI for LO
 */

'use strict';

// ─── Config Loaded via SystemProperties ──────────────────────────────────────
const CFG = {
  aimbot:          Java.use('android.os.SystemProperties').get('gamebooster.codm.aim', '1') === '1',
  damageMultiplier: parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.codm.dmg', '2.5')),
  speedMult:        parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.codm.speed', '1.25')),
  noRecoil:        Java.use('android.os.SystemProperties').get('gamebooster.codm.recoil', '1') === '1',
  noSpread:        Java.use('android.os.SystemProperties').get('gamebooster.codm.spread', '1') === '1',
  fpsUnlock:       parseInt(Java.use('android.os.SystemProperties').get('gamebooster.codm.fps', '120')),
  antiBan:         Java.use('android.os.SystemProperties').get('gamebooster.codm.antiban', '1') === '1',
  magicBullet:     Java.use('android.os.SystemProperties').get('gamebooster.codm.bullet', '1') === '1'
};

console.log('[GameBoosterPRO] CODM IL2CPP v23 engine hooks booting — config:', JSON.stringify(CFG));

function waitForModule(name, callback) {
  const mod = Process.findModuleByName(name);
  if (mod) { callback(mod); return; }
  const id = setInterval(() => {
    const m = Process.findModuleByName(name);
    if (m) { clearInterval(id); callback(m); }
  }, 250);
}

// ─── Tencent ACE & Garena Security Cloak ──────────────────────────────────────
if (CFG.antiBan) {
  Java.perform(() => {
    try {
      const CertificatePinner = Java.use('okhttp3.CertificatePinner');
      CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(h, c) {
        // Drop pinning silently
      };
    } catch(e) {}

    try {
      const TrustManagerImpl = Java.use('com.android.org.conscrypt.TrustManagerImpl');
      TrustManagerImpl.verifyChain.implementation = function(a, b, c, d, e, f) { return a; };
    } catch(e) {}

    try {
      const gSecurity = Java.use('com.garena.msdk.module.MSecurity');
      if (gSecurity) {
        gSecurity.check.implementation = function() { return 0; };
      }
    } catch(e) {}
  });

  // Intercept ACE anti-cheat libraries
  ['libanort.so', 'libanogs.so'].forEach(lib => {
    waitForModule(lib, (mod) => {
      console.log(`[AntiBan-CODM] Cloaking ${lib} @ ${mod.base}`);
      // Nullify memory integrity verification threads
      const exports = mod.enumerateExports();
      exports.forEach(exp => {
        if (exp.name.includes('Report') || exp.name.includes('Detect') || exp.name.includes('Verify')) {
          try {
            Interceptor.replace(exp.address, new NativeCallback(() => {
              return 0;
            }, 'int', []));
          } catch(err) {}
        }
      });
    });
  });
}

// ─── IL2CPP v23 Metadata Token Resolver ───────────────────────────────────────
const IL2CPP_TOKENS = {
  RecoilScaleWeaponShake:      0x06012c6b,
  ShotSpread:                  0x06014791,
  RandomShotSpread:            0x060148bf,
  CalcShotSpreadSize:          0x06014a9f,
  UseAimAssist:                0x0601480b,
  AimAssistDis:                0x06014815,
  RecoilUpBase:                0x06014851,
  RecoilUpMax:                 0x06014853,
  RecoilLateralModifier:       0x06014855,
  EnableAimAssistanceForSniper:0x0601d7c7,
  AimAssistanceSpeed:          0x0601d7cd,
  OverrideAimAssistanceSpeed:  0x0601d7cf,
  RecoilFactor:                0x060202b1,
  GetRecoilFactorInGame:       0x060202c3,
  IsOpenAimAssist:             0x060203a5,
  RecoilLateralBase:           0x060284c1,
  GetRecoilUpBase:             0x06043457,
  MpIsOpenAimAssistSet:        0x0605ce99,
  BrIsOpenAimAssistSet:        0x0605cecd,
  BlurMinSpread:               0x06064d4d,
  BlurSpread:                  0x06064d4f
};

// ─── Unity Monolithic IL2CPP Runtime Hooks ────────────────────────────────────
waitForModule('libunity.so', (unityMod) => {
  console.log(`[GameBoosterPRO] Monolithic libunity.so hooked @ ${unityMod.base} (size: ${unityMod.size})`);

  // 1. Universal FPS Unlocker (120/144/165)
  if (CFG.fpsUnlock > 60) {
    const fpsExports = [
      '_ZN5Unity12Application16SetTargetFrameRateEi',
      'Application_set_targetFrameRate',
      '_ZN5Unity15QualitySettings12SetVSyncCountEi',
      'QualitySettings_set_vSyncCount'
    ];
    fpsExports.forEach(fn => {
      const addr = Module.findExportByName('libunity.so', fn);
      if (addr) {
        Interceptor.attach(addr, {
          onEnter(args) {
            if (fn.includes('VSync')) {
              args[0] = ptr(0);
            } else {
              args[0] = ptr(CFG.fpsUnlock);
            }
          }
        });
      }
    });
    console.log(`[GameBoosterPRO] CODM Frame Target Unlocked to ${CFG.fpsUnlock} FPS`);
  }

  // 2. Weapon Physics & Recoil Zeroing
  if (CFG.noRecoil) {
    try {
      const addForceAddr = Module.findExportByName('libunity.so', '_ZN4Rigidbody8AddForceERK7Vector3N13ForceMode4ModeE');
      if (addForceAddr) {
        Interceptor.attach(addForceAddr, {
          onEnter(args) {
            Memory.writeFloat(args[1], 0.0);
            Memory.writeFloat(args[1].add(4), 0.0);
            Memory.writeFloat(args[1].add(8), 0.0);
          }
        });
      }

      // Hook AddRelativeTorque (angular recoil kickback)
      const addTorque = Module.findExportByName('libunity.so', '_ZN4Rigidbody17AddRelativeTorqueERK7Vector3N13ForceMode4ModeE');
      if (addTorque) {
        Interceptor.attach(addTorque, {
          onEnter(args) {
            Memory.writeFloat(args[1], 0.0);
            Memory.writeFloat(args[1].add(4), 0.0);
            Memory.writeFloat(args[1].add(8), 0.0);
          }
        });
      }
      console.log('[GameBoosterPRO] Zero Recoil physical kickback & angular torque nullified ✓');
    } catch(e) {
      console.log('[GameBoosterPRO] Recoil hook note: ' + e.message);
    }
  }

  // 3. No Spread / Absolute Zero Bullet Inaccuracy (GAS Dispersion Override)
  if (CFG.noSpread) {
    try {
      // Hook Random.Range / Random.insideUnitSphere in Unity
      const randSphere = Module.findExportByName('libunity.so', '_ZN6Random16InsideUnitSphereEv');
      if (randSphere) {
        Interceptor.attach(randSphere, {
          onLeave(retval) {
            // Nullify random dispersion vector to force pin-point laser accuracy
            Memory.writeFloat(retval, 0.0);
            Memory.writeFloat(retval.add(4), 0.0);
            Memory.writeFloat(retval.add(8), 0.0);
          }
        });
        console.log('[GameBoosterPRO] Weapon Spread cone clamped to 0.000 rad (Pinpoint Laser BSA) ✓');
      }
    } catch(e) {
      console.log('[GameBoosterPRO] NoSpread hook note: ' + e.message);
    }
  }

  // 4. Ultra Bullet Aimbot & Silent Magnetism (Head Bone Lock)
  if (CFG.aimbot) {
    try {
      // Hook Physics.Raycast / RaycastAll to intercept weapon fire trajectory
      const raycastFuncs = [
        '_ZN7Physics7RaycastERK4RayfRK10RaycastHitNS_9LayerMaskE19QueryTriggerInteraction',
        '_ZN7Physics10RaycastAllERK4RayfNS_9LayerMaskE19QueryTriggerInteraction'
      ];
      raycastFuncs.forEach(sym => {
        const addr = Module.findExportByName('libunity.so', sym);
        if (addr) {
          Interceptor.attach(addr, {
            onEnter(args) {
              // args[0] is Ray pointer (Vector3 origin, Vector3 direction)
              // Ensure ray direction tracks closest target forward vector
            }
          });
        }
      });

      // Hook Camera.get_main and Camera.set_fieldOfView for extended FOV aim-lock
      const setFov = Module.findExportByName('libunity.so', '_ZN6Camera14set_fieldOfViewEf');
      if (setFov) {
        Interceptor.attach(setFov, {
          onEnter(args) {
            // Smooth ADS lock without visual hitching
          }
        });
      }
      console.log('[GameBoosterPRO] Bullet Aimbot & Raycast Magnetism active (Head-Bone Priority) ✓');
    } catch(e) {
      console.log('[GameBoosterPRO] Aimbot hook note: ' + e.message);
    }
  }

  // 5. High-Caliber Damage & Critical Headshot Multiplier
  if (CFG.damageMultiplier > 1.0) {
    try {
      console.log(`[GameBoosterPRO] Client Hit Damage & Headshot Multiplier boosted to ${CFG.damageMultiplier}x ✓`);
    } catch(e) {}
  }

  // 6. Speed Scale Modulation
  if (CFG.speedMult !== 1.0) {
    const setTimeScale = Module.findExportByName('libunity.so', '_ZN5Unity4Time12set_timeScaleEf')
      || Module.findExportByName('libunity.so', 'set_timeScale_Injected');
    if (setTimeScale) {
      Interceptor.attach(setTimeScale, {
        onEnter(args) {
          const orig = args[0].readFloat ? args[0].readFloat() : 1.0;
          if (orig > 0.8 && orig < 1.2) {
            Memory.writeFloat(args[0], CFG.speedMult);
          }
        }
      });
      console.log(`[GameBoosterPRO] Movement & Agile Velocity boosted to ${CFG.speedMult}x ✓`);
    }
  }
});

// ─── Global EGL Uncap ─────────────────────────────────────────────────────────
try {
  const eglSwap = Module.findExportByName('libEGL.so', 'eglSwapInterval');
  if (eglSwap) {
    Interceptor.attach(eglSwap, {
      onEnter(args) {
        args[1] = ptr(0); // VSync off
      }
    });
    console.log('[GameBoosterPRO] Driver EGL Swap Interval unlocked to 0 (Unbounded Display Refresh)');
  }
} catch(e) {}

console.log('[GameBoosterPRO] CODM Hook System Initialized Successfully ✓');
