package com.gamebooster.app.booster;

import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GamePackageRegistry;

public class CpuGovernorChannel {

    private static final String TAG = "CpuGovernorChannel";

    public static boolean setGovernor(String governor) {
        boolean isExtreme = "extreme".equalsIgnoreCase(governor) || "performance".equalsIgnoreCase(governor);
        if (isExtreme) {
            CommandExecutor.executeSystemCommand("cmd power set-mode 2 1");
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
            CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "-20");
            CommandExecutor.setSystemProperty("sys.use_fifo", "1");

            // Apply per-game performance governor and CPU scheduler boost to all registered games
            for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
                try {
                    CommandExecutor.executeSystemCommand("cmd game mode performance " + pkg);
                    CommandExecutor.executeSystemCommand("cmd game set --fps 185 " + pkg);
                } catch (Throwable ignored) {}
            }
        } else {
            CommandExecutor.executeSystemCommand("cmd power set-mode 2 0");
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 0");
            CommandExecutor.setSystemProperty("debug.hwui.render_thread_priority", "0");
            CommandExecutor.setSystemProperty("sys.use_fifo", "0");

            for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
                try {
                    CommandExecutor.executeSystemCommand("cmd game mode standard " + pkg);
                    CommandExecutor.executeSystemCommand("cmd game reset " + pkg);
                } catch (Throwable ignored) {}
            }
        }
        return true;
    }

    public static boolean setPerformanceLock() {
        return setGovernor("extreme");
    }
}
