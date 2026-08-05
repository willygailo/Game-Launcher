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
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

        startForeground(NOTIFICATION_ID, buildNotification());
        showOverlayView();
        return START_STICKY;
    }

    private void showOverlayView() {
        if (isOverlayActive || windowManager == null) return;

        overlayView = new CrosshairOverlayView(this);
        overlayView.setPreset(CrosshairPreset.TACTICAL_CROSS);
        overlayView.setColor(Color.parseColor("#00FF66"));
        overlayView.setSizePx(100);

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                120,
                120,
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
        } catch (Exception e) {
            isOverlayActive = false;
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
    }

    @Override
    public void onDestroy() {

        removeOverlayView();
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
