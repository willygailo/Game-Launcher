package com.gamebooster.app;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.role.ShizukuRole;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerminalCommandExecutionTest {

    @Test
    public void testRoleTerminalAccess() {
        assertTrue("ADMIN role must be allowed to use terminal", ShizukuRole.ADMIN.canUseTerminal());
        assertTrue("USER role must be allowed to use terminal", ShizukuRole.USER.canUseTerminal());
        assertFalse("READONLY role must NOT be allowed to use terminal", ShizukuRole.READONLY.canUseTerminal());
    }

    @Test
    public void testEmptyCommand() {
        String result = ShizukuExecutor.executeShizukuCommand("");
        assertEquals("ERROR: Empty command", result);
    }

    @Test
    public void testAdbShellPrefixHandling() {
        // adb shell alone returns SUCCESS
        String result = ShizukuExecutor.executeShizukuCommand("adb shell");
        assertEquals("SUCCESS", result);
    }
}
