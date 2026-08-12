package com.gamebooster.app.platform.shell;

import org.junit.Test;
import static org.junit.Assert.*;

public class SetPropValidatorTest {

    @Test
    public void testFilterCommandNullOrNonSetprop() {
        assertNull(SetPropValidator.filterCommand(null));
        assertEquals("echo hello", SetPropValidator.filterCommand("echo hello"));
    }

    @Test
    public void testFilterCommandValid() {
        String cmd = "setprop debug.custom.test 1";
        String filtered = SetPropValidator.filterCommand(cmd);
        assertEquals("setprop debug.custom.test 1", filtered);
    }
}
