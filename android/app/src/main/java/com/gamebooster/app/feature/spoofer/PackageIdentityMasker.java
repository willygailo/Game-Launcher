package com.gamebooster.app.feature.spoofer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * PackageIdentityMasker — Privacy-preserving package identity obfuscation & alias mapping engine.
 *
 * Generates deterministic, pseudo-anonymous package aliases (e.g., `pkg_alias_707562676d`)
 * to hide raw game package identities from launcher telemetry and external logging overlays.
 */
public class PackageIdentityMasker {

    private static final String ALIAS_PREFIX = "pkg_alias_";
    private static final Map<String, String> PACKAGE_TO_ALIAS = new HashMap<>();
    private static final Map<String, String> ALIAS_TO_PACKAGE = new HashMap<>();

    static {
        // Mobile Legends: Bang Bang (MLBB) & Regional Variants
        registerAlias("com.mobile.legends", "pkg_alias_mlbb_global");
        registerAlias("com.mobile.legends.vng", "pkg_alias_mlbb_vng");
        registerAlias("com.mobile.legends.kr", "pkg_alias_mlbb_kr");
        registerAlias("com.mobile.legends.jp", "pkg_alias_mlbb_jp");

        // PUBG Mobile & Regional Variants & New State
        registerAlias("com.tencent.ig", "pkg_alias_pubgm_global");
        registerAlias("com.pubg.krmobile", "pkg_alias_pubgm_kr");
        registerAlias("com.vng.pubgmobile", "pkg_alias_pubgm_vng");
        registerAlias("com.pubg.imobile", "pkg_alias_bgmi_india");
        registerAlias("com.tencent.iglite", "pkg_alias_pubgm_lite");
        registerAlias("com.pubg.newstate", "pkg_alias_pubg_newstate");

        // Call of Duty: Mobile & Warzone
        registerAlias("com.garena.game.codm", "pkg_alias_codm_garena");
        registerAlias("com.activision.callofduty.shooter", "pkg_alias_codm_global");
        registerAlias("com.vng.codmvn", "pkg_alias_codm_vng");
        registerAlias("com.activision.callofduty.warzone", "pkg_alias_cod_warzone");

        // Honor of Kings & Arena of Valor
        registerAlias("com.levelinfinite.sgameGlobal", "pkg_alias_hok_global");
        registerAlias("com.tencent.tmgp.sgame", "pkg_alias_hok_cn");
        registerAlias("com.garena.game.kgtw", "pkg_alias_aov_tw");
        registerAlias("com.garena.game.kgvn", "pkg_alias_aov_vn");

        // HoYoverse & Kuro Games
        registerAlias("com.miHoYo.GenshinImpact", "pkg_alias_genshin_cn");
        registerAlias("com.cognosphere.GenshinImpact", "pkg_alias_genshin_global");
        registerAlias("com.HoYoverse.hkrpgoversea", "pkg_alias_star_rail");
        registerAlias("com.HoYoverse.nap", "pkg_alias_zenless_zone_zero");
        registerAlias("com.kurogame.wutheringwaves.global", "pkg_alias_wuthering_waves");

        // Free Fire, Wild Rift, Delta Force, Blood Strike, Standoff 2, Farlight 84, Roblox
        registerAlias("com.dts.freefireth", "pkg_alias_freefire_std");
        registerAlias("com.dts.freefiremax", "pkg_alias_freefire_max");
        registerAlias("com.riotgames.league.wildrift", "pkg_alias_wild_rift");
        registerAlias("com.tencent.dfm", "pkg_alias_delta_force");
        registerAlias("com.proxima.deltaforce", "pkg_alias_delta_force_global");
        registerAlias("com.ofg.bloodstrike", "pkg_alias_blood_strike");
        registerAlias("com.netease.bloodstrike", "pkg_alias_blood_strike_netease");
        registerAlias("com.axlebolt.standoff2", "pkg_alias_standoff2");
        registerAlias("com.miracle.farlight84", "pkg_alias_farlight84");
        registerAlias("com.roblox.client", "pkg_alias_roblox");
    }

    private static synchronized void registerAlias(String pkg, String alias) {
        PACKAGE_TO_ALIAS.put(pkg, alias);
        ALIAS_TO_PACKAGE.put(alias, pkg);
    }

    /**
     * Generates or retrieves a deterministic masked package alias for the given raw package name.
     *
     * @param rawPackage Raw Android package identifier.
     * @return Obfuscated package alias string.
     */
    public static synchronized String maskPackageName(String rawPackage) {
        if (rawPackage == null || rawPackage.trim().isEmpty()) {
            return ALIAS_PREFIX + "unknown";
        }

        if (PACKAGE_TO_ALIAS.containsKey(rawPackage)) {
            return PACKAGE_TO_ALIAS.get(rawPackage);
        }

        // Generate deterministic SHA-256 derived hex alias
        String generatedAlias = generateHashAlias(rawPackage);
        registerAlias(rawPackage, generatedAlias);
        return generatedAlias;
    }

    /**
     * Unmasks an obfuscated package alias back to its raw Android package name.
     *
     * @param alias Obfuscated package alias string.
     * @return Raw package name, or original input if unmapped.
     */
    public static synchronized String unmaskAlias(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return "";
        }

        if (ALIAS_TO_PACKAGE.containsKey(alias)) {
            return ALIAS_TO_PACKAGE.get(alias);
        }

        return alias;
    }

    /**
     * Checks whether an input string is a formatted package alias.
     */
    public static boolean isMaskedAlias(String input) {
        return input != null && input.startsWith(ALIAS_PREFIX);
    }

    /**
     * Returns an unmodifiable copy of the active package alias map.
     */
    public static synchronized Map<String, String> getActiveAliasMap() {
        return Collections.unmodifiableMap(new HashMap<>(PACKAGE_TO_ALIAS));
    }

    private static String generateHashAlias(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return ALIAS_PREFIX + hexString.toString();
        } catch (Throwable t) {
            return ALIAS_PREFIX + Math.abs(input.hashCode());
        }
    }
}
