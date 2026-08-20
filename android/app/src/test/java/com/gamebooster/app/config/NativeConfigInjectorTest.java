package com.gamebooster.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class NativeConfigInjectorTest {

    @Test
    public void testNativeLoadedCheckDoesNotCrash() {
        // Must return boolean without throwing any exception
        boolean loaded = NativeConfigInjector.isNativeLoaded();
        assertNotNull(Boolean.valueOf(loaded));
    }

    @Test
    public void testInjectConfigDirectFallback() throws IOException {
        File tempFile = File.createTempFile("test_cfg_", ".ini");
        tempFile.deleteOnExit();

        String content = "[Graphics]\nFPS=185\nAimAssist=1\nNoRecoil=1\nDamageMultiplier=2.50\n";
        boolean ok = NativeConfigInjector.injectConfig(tempFile.getAbsolutePath(), content);
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("FPS=185"));
        assertTrue(readBack.contains("AimAssist=1"));
        assertTrue(readBack.contains("NoRecoil=1"));
        assertTrue(readBack.contains("DamageMultiplier=2.50"));
    }

    @Test
    public void testInjectHighDamageKeys() throws IOException {
        File tempFile = File.createTempFile("test_dmg_", ".ini");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "[Settings]\nFPS=185\n".getBytes());

        boolean ok = NativeConfigInjector.injectHighDamage(tempFile.getAbsolutePath());
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("DamageMultiplier=2.50"));
        assertTrue(readBack.contains("PhysicalDamageBoost=2.50"));
        assertTrue(readBack.contains("CriticalHitRate=99"));
        assertTrue(readBack.contains("HeadshotDamageMultiplier=3.50"));
    }

    @Test
    public void testInjectNoRecoilKeys() throws IOException {
        File tempFile = File.createTempFile("test_recoil_", ".ini");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "[Settings]\nFPS=185\n".getBytes());

        boolean ok = NativeConfigInjector.injectNoRecoil(tempFile.getAbsolutePath());
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("ZeroRecoil=1"));
        assertTrue(readBack.contains("NoRecoil=1"));
        assertTrue(readBack.contains("RecoilScale=0.00"));
        assertTrue(readBack.contains("WeaponStability=150"));
    }

    @Test
    public void testInjectAimAssistKeys() throws IOException {
        File tempFile = File.createTempFile("test_aim_", ".ini");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "[Settings]\nFPS=185\n".getBytes());

        boolean ok = NativeConfigInjector.injectAimAssist(tempFile.getAbsolutePath());
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("AimAssist=1"));
        assertTrue(readBack.contains("AimAssistStrength=150"));
        assertTrue(readBack.contains("AutoAim=1"));
        assertTrue(readBack.contains("GyroSampleRate=1000"));
    }

    @Test
    public void testInjectTrackingBulletKeys() throws IOException {
        File tempFile = File.createTempFile("test_track_", ".ini");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "[Settings]\nFPS=185\n".getBytes());

        boolean ok = NativeConfigInjector.injectTrackingBullet(tempFile.getAbsolutePath());
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("TrackingBullet=1"));
        assertTrue(readBack.contains("BulletTracking=1"));
        assertTrue(readBack.contains("MagicBullet=1"));
        assertTrue(readBack.contains("HitboxExpansion=1.50"));
        assertTrue(readBack.contains("BulletMagnetism=1.50"));
    }

    @Test
    public void testInjectArmorDefKeys() throws IOException {
        File tempFile = File.createTempFile("test_armor_", ".ini");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "[Settings]\nFPS=185\n".getBytes());

        boolean ok = NativeConfigInjector.injectArmorDef(tempFile.getAbsolutePath());
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("DamageReductionRatio=0.50"));
        assertTrue(readBack.contains("ShieldMultiplier=2.00"));
        assertTrue(readBack.contains("PhysicalDefenseBoost=2.50"));
    }

    @Test
    public void testInjectPerGameConfig() throws IOException {
        File tempFile = File.createTempFile("test_per_game_", ".ini");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "[Settings]\n".getBytes());

        boolean ok = NativeConfigInjector.injectPerGameConfig(
            tempFile.getAbsolutePath(),
            "PUBGM",
            185,
            true, // highDamage
            true, // noRecoil
            true, // trackingBullet
            true  // aimAssist
        );
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("DamageMultiplier=2.50"));
        assertTrue(readBack.contains("ZeroRecoil=1"));
        assertTrue(readBack.contains("TrackingBullet=1"));
        assertTrue(readBack.contains("AimAssist=1"));
    }

    @Test
    public void testInjectUltraExtremeGraphics() throws IOException {
        File tempFile = File.createTempFile("test_ultra_graphics_", ".ini");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "[Settings]\n".getBytes());

        boolean ok = NativeConfigInjector.injectUltraExtremeGraphics(tempFile.getAbsolutePath(), 185);
        assertTrue(ok);

        String readBack = new String(Files.readAllBytes(tempFile.toPath()));
        assertTrue(readBack.contains("FPS=185"));
        assertTrue(readBack.contains("UltraExtreme=1"));
        assertTrue(readBack.contains("GraphicsQuality=5"));
        assertTrue(readBack.contains("HDRMode=1"));
        assertTrue(readBack.contains("Unlock185Hz=1"));
        assertTrue(readBack.contains("Unlock165Hz=1"));
        assertTrue(readBack.contains("Unlock144Hz=1"));
        assertTrue(readBack.contains("Unlock120Hz=1"));
    }

    @Test
    public void testFpsTiers120To185Resolution() {
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
    }
}
