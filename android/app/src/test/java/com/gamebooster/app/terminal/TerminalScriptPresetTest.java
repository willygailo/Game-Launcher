package com.gamebooster.app.terminal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TerminalScriptPresetTest {

    @Test
    public void testScriptPresetFields() {
        TerminalScriptPreset preset = new TerminalScriptPreset(
                "test_id",
                "Test Title",
                "Test Description",
                "echo 'hello'"
        );

        assertEquals("test_id", preset.getId());
        assertEquals("Test Title", preset.getTitle());
        assertEquals("Test Description", preset.getDescription());
        assertEquals("echo 'hello'", preset.getCommand());
    }
}
