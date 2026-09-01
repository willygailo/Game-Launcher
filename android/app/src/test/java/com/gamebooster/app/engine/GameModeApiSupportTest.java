package com.gamebooster.app.engine;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameModeApiSupportTest {

    @Test
    public void testGameModeApiGates() {
        assertFalse(GameModeApiSupport.isGameModeApiAvailable(30));
        assertTrue(GameModeApiSupport.isGameModeApiAvailable(31)); // Android 12
        assertTrue(GameModeApiSupport.isGameModeApiAvailable(33)); // Android 13
        assertTrue(GameModeApiSupport.isGameModeApiAvailable(34)); // Android 14
        assertTrue(GameModeApiSupport.isGameModeApiAvailable(35)); // Android 15
        assertTrue(GameModeApiSupport.isGameModeApiAvailable(36)); // Android 16
    }

    @Test
    public void testGameOverlayApiGates() {
        assertFalse(GameModeApiSupport.isGameOverlayApiAvailable(31));
        assertFalse(GameModeApiSupport.isGameOverlayApiAvailable(32));
        assertTrue(GameModeApiSupport.isGameOverlayApiAvailable(33)); // Android 13
        assertTrue(GameModeApiSupport.isGameOverlayApiAvailable(34));
        assertTrue(GameModeApiSupport.isGameOverlayApiAvailable(35));
        assertTrue(GameModeApiSupport.isGameOverlayApiAvailable(36));
    }

    @Test
    public void testAppRefreshRateAndFpsGates() {
        assertFalse(GameModeApiSupport.isAppRefreshRateApiAvailable(33));
        assertTrue(GameModeApiSupport.isAppRefreshRateApiAvailable(34)); // Android 14
        assertTrue(GameModeApiSupport.isAppRefreshRateApiAvailable(35));
        assertTrue(GameModeApiSupport.isAppRefreshRateApiAvailable(36));

        assertFalse(GameModeApiSupport.isGameFpsOverrideAvailable(33));
        assertTrue(GameModeApiSupport.isGameFpsOverrideAvailable(34));
    }

    @Test
    public void testAndroid15And16Gates() {
        assertFalse(GameModeApiSupport.isAndroid15Api(34));
        assertTrue(GameModeApiSupport.isAndroid15Api(35)); // Android 15
        assertTrue(GameModeApiSupport.isAndroid15Api(36));

        assertFalse(GameModeApiSupport.isAndroid16Api(35));
        assertTrue(GameModeApiSupport.isAndroid16Api(36)); // Android 16
    }

    @Test
    public void testFullRecommendationGate() {
        assertFalse(GameModeApiSupport.isAvailable(31));
        assertFalse(GameModeApiSupport.isAvailable(32));
        assertFalse(GameModeApiSupport.isAvailable(33));
        assertTrue(GameModeApiSupport.isAvailable(34));
        assertTrue(GameModeApiSupport.isAvailable(35));
        assertTrue(GameModeApiSupport.isAvailable(36));
    }
}
