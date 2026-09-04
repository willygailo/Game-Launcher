package com.gamebooster.app.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NativeConfigInjectorTest {

    private File mTempDir;

    @Before
    public void setUp() throws IOException {
        mTempDir = Files.createTempDirectory("native_injector_test").toFile();
    }

    @After
    public void tearDown() {
        if (mTempDir != null && mTempDir.exists()) {
            File[] files = mTempDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            mTempDir.delete();
        }
    }

    @Test
    public void testSafeNullAndInvalidInputs() {
        assertNull(NativeConfigInjector.patchContentNativeInMemory(null, null, null, 0));
        assertNull(NativeConfigInjector.patchContentNativeInMemory("test", null, null, 0));
        assertNull(NativeConfigInjector.patchContentNativeInMemory("test", new String[]{"k"}, new String[]{"v1", "v2"}, 0));

        assertFalse(NativeConfigInjector.injectAimAssist(null));
        assertFalse(NativeConfigInjector.injectHighDamage(null));
        assertFalse(NativeConfigInjector.injectNoRecoil(null));
        assertFalse(NativeConfigInjector.injectHitRegDpsBoost(null));
        assertFalse(NativeConfigInjector.injectScopeAimCalibration(null));
        assertFalse(NativeConfigInjector.injectHardwareMaskProfile(null, "Adreno", "Snapdragon", 12, 120));
        assertFalse(NativeConfigInjector.injectDamageLockMax(null));
        assertFalse(NativeConfigInjector.injectAimAssistLockMax(null));
        assertFalse(NativeConfigInjector.injectLingHeroDamageCombo(null));
        assertFalse(NativeConfigInjector.injectMagicBulletAimbot(null));
        assertFalse(NativeConfigInjector.injectNoRecoilNoSpread(null));

        assertFalse(NativeConfigInjector.setProcessCpuAffinity(-1, 0));
        assertFalse(NativeConfigInjector.setProcessCpuAffinity(0, 0));
        assertFalse(NativeConfigInjector.setRealtimeThreadScheduling(-1, 50));
        assertFalse(NativeConfigInjector.setProcessIoPriority(-1, 1, 0));
        assertFalse(NativeConfigInjector.boostProcessResources(-1, 120));
    }

    @Test
    public void testKernelResourceOptimizationFallbacks() {
        // PID > 0 should execute elevated commands and return true without throwing
        assertTrue(NativeConfigInjector.setProcessCpuAffinity(99999, 0xF0));
        assertTrue(NativeConfigInjector.setRealtimeThreadScheduling(99999, 50));
        assertTrue(NativeConfigInjector.setProcessIoPriority(99999, 1, 0));
        assertTrue(NativeConfigInjector.boostProcessResources(99999, 120));
    }

    @Test
    public void testFallbackPipelineDamageLockMax() throws IOException {
        File configFile = new File(mTempDir, "BattleConfig.json");
        String initialContent = "{\"TargetFPS\":60}\n";
        Files.write(configFile.toPath(), initialContent.getBytes(StandardCharsets.UTF_8));

        // When native library is absent or fails, fallback pipeline must patch the file
        boolean success = NativeConfigInjector.injectDamageLockMax(configFile.getAbsolutePath());
        assertTrue("injectDamageLockMax should succeed via fallback pipeline", success);

        String result = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
        assertTrue("Result must contain DamageLockMax", result.contains("DamageLockMax"));
        assertTrue("Result must contain HitRegSyncRate", result.contains("HitRegSyncRate"));
    }

    @Test
    public void testFallbackPipelineAimAssistLockMax() throws IOException {
        File configFile = new File(mTempDir, "AimConfig.ini");
        String initialContent = "[Aim]\nSensitivity=1.0\n";
        Files.write(configFile.toPath(), initialContent.getBytes(StandardCharsets.UTF_8));

        boolean success = NativeConfigInjector.injectAimAssistLockMax(configFile.getAbsolutePath());
        assertTrue("injectAimAssistLockMax should succeed via fallback pipeline", success);

        String result = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
        assertTrue("Result must contain HeroLock", result.contains("HeroLock=1"));
        assertTrue("Result must contain TouchPollingRate", result.contains("TouchPollingRate=1000"));
    }

    @Test
    public void testFallbackPipelineSpecificGameCombos() throws IOException {
        File lingFile = new File(mTempDir, "ling_combo.ini");
        assertTrue(NativeConfigInjector.injectLingHeroDamageCombo(lingFile.getAbsolutePath()));
        String lingResult = new String(Files.readAllBytes(lingFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(lingResult.contains("HitRegSyncRate=1000") || lingResult.contains("TouchPollingRate=1000"));

        File pubgmFile = new File(mTempDir, "pubgm_aimbot.ini");
        assertTrue(NativeConfigInjector.injectMagicBulletAimbot(pubgmFile.getAbsolutePath()));
        String pubgmResult = new String(Files.readAllBytes(pubgmFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(pubgmResult.contains("GyroSampleRate=1000") || pubgmResult.contains("Scope8xGyro1000Hz=1"));

        File codmFile = new File(mTempDir, "codm_recoil.ini");
        assertTrue(NativeConfigInjector.injectNoRecoilNoSpread(codmFile.getAbsolutePath()));
        String codmResult = new String(Files.readAllBytes(codmFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(codmResult.contains("GyroStabilization=1") || codmResult.contains("InputBufferRate=1000"));
    }

    @Test
    public void testHardwareMaskProfileFallback() throws IOException {
        File hwFile = new File(mTempDir, "DeviceProfile.ini");
        boolean ok = NativeConfigInjector.injectHardwareMaskProfile(
                hwFile.getAbsolutePath(),
                "Adreno (TM) 750",
                "Snapdragon 8 Gen 3",
                16384,
                144
        );
        assertTrue("injectHardwareMaskProfile should succeed via fallback", ok);

        String result = new String(Files.readAllBytes(hwFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(result.contains("GpuRenderer=Adreno (TM) 750"));
        assertTrue(result.contains("SocModel=Snapdragon 8 Gen 3"));
        assertTrue(result.contains("SystemRamMB=16384"));
        assertTrue(result.contains("TargetFPS=144"));
    }

    @Test
    public void testConfigFileHelperFallbackPatcher() {
        String ini = "[Engine]\nFPS=60\n";
        String[] updates = new String[]{"FPS=120", "Vsync=0"};
        String patched = ConfigFileHelper.patchContentInMemory(ini, updates, "[Engine]", "test.ini");
        assertNotNull(patched);
        assertTrue(patched.contains("FPS=120"));
        assertTrue(patched.contains("Vsync=0"));
    }

    @Test
    public void testHardwareMaskProfileXmlPreservesExistingContent() throws IOException {
        File xmlFile = new File(mTempDir, "com.mobile.legends.v4.playerprefs.xml");
        String initialXml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
                + "<map>\n"
                + "  <string name=\"account_session_token\">AUTH_TOKEN_REAL_987</string>\n"
                + "  <int name=\"user_level\" value=\"45\" />\n"
                + "</map>\n";
        Files.write(xmlFile.toPath(), initialXml.getBytes(StandardCharsets.UTF_8));

        boolean ok = NativeConfigInjector.injectHardwareMaskProfile(
                xmlFile.getAbsolutePath(),
                "Adreno (TM) 840",
                "Snapdragon 8 Elite",
                24576,
                165
        );
        assertTrue("XML profile injection must succeed", ok);

        String result = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue("Must preserve account session token", result.contains("AUTH_TOKEN_REAL_987"));
        assertTrue("Must preserve user level", result.contains("user_level"));
        assertTrue("Must contain GPU renderer", result.contains("Adreno (TM) 840"));
        assertTrue("Must contain SoC model", result.contains("Snapdragon 8 Elite"));
        assertTrue("Must contain valid XML map tag", result.contains("<map>") && result.contains("</map>"));
        assertFalse("Must not inject raw INI header into XML", result.contains("[HardwareProfile]"));
    }
}
