package com.gamebooster.app.terminal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnsiColorParserTest {

    @Test
    public void testStripAnsiEmptyAndNull() {
        assertEquals("", AnsiColorParser.stripAnsi(null));
        assertEquals("", AnsiColorParser.stripAnsi(""));
    }

    @Test
    public void testStripAnsiPlainText() {
        String plain = "Hello World Terminal";
        assertEquals(plain, AnsiColorParser.stripAnsi(plain));
    }

    @Test
    public void testStripAnsiColors() {
        String greenText = "\u001B[32mSuccess\u001B[0m";
        assertEquals("Success", AnsiColorParser.stripAnsi(greenText));
    }

    @Test
    public void testStripAnsiMultipleColorsAndReset() {
        String mixed = "\u001B[1;36mHeader\u001B[0m: \u001B[31mError\u001B[0m \u001B[32mOK\u001B[0m";
        assertEquals("Header: Error OK", AnsiColorParser.stripAnsi(mixed));
    }

    @Test
    public void testStripAnsiBrightColors() {
        String bright = "\u001B[92mBright Green\u001B[0m \u001B[96mBright Cyan\u001B[0m";
        assertEquals("Bright Green Bright Cyan", AnsiColorParser.stripAnsi(bright));
    }

    @Test
    public void testColorConstants() {
        assertEquals(0xFF00FF66, AnsiColorParser.COLOR_DEFAULT);
        assertEquals(0xFFFF3366, AnsiColorParser.COLOR_RED);
        assertEquals(0xFF00FF66, AnsiColorParser.COLOR_GREEN);
        assertEquals(0xFFFFD700, AnsiColorParser.COLOR_YELLOW);
        assertEquals(0xFF38BDF8, AnsiColorParser.COLOR_BLUE);
        assertEquals(0xFFC084FC, AnsiColorParser.COLOR_MAGENTA);
        assertEquals(0xFF00F0FF, AnsiColorParser.COLOR_CYAN);
        assertEquals(0xFFF1F5F9, AnsiColorParser.COLOR_WHITE);
    }
}
