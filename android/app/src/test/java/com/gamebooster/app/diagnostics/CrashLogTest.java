package com.gamebooster.app.diagnostics;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class CrashLogTest {

    @Test
    public void formatCrash_containsThreadAndStacktrace() {
        RuntimeException ex = new RuntimeException("boom");
        StackTraceElement el = new StackTraceElement("com.gamebooster.app.X", "run", "X.java", 42);
        ex.setStackTrace(new StackTraceElement[]{el});
        String text = CrashLog.formatCrash(Thread.currentThread(), ex);
        assertTrue(text.contains("Thread:"));
        assertTrue(text.contains("java.lang.RuntimeException: boom"));
        assertTrue(text.contains("at com.gamebooster.app.X.run(X.java:42)"));
    }

    @Test
    public void formatCrash_toleratesNulls() {
        String text = CrashLog.formatCrash(null, null);
        assertTrue(text.contains("Exception: null"));
    }

    @Test
    public void format_returnsHeadSlice() {
        List<String> lines = java.util.Arrays.asList("a", "b", "c");
        assertTrue(CrashLog.format(2, lines).size() == 2);
        assertTrue(CrashLog.format(99, lines).size() == 3);
        assertTrue(CrashLog.format(0, lines).isEmpty());
    }
}