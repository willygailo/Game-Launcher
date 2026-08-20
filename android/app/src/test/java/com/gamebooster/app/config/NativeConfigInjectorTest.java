package com.gamebooster.app.config;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class NativeConfigInjectorTest {

    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("native_injector_test").toFile();
    }

    @After
    public void tearDown() {
        if (tempDir != null && tempDir.exists()) {
            for (File f : tempDir.listFiles()) {
                f.delete();
            }
            tempDir.delete();
        }
    }

    @Test
    public void testFpsTiers120To185Resolution() {
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(240));
    }

    @Test
    public void testInjectIniConfigInMemory() throws IOException {
        File iniFile = new File(tempDir, "UserCustom.ini");
        try (FileWriter fw = new FileWriter(iniFile)) {
            fw.write("[UserCustom]\nFPS=60\nGraphicsQuality=2\n");
        }

        NativeConfigInjector.injectAimAssist(iniFile.getAbsolutePath(), 185);
        NativeConfigInjector.injectDamageScript(iniFile.getAbsolutePath(), 185);
        NativeConfigInjector.injectNoRecoil(iniFile.getAbsolutePath(), 185);
        NativeConfigInjector.injectTrackingBullet(iniFile.getAbsolutePath(), 185);
        NativeConfigInjector.injectUltraExtremeGraphics(iniFile.getAbsolutePath(), 185);

        String content = new String(Files.readAllBytes(iniFile.toPath()));
        assertTrue(content.contains("AimAssist=1"));
        assertTrue(content.contains("AimAssistStrength=150"));
        assertTrue(content.contains("DamageMultiplier=2.50"));
        assertTrue(content.contains("RecoilControl=1"));
        assertTrue(content.contains("RecoilScale=0.00"));
        assertTrue(content.contains("TrackingBullet=1"));
        assertTrue(content.contains("BulletMagnetism=1.50"));
        assertTrue(content.contains("UltraExtreme=1"));
        assertTrue(content.contains("bUseUltraExtreme=True"));
        assertTrue(content.contains("GraphicsQuality=5"));
        assertTrue(content.contains("Unlock185FPS=1"));
        assertTrue(content.contains("FPS=185"));
    }

    @Test
    public void testInjectJsonConfigInMemory() throws IOException {
        File jsonFile = new File(tempDir, "settings.json");
        try (FileWriter fw = new FileWriter(jsonFile)) {
            fw.write("{\n  \"frameRate\": 60,\n  \"graphics\": 2\n}\n");
        }

        NativeConfigInjector.injectAimAssist(jsonFile.getAbsolutePath(), 144);
        NativeConfigInjector.injectTrackingBullet(jsonFile.getAbsolutePath(), 144);
        NativeConfigInjector.injectUltraExtremeGraphics(jsonFile.getAbsolutePath(), 144);

        String content = new String(Files.readAllBytes(jsonFile.toPath()));
        assertTrue(content.contains("\"AimAssist\": 1"));
        assertTrue(content.contains("\"TrackingBullet\": 1"));
        assertTrue(content.contains("\"UltraExtreme\": 1"));
        assertTrue(content.contains("\"GraphicQuality\": 4"));
    }

    @Test
    public void testInjectXmlConfigInMemory() throws IOException {
        File xmlFile = new File(tempDir, "config.xml");
        try (FileWriter fw = new FileWriter(xmlFile)) {
            fw.write("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n    <int name=\"fps\" value=\"60\" />\n</map>\n");
        }

        NativeConfigInjector.injectAimAssist(xmlFile.getAbsolutePath(), 165);
        NativeConfigInjector.injectDamageScript(xmlFile.getAbsolutePath(), 165);
        NativeConfigInjector.injectNoRecoil(xmlFile.getAbsolutePath(), 165);
        NativeConfigInjector.injectTrackingBullet(xmlFile.getAbsolutePath(), 165);
        NativeConfigInjector.injectUltraExtremeGraphics(xmlFile.getAbsolutePath(), 165);

        String content = new String(Files.readAllBytes(xmlFile.toPath()));
        assertTrue(content.contains("<string name=\"AimAssist\">1</string>"));
        assertTrue(content.contains("<string name=\"DamageMultiplier\">2.50</string>"));
        assertTrue(content.contains("<string name=\"RecoilControl\">1</string>"));
        assertTrue(content.contains("<string name=\"TrackingBullet\">1</string>"));
        assertTrue(content.contains("<string name=\"UltraExtreme\">1</string>"));
    }

    @Test
    public void testExtractKeyForCVars() {
        assertEquals("+CVars=r.PUBGDeviceFPS", NativeConfigInjector.extractKey("+CVars=r.PUBGDeviceFPS=10"));
        assertEquals("+CVars=r.PUBGQualityLevel", NativeConfigInjector.extractKey("+CVars=r.PUBGQualityLevel=4"));
        assertEquals("FPS", NativeConfigInjector.extractKey("FPS=185"));
        assertEquals("AimAssist", NativeConfigInjector.extractKey("AimAssist=1"));
    }
}
