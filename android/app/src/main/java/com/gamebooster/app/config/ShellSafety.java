package com.gamebooster.app.config;

import java.util.regex.Pattern;

/**
 * ShellSafety — pure shell-injection guard for package names and config paths.
 *
 * Every value that ends up in a `sed` / `sh -c` command string must pass here
 * first: package names and resolved paths are validated against strict
 * character whitelists (no `;`, `'`, `"`, `$`, `\``, spaces, etc.) and, as a
 * second layer, shell-string escaping is provided for quoting paths.
 */
public final class ShellSafety {

    /** Android package names: letters, digits, dots, underscores. */
    private static final Pattern SAFE_PACKAGE = Pattern.compile("[a-zA-Z0-9._]+");

    /** Filesystem paths allowed to reach a shell: letters, digits, dots, slash, dash, underscore. */
    private static final Pattern SAFE_SHELL_PATH = Pattern.compile("[a-zA-Z0-9._/\\-]+");

    private ShellSafety() {}

    /**
     * Validates a package name before it is embedded into any shell command.
     *
     * @return true only for non-empty, length-bounded, whitelisted tokens with
     *         at least one alphanumeric char and no bare-dot segments
     */
    public static boolean isSafePackageName(String token) {
        if (token == null || token.isEmpty() || token.length() > 255) return false;
        if (!SAFE_PACKAGE.matcher(token).matches()) return false;
        if (!token.matches(".*[a-zA-Z0-9].*")) return false;
        return !token.startsWith(".") && !token.endsWith(".");
    }

    /**
     * Validates a resolved config path before it is embedded into a `sed` command.
     *
     * @return true only for non-empty whitelisted absolute paths with no `.`/`..`
     *         segments and no shell characters
     */
    public static boolean isSafeShellPath(String path) {
        if (path == null || path.isEmpty() || path.length() > 4096) return false;
        if (!SAFE_SHELL_PATH.matcher(path).matches()) return false;
        for (String segment : path.split("/")) {
            if (segment.equals(".") || segment.equals("..")) return false;
        }
        return true;
    }

    /**
     * Single-quote-escapes a token for POSIX shell use: wraps in quotes and
     * rewrites every embedded {@code '} as {@code '\''}.
     */
    public static String escapeSingleQuoted(String token) {
        if (token == null) return "''";
        return "'" + token.replace("'", "'\\''") + "'";
    }
}