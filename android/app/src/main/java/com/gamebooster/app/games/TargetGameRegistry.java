package com.gamebooster.app.games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * TargetGameRegistry — Canonical registry of target eSports titles & gaming platforms.
 * Used for game discovery, supported Android Game Mode requests, and manifest package queries.
 */
public final class TargetGameRegistry {

    private static final Set<String> TARGET_PACKAGES = new LinkedHashSet<>();

    static {
        // Mobile Legends: Bang Bang (MLBB)
        TARGET_PACKAGES.add("com.mobile.legends");
        TARGET_PACKAGES.add("com.mobile.legends.vng");
        TARGET_PACKAGES.add("com.mobile.legends.kr");
        TARGET_PACKAGES.add("com.mobile.legends.jp");
        TARGET_PACKAGES.add("com.mobilelegends.win");

        // PUBG Mobile & BGMI & New State
        TARGET_PACKAGES.add("com.tencent.ig");
        TARGET_PACKAGES.add("com.pubg.imobile");
        TARGET_PACKAGES.add("com.pubg.krmobile");
        TARGET_PACKAGES.add("com.vng.pubgmobile");
        TARGET_PACKAGES.add("com.tencent.iglite");
        TARGET_PACKAGES.add("com.pubg.newstate");

        // Call of Duty: Mobile (CODM) & Warzone
        TARGET_PACKAGES.add("com.activision.callofduty.shooter");
        TARGET_PACKAGES.add("com.garena.game.codm");
        TARGET_PACKAGES.add("com.vng.codmvn");
        TARGET_PACKAGES.add("com.activision.callofduty.warzone");

        // Honor of Kings (HOK) / Arena of Valor (AOV)
        TARGET_PACKAGES.add("com.levelinfinite.sgameGlobal");
        TARGET_PACKAGES.add("com.tencent.tmgp.sgame");
        TARGET_PACKAGES.add("com.garena.game.kgtw");
        TARGET_PACKAGES.add("com.garena.game.kgvn");

        // HoYoverse & Kuro Games (Genshin, Star Rail, ZZZ, WuWa)
        TARGET_PACKAGES.add("com.miHoYo.GenshinImpact");
        TARGET_PACKAGES.add("com.cognosphere.GenshinImpact");
        TARGET_PACKAGES.add("com.HoYoverse.hkrpgoversea");
        TARGET_PACKAGES.add("com.miHoYo.hkrpg");
        TARGET_PACKAGES.add("com.HoYoverse.nap");
        TARGET_PACKAGES.add("com.kurogame.wutheringwaves.global");

        // Free Fire, Wild Rift, Delta Force, Blood Strike, Standoff 2, Farlight 84, Roblox
        TARGET_PACKAGES.add("com.dts.freefireth");
        TARGET_PACKAGES.add("com.dts.freefiremax");
        TARGET_PACKAGES.add("com.riotgames.league.wildrift");
        TARGET_PACKAGES.add("com.proxima.deltaforce");
        TARGET_PACKAGES.add("com.tencent.dfm");
        TARGET_PACKAGES.add("com.ofg.bloodstrike");
        TARGET_PACKAGES.add("com.netease.bloodstrike");
        TARGET_PACKAGES.add("com.axlebolt.standoff2");
        TARGET_PACKAGES.add("com.miracle.farlight84");
        TARGET_PACKAGES.add("com.roblox.client");

        // Gaming Platforms & App Stores
        TARGET_PACKAGES.add("com.taptap.global");
        TARGET_PACKAGES.add("com.taptap");
        TARGET_PACKAGES.add("com.garena.appstore");
        TARGET_PACKAGES.add("com.sec.android.app.samsungapps");
        TARGET_PACKAGES.add("com.apkpure.aether");
        TARGET_PACKAGES.add("com.qooapp.qoohelper");
    }

    private TargetGameRegistry() {}

    /**
     * Returns an unmodifiable list of all registered target package names.
     */
    public static List<String> getAllPackages() {
        return Collections.unmodifiableList(new ArrayList<>(TARGET_PACKAGES));
    }

    /**
     * Checks if a package name is a registered target game or platform.
     */
    public static boolean isTargetPackage(String packageName) {
        return packageName != null && TARGET_PACKAGES.contains(packageName);
    }
}
