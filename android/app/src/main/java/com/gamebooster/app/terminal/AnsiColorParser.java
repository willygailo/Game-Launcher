package com.gamebooster.app.terminal;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AnsiColorParser — Parses ANSI escape sequences (colors, bold, reset)
 * into Android SpannableStringBuilder for true terminal color rendering.
 */
public class AnsiColorParser {

    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[([0-9;]*)m");

    // Standard Terminal ANSI Colors (XTerm Palette)
    public static final int COLOR_DEFAULT = 0xFF00FF66; // Cyber Green default
    public static final int COLOR_BLACK   = 0xFF000000;
    public static final int COLOR_RED     = 0xFFFF3366;
    public static final int COLOR_GREEN   = 0xFF00FF66;
    public static final int COLOR_YELLOW  = 0xFFFFD700;
    public static final int COLOR_BLUE    = 0xFF38BDF8;
    public static final int COLOR_MAGENTA = 0xFFC084FC;
    public static final int COLOR_CYAN    = 0xFF00F0FF;
    public static final int COLOR_WHITE   = 0xFFF1F5F9;

    public static final int COLOR_BRIGHT_BLACK   = 0xFF64748B;
    public static final int COLOR_BRIGHT_RED     = 0xFFFF4D4D;
    public static final int COLOR_BRIGHT_GREEN   = 0xFF4ADE80;
    public static final int COLOR_BRIGHT_YELLOW  = 0xFFFDE047;
    public static final int COLOR_BRIGHT_BLUE    = 0xFF60A5FA;
    public static final int COLOR_BRIGHT_MAGENTA = 0xFFE879F9;
    public static final int COLOR_BRIGHT_CYAN    = 0xFF67E8F9;
    public static final int COLOR_BRIGHT_WHITE   = 0xFFFFFFFF;

    /**
     * Strips all ANSI escape codes from the input string.
     */
    public static String stripAnsi(CharSequence input) {
        if (input == null) return "";
        return ANSI_PATTERN.matcher(input.toString()).replaceAll("");
    }

    /**
     * Parses raw terminal output containing ANSI codes into a formatted SpannableStringBuilder.
     */
    public static SpannableStringBuilder parseAnsi(CharSequence input, int defaultColor) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (input == null || input.length() == 0) {
            return ssb;
        }

        String text = input.toString();
        Matcher matcher = ANSI_PATTERN.matcher(text);

        int lastEnd = 0;
        int currentColor = defaultColor;
        boolean isBold = false;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Append preceding text segment with current styles
            if (start > lastEnd) {
                String segment = text.substring(lastEnd, start);
                int segStart = ssb.length();
                ssb.append(segment);
                ssb.setSpan(new ForegroundColorSpan(currentColor), segStart, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isBold) {
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), segStart, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // Parse codes
            String codes = matcher.group(1);
            if (codes == null || codes.isEmpty() || "0".equals(codes)) {
                currentColor = defaultColor;
                isBold = false;
            } else {
                String[] parts = codes.split(";");
                for (String part : parts) {
                    try {
                        int code = Integer.parseInt(part.trim());
                        switch (code) {
                            case 0:
                                currentColor = defaultColor;
                                isBold = false;
                                break;
                            case 1:
                                isBold = true;
                                break;
                            case 22:
                                isBold = false;
                                break;
                            // Foreground Colors
                            case 30: currentColor = COLOR_BLACK; break;
                            case 31: currentColor = COLOR_RED; break;
                            case 32: currentColor = COLOR_GREEN; break;
                            case 33: currentColor = COLOR_YELLOW; break;
                            case 34: currentColor = COLOR_BLUE; break;
                            case 35: currentColor = COLOR_MAGENTA; break;
                            case 36: currentColor = COLOR_CYAN; break;
                            case 37: currentColor = COLOR_WHITE; break;
                            case 39: currentColor = defaultColor; break;
                            // High Intensity Foreground Colors
                            case 90: currentColor = COLOR_BRIGHT_BLACK; break;
                            case 91: currentColor = COLOR_BRIGHT_RED; break;
                            case 92: currentColor = COLOR_BRIGHT_GREEN; break;
                            case 93: currentColor = COLOR_BRIGHT_YELLOW; break;
                            case 94: currentColor = COLOR_BRIGHT_BLUE; break;
                            case 95: currentColor = COLOR_BRIGHT_MAGENTA; break;
                            case 96: currentColor = COLOR_BRIGHT_CYAN; break;
                            case 97: currentColor = COLOR_BRIGHT_WHITE; break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            lastEnd = end;
        }

        // Append remaining text
        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd);
            int segStart = ssb.length();
            ssb.append(remaining);
            ssb.setSpan(new ForegroundColorSpan(currentColor), segStart, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (isBold) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), segStart, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return ssb;
    }
}
