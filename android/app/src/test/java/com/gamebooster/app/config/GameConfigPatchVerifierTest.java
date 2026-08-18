package com.gamebooster.app.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GameConfigPatchVerifierTest {

    // ── verifyFpsInContent ──────────────────────────────────────────────────

    @Test
    public void verify_genericIniFps() {
        assertTrue(GameConfigPatchVerifier.verifyFpsInContent(
                "[Graphics]\nFPS=185\nFrameRate=185\nHighFPSMode=1\nMaxFrameRate=185\n", 185));
    }

    @Test
    public void verify_pubgCvarLine() {
        assertTrue(GameConfigPatchVerifier.verifyFpsInContent(
                "+CVars=r.PUBGMaxFPS=185\n+CVars=r.MobileFPSLimit=185\n", 185));
    }

    @Test
    public void verify_jsonColonFormat() {
        assertTrue(GameConfigPatchVerifier.verifyFpsInContent(
                "{\"MaxFPS\": 185, \"FrameRate\": 185}", 185));
    }

    @Test
    public void verify_quotedValue() {
        assertTrue(GameConfigPatchVerifier.verifyFpsInContent(
                "MaxFrameRate=\"120\"", 120));
    }

    @Test
    public void verify_wrongValueNotAccepted() {
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(
                "MaxFrameRate=144\n", 185));
    }

    @Test
    public void verify_levelValueNotFps() {
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(
                "FrameRateLevel=6\n", 185));
    }

    @Test
    public void verify_unlockHzFlagNotFps() {
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(
                "+CVars=r.Unlock120Hz=1\n+CVars=r.Unlock185Hz=1\n", 185));
    }

    @Test
    public void verify_commentOnlyNotAccepted() {
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(
                "# FPS=185\n// MaxFrameRate=185\n", 185));
    }

    @Test
    public void verify_nullOrEmptyContentRejected() {
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent(null, 185));
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent("", 185));
    }

    @Test
    public void verify_invalidFpsRejected() {
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent("FPS=0", 0));
        assertFalse(GameConfigPatchVerifier.verifyFpsInContent("FPS=185", -1));
    }

    // ── buildVerificationSummary ────────────────────────────────────────────

    @Test
    public void summary_allFilesConfirmed() {
        String s = GameConfigPatchVerifier.buildVerificationSummary(3, 3);
        assertTrue(s.contains(GameConfigPatchVerifier.CONFIRMED));
        assertTrue(s.contains("3/3"));
    }

    @Test
    public void summary_partialConfirmation() {
        String s = GameConfigPatchVerifier.buildVerificationSummary(1, 3);
        assertTrue(s.contains("partially confirmed"));
        assertTrue(s.contains("1/3"));
    }

    @Test
    public void summary_nothingVerified() {
        String s = GameConfigPatchVerifier.buildVerificationSummary(0, 3);
        assertTrue(s.contains(GameConfigPatchVerifier.UNVERIFIED));
        assertTrue(s.contains("3 files"));
    }

    @Test
    public void summary_noReadableFiles() {
        String s = GameConfigPatchVerifier.buildVerificationSummary(0, 0);
        assertTrue(s.contains(GameConfigPatchVerifier.UNVERIFIED));
    }

    // ── getPatchCompatibilityNote ───────────────────────────────────────────

    @Test
    public void note_codmGetsResetWarning() {
        String note = GameConfigPatchVerifier.getPatchCompatibilityNote("com.activision.callofduty.shooter");
        assertNotNull(note);
        assertTrue(note.contains("re-apply"));
    }

    @Test
    public void note_mobileLegendsGetsResetWarning() {
        assertNotNull(GameConfigPatchVerifier.getPatchCompatibilityNote("com.mobile.legends"));
    }

    @Test
    public void note_genshinGetsIntegrityWarning() {
        String note = GameConfigPatchVerifier.getPatchCompatibilityNote("com.miHoYo.Yuanshen");
        assertNotNull(note);
        assertTrue(note.contains("Integrity"));
    }

    @Test
    public void note_unknownFamilyNoWarning() {
        assertNull(GameConfigPatchVerifier.getPatchCompatibilityNote("com.roblox.client"));
        assertNull(GameConfigPatchVerifier.getPatchCompatibilityNote(null));
        assertEquals(GameConfigPatchVerifier.getPatchCompatibilityNote("COM.MOBILE.LEGENDS"),
                GameConfigPatchVerifier.getPatchCompatibilityNote("com.mobile.legends"));
    }
}