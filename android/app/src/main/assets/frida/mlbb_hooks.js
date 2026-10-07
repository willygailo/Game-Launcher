/**
 * MLBB Frida Hook Scripts
 * Game: Mobile Legends Bang Bang (com.mobile.legends)
 * Engine: Unity + IL2CPP
 * Target lib: libil2cpp.so, libmain.so
 * Version: 2.2.16.12322
 */

'use strict';

// ─── Config (set from Game Launcher PRO) ──────────────────────────────────────
const CFG = {
  damageMultiplier:  parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.dmg', '1.0')),
  attackSpeedMult:   parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.aspd', '1.0')),
  noSkillCooldown:   Java.use('android.os.SystemProperties').get('gamebooster.mlbb.nocool', '0') === '1',
  mapHack:           Java.use('android.os.SystemProperties').get('gamebooster.mlbb.map', '0') === '1',
  fpsUnlock:         parseInt(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.fps', '60')),
  antiBan:           Java.use('android.os.SystemProperties').get('gamebooster.mlbb.antiban', '1') === '1',
};

console.log('[GameBoosterPRO] MLBB hooks loading — config:', JSON.stringify(CFG));

// ─── Wait for libil2cpp.so (Avoid loader stub trampoline < 500KB) ────────────
function waitForModule(name, callback) {
  const checkValid = (m) => {
    if (!m) return false;
    // The initial MobaGameSO2010050 stub is ~384KB; wait for full decrypted mapping
    if (name === 'libil2cpp.so' && m.size < 500000) return false;
    return true;
  };

  const mod = Process.findModuleByName(name);
  if (checkValid(mod)) {
    callback(mod);
  } else {
    const id = setInterval(() => {
      const m = Process.findModuleByName(name);
      if (checkValid(m)) {
        clearInterval(id);
        callback(m);
      }
    }, 500);
  }
}


// ─── SSL Unpin (Anti-Ban Layer) ────────────────────────────────────────────────
if (CFG.antiBan) {
  Java.perform(() => {
    try {
      // Bypass OkHttp CertificatePinner
      const CertificatePinner = Java.use('okhttp3.CertificatePinner');
      CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(hostname, certs) {
        console.log('[AntiBAn] SSL pin bypassed for: ' + hostname);
        return;
      };
    } catch (e) { console.log('[AntiBan] OkHttp bypass skipped: ' + e.message); }

    try {
      // Bypass TrustManager
      const TrustManagerImpl = Java.use('com.android.org.conscrypt.TrustManagerImpl');
      TrustManagerImpl.verifyChain.implementation = function(untrustedChain, trustAnchorChain, host, clientAuth, ocspData, tlsSctData) {
        return untrustedChain;
      };
    } catch (e) { console.log('[AntiBan] TrustManager bypass skipped: ' + e.message); }
  });
  console.log('[GameBoosterPRO] Anti-ban SSL unpin active');
}

// ─── IL2CPP Native Hooks ───────────────────────────────────────────────────────
waitForModule('libil2cpp.so', (mod) => {
  console.log('[GameBoosterPRO] libil2cpp.so found @ ' + mod.base + ', size: ' + mod.size);

  // ── Damage Multiplier ─────────────────────────────────────────────────────
  if (CFG.damageMultiplier !== 1.0) {
    // Pattern scan for damage calculation function in IL2CPP
    // These offsets are derived from IL2CPP metadata analysis of v2.2.16.12322
    // Re-derive with il2cppdumper after each game update
    const dmgPatterns = [
      // Pattern: FMUL instruction cluster near "HeroDamageCalc" class
      '4F 00 00 54 ?? ?? ?? ?? ?? ?? ?? ?? 00 00 80 52',  // primary
      '3F 00 00 71 ?? 00 00 54 ?? ?? ?? 1E',              // fallback
    ];

    let dmgFound = false;
    for (const pattern of dmgPatterns) {
      try {
        const matches = Memory.scanSync(mod.base, mod.size, pattern);
        if (matches.length > 0) {
          const target = matches[0].address;
          Interceptor.attach(target, {
            onLeave(retval) {
              // retval is float damage value
              const orig = retval.readFloat ? retval.readFloat() : retval.toInt32();
              const boosted = orig * CFG.damageMultiplier;
              retval.replace(ptr(Math.round(boosted)));
              // console.log('[DMG] ' + orig + ' → ' + boosted); // verbose
            }
          });
          console.log('[GameBoosterPRO] Damage x' + CFG.damageMultiplier + ' hooked @ ' + target);
          dmgFound = true;
          break;
        }
      } catch(e) {}
    }
    if (!dmgFound) console.log('[GameBoosterPRO] Damage hook: offset not found — re-derive after update');
  }

  // ── No Skill Cooldown ─────────────────────────────────────────────────────
  if (CFG.noSkillCooldown) {
    try {
      // Hook float return that represents remaining cooldown time
      // Target: SkillSystem$$GetSkillCooldownRemaining or similar
      const cooldownPatterns = [
        'C0 03 5F D6 ?? ?? ?? ?? 00 00 00 00 00 00 F0 3F', // returns 0.0 double
      ];
      for (const pattern of cooldownPatterns) {
        const matches = Memory.scanSync(mod.base, mod.size, pattern);
        if (matches.length > 0) {
          Interceptor.attach(matches[0].address, {
            onLeave(retval) { retval.replace(ptr(0)); }
          });
          console.log('[GameBoosterPRO] No-cooldown hooked @ ' + matches[0].address);
          break;
        }
      }
    } catch(e) { console.log('[GameBoosterPRO] Cooldown hook error: ' + e); }
  }

  // ── Attack Speed ──────────────────────────────────────────────────────────
  if (CFG.attackSpeedMult !== 1.0) {
    try {
      // AttackSpeed property getter — typically returns float in [0.5, 3.0]
      const aspd = Memory.scanSync(mod.base, mod.size,
        'E0 03 00 AA ?? ?? ?? ?? 00 00 80 3F');  // FMOV s0, #1.0 pattern
      if (aspd.length > 0) {
        Interceptor.attach(aspd[0].address, {
          onLeave(retval) {
            const v = retval.readFloat ? retval.readFloat() : retval.toInt32();
            retval.replace(ptr(Math.round(v * CFG.attackSpeedMult)));
          }
        });
        console.log('[GameBoosterPRO] Attack speed x' + CFG.attackSpeedMult + ' hooked');
      }
    } catch(e) {}
  }

  // ── Map Hack / Vision ─────────────────────────────────────────────────────
  if (CFG.mapHack) {
    try {
      // Hook FogOfWar visibility check — return true (visible) for all units
      const fogPatterns = [
        '20 00 80 52 C0 03 5F D6',  // MOV W0, #1; RET
      ];
      for (const p of fogPatterns) {
        const m = Memory.scanSync(mod.base, mod.size, p);
        if (m.length > 0) {
          Interceptor.attach(m[0].address, {
            onLeave(retval) { retval.replace(ptr(1)); }
          });
          console.log('[GameBoosterPRO] Map hack (fog bypass) active @ ' + m[0].address);
          break;
        }
      }
    } catch(e) { console.log('[GameBoosterPRO] Map hack error: ' + e); }
  }
});

// ─── FPS Unlock (120 / 144 / 165 FPS) ────────────────────────────────────────
// Note: eglSwapInterval(0) is intentionally omitted to avoid Mali/Adreno driver swapchain black screen
if (CFG.fpsUnlock > 60) {

  // 2. Unity target frame rate override
  waitForModule('libunity.so', (mod) => {
    try {
      const fpsExports = ['_ZN5Unity12Application16SetTargetFrameRateEi',
                          'Application_set_targetFrameRate',
                          '_ZN12UnityPlayer18setTargetFrameRateEi'];
      for (const exp of fpsExports) {
        const addr = Module.findExportByName('libunity.so', exp);
        if (addr) {
          Interceptor.attach(addr, {
            onEnter(args) {
              args[0] = ptr(CFG.fpsUnlock);
            }
          });
          console.log('[GameBoosterPRO] MLBB FPS unlocked to ' + CFG.fpsUnlock + ' via ' + exp);
          break;
        }
      }

      // Force QualitySettings.vSyncCount = 0
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
    } catch(e) { console.log('[GameBoosterPRO] MLBB FPS hook error: ' + e); }
  });

  // 3. IL2CPP internal setter hook
  waitForModule('libil2cpp.so', (mod) => {
    try {
      const il2cppFpsExports = ['UnityEngine_Application_set_targetFrameRate',
                                '_ZN11UnityEngine11Application18set_targetFrameRateEi'];
      for (const exp of il2cppFpsExports) {
        const addr = Module.findExportByName('libil2cpp.so', exp);
        if (addr) {
          Interceptor.attach(addr, {
            onEnter(args) { args[0] = ptr(CFG.fpsUnlock); }
          });
          console.log('[GameBoosterPRO] MLBB IL2CPP targetFrameRate hooked @ ' + addr);
          break;
        }
      }
    } catch(e) {}
  });
}

console.log('[GameBoosterPRO] MLBB hooks installed ✓');
