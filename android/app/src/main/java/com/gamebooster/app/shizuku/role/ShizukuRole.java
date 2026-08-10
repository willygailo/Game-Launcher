package com.gamebooster.app.shizuku.role;

/**
 * ShizukuRole — Defines privilege levels for the Game Booster Shizuku bridge.
 *
 * <ul>
 *   <li>{@link #ADMIN}    — Full access: Force Apply, setprop, file bridge, permission grants,
 *                           terminal execution, spoofer profiles. Requires Shizuku permission.</li>
 *   <li>{@link #USER}     — Standard access: Apply tweaks, launch games, view/select spoofer
 *                           profiles, toggle Hz/FPS, view stats. Cannot execute raw terminal
 *                           commands or apply Force Apply.</li>
 *   <li>{@link #READONLY} — View-only: Inspect device info, view applied profiles, read terminal
 *                           output. Cannot execute any write commands.</li>
 * </ul>
 */
public enum ShizukuRole {

    /** Full system-level privileged access. All features enabled. */
    ADMIN("Admin", "Full Shizuku privileged access — all features unlocked", "⚡"),

    /** Standard user access. Tweaks and game launch only. */
    USER("User", "Standard access — tweaks, profiles and game launch", "👤"),

    /** View-only mode. No command execution. */
    READONLY("Read-Only", "View-only — no command execution", "👁️");

    // -----------------------------------------------------------------------------------------

    private final String displayName;
    private final String description;
    private final String emoji;

    ShizukuRole(String displayName, String description, String emoji) {
        this.displayName = displayName;
        this.description = description;
        this.emoji = emoji;
    }

    /** Human-readable name shown in UI. */
    public String getDisplayName() {
        return displayName;
    }

    /** Short description shown in UI role selector. */
    public String getDescription() {
        return description;
    }

    /** Emoji icon associated with the role. */
    public String getEmoji() {
        return emoji;
    }

    /** Returns true if this role can execute privileged Shizuku commands. */
    public boolean canExecuteCommands() {
        return this == ADMIN;
    }

    /** Returns true if this role can apply tweaks and modify game settings. */
    public boolean canApplyTweaks() {
        return this == ADMIN || this == USER;
    }

    /** Returns true if this role can read/view device info and profiles. */
    public boolean canReadData() {
        return true; // All roles can read
    }

    /** Returns true if this role can use Force Apply engine. */
    public boolean canForceApply() {
        return this == ADMIN;
    }

    /** Returns true if this role can access the terminal and execute raw shell commands. */
    public boolean canUseTerminal() {
        return this == ADMIN;
    }

    /** Returns true if this role can modify spoofer profiles. */
    public boolean canModifySpoofProfiles() {
        return this == ADMIN || this == USER;
    }

    /**
     * Returns the ShizukuRole from a stored string preference value.
     * Defaults to USER if value is null or unrecognized.
     */
    public static ShizukuRole fromString(String value) {
        if (value == null) return USER;
        switch (value.toUpperCase()) {
            case "ADMIN":    return ADMIN;
            case "READONLY": return READONLY;
            default:         return USER;
        }
    }
}
