package com.gamebooster.app.diagnostics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gamebooster.app.engine.MasterOptimizationEnforcer;

import java.util.List;

public class DiagnosticsExporterTest {

    private static MasterOptimizationEnforcer.EnforcementStatus status(
            boolean shizuku, boolean aidl, int applied, int total) {
        return new MasterOptimizationEnforcer.EnforcementStatus(
                shizuku, aidl, applied, total, "15", 35);
    }

    @Test
    public void buildSnapshot_containsCoreLines() {
        List<String> lines = DiagnosticsExporter.buildSnapshot(
                "16.0.0-PRO", "SM-S26 (Samsung)", "15", 35,
                status(true, true, 12, 24),
                true, "apple_a18", "");
        String joined = DiagnosticsExporter.join(lines);
        assertTrue(joined.contains("=== GAME BOOSTER PRO DIAGNOSTICS ==="));
        assertTrue(joined.contains("App version: 16.0.0-PRO"));
        assertTrue(joined.contains("Device: SM-S26 (Samsung)"));
        assertTrue(joined.contains("Android: 15 (API 35)"));
        assertTrue(joined.contains("Shizuku/root available: true"));
        assertTrue(joined.contains("AIDL service connected: true"));
        assertTrue(joined.contains("Tweaks enforced: 12 / 24"));
        assertTrue(joined.contains("Spoof profile: apple_a18 (enabled=true)"));
        assertTrue(joined.contains("--- End ---"));
    }

    @Test
    public void buildSnapshot_reflectsDegradedState() {
        List<String> lines = DiagnosticsExporter.buildSnapshot(
                null, null, null, 0,
                status(false, false, 0, 24),
                false, null, "");
        String joined = DiagnosticsExporter.join(lines);
        assertTrue(joined.contains("App version: unknown"));
        assertTrue(joined.contains("Shizuku/root available: false"));
        assertTrue(joined.contains("Tweaks enforced: 0 / 24"));
        assertTrue(joined.contains("Spoof profile: none (enabled=false)"));
        assertFalse(joined.contains("crash"));
    }

    @Test
    public void buildSnapshot_appendsCrashTailWhenPresent() {
        List<String> lines = DiagnosticsExporter.buildSnapshot(
                "16.0.0-PRO", "Device", "15", 35,
                status(true, true, 3, 24),
                false, null, "Thread: main\nException: Boom\n    at com.gamebooster.X.f()");
        String joined = DiagnosticsExporter.join(lines);
        assertTrue(joined.contains("--- Last captured crash ---"));
        assertTrue(joined.contains("Exception: Boom"));
    }

    @Test
    public void join_joinsLinesWithNewlines() {
        assertEquals("a\nb\nc", DiagnosticsExporter.join(
                java.util.Arrays.asList("a", "b", "c")));
        assertEquals("", DiagnosticsExporter.join(java.util.Collections.emptyList()));
    }
}