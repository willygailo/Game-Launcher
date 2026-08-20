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
}
