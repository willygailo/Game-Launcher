package com.gamebooster.app;

import com.gamebooster.app.feature.performance.refreshrate.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class RefreshRateStrategyTest {

    @Test
    public void testGenericStrategyFormatting() {
        GenericHzStrategy strategy = new GenericHzStrategy();
        assertEquals("Generic AOSP / Pixel SurfaceFlinger Refresh Rate", strategy.getStrategyName());
        
        String forceOutput = strategy.forceRefreshRate(120, "com.dts.freefireth");
        assertNotNull(forceOutput);
        assertTrue(forceOutput.contains("peak_refresh_rate"));
        assertTrue(forceOutput.contains("min_refresh_rate"));
        assertTrue(forceOutput.contains("cmd window set-app-refresh-rate (com.dts.freefireth @ 120Hz)"));
        assertTrue(forceOutput.contains("cmd game set (com.dts.freefireth @ 120FPS)"));
        assertFalse(forceOutput.contains("set-app-refresh-rate global"));

        String resetOutput = strategy.resetRefreshRate("com.dts.freefireth");
        assertNotNull(resetOutput);
        assertTrue(resetOutput.contains("reset peak_refresh_rate"));
        assertTrue(resetOutput.contains("cmd game reset (com.dts.freefireth)"));
    }

    @Test
    public void testXiaomiStrategyFormatting() {
        XiaomiHzStrategy strategy = new XiaomiHzStrategy();
        assertEquals("Xiaomi HyperOS / MIUI Display DFPS Refresh Rate", strategy.getStrategyName());

        String forceOutput = strategy.forceRefreshRate(144, "com.mobile.legends");
        assertNotNull(forceOutput);
        assertTrue(forceOutput.contains("miui_refresh_rate"));
        assertTrue(forceOutput.contains("dfps level"));
        assertTrue(forceOutput.contains("joyose fps"));
        assertTrue(forceOutput.contains("cmd window set-app-refresh-rate (com.mobile.legends @ 144Hz)"));
        assertFalse(forceOutput.contains("game_overlay global"));

        String resetOutput = strategy.resetRefreshRate("com.mobile.legends");
        assertNotNull(resetOutput);
        assertTrue(resetOutput.contains("reset dfps level"));
        assertTrue(resetOutput.contains("cmd game reset (com.mobile.legends)"));
    }

    @Test
    public void testSamsungStrategyFormatting() {
        SamsungHzStrategy strategy = new SamsungHzStrategy();
        assertEquals("Samsung OneUI Motion Smoothness Refresh Rate", strategy.getStrategyName());

        String forceOutput = strategy.forceRefreshRate(120, "com.tencent.ig");
        assertNotNull(forceOutput);
        assertTrue(forceOutput.contains("refresh_rate_mode"));
        assertTrue(forceOutput.contains("game_perf_mode"));
        assertTrue(forceOutput.contains("gos_fps_limit"));
        assertTrue(forceOutput.contains("cmd game set (com.tencent.ig @ 120FPS)"));
        assertFalse(forceOutput.contains("cmd game set --fps 120 global"));

        String resetOutput = strategy.resetRefreshRate("com.tencent.ig");
        assertNotNull(resetOutput);
        assertTrue(resetOutput.contains("reset refresh_rate_mode"));
        assertTrue(resetOutput.contains("cmd game reset (com.tencent.ig)"));
    }

    @Test
    public void testOnePlusOppoStrategyFormatting() {
        OnePlusOppoHzStrategy strategy = new OnePlusOppoHzStrategy();
        assertEquals("OnePlus / OPPO / Realme ColorOS Refresh Rate", strategy.getStrategyName());

        String forceOutput = strategy.forceRefreshRate(165, "com.activision.callofduty.shooter");
        assertNotNull(forceOutput);
        assertTrue(forceOutput.contains("oplus_screen_refresh_rate"));
        assertTrue(forceOutput.contains("oplus_display_level"));
        assertTrue(forceOutput.contains("cmd window set-app-refresh-rate (com.activision.callofduty.shooter @ 165Hz)"));

        String resetOutput = strategy.resetRefreshRate("com.activision.callofduty.shooter");
        assertNotNull(resetOutput);
        assertTrue(resetOutput.contains("reset oplus_customize_screen_refresh_rate"));
        assertTrue(resetOutput.contains("cmd game reset (com.activision.callofduty.shooter)"));
    }

    @Test
    public void testVivoIqooStrategyFormatting() {
        VivoIqooHzStrategy strategy = new VivoIqooHzStrategy();
        assertEquals("Vivo OriginOS / iQOO Ultra Game Mode Hz Strategy", strategy.getStrategyName());

        String forceOutput = strategy.forceRefreshRate(120, "com.riotgames.league.wildrift");
        assertNotNull(forceOutput);
        assertTrue(forceOutput.contains("vivo_screen_refresh_rate"));
        assertTrue(forceOutput.contains("iqoo_game_fps_target"));
        assertTrue(forceOutput.contains("cmd window set-app-refresh-rate (com.riotgames.league.wildrift @ 120Hz)"));

        String resetOutput = strategy.resetRefreshRate("com.riotgames.league.wildrift");
        assertNotNull(resetOutput);
        assertTrue(resetOutput.contains("reset peak_refresh_rate"));
        assertTrue(resetOutput.contains("cmd game reset (com.riotgames.league.wildrift)"));
    }

    @Test
    public void testTranssionAndInfinixAndTecnoStrategies() {
        InfinixHzStrategy infinix = new InfinixHzStrategy();
        TecnoHzStrategy tecno = new TecnoHzStrategy();
        TranssionHzStrategy transsion = new TranssionHzStrategy();

        assertTrue(infinix.getStrategyName().contains("Infinix"));
        assertTrue(tecno.getStrategyName().contains("Tecno"));
        assertTrue(transsion.getStrategyName().contains("Transsion"));

        String infOutput = infinix.forceRefreshRate(120, "com.miHoYo.GenshinImpact");
        assertTrue(infOutput.contains("infinix_refresh_rate_mode"));
        assertTrue(infOutput.contains("persist.sys.darlink.mode"));
        assertTrue(infOutput.contains("cmd window set-app-refresh-rate (com.miHoYo.GenshinImpact @ 120Hz)"));

        String tecOutput = tecno.forceRefreshRate(90, "com.miHoYo.GenshinImpact");
        assertTrue(tecOutput.contains("tecno_refresh_rate_mode"));
        assertTrue(tecOutput.contains("persist.sys.darlink.mode"));
        assertTrue(tecOutput.contains("cmd window set-app-refresh-rate (com.miHoYo.GenshinImpact @ 90Hz)"));
    }

    @Test
    public void testAsusRogAndRedMagicStrategies() {
        AsusRogHzStrategy rog = new AsusRogHzStrategy();
        RedMagicHzStrategy redmagic = new RedMagicHzStrategy();

        String rogOutput = rog.forceRefreshRate(165, "com.axlebolt.standoff2");
        assertTrue(rogOutput.contains("asus_gaming_mode"));
        assertTrue(rogOutput.contains("asus_refresh_rate"));
        assertTrue(rogOutput.contains("cmd window set-app-refresh-rate (com.axlebolt.standoff2 @ 165Hz)"));

        String redOutput = redmagic.forceRefreshRate(165, "com.axlebolt.standoff2");
        assertTrue(redOutput.contains("redmagic_game_mode"));
        assertTrue(redOutput.contains("nubia_refresh_rate"));
        assertTrue(redOutput.contains("cmd window set-app-refresh-rate (com.axlebolt.standoff2 @ 165Hz)"));
    }
}

