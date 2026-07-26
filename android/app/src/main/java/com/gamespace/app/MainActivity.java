package com.gamespace.app;

import androidx.annotation.NonNull;

import com.gamespace.app.channels.DeviceInfoChannel;
import com.gamespace.app.channels.GameLibraryChannel;
import com.gamespace.app.channels.HzFpsChannel;
import com.gamespace.app.channels.MagiskExporterChannel;
import com.gamespace.app.channels.PerformanceChannel;
import com.gamespace.app.channels.PermissionChannel;
import com.gamespace.app.channels.RootCommandChannel;
import com.gamespace.app.channels.ShizukuChannel;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;

public class MainActivity extends FlutterActivity {

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        BinaryMessenger messenger = flutterEngine.getDartExecutor().getBinaryMessenger();

        new RootCommandChannel(messenger);
        new PermissionChannel(messenger, getApplicationContext());
        new DeviceInfoChannel(messenger);
        new PerformanceChannel(messenger, getApplicationContext());
        new GameLibraryChannel(messenger, getApplicationContext());
        new MagiskExporterChannel(messenger, getApplicationContext());
        new ShizukuChannel(messenger, getApplicationContext());
        new HzFpsChannel(messenger, getApplicationContext());
    }
}
