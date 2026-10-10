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
  damageMultiplier:     parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.dmg', '1.0')),
  attackSpeedMult:      parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.aspd', '1.0')),
  defenseMultiplier:    parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.def', '1.0')),
  cooldownReductionPct: parseInt(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.cdr', '0')),
  manaBoost:            parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.mana', '1.0')),
  energyBoost:          parseFloat(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.energy', '1.0')),
  droneTier:            parseInt(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.drone', '0')),
  noSkillCooldown:      Java.use('android.os.SystemProperties').get('gamebooster.mlbb.nocool', '0') === '1',
  mapHack:              Java.use('android.os.SystemProperties').get('gamebooster.mlbb.map', '0') === '1',
  fpsUnlock:            parseInt(Java.use('android.os.SystemProperties').get('gamebooster.mlbb.fps', '60')),
  antiBan:              Java.use('android.os.SystemProperties').get('gamebooster.mlbb.antiban', '1') === '1',
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
        console.log('[AntiBan] SSL pin bypassed for: ' + hostname);
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

  // RVAs from mlbb_il2cpp_offsets.json (v2.2.16.12322)
  const RVAS = {
    damageCalc:   0x0182C4D0,
    attackSpeed:  0x01831E20,
    defenseArmor: 0x01859E40,
    skillCooldown:0x0184A100,
    fowVisible:   0x019056B0,
    cameraHeight: 0x019128A0,
    manaEnergy:   0x01882350
  };

  // Helper to safely hook RVA with fallback pattern scan
  function safeHook(name, rva, patterns, onLeaveHandler) {
    let hooked = false;
    if (rva > 0 && rva < mod.size) {
      try {
        const target = mod.base.add(rva);
        Interceptor.attach(target, { onLeave: onLeaveHandler });
        console.log('[GameBoosterPRO] ' + name + ' hooked @ RVA 0x' + rva.toString(16));
        hooked = true;
      } catch (e) {
        console.log('[GameBoosterPRO] ' + name + ' RVA hook failed: ' + e);
      }
    }
    if (!hooked && patterns && patterns.length > 0) {
      for (const pattern of patterns) {
        try {
          const matches = Memory.scanSync(mod.base, mod.size, pattern);
          if (matches.length > 0) {
            Interceptor.attach(matches[0].address, { onLeave: onLeaveHandler });
            console.log('[GameBoosterPRO] ' + name + ' hooked via signature @ ' + matches[0].address);
            hooked = true;
            break;
          }
        } catch (e) {}
      }
    }
  }

  // 1. Damage Multiplier (10% to 1000% / 10x)
  if (CFG.damageMultiplier !== 1.0) {
    const dmgPatterns = [
      '4F 00 00 54 ?? ?? ?? ?? ?? ?? ?? ?? 00 00 80 52',
      '3F 00 00 71 ?? 00 00 54 ?? ?? ?? 1E'
    ];
    safeHook('DamageCalc', RVAS.damageCalc, dmgPatterns, function(retval) {
      const orig = retval.readFloat ? retval.readFloat() : retval.toInt32();
      const boosted = orig * CFG.damageMultiplier;
      retval.replace(ptr(Math.round(boosted)));
    });
  }

  // 2. Attack Speed Multiplier (0.5x to 5x)
  if (CFG.attackSpeedMult !== 1.0) {
    const aspdPatterns = ['E0 03 00 AA ?? ?? ?? ?? 00 00 80 3F'];
    safeHook('AttackSpeed', RVAS.attackSpeed, aspdPatterns, function(retval) {
      const v = retval.readFloat ? retval.readFloat() : retval.toInt32();
      retval.replace(ptr(Math.round(v * CFG.attackSpeedMult)));
    });
  }

  // 3. Extra Defense Multiplier (0.5x to 5x)
  if (CFG.defenseMultiplier !== 1.0) {
    const defPatterns = ['F4 4F 3E A9 FD 7B 01 A9 FD 43 00 91 E0 03 00 91'];
    safeHook('HeroDefense', RVAS.defenseArmor, defPatterns, function(retval) {
      const orig = retval.readFloat ? retval.readFloat() : retval.toInt32();
      const boosted = orig * CFG.defenseMultiplier;
      retval.replace(ptr(Math.round(boosted)));
    });
  }

  // 4. Cooldown Reduction (0% to 100% / No Cooldown)
  if (CFG.noSkillCooldown || CFG.cooldownReductionPct > 0) {
    const cdrRatio = CFG.noSkillCooldown ? 0.0 : Math.max(0.0, (100 - CFG.cooldownReductionPct) / 100.0);
    const cdrPatterns = [
      'C0 03 5F D6 ?? ?? ?? ?? 00 00 00 00 00 00 F0 3F',
      '00 00 80 52 00 00 00 1E'
    ];
    safeHook('SkillCooldown', RVAS.skillCooldown, cdrPatterns, function(retval) {
      if (cdrRatio === 0.0) {
        retval.replace(ptr(0));
      } else {
        const orig = retval.readFloat ? retval.readFloat() : retval.toInt32();
        retval.replace(ptr(Math.round(orig * cdrRatio)));
      }
    });
  }

  // 5. Extra / Infinite Mana and Energy
  if (CFG.manaBoost > 1.0 || CFG.energyBoost > 1.0) {
    const maxBoost = Math.max(CFG.manaBoost, CFG.energyBoost);
    safeHook('ManaEnergyRegen', RVAS.manaEnergy, [], function(retval) {
      const orig = retval.readFloat ? retval.readFloat() : retval.toInt32();
      const boosted = maxBoost >= 9999 ? 99999 : (orig * maxBoost);
      retval.replace(ptr(Math.round(boosted)));
    });
  }

  // 6. Drone View / Free Camera Zoom (Tier 1 to 5X and 10X)
  if (CFG.droneTier > 0) {
    let camHeight = 80;
    if (CFG.droneTier >= 100) camHeight = 500; // 10X
    else if (CFG.droneTier >= 50) camHeight = 320; // 5X
    else if (CFG.droneTier >= 40) camHeight = 240; // 4X
    else if (CFG.droneTier >= 30) camHeight = 180; // 3X
    else if (CFG.droneTier >= 20) camHeight = 120; // 2X
    else camHeight = 90; // 1.5X

    const camPatterns = ['F4 4F 01 A9 FD 7B 02 A9 FD 03 00 91 ?? ?? ?? 1E'];
    safeHook('CameraManager_SetHeight', RVAS.cameraHeight, camPatterns, function(retval) {
      retval.replace(ptr(camHeight));
    });
  }

  // 7. Map Hack / Vision (Fog of War)
  if (CFG.mapHack) {
    const fogPatterns = ['20 00 80 52 C0 03 5F D6'];
    safeHook('FogOfWar', RVAS.fowVisible, fogPatterns, function(retval) {
      retval.replace(ptr(1));
    });
  }
});

// ─── FPS Unlock (120 / 144 / 165 FPS) ────────────────────────────────────────
if (CFG.fpsUnlock > 60) {
  // Unity target frame rate override
  waitForModule('libunity.so', (mod) => {
    try {
      const fpsExports = [
        '_ZN5Unity12Application16SetTargetFrameRateEi',
        'Application_set_targetFrameRate',
        '_ZN12UnityPlayer18setTargetFrameRateEi'
      ];
      for (const exp of fpsExports) {
        const addr = Module.findExportByName('libunity.so', exp);
        if (addr) {
          Interceptor.attach(addr, {
            onEnter(args) { args[0] = ptr(CFG.fpsUnlock); }
          });
          console.log('[GameBoosterPRO] MLBB FPS unlocked to ' + CFG.fpsUnlock + ' via ' + exp);
          break;
        }
      }

      // Force QualitySettings.vSyncCount = 0
      const vsyncExports = [
        '_ZN5Unity15QualitySettings12SetVSyncCountEi',
        'QualitySettings_set_vSyncCount'
      ];
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

  // IL2CPP internal setter hook
  waitForModule('libil2cpp.so', (mod) => {
    try {
      const il2cppFpsExports = [
        'UnityEngine_Application_set_targetFrameRate',
        '_ZN11UnityEngine11Application18set_targetFrameRateEi'
      ];
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
