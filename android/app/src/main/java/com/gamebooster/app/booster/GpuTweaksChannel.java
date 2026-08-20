package com.gamebooster.app.booster;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.LinkedHashSet;
import java.util.Set;

public class GpuTweaksChannel {

    public static final String TARGET_GAMES_PACKAGES =
            "com.mobile.legends,com.mobilelegends.mi,com.vng.mlbbvn,com.mobilelegends.na,com.mobilelegends.hw,com.mobile.legends.moonton,com.mobile.legends.kr,com.mobile.legends.jp," +
            "com.tencent.ig,com.pubg.imobile,com.vng.pubgmobile,com.pubg.krmobile,com.rekoo.pubgm,com.tencent.tmgp.pubgmhd,com.tencent.iglite,com.pubg.newstate,com.tencent.tmgp.pubgm," +
            "com.activision.callofduty.shooter,com.garena.game.codm,com.tencent.tmgp.kr.codm,com.vng.codmvn,com.tencent.tmgp.cod,com.activision.callofduty.warzone," +
            "com.dts.freefireth,com.dts.freefiremax," +
            "com.miHoYo.GenshinImpact,com.cognosphere.GenshinImpact,com.HoYoverse.hkrpgoversea,com.HoYoverse.nap,com.miHoYo.bh3oversea,com.kurogame.wutheringwaves.global," +
            "com.levelinfinite.sgameGlobal,com.levelinfinite.sgameGlobal.gpkg,com.tencent.tmgp.sgame,com.garena.game.kgtw,com.garena.game.kgvn,com.garena.game.kgid,com.riotgames.league.wildrift," +
            "com.roblox.client,com.riotgames.valorant.mobile,com.riotgames.valorantmobile,com.tencent.tmgp.projectc,com.farlightgames.farlight84.android,com.miracle.farlight84," +
            "com.netease.bloodstrike,com.netease.newspike,com.axlebolt.standoff2," +
            "com.h20.carxstreet,com.gameloft.anmp.android.glofta9hm,com.ea.games.r3_row,com.garena.game.fdtw," +
            "com.proximabeta.mf.uamo,com.levelinfinite.deltaforce," +
            "com.supercell.brawlstars,com.supercell.clashroyale,com.supercell.clashofclans,com.supercell.squad";

    /**
     * Dynamically compiles the complete CSV of target game packages from GamePackageRegistry.
     */
    public static String getTargetGamesCsv() {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(GamePackageRegistry.getAllKnownGames().keySet());
        for (String p : TARGET_GAMES_PACKAGES.split(",")) {
            String clean = p.trim();
            if (!clean.isEmpty()) set.add(clean);
        }
        return String.join(",", set);
    }

    public static boolean enableVulkanRenderer() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.hwui.renderer", "vulkan");
        ok &= CommandExecutor.setSystemProperty("debug.renderengine.backend", "vulkan");
        ok &= CommandExecutor.setSystemProperty("debug.renderengine.skia_pipeline", "true");
        ok &= CommandExecutor.setSystemProperty("debug.hwui.use_gpu_pixel_buffers", "true");
        ok &= CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "-20");
        ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "1");
        ok &= CommandExecutor.setSystemProperty("debug.sf.latch_unsignaled", "1");

        // Apply Vulkan Game Overlay to all registered games on Android 13+
        for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
            try {
                CommandExecutor.executeSystemCommand("device_config put game_overlay " + pkg + " mode=2,useAngle=true,fps=185:mode=3,useAngle=true,fps=185");
            } catch (Throwable ignored) {}
        }
        return ok;
    }

    public static boolean enableAdrenoTurbo() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.adreno.turbo", "1");
        ok &= CommandExecutor.setSystemProperty("debug.adreno.perf_level", "0");
        ok &= CommandExecutor.setSystemProperty("debug.qualcomm.sns.hal", "1");
        return ok;
    }

    public static boolean enableMaliBoost() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("debug.mali.sched.priority", "-20");
        ok &= CommandExecutor.setSystemProperty("debug.mali.force_gpu_boost", "1");
        return ok;
    }

    public static boolean enableForceMsaa() {
        return CommandExecutor.setSystemProperty("debug.egl.force_msaa", "1");
    }

    public static boolean setGpuMaxPerformance() {
        boolean ok = enableVulkanRenderer();
        ok &= enableAdrenoTurbo();
        ok &= enableMaliBoost();
        ok &= enableForceMsaa();
        return ok;
    }

    public static boolean setAngleMode(boolean enabled) {
        String targetCsv = getTargetGamesCsv();
        if (enabled) {
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 0");
            CommandExecutor.setSystemProperty("debug.angle.backend", "2");
            CommandExecutor.executeSystemCommand("settings put global angle_enabled_pkgs 1");
            CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_selection_pkgs " + targetCsv);

            String[] pkgs = targetCsv.split(",");
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
        String targetCsv = getTargetGamesCsv();
        if (enabled) {
            CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global updatable_driver_all_apps 0");
            CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps " + targetCsv);
            CommandExecutor.executeSystemCommand("settings put global game_driver_prerelease_opt_in_apps " + targetCsv);
            String res = CommandExecutor.executeSystemCommand("settings put global updatable_driver_production_opt_in_apps " + targetCsv);
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
