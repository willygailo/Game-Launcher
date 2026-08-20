package com.gamebooster.app.anticheat;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class GameAntiCheatBypassEngineTest {

    @Test
    public void testDetectAntiCheatAcrossAllGames() {
        // 1. Tencent ACE
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.TENCENT_ACE,
                GameAntiCheatBypassEngine.detectAntiCheat("com.tencent.ig"));
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.TENCENT_ACE,
                GameAntiCheatBypassEngine.detectAntiCheat("com.pubg.imobile"));
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.TENCENT_ACE,
                GameAntiCheatBypassEngine.detectAntiCheat("com.proximabeta.mf.uamo"));
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.TENCENT_ACE,
                GameAntiCheatBypassEngine.detectAntiCheat("com.levelinfinite.deltaforce"));

        // 2. Moonton
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.MOONTON_GUARD,
                GameAntiCheatBypassEngine.detectAntiCheat("com.mobile.legends"));
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.MOONTON_GUARD,
                GameAntiCheatBypassEngine.detectAntiCheat("com.mobilelegends.mi"));

        // 3. Activision / RICOCHET
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.ACTIVISION_RICOCHET,
                GameAntiCheatBypassEngine.detectAntiCheat("com.activision.callofduty.shooter"));
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.ACTIVISION_RICOCHET,
                GameAntiCheatBypassEngine.detectAntiCheat("com.activision.callofduty.warzone"));

        // 4. Garena
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.GARENA_PROTECT,
                GameAntiCheatBypassEngine.detectAntiCheat("com.dts.freefireth"));
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.GARENA_PROTECT,
                GameAntiCheatBypassEngine.detectAntiCheat("com.dts.freefiremax"));

        // 5. HoYoverse
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.HOYO_PROTECT,
                GameAntiCheatBypassEngine.detectAntiCheat("com.miHoYo.GenshinImpact"));
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.HOYO_PROTECT,
                GameAntiCheatBypassEngine.detectAntiCheat("com.HoYoverse.hkrpgoversea"));

        // 6. NetEase
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.NETEASE_NETPROTECT,
                GameAntiCheatBypassEngine.detectAntiCheat("com.netease.bloodstrike"));

        // 7. Riot
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.RIOT_INTEGRITY,
                GameAntiCheatBypassEngine.detectAntiCheat("com.riotgames.league.wildrift"));

        // 8. Axlebolt
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.AXLEBOLT_AC,
                GameAntiCheatBypassEngine.detectAntiCheat("com.axlebolt.standoff2"));

        // 9. Roblox
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.HYPERION_BYFRON,
                GameAntiCheatBypassEngine.detectAntiCheat("com.roblox.client"));

        // 10. Supercell
        assertEquals(GameAntiCheatBypassEngine.AntiCheatType.SUPERCELL_FAIRPLAY,
                GameAntiCheatBypassEngine.detectAntiCheat("com.supercell.brawlstars"));
    }

    @Test
    public void testAntiCheatLogPathsResolution() {
        List<String> pubgLogs = GameAntiCheatBypassEngine.getAntiCheatLogPaths(
                "com.tencent.ig", GameAntiCheatBypassEngine.AntiCheatType.TENCENT_ACE);
        assertNotNull(pubgLogs);
        assertFalse(pubgLogs.isEmpty());
        assertTrue(pubgLogs.stream().anyMatch(p -> p.contains("ShadowTrackerExtra")));

        List<String> mlbbLogs = GameAntiCheatBypassEngine.getAntiCheatLogPaths(
                "com.mobile.legends", GameAntiCheatBypassEngine.AntiCheatType.MOONTON_GUARD);
        assertNotNull(mlbbLogs);
        assertFalse(mlbbLogs.isEmpty());
        assertTrue(mlbbLogs.stream().anyMatch(p -> p.contains("dragon2017")));
    }
}
