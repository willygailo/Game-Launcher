package com.gamebooster.app.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

/**
 * Service managing crosshair overlay lifecycle with non-touchable pass-through flags,
 * persistent preset selection, and clean view detachment.
 */
public class CrosshairOverlayService extends Service {

    public static final String PREF_NAME = "gamebooster_crosshair_prefs";
    public static final String KEY_PRESET = "crosshair_preset";
    public static final String KEY_COLOR = "crosshair_color";
    public static final String KEY_SIZE = "crosshair_size";

    private static final String CHANNEL_ID = "crosshair_overlay_channel";
    private static final int NOTIFICATION_ID = 2002;

    private static CrosshairOverlayService instance;

    private WindowManager windowManager;
    private CrosshairOverlayView overlayView;
    private boolean isOverlayActive = false;

    public static boolean isRunning() {
        return instance != null && instance.isOverlayActive;
    }

    public static void startOverlay(Context context) {
        Intent intent = new Intent(context, CrosshairOverlayService.class);
        intent.setAction("ACTION_START");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopOverlay(Context context) {
        Intent intent = new Intent(context, CrosshairOverlayService.class);
        intent.setAction("ACTION_STOP");
        context.startService(intent);
        // Also force manager hide cleanup
        CrosshairOverlayManager.forceHide(context);
    }

    public static void updatePreset(Context context, CrosshairPreset preset) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PRESET, preset.name()).apply();

        if (instance != null && instance.overlayView != null) {
            instance.overlayView.setPreset(preset);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_STOP".equals(intent.getAction())) {
            removeOverlayView();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, buildNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, buildNotification());
        }
        showOverlayView();
        return START_STICKY;
    }

    private void showOverlayView() {
        if (windowManager == null) return;
        if (isOverlayActive && overlayView != null) {
            reloadPreferences();
            return;
        }

        overlayView = new CrosshairOverlayView(this);
        reloadPreferences();

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        // Sleek 60x60 px dimensions
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                60,
                60,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
        );
        params.preferredRefreshRate = 185.0f;

        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(overlayView, params);
            isOverlayActive = true;
        } catch (Exception e) {
            isOverlayActive = false;
        }
    }

    private void reloadPreferences() {
        if (overlayView == null) return;
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String presetName = prefs.getString(KEY_PRESET, CrosshairPreset.TACTICAL_CROSS.name());
        int color = prefs.getInt(KEY_COLOR, Color.parseColor("#00FF66"));
        int size = prefs.getInt(KEY_SIZE, 60);

        try {
            CrosshairPreset preset = CrosshairPreset.valueOf(presetName);
            overlayView.setPreset(preset);
        } catch (Exception e) {
            overlayView.setPreset(CrosshairPreset.TACTICAL_CROSS);
        }
        overlayView.setColor(color);
        overlayView.setSizePx(size);
    }

    private void removeOverlayView() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
        }
        overlayView = null;
        isOverlayActive = false;
    }

    @Override
    public void onDestroy() {
        removeOverlayView();
        instance = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Crosshair Overlay",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Displays floating crosshair overlay");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Precision Crosshair Active")
                .setContentText("Hardware-accelerated visual overlay running")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
