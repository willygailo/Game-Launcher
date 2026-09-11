package com.gamebooster.app.terminal;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TerminalCoreEngineTest {

    @Test
    public void testPromptSymbol() {
        TerminalCoreEngine engine = TerminalCoreEngine.getInstance();
        assertNotNull(engine);
        assertEquals("$", engine.getPromptSymbol());
    }

    @Test
    public void testResolveInitialDirectory() {
        String initialDir = TerminalCoreEngine.resolveInitialDirectory();
        assertNotNull(initialDir);
        assertTrue(initialDir.startsWith("/"));
    }

    @Test
    public void testPresetScripts() {
        TerminalCoreEngine engine = TerminalCoreEngine.getInstance();
        List<TerminalScriptPreset> presets = engine.getPresetScripts();
        assertNotNull(presets);
        assertFalse(presets.isEmpty());

        for (TerminalScriptPreset preset : presets) {
            assertNotNull(preset.getId());
            assertNotNull(preset.getTitle());
            assertNotNull(preset.getCommand());
        }
    }

    @Test
    public void testCompletions() {
        TerminalCoreEngine engine = TerminalCoreEngine.getInstance();
        List<TerminalScriptPreset> presets = engine.getPresetScripts();
        assertNotNull(presets);

        List<String> lsCompletions = engine.getCompletions("l");
        assertNotNull(lsCompletions);
        assertTrue(lsCompletions.contains("ls") || lsCompletions.contains("logcat"));

        List<String> emptyCompletions = engine.getCompletions("");
        assertNotNull(emptyCompletions);
        assertTrue(emptyCompletions.isEmpty());
    }

    @Test
    public void testHelpCommand() {
        TerminalCoreEngine engine = TerminalCoreEngine.getInstance();
        TerminalCoreEngine.TerminalResult res = engine.executeCommand("help");
        assertNotNull(res);
        assertEquals(0, res.exitCode);
        assertNotNull(res.output);
        assertTrue(res.output.contains("SHELL BUILT-INS & TOOLS"));
    }

    @Test
    public void testPkgHelpCommand() {
        TerminalCoreEngine engine = TerminalCoreEngine.getInstance();
        TerminalCoreEngine.TerminalResult res = engine.executeCommand("pkg help");
        assertNotNull(res);
        assertEquals(0, res.exitCode);
        assertNotNull(res.output);
        assertTrue(res.output.contains("Termux Package Helper"));
    }
}
