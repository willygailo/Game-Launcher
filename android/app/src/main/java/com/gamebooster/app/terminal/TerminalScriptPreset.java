package com.gamebooster.app.terminal;

/**
 * Model representing a pre-configured shell tweak script or diagnostic command
 * compatible with Android 13, 14, 15, and 16 (API levels 33 to 36).
 */
public class TerminalScriptPreset {

    private final String id;
    private final String title;
    private final String description;
    private final String command;

    public TerminalScriptPreset(String id, String title, String description, String command) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCommand() {
        return command;
    }
}
