package com.gamebooster.app.spoofer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * GameSpoofSafetyRegistry — per-game anti-cheat risk tiers (Phase 2.5).
 *
 * Classifies known titles so the spoof engine applies the right level of
 * pre-apply safety checking per game:
 *
 * - HIGH_RISK   : kernel-level anti-cheat (Tencent ACE and peers) — GL vendor /
 *                 SoC swaps are provably detectable; a feature-set mismatch
 *                 blocks the apply to protect the account.
 * - MEDIUM_RISK : soft / server-side anti-cheat — mismatch warns but applies.
 * - LOW_RISK    : no meaningful anti-cheat — mismatch warns but applies.
 *
 * Unknown or null packages default to LOW_RISK, so a user-selected profile in
 * the settings screen always applies (with a warning) instead of being
 * hard-blocked on non-Snapdragon devices — this was the root cause of
 * "no success spoof application set apply" on MediaTek / Exynos hardware.
 */
public final class GameSpoofSafetyRegistry {

    public enum RiskTier { HIGH_RISK, MEDIUM_RISK, LOW_RISK }

    // Tencent ACE (kernel-level) protected titles: strictest tier.
    private static final Set<String> HIGH_RISK_PATTERNS = new HashSet<>(Arrays.asList(
            // PUBG Mobile family
            "tencent.ig", "tencent.iglite", "tmgp.pubgm", "vng.pubgmobile", "pubgm",
            // Call of Duty Mobile / Warzone
            "callofduty", "activision", "tmgp.cod", "warzone",
            // Honor of Kings
            "tmgp.sgame", "sgameglobal",
            // Valorant Mobile / Project C
            "tmgp.projectc", "tmgp.valorant",
            // Arena Breakout / Delta Force
            "proximabeta.mf.uamo", "levelinfinite.deltaforce"
    ));

    // Soft / server-side anti-cheat titles: warn on mismatch, still apply.
    private static final Set<String> MEDIUM_RISK_PATTERNS = new HashSet<>(Arrays.asList(
            // Mobile Legends
            "mobile.legends", "mobilelegends", "moonton",
            // Free Fire
            "freefire", "dts.freefire",
            // Standoff 2
            "standoff2", "axlebolt",
            // Wild Rift
            "wildrift", "riotgames.league",
            // Blood Strike / NewSpike
            "bloodstrike", "newspike",
            // Farlight 84
            "farlight84"
    ));

    private GameSpoofSafetyRegistry() {}

    /**
     * Resolves the anti-cheat risk tier for a game package.
     * Null or unknown packages are LOW_RISK (permissive-with-warning).
     */
    public static RiskTier riskTierFor(String packageName) {
        if (packageName == null) return RiskTier.LOW_RISK;
        String pkg = packageName.toLowerCase();
        for (String pattern : HIGH_RISK_PATTERNS) {
            if (pkg.contains(pattern)) return RiskTier.HIGH_RISK;
        }
        for (String pattern : MEDIUM_RISK_PATTERNS) {
            if (pkg.contains(pattern)) return RiskTier.MEDIUM_RISK;
        }
        return RiskTier.LOW_RISK;
    }

    /**
     * True when the package matches a known game title (any tier) or is the
     * launcher itself. Used to gate the spoof ContentProvider so arbitrary
     * third-party apps (e.g. device fingerprinting / anti-cheat scanners)
     * cannot read the spoof configuration.
     */
    public static boolean isTrustedConfigReader(String packageName) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase();
        if (pkg.equals("com.gamebooster.app")) return true;
        return riskTierFor(pkg) != RiskTier.LOW_RISK;
    }

    public static String describe(RiskTier tier) {
        if (tier == null) return "unknown";
        switch (tier) {
            case HIGH_RISK: return "kernel-level anti-cheat (strict block on mismatch)";
            case MEDIUM_RISK: return "soft anti-cheat (warn on mismatch)";
            default: return "no meaningful anti-cheat (warn on mismatch)";
        }
    }
}
