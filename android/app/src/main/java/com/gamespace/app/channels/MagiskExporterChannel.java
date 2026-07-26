package com.gamespace.app.channels;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MagiskExporterChannel implements MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/magisk_exporter";
    private final Context context;
    private final MethodChannel channel;

    public MagiskExporterChannel(BinaryMessenger messenger, Context context) {
        this.context = context;
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        if ("exportMagiskModule".equals(call.method)) {
            Map<String, String> tweaks = call.argument("tweaks");
            if (tweaks != null) {
                String zipPath = exportMagiskZip(tweaks);
                if (zipPath != null) {
                    result.success(zipPath);
                } else {
                    result.error("EXPORT_FAILED", "Failed to generate Magisk module zip", null);
                }
            } else {
                result.error("INVALID_ARGUMENT", "Tweaks map required", null);
            }
        } else {
            result.notImplemented();
        }
    }

    private String exportMagiskZip(Map<String, String> tweaks) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            File zipFile = new File(downloadsDir, "GameSpace_Optimization_Module.zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            String moduleProp = "id=gamespace_optimizer\n" +
                    "name=Game Space Universal Performance Module\n" +
                    "version=v1.0.0\n" +
                    "versionCode=100\n" +
                    "author=GAME SPACE\n" +
                    "description=Applies system properties, touch sampling, and graphics optimizations.\n";

            zos.putNextEntry(new ZipEntry("module.prop"));
            zos.write(moduleProp.getBytes());
            zos.closeEntry();

            StringBuilder serviceSh = new StringBuilder("#!/system/bin/sh\n" +
                    "MODDIR=${0%/*}\n" +
                    "sleep 20\n");

            for (Map.Entry<String, String> entry : tweaks.entrySet()) {
                serviceSh.append("resetprop ").append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
            }

            zos.putNextEntry(new ZipEntry("service.sh"));
            zos.write(serviceSh.toString().getBytes());
            zos.closeEntry();

            zos.close();
            fos.close();

            return zipFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}
