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
import android.view.Gravity;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

/**
 * Service managing crosshair overlay lifecycle with non-touchable pass-through flags.
 */
public class CrosshairOverlayService extends Service {

    private static final String CHANNEL_ID = "crosshair_overlay_channel";
    private static final int NOTIFICATION_ID = 2002;

    private WindowManager windowManager;
    private CrosshairOverlayView overlayView;
    private boolean isOverlayActive = false;
    private static boolean isRunning = false;
    private static CrosshairOverlayService instance = null;

    public static boolean isOverlayRunning() {
        return isRunning;
    }

    public static void startOverlay(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, CrosshairOverlayService.class);
        intent.setAction("ACTION_START");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopOverlay(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, CrosshairOverlayService.class);
        intent.setAction("ACTION_STOP");
        context.startService(intent);
    }

    public static void updateOverlay(Context context) {
        if (context == null || !isRunning) return;
        Intent intent = new Intent(context, CrosshairOverlayService.class);
        intent.setAction("ACTION_UPDATE");
        context.startService(intent);
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

        if (intent != null && "ACTION_UPDATE".equals(intent.getAction())) {
            if (isOverlayActive && overlayView != null) {
                applyPreferencesToOverlay();
            }
            return START_STICKY;
        }

        startForeground(NOTIFICATION_ID, buildNotification());
        showOverlayView();
        return START_STICKY;
    }

    private void showOverlayView() {
        if (isOverlayActive || windowManager == null) return;

        overlayView = new CrosshairOverlayView(this);
        applyPreferencesToOverlay();

        int sizePx = CrosshairPreferences.getSizePx(this);
        int windowSize = sizePx + 40;

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                windowSize,
                windowSize,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(overlayView, params);
            isOverlayActive = true;
            isRunning = true;
            CrosshairPreferences.setCrosshairEnabled(this, true);
        } catch (Exception e) {
            isOverlayActive = false;
            isRunning = false;
        }
    }

    private void applyPreferencesToOverlay() {
        if (overlayView == null) return;
        overlayView.setPreset(CrosshairPreferences.getPreset(this));
        overlayView.setColor(CrosshairPreferences.getColor(this));
        overlayView.setSizePx(CrosshairPreferences.getSizePx(this));
        overlayView.setStrokeWidth(CrosshairPreferences.getStrokeWidth(this));
        overlayView.setOpacity(CrosshairPreferences.getOpacity(this));

        if (isOverlayActive && windowManager != null) {
            int sizePx = CrosshairPreferences.getSizePx(this);
            int windowSize = sizePx + 40;
            try {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayView.getLayoutParams();
                if (params != null) {
                    params.width = windowSize;
                    params.height = windowSize;
                    windowManager.updateViewLayout(overlayView, params);
                }
            } catch (Exception ignored) {}
        }
    }

    private void removeOverlayView() {
        if (isOverlayActive && overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
        }
        overlayView = null;
        isOverlayActive = false;
        isRunning = false;
        CrosshairPreferences.setCrosshairEnabled(this, false);
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
