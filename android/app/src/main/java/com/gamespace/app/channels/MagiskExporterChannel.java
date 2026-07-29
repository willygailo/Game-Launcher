package com.gamespace.app.channels;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;

public class MagiskExporterChannel {

    public static boolean exportMagiskModule(Context context, File outputFile) {
        if (context == null || outputFile == null) return false;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("#!/system/bin/sh\n");
            sb.append("# Game Space Auto-Optimization Magisk Script\n");
            sb.append("setprop debug.hwui.renderer vulkan\n");
            sb.append("setprop debug.sf.hw 1\n");
            sb.append("setprop debug.sf.latch_unsignaled 1\n");
            sb.append("setprop system.touch_slop_reduction 1\n");
            sb.append("setprop view.touch_slop 2\n");
            sb.append("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > $f; done\n");
            sb.append("cmd thermalservice override-status 0 || true\n");

            FileWriter writer = new FileWriter(outputFile);
            writer.write(sb.toString());
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
