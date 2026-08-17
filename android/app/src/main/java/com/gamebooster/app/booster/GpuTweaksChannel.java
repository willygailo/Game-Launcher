package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;

public class GpuTweaksChannel {

    public static boolean enableVulkanRenderer() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.hwui.renderer", "vulkan");
        ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "1");
        ok &= CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "1");
        return ok;
    }

    public static boolean enableForceMsaa() {
        return CommandExecutor.setSystemProperty("debug.egl.force_msaa", "1");
    }

    public static boolean setGpuMaxPerformance() {
        boolean ok = enableVulkanRenderer();
        ok &= enableForceMsaa();
        return ok;
    }

    public static final String TARGET_GAMES_PACKAGES =
            "com.mobile.legends,com.mobilelegends.mi,com.vng.mlbbvn,com.mobilelegends.na,com.mobilelegends.hw,com.mobile.legends.moonton,com.mobile.legends.kr,com.mobile.legends.jp," +
            "com.tencent.ig,com.pubg.imobile,com.vng.pubgmobile,com.pubg.krmobile,com.rekoo.pubgm,com.tencent.tmgp.pubgmhd,com.tencent.iglite,com.pubg.newstate," +
            "com.activision.callofduty.shooter,com.garena.game.codm,com.tencent.tmgp.kr.codm,com.vng.codmvn,com.tencent.tmgp.cod," +
            "com.dts.freefireth,com.dts.freefiremax," +
            "com.miHoYo.GenshinImpact,com.cognosphere.GenshinImpact,com.HoYoverse.hkrpgoversea,com.HoYoverse.nap,com.miHoYo.bh3oversea," +
            "com.levelinfinite.sgameGlobal,com.levelinfinite.sgameGlobal.gpkg,com.tencent.tmgp.sgame,com.garena.game.kgtw,com.garena.game.kgvn,com.garena.game.kgid,com.riotgames.league.wildrift," +
            "com.roblox.client,com.proximabeta.mf.uamo,com.kurogame.wutheringwaves.global,com.carxtech.sr,com.ea.gp.apexlegendsmobilecms,com.riotgames.league.teamfighttactics,com.miracle.farlight84,com.tencent.tmgp.projectc";

    public static boolean setAngleMode(boolean enabled) {
        if (enabled) {
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 0");
            CommandExecutor.setSystemProperty("debug.angle.backend", "2");
            CommandExecutor.executeSystemCommand("settings put global angle_enabled_pkgs 1");
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_selection_pkgs " + TARGET_GAMES_PACKAGES);

            // Generate "angle,angle,angle..." for each package
            String[] pkgs = TARGET_GAMES_PACKAGES.split(",");
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < pkgs.length; i++) {
                if (i > 0) values.append(",");
                values.append("angle");
            }
            String res = CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_selection_values " + values.toString());
            return CommandExecutor.isSuccessOutput(res);
        } else {
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 0");
            CommandExecutor.setSystemProperty("debug.angle.backend", "0");
            CommandExecutor.executeSystemCommand("settings put global angle_enabled_pkgs 0");
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_selection_pkgs \"\"");
            String res = CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_selection_values \"\"");
            return CommandExecutor.isSuccessOutput(res);
        }
    }

    public static boolean setGameDriverMode(boolean enabled) {
        if (enabled) {
            CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps " + TARGET_GAMES_PACKAGES);
            CommandExecutor.executeSystemCommand("settings put global game_driver_prerelease_opt_in_apps " + TARGET_GAMES_PACKAGES);
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps " + TARGET_GAMES_PACKAGES);
            return CommandExecutor.isSuccessOutput(res);
        } else {
            CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps \"\"");
            CommandExecutor.executeSystemCommand("settings put global game_driver_prerelease_opt_in_apps \"\"");
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps \"\"");
            return CommandExecutor.isSuccessOutput(res);
        }
    }
}
