package com.gamebooster.app.diagnostics;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HardwareDiagnosticsTest {

    @Test
    public void testCpuCoreFrequencyReading() {
        Map<String, String> freqs = HardwareDiagnosticsEngine.getCpuCoreFrequencies();
        assertNotNull(freqs);
        assertFalse(freqs.isEmpty());
        assertTrue(freqs.containsKey("Core #0"));
    }

    @Test
    public void testAuditSpoofIntegrityFallback() {
        HardwareDiagnosticsEngine.SpoofDiagnosticReport report =
                HardwareDiagnosticsEngine.auditSpoofIntegrity(null);
        assertNotNull(report);
        assertNotNull(report.activeProfile);
        assertNotNull(report.activeProfile.getAndroidId());
    }

    @Test
    public void testAuditGamePatches() {
        List<HardwareDiagnosticsEngine.GamePatchDiagnostic> patches =
                HardwareDiagnosticsEngine.auditGamePatches(null);
        assertNotNull(patches);
        assertFalse(patches.isEmpty());
        for (HardwareDiagnosticsEngine.GamePatchDiagnostic p : patches) {
            assertNotNull(p.packageName);
            assertNotNull(p.gameName);
            assertTrue("Target FPS should be positive: " + p.targetFps, p.targetFps >= 60);
            assertTrue(p.ultraExtremeReady);
        }
    }

    @Test
    public void testDiagnosticsExporterSnapshotFormat() {
        List<String> lines = DiagnosticsExporter.buildSnapshot(
                "3.5.0-PRO",
                "Samsung Galaxy S25 Ultra",
                "15",
                35,
                null,
                true,
                "samsung_s25_ultra",
                ""
        );
        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        String joined = DiagnosticsExporter.join(lines);
        assertTrue(joined.contains("GAME BOOSTER PRO SYSTEM DIAGNOSTICS"));
        assertTrue(joined.contains("HARDWARE & SOC SPECIFICATIONS"));
        assertTrue(joined.contains("185FPS ULTRAEXTREME PATCH STATUS"));
        assertTrue(joined.contains("SurfaceFlinger 185Hz Uncap"));
        assertTrue(joined.contains("1000Hz Ultra-Touch Response"));
        assertTrue(joined.contains("Android ID (64-bit Hex)"));
        assertTrue(joined.contains("Build Serial Number"));
        assertTrue(joined.contains("Wi-Fi MAC (OEM OUI)"));
        assertTrue(joined.contains("Bluetooth MAC"));
        assertTrue(joined.contains("OAID / MSA UUID"));
        assertTrue(joined.contains("GSF ID"));
        assertTrue(joined.contains("Widevine DRM Hardware ID"));
    }
}
