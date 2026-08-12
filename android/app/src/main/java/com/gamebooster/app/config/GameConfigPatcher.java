package com.gamebooster.app.config;

/**
 * Compatibility boundary for legacy game-file patch calls.
 *
 * <p>Modifying another app's graphics, FPS, HDR, input, or configuration files
 * is not a safe or supported way to improve online-game performance. It can
 * break updates and violate a game's rules, so this class intentionally does
 * not read, create, or write any third-party game file.</p>
 */
public final class GameConfigPatcher {
    private GameConfigPatcher() { }

    public static final class PatchResult {
        public final boolean success;
        public final String message;

        public PatchResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static PatchResult applyGameFpsPatch(String packageName, int targetFps) {
        return unavailable(packageName);
    }

    public static PatchResult applyCompetitivePatch(String packageName, int targetFps) {
        return unavailable(packageName);
    }

    private static PatchResult unavailable(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid package name");
        }
        return new PatchResult(false,
                "Game-file editing is disabled. Use the game's own graphics settings and this app's supported display/Game Mode request.");
    }
}
