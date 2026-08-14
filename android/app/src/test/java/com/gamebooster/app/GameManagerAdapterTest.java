package com.gamebooster.app;

import com.gamebooster.app.feature.performance.booster.GameManagerAdapter;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameManagerAdapterTest {

    @Test
    public void testGameModeConstants() {
        assertEquals(0, GameManagerAdapter.GAME_MODE_UNSUPPORTED);
        assertEquals(1, GameManagerAdapter.GAME_MODE_STANDARD);
        assertEquals(2, GameManagerAdapter.GAME_MODE_PERFORMANCE);
        assertEquals(3, GameManagerAdapter.GAME_MODE_BATTERY);
        assertEquals(4, GameManagerAdapter.GAME_MODE_CUSTOM);
    }

    @Test
    public void testGameManagerNullSafety() {
        GameManagerAdapter adapter = new GameManagerAdapter(null);
        assertFalse(adapter.setGameMode(null, GameManagerAdapter.GAME_MODE_PERFORMANCE));
        assertFalse(adapter.setGameMode("", GameManagerAdapter.GAME_MODE_PERFORMANCE));
        assertFalse(adapter.setSurfaceFrameRateHint(null, 120f));
        assertFalse(adapter.applyGameOverlay(null, 2, 120, 1.0f, false));
        assertFalse(adapter.applyGameOverlay("global", 2, 120, 1.0f, false));
        assertEquals(GameManagerAdapter.GAME_MODE_UNSUPPORTED, adapter.getGameMode(null));
    }
}
