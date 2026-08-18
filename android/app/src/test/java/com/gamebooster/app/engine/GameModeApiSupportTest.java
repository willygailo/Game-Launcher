package com.gamebooster.app.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Phase 2.1 — pure SDK-availability gates for the privileged GameMode shell set.
 * Boundaries: full set = Android 14+ (34); game mode = 12+ (31); game_overlay
 * namespace = 13+ (33); fps override / set-app-refresh-rate = 14+ (34).
 */
public class GameModeApiSupportTest {

    @Test
    public void fullSet_requiresAndroid14() {
        assertFalse(GameModeApiSupport.isAvailable(24));
        assertFalse(GameModeApiSupport.isAvailable(33));
        assertTrue(GameModeApiSupport.isAvailable(34));
        assertTrue(GameModeApiSupport.isAvailable(36));
    }

    @Test
    public void gameModeCommand_isAndroid12Plus() {
        assertFalse(GameModeApiSupport.isGameModeApiAvailable(24));
        assertFalse(GameModeApiSupport.isGameModeApiAvailable(30));
        assertTrue(GameModeApiSupport.isGameModeApiAvailable(31));
        assertTrue(GameModeApiSupport.isGameModeApiAvailable(33));
    }

    @Test
    public void gameOverlayNamespace_isAndroid13Plus() {
        assertFalse(GameModeApiSupport.isGameOverlayApiAvailable(24));
        assertFalse(GameModeApiSupport.isGameOverlayApiAvailable(32));
        assertTrue(GameModeApiSupport.isGameOverlayApiAvailable(33));
        assertTrue(GameModeApiSupport.isGameOverlayApiAvailable(34));
    }

    @Test
    public void fpsOverrideAndRefreshRate_areAndroid14Plus() {
        assertFalse(GameModeApiSupport.isGameFpsOverrideAvailable(24));
        assertFalse(GameModeApiSupport.isGameFpsOverrideAvailable(33));
        assertTrue(GameModeApiSupport.isGameFpsOverrideAvailable(34));
        assertFalse(GameModeApiSupport.isAppRefreshRateApiAvailable(33));
        assertTrue(GameModeApiSupport.isAppRefreshRateApiAvailable(34));
    }

    @Test
    public void fullSetMatchesNewestSubApi() {
        // Android 14 is the max of the per-command minimums (31/33/34/34)
        assertTrue(GameModeApiSupport.isAvailable(
                Math.max(GameModeApiSupport.MIN_GAME_MODE_API,
                        Math.max(GameModeApiSupport.MIN_GAME_OVERLAY_API,
                                GameModeApiSupport.MIN_APP_REFRESH_RATE_API))));
        assertFalse(GameModeApiSupport.isAvailable(
                GameModeApiSupport.MIN_APP_REFRESH_RATE_API - 1));
    }
}