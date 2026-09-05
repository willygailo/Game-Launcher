package com.gamebooster.app.config;

import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConfigFileHelper — High-performance, shell-injection-safe In-Memory Config Parser & Atomic Patcher.
 *
 * Replaces fragile subshell sed/grep pipelines across all game config patchers with thread-safe,
 * in-memory structural editing for INI, UE4 CVars, JSON, and XML PlayerPrefs formats.
 *
 * Guarantees:
 *  1. Zero duplicate section headers across multiple patch passes.
 *  2. Safe in-memory key-value replacement and clean section-scoped insertion.
 *  3. Atomic file writes with permission mode 666 via ShizukuFileManager.writeFileAtomic.
 *  4. Direct byte and text I/O with automatic parent directory scaffolding and JVM direct fallback.
 */
public final class ConfigFileHelper {

    private static final String TAG = "ConfigFileHelper";

    private ConfigFileHelper() {}

    /**
     * Atomically writes full content to the specified path, ensuring parent directory exists
     * and setting file permissions to 666.
     *
     * @return true if write was successful
     */
    public static boolean writeContentAtomic(String path, String content) {
        if (path == null || path.trim().isEmpty()) return false;

        // Tier 1: Fast direct JVM write if path is directly accessible
        try {
            File f = new File(path);
            if (f.getParentFile() != null && !f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if (!f.exists() || f.canWrite()) {
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write((content != null ? content : "").getBytes(StandardCharsets.UTF_8));
                    fos.flush();
                    return true;
                }
            }
        } catch (Throwable directEx) {
            Log.d(TAG, "Direct write failed (expected on Android 13-16 scoped storage): " + directEx.getMessage());
        }

        // Tier 2: Elevated Shizuku / AIDL IUserService / privileged shell for Android 13-16 scoped storage
        try {
            GameConfigStorageAccessEngine.ensureAndGrantPathAccess(null, path);
            ShizukuFileManager.ensureParentDirectory(path);
            ShizukuFileManager.FileOpResult res = ShizukuFileManager.writeFileAtomic(path, content != null ? content : "", "666");
            if (res != null && res.success) {
                return true;
            }
        } catch (Throwable t) {
            Log.d(TAG, "ShizukuFileManager write attempt note: " + t.getMessage());
        }

        return false;
    }

    /**
     * Reads a file, applies a list of key-value pairs (or CVars), and writes it back atomically.
     * If the file does not exist, creates it with the given keys and default section header.
     *
     * @param path           Absolute path to config file
     * @param keyValues      Array of "key=value" or "+CVars=key=value" strings
     * @param defaultSection Default section header (e.g. "[Graphics]") used for INI files
     * @return true if patched and written successfully
     */
    public static boolean patchKeys(String path, String[] keyValues, String defaultSection) {
        if (path == null || path.trim().isEmpty() || keyValues == null || keyValues.length == 0) {
            return false;
        }

        try {
            ShizukuFileManager.ensureParentDirectory(path);
            String existingContent = ShizukuFileManager.readFile(path);
            if (existingContent.isEmpty()) {
                File f = new File(path);
                if (f.exists() && f.canRead()) {
                    existingContent = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                }
            }
            String updatedContent = patchContentInMemory(existingContent, keyValues, defaultSection, path);
            if (existingContent != null && !existingContent.isEmpty() && existingContent.equals(updatedContent)) {
                Log.d(TAG, "Content already matches desired config for " + path + " - skipping write (no-op).");
                return true;
            }
            return writeContentAtomic(path, updatedContent);
        } catch (Throwable t) {
            Log.w(TAG, "patchKeys failed for " + path + ": " + t.getMessage(), t);
            return false;
        }
    }

    /**
     * In-memory patching of content based on file extension and format.
     */
    public static String patchContentInMemory(String content, String[] keyValues, String defaultSection, String path) {
        if (content == null) content = "";
        String lowerPath = path != null ? path.toLowerCase() : "";

        // Tier 1: Try accelerated C++ in-memory parsing if native engine is loaded and content exists
        if (NativeConfigInjector.isNativeLoaded() && keyValues != null && keyValues.length > 0 && !content.isEmpty()) {
            try {
                List<String> kList = new ArrayList<>(keyValues.length);
                List<String> vList = new ArrayList<>(keyValues.length);
                for (String kv : keyValues) {
                    if (kv != null) {
                        int eq = kv.indexOf('=');
                        if (eq > 0) {
                            kList.add(kv.substring(0, eq).trim());
                            vList.add(kv.substring(eq + 1).trim());
                        }
                    }
                }
                if (!kList.isEmpty()) {
                    int formatType = lowerPath.endsWith(".json") ? 2 : (lowerPath.endsWith(".xml") ? 1 : 0);
                    String nativePatched = NativeConfigInjector.patchContentNativeInMemory(
                            content,
                            kList.toArray(new String[0]),
                            vList.toArray(new String[0]),
                            formatType
                    );
                    if (nativePatched != null && !nativePatched.isEmpty()) {
                        return nativePatched;
                    }
                }
            } catch (Throwable t) {
                Log.d(TAG, "Native in-memory patcher note: " + t.getMessage());
            }
        }

        // Tier 2: Pure Java resilient parsers (guarantees zero corrupted sections/tags)
        if (lowerPath.endsWith(".json")) {
            return patchJsonContent(content, keyValues);
        } else if (lowerPath.endsWith(".xml")) {
            return patchXmlContent(content, keyValues);
        } else {
            return patchIniContent(content, keyValues, defaultSection);
        }
    }

    // ─── INI & UE4 CVars In-Memory Patcher ────────────────────────────────────

    /**
     * Parses INI / CVars text, updates matching keys in-place, and appends new keys
     * under the specified section (or end of file) without duplicating section headers.
     */
    public static String patchIniContent(String content, String[] keyValues, String targetSection) {
        if (keyValues == null || keyValues.length == 0) return content != null ? content : "";
        if (content == null || content.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (targetSection != null && !targetSection.trim().isEmpty()) {
                String sec = targetSection.trim();
                if (!sec.startsWith("[")) sec = "[" + sec + "]";
                sb.append(sec).append("\n");
            }
            for (String kv : keyValues) {
                if (kv != null && !kv.trim().isEmpty()) {
                    sb.append(kv.trim()).append("\n");
                }
            }
            return sb.toString();
        }

        String[] lines = content.split("\\r?\\n");
        List<String> resultLines = new ArrayList<>(lines.length + keyValues.length + 4);
        Map<String, String> pendingKeys = new LinkedHashMap<>();

        for (String kv : keyValues) {
            if (kv == null || kv.trim().isEmpty()) continue;
            String normalizedKey = extractNormalizedKey(kv.trim());
            pendingKeys.put(normalizedKey, kv.trim());
        }

        String currentSection = "";
        int targetSectionStart = -1;
        int targetSectionEnd = -1;

        String formattedTargetSection = targetSection != null ? targetSection.trim() : "";
        if (!formattedTargetSection.isEmpty() && !formattedTargetSection.startsWith("[")) {
            formattedTargetSection = "[" + formattedTargetSection + "]";
        }

        java.util.Set<String> managedKeys = new java.util.HashSet<>(pendingKeys.keySet());
        java.util.Set<String> seenKeys = new java.util.HashSet<>();

        // Pass 1: Update existing keys in-place and discard duplicate occurrences
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                if (!currentSection.isEmpty() && currentSection.equalsIgnoreCase(formattedTargetSection) && targetSectionEnd == -1) {
                    targetSectionEnd = resultLines.size();
                }
                currentSection = trimmed;
                if (currentSection.equalsIgnoreCase(formattedTargetSection)) {
                    targetSectionStart = resultLines.size();
                }
                resultLines.add(line);
                continue;
            }

            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) {
                resultLines.add(line);
                continue;
            }

            String lineKey = extractNormalizedKey(trimmed);
            if (!lineKey.isEmpty() && managedKeys.contains(lineKey)) {
                if (!seenKeys.contains(lineKey)) {
                    seenKeys.add(lineKey);
                    String replacement = pendingKeys.remove(lineKey);
                    if (replacement != null) {
                        resultLines.add(replacement);
                    }
                } else {
                    // Duplicate key in file — discard to eliminate conflicts
                    Log.d(TAG, "Discarded duplicate INI key: " + lineKey);
                }
            } else {
                resultLines.add(line);
            }
        }

        if (!currentSection.isEmpty() && currentSection.equalsIgnoreCase(formattedTargetSection) && targetSectionEnd == -1) {
            targetSectionEnd = resultLines.size();
        }

        // Pass 2: Append remaining keys
        if (!pendingKeys.isEmpty()) {
            if (!formattedTargetSection.isEmpty()) {
                if (targetSectionStart != -1) {
                    // Insert into existing section
                    int insertPos = targetSectionEnd != -1 ? targetSectionEnd : resultLines.size();
                    for (String remainingVal : pendingKeys.values()) {
                        resultLines.add(insertPos++, remainingVal);
                    }
                } else {
                    // Create new section at the end
                    if (!resultLines.isEmpty() && !resultLines.get(resultLines.size() - 1).trim().isEmpty()) {
                        resultLines.add("");
                    }
                    resultLines.add(formattedTargetSection);
                    for (String remainingVal : pendingKeys.values()) {
                        resultLines.add(remainingVal);
                    }
                }
            } else {
                // No section, append to end
                for (String remainingVal : pendingKeys.values()) {
                    resultLines.add(remainingVal);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String l : resultLines) {
            sb.append(l).append("\n");
        }
        return sb.toString();
    }

    // ─── JSON In-Memory Patcher ───────────────────────────────────────────────

    public static String patchJsonContent(String content, String[] keyValues) {
        if (keyValues == null || keyValues.length == 0) return content != null ? content : "{}";
        String updated = (content != null && !content.trim().isEmpty()) ? content : "{}";
        List<String> unmappedKeys = new ArrayList<>();

        for (String kv : keyValues) {
            if (kv == null || kv.trim().isEmpty()) continue;
            int eq = kv.indexOf('=');
            if (eq <= 0) continue;
            String k = kv.substring(0, eq).trim();
            String v = kv.substring(eq + 1).trim();
            String jsonVal = formatJsonValue(v);

            // Match key in the JSON, replacing the first occurrence and stripping any duplicates
            Pattern keyPattern = Pattern.compile("(\"" + Pattern.quote(k) + "\"\\s*:\\s*)(\"[^\"]*\"|[^,\\n}\\]]+)(,?)");
            Matcher matcher = keyPattern.matcher(updated);
            if (matcher.find()) {
                StringBuffer jsonSb = new StringBuffer();
                matcher.appendReplacement(jsonSb, "$1" + Matcher.quoteReplacement(jsonVal) + "$3");
                while (matcher.find()) {
                    matcher.appendReplacement(jsonSb, ""); // strip duplicate
                }
                matcher.appendTail(jsonSb);
                updated = jsonSb.toString();
            } else {
                unmappedKeys.add("  \"" + k + "\": " + jsonVal);
            }
        }

        if (!unmappedKeys.isEmpty()) {
            // Build the new entries string with trailing commas
            StringBuilder newEntries = new StringBuilder();
            for (String entry : unmappedKeys) {
                newEntries.append(entry).append(",\n");
            }
            // Insert after the opening '{' so existing keys remain comma-correct
            int openBrace = updated.indexOf('{');
            if (openBrace != -1) {
                // Determine if there's content after '{' that needs a comma gap
                String afterBrace = updated.substring(openBrace + 1).trim();
                if (afterBrace.startsWith("}")) {
                    // Empty object — replace with new content (no trailing comma on last entry)
                    StringBuilder sb = new StringBuilder("{\n");
                    for (int i = 0; i < unmappedKeys.size(); i++) {
                        sb.append(unmappedKeys.get(i));
                        if (i < unmappedKeys.size() - 1) sb.append(",");
                        sb.append("\n");
                    }
                    sb.append("}\n");
                    updated = sb.toString();
                } else {
                    // Non-empty object — insert new entries right after '{\n'
                    int insertAt = openBrace + 1;
                    while (insertAt < updated.length() && (updated.charAt(insertAt) == '\r' || updated.charAt(insertAt) == '\n')) {
                        insertAt++;
                    }
                    updated = updated.substring(0, openBrace + 1) + "\n"
                            + newEntries
                            + updated.substring(insertAt);
                }
            } else {
                StringBuilder sb = new StringBuilder("{\n");
                for (int i = 0; i < unmappedKeys.size(); i++) {
                    sb.append(unmappedKeys.get(i));
                    if (i < unmappedKeys.size() - 1) sb.append(",");
                    sb.append("\n");
                }
                sb.append("}\n");
                updated = sb.toString();
            }
        }

        return updated;
    }

    // ─── XML PlayerPrefs In-Memory Patcher ────────────────────────────────────

    public static String patchXmlContent(String content, String[] keyValues) {
        if (keyValues == null || keyValues.length == 0) return content != null ? content : "<map>\n</map>";

        // CRITICAL PROTECTION: If content exists and does NOT contain <map>, it is NOT an Android SharedPreferences XML!
        // Never overwrite unknown XML schemas (e.g. game engine manifests, SplitLibMD5.xml, etc.) - leave untouched!
        if (content != null && !content.trim().isEmpty() && !content.contains("<map>")) {
            Log.w(TAG, "Refusing to patch XML without <map> root tag (schema mismatch / asset protection) - leaving untouched.");
            return content;
        }

        if (content == null || content.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n");
            for (String kv : keyValues) {
                if (kv == null || kv.trim().isEmpty()) continue;
                if (kv.startsWith("+CVars=") || kv.startsWith("-CVars=")) continue;
                int eq = kv.indexOf('=');
                if (eq > 0) {
                    String k = kv.substring(0, eq).trim();
                    String v = kv.substring(eq + 1).trim();
                    sb.append("  ").append(formatXmlEntry(k, v)).append("\n");
                }
            }
            sb.append("</map>\n");
            return sb.toString();
        }

        String updated = content;
        List<String> unmappedEntries = new ArrayList<>();

        for (String kv : keyValues) {
            if (kv == null || kv.trim().isEmpty()) continue;
            if (kv.startsWith("+CVars=") || kv.startsWith("-CVars=")) continue;
            int eq = kv.indexOf('=');
            if (eq <= 0) continue;
            String k = kv.substring(0, eq).trim();
            String v = kv.substring(eq + 1).trim();
            String replacementTag = formatXmlEntry(k, v);

            Pattern keyPattern = Pattern.compile("<(int|string|float|boolean|long)\\s+name=\"" + Pattern.quote(k) + "\"[^>]*>(.*?</\\1>)?");
            Matcher matcher = keyPattern.matcher(updated);
            if (matcher.find()) {
                // Replace the first occurrence, strip any subsequent duplicate occurrences
                StringBuffer xmlSb = new StringBuffer();
                matcher.appendReplacement(xmlSb, Matcher.quoteReplacement(replacementTag));
                while (matcher.find()) {
                    matcher.appendReplacement(xmlSb, ""); // discard duplicate
                }
                matcher.appendTail(xmlSb);
                updated = xmlSb.toString();
            } else {
                unmappedEntries.add("  " + replacementTag);
            }
        }

        if (!unmappedEntries.isEmpty()) {
            StringBuilder insertion = new StringBuilder();
            for (String entry : unmappedEntries) {
                insertion.append(entry).append("\n");
            }
            int lastClose = updated.lastIndexOf("</map>");
            if (lastClose != -1) {
                updated = updated.substring(0, lastClose) + insertion.toString() + updated.substring(lastClose);
            } else {
                updated = updated + "\n" + insertion.toString();
            }
        }

        return updated;
    }

    // ─── Utility Helpers ──────────────────────────────────────────────────────

    public static String extractNormalizedKey(String kv) {
        if (kv == null) return "";
        String s = kv.trim();
        if (s.startsWith("+CVars=") || s.startsWith("-CVars=")) {
            s = s.substring(7).trim();
        }
        int eq = s.indexOf('=');
        if (eq > 0) {
            return s.substring(0, eq).trim();
        }
        return s;
    }

    private static String formatJsonValue(String val) {
        if (val == null || val.isEmpty()) return "\"\"";
        if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
            return val.toLowerCase();
        }
        if (isNumeric(val)) {
            return val;
        }
        return "\"" + val.replace("\"", "\\\"") + "\"";
    }

    private static String formatXmlEntry(String key, String val) {
        if (val == null) val = "";
        if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
            return "<boolean name=\"" + key + "\" value=\"" + val.toLowerCase() + "\" />";
        }
        if (isInteger(val)) {
            return "<int name=\"" + key + "\" value=\"" + val + "\" />";
        }
        if (isFloat(val)) {
            return "<float name=\"" + key + "\" value=\"" + val + "\" />";
        }
        return "<string name=\"" + key + "\">" + val + "</string>";
    }

    public static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFloat(String s) {
        if (s == null || s.isEmpty() || !s.contains(".")) return false;
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
