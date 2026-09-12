package com.gamebooster.app.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;

/**
 * VirtualShoulderKeyService — Manages in-game floating L1/R1 trigger buttons.
 */
public class VirtualShoulderKeyService extends Service {

    private static final String TAG = "VirtualShoulderKeySvc";
    private static final String CHANNEL_ID = "virtual_shoulder_key_channel";
    private static final int NOTIF_ID = 921;

    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

    private static boolean sIsRunning = false;

    private WindowManager windowManager;
    private VirtualTriggerOverlayView viewL1;
    private VirtualTriggerOverlayView viewR1;

    private WindowManager.LayoutParams paramsL1;
    private WindowManager.LayoutParams paramsR1;

    private String currentPackage = "default";
    private ShoulderKeySchemeManager.Scheme currentScheme;

    public static boolean isRunning() {
        return sIsRunning;
    }

    public static void start(Context context, String packageName) {
        if (context == null || sIsRunning) return;
        if (!Settings.canDrawOverlays(context)) return;

        Intent intent = new Intent(context, VirtualShoulderKeyService.class);
        if (packageName != null) {
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed starting VirtualShoulderKeyService", e);
        }
    }

    public static void stop(Context context) {
        if (context == null || !sIsRunning) return;
        try {
            Intent intent = new Intent(context, VirtualShoulderKeyService.class);
            context.stopService(intent);
        } catch (Exception ignored) {}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIsRunning = true;
        createNotificationChannel();
        startForeground(NOTIF_ID, createNotification());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_PACKAGE_NAME)) {
            currentPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        }

        setupTriggers();
        return START_NOT_STICKY;
    }

    private void setupTriggers() {
        if (windowManager == null) return;
        currentScheme = ShoulderKeySchemeManager.getSchemeForPackage(this, currentPackage);

        int triggerSize = dpToPx(56);
        int overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        // L1 Setup
        paramsL1 = new WindowManager.LayoutParams(
                triggerSize,
                triggerSize,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        paramsL1.gravity = Gravity.TOP | Gravity.START;
        paramsL1.x = currentScheme.l1.x;
        paramsL1.y = currentScheme.l1.y;

        viewL1 = new VirtualTriggerOverlayView(this);
        viewL1.setup("L1", Color.parseColor("#00F0FF"), windowManager, paramsL1, (newX, newY) -> {
            currentScheme.l1.x = newX;
            currentScheme.l1.y = newY;
            ShoulderKeySchemeManager.saveSchemeForPackage(this, currentPackage, currentScheme);
        });

        // R1 Setup
        paramsR1 = new WindowManager.LayoutParams(
                triggerSize,
                triggerSize,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        paramsR1.gravity = Gravity.TOP | Gravity.START;
        paramsR1.x = currentScheme.r1.x;
        paramsR1.y = currentScheme.r1.y;

        viewR1 = new VirtualTriggerOverlayView(this);
        viewR1.setup("R1", Color.parseColor("#FF0055"), windowManager, paramsR1, (newX, newY) -> {
            currentScheme.r1.x = newX;
            currentScheme.r1.y = newY;
            ShoulderKeySchemeManager.saveSchemeForPackage(this, currentPackage, currentScheme);
        });

        try {
            windowManager.addView(viewL1, paramsL1);
            windowManager.addView(viewR1, paramsR1);
        } catch (Exception e) {
            Log.e(TAG, "Error adding trigger views", e);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null) {
            if (viewL1 != null) {
                try { windowManager.removeView(viewL1); } catch (Exception ignored) {}
                viewL1 = null;
            }
            if (viewR1 != null) {
                try { windowManager.removeView(viewR1); } catch (Exception ignored) {}
                viewR1 = null;
            }
        }
        sIsRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID,
                    "Virtual Shoulder Keys",
                    NotificationManager.IMPORTANCE_MIN
            );
            chan.setDescription("In-game tactical L1/R1 triggers");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(chan);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🎯 Virtual Shoulder Keys Active")
                .setContentText("L1 & R1 Tactical triggers ready")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
    }
}
