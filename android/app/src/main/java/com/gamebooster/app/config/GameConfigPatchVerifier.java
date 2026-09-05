package com.gamebooster.app.config;

/**
 * GameConfigPatchVerifier — pure read-back verification helpers (Phase 2.3).
 *
 * After a config patch is written, the patcher reads the file back and asserts
 * the forced FPS and Ultra Extreme graphics values are actually present.
 * All methods here are pure so the decision logic is unit-testable on the JVM.
 */
public final class GameConfigPatchVerifier {

    public static final String CONFIRMED = "patch confirmed";
    public static final String UNVERIFIED = "written but unverified";

    private GameConfigPatchVerifier() {}

    private static final java.util.regex.Pattern FPS_ASSIGNMENT =
            java.util.regex.Pattern.compile("(?i)(fps|framerate|fpslimit)[^=:]{0,40}[=:]\\s*\"?\\s*([0-9]+)\\s*\"?");

    /**
     * Pure check: does this config file content assert {@code targetFps} bound to
     * an FPS / framerate key? Supports INI (FPS=185), CVar (+CVars=r.PUBGMaxFPS=185),
     * and JSON ("MaxFPS":185) line formats.
     */
    public static boolean verifyFpsInContent(String content, int targetFps) {
        if (content == null || targetFps <= 0) return false;
        String fps = String.valueOf(targetFps);
        for (String rawLine : content.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

            java.util.regex.Matcher matcher = FPS_ASSIGNMENT.matcher(line);
            if (matcher.find() && matcher.group(2).equals(fps)) return true;
        }
        return false;
    }

    /**
     * Pure check: does this config file assert Ultra Extreme Graphics, Damage Lock, or Aim Assist unlock?
     */
    public static boolean verifyUltraExtremeInContent(String content) {
        if (content == null || content.isEmpty()) return false;
        return content.contains("UltraExtreme=1")
                || content.contains("bUseUltraExtreme=True")
                || content.contains("GraphicsQuality=5")
                || content.contains("GraphicQuality=4")
                || content.contains("GraphicLevel=4")
                || content.contains("r.PUBGQualityLevel=4")
                || content.contains("r.MobileHDR=1")
                || content.contains("HDRMode=1")
                || content.contains("\"GraphicQuality\": 4")
                || content.contains("name=\"GraphicQuality\" value=\"4\"")
                || content.contains("DamageLockMax=1")
                || content.contains("AimAssistLockMax=1")
                || content.contains("HitboxMultiplier=")
                || content.contains("HitboxScale=")
                || content.contains("ZeroRecoil=1")
                || content.contains("AutoDragHeadshot=1")
                || content.contains("InstantGlooWall=1")
                || content.contains("AutoRetriLordTurtle=1")
                || content.contains("AutoSmiteObjective=1")
                || content.contains("UnlimitedEnergyMode=1")
                || content.contains("LingFastSword=1")
                || content.contains("FannyFastCable=1")
                || content.contains("MagicBulletAimbot=1")
                || content.contains("VulkanPipelineCache=1")
                || content.contains("TouchPollingRate=1000")
                || content.contains("HitRegSyncRate=1000");
    }

    /**
     * Pure check: verifies FPS, Ultra Extreme graphics, or competitive locks in file content.
     */
    public static boolean verifyPatchInContent(String content, int targetFps) {
        return verifyFpsInContent(content, targetFps) || verifyUltraExtremeInContent(content);
    }

    /**
     * Pure summary of a read-back pass: "patch confirmed", "partially confirmed",
     * or "written but unverified", with the verified/total counts.
     */
    public static String buildVerificationSummary(int verifiedFiles, int totalFiles) {
        if (totalFiles <= 0) {
            return UNVERIFIED + " (no config file could be read back)";
        }
        if (verifiedFiles <= 0) {
            return UNVERIFIED + " (read-back found no " + "FPS" + " value in "
                    + totalFiles + " files)";
        }
        if (verifiedFiles >= totalFiles) {
            return CONFIRMED + " (" + verifiedFiles + "/" + totalFiles + " files read-back verified)";
        }
        return "partially confirmed (" + verifiedFiles + "/" + totalFiles + " files read-back verified)";
    }

    /**
     * Pure: per-game compatibility note shown on the Games screen, or null when
     * the game family is not known to reset patched config files.
     */
    public static String getPatchCompatibilityNote(String packageName) {
        if (packageName == null) return null;
        String pkg = packageName.trim().toLowerCase();
        if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) {
            return "May reset config on update — re-apply after game update";
        }
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            return "May reset config on update — re-apply after game update";
        }
        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile")) {
            return "May reset config on update — re-apply after game update";
        }
        if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("hkrpg")
                || pkg.contains("nap") || pkg.contains("hoyoverse")) {
            return "Integrity check may revert patches — re-apply before play";
        }
        return null;
    }
}