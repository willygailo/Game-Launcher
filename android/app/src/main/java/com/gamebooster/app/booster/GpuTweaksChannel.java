package com.gamebooster.app.booster;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Universal GPU & ANGLE Vulkan Driver Engine for Game Launcher PRO.
 * Enforces hardware Vulkan 3D HWUI rendering, Google ANGLE translation layer,
 * and System Game Driver opt-in across all competitive games.
 */
public class GpuTweaksChannel {

    private static final String TAG = "GpuTweaksChannel";

    public static final String TARGET_GAMES_PACKAGES =
            "com.mobile.legends,com.mobilelegends.mi,com.vng.mlbbvn,com.mobilelegends.na,com.mobilelegends.hw,com.mobile.legends.moonton,com.mobile.legends.kr,com.mobile.legends.jp," +
            "com.tencent.ig,com.pubg.imobile,com.vng.pubgmobile,com.pubg.krmobile,com.rekoo.pubgm,com.tencent.tmgp.pubgmhd,com.tencent.iglite,com.pubg.newstate,com.tencent.tmgp.pubgm," +
            "com.activision.callofduty.shooter,com.garena.game.codm,com.tencent.tmgp.kr.codm,com.vng.codmvn,com.tencent.tmgp.cod," +
            "com.dts.freefireth,com.dts.freefiremax," +
            "com.miHoYo.GenshinImpact,com.cognosphere.GenshinImpact,com.HoYoverse.hkrpgoversea,com.HoYoverse.nap,com.miHoYo.bh3oversea," +
            "com.levelinfinite.sgameGlobal,com.levelinfinite.sgameGlobal.gpkg,com.tencent.tmgp.sgame,com.garena.game.kgtw,com.garena.game.kgvn,com.garena.game.kgid,com.riotgames.league.wildrift," +
            "com.roblox.client,com.proximabeta.mf.uamo,com.kurogame.wutheringwaves.global,com.carxtech.sr,com.ea.gp.apexlegendsmobilecms,com.riotgames.league.teamfighttactics," +
            "com.miracle.farlight84,com.farlightgames.farlight84.gp,com.farlightgames.farlight84.global,com.tencent.tmgp.projectc,com.riotgames.valorantmobile,com.axlebolt.standoff2,com.netease.bloodstrike";

    private static void exec(String cmd) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
        }
    }

    public static boolean enableVulkanRenderer() {
        exec("setprop debug.hwui.renderer vulkan");
        exec("setprop debug.sf.hw 1");
        exec("setprop debug.sf.latch_unsignaled 1");
        exec("setprop debug.renderengine.backend skiagl");
        return true;
    }

    public static boolean enableForceMsaa() {
        exec("setprop debug.egl.force_msaa 1");
        return true;
    }

    public static boolean setGpuMaxPerformance() {
        boolean ok = enableVulkanRenderer();
        ok &= enableForceMsaa();
        return ok;
    }

    public static String buildAllGamesPackageList(Context context) {
        Set<String> pkgs = new HashSet<>();
        for (String p : TARGET_GAMES_PACKAGES.split(",")) {
            if (!p.trim().isEmpty()) pkgs.add(p.trim());
        }
        for (String p : GamePackageRegistry.getAllKnownGames().keySet()) {
            if (!p.trim().isEmpty()) pkgs.add(p.trim());
        }

        if (context != null) {
            try {
                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> installed = pm.getInstalledApplications(0);
                for (ApplicationInfo app : installed) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && app.category == ApplicationInfo.CATEGORY_GAME) {
                        pkgs.add(app.packageName);
                    } else if ((app.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                        pkgs.add(app.packageName);
                    }
                }
            } catch (Throwable ignored) {}
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String p : pkgs) {
            if (count > 0) sb.append(",");
            sb.append(p);
            count++;
        }
        return sb.toString();
    }

    public static boolean setAngleMode(boolean enabled) {
        return setAngleMode(null, enabled);
    }

    public static boolean setAngleMode(Context context, boolean enabled) {
        String packageList = buildAllGamesPackageList(context);
        if (enabled) {
            exec("settings put global angle_gl_driver_all_angle 0");
            exec("setprop debug.angle.backend 2");
            exec("settings put global angle_enabled_pkgs 1");
            exec("settings put global angle_gl_driver_selection_pkgs " + packageList);

            String[] pkgs = packageList.split(",");
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < pkgs.length; i++) {
                if (i > 0) values.append(",");
                values.append("angle");
            }
            exec("settings put global angle_gl_driver_selection_values " + values.toString());
            Log.i(TAG, "⚡ Google ANGLE Vulkan Driver enabled for " + pkgs.length + " games");
            return true;
        } else {
            exec("settings put global angle_gl_driver_all_angle 0");
            exec("setprop debug.angle.backend 0");
            exec("settings put global angle_enabled_pkgs 0");
            exec("settings put global angle_gl_driver_selection_pkgs \"\"");
            exec("settings put global angle_gl_driver_selection_values \"\"");
            Log.i(TAG, "ANGLE Driver disabled");
            return true;
        }
    }

    public static boolean setGameDriverMode(boolean enabled) {
        return setGameDriverMode(null, enabled);
    }

    public static boolean setGameDriverMode(Context context, boolean enabled) {
        String packageList = buildAllGamesPackageList(context);
        if (enabled) {
            exec("settings put global game_driver_all_apps 0");
            exec("settings put global updatable_driver_all_apps 0");
            exec("settings put global game_driver_opt_in_apps " + packageList);
            exec("settings put global game_driver_prerelease_opt_in_apps " + packageList);
            exec("settings put global updatable_driver_production_opt_in_apps " + packageList);
            Log.i(TAG, "🎮 System Game Driver enabled for " + packageList.split(",").length + " games");
            return true;
        } else {
            exec("settings put global game_driver_all_apps 0");
            exec("settings put global updatable_driver_all_apps 0");
            exec("settings put global game_driver_opt_in_apps \"\"");
            exec("settings put global game_driver_prerelease_opt_in_apps \"\"");
            exec("settings put global updatable_driver_production_opt_in_apps \"\"");
            Log.i(TAG, "Game Driver disabled");
            return true;
        }
    }
}
