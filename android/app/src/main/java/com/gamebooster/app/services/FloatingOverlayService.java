package com.gamebooster.app.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.gamebooster.app.R;
import com.gamebooster.app.config.MlbbConfigPatcher;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * FloatingOverlayService — Live In-Game HUD Controller.
 * Provides instant on-the-fly map re-sync, 185 FPS force toggle, and Vulkan cache flush.
 */
public class FloatingOverlayService extends Service {

    private static final String TAG = "FloatingOverlayService";

    private WindowManager mWindowManager;
    private View mOverlayView;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "⚡ Initializing In-Game Floating Overlay HUD Service...");

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 200;

        try {
            mOverlayView = LayoutInflater.from(this).inflate(R.layout.layout_floating_hud, null);

            View rootContainer = mOverlayView.findViewById(R.id.hud_root_container);
            if (rootContainer != null) {
                rootContainer.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mOverlayView, params);
                            return true;
                    }
                    return false;
                });
            }

            Button btnResyncMap = mOverlayView.findViewById(R.id.btn_hud_resync_map);
            if (btnResyncMap != null) {
                btnResyncMap.setOnClickListener(v -> {
                    Log.i(TAG, "HUD Trigger: Re-Sync Map & ResCheck Lock fired!");
                    MlbbConfigPatcher.applyMlbb2216PatchFix("com.mobile.legends");
                    Toast.makeText(this, "⚡ Map & ResCheck Lock Re-Synced!", Toast.LENGTH_SHORT).show();
                });
            }

            Button btnForce185 = mOverlayView.findViewById(R.id.btn_hud_force_185);
            if (btnForce185 != null) {
                btnForce185.setOnClickListener(v -> {
                    Log.i(TAG, "HUD Trigger: Force 185 FPS SurfaceFlinger executed!");
                    ShizukuExecutor.executeShizukuCommand(
                        "service call SurfaceFlinger 1034 i32 185 2>/dev/null; " +
                        "setprop persist.game_mode.performance.fps 185 2>/dev/null"
                    );
                    Toast.makeText(this, "🚀 185 FPS SurfaceFlinger Overdrive Active!", Toast.LENGTH_SHORT).show();
                });
            }

            Button btnFlushVulkan = mOverlayView.findViewById(R.id.btn_hud_flush_vulkan);
            if (btnFlushVulkan != null) {
                btnFlushVulkan.setOnClickListener(v -> {
                    Log.i(TAG, "HUD Trigger: Vulkan Cache Flush fired!");
                    ShizukuExecutor.executeShizukuCommand(
                        "rm -rf /data/data/com.mobile.legends/cache/vulkan_pipeline/* 2>/dev/null; " +
                        "rm -rf /data/data/com.activision.callofduty.shooter/cache/vulkan_pipeline/* 2>/dev/null; " +
                        "rm -rf /data/data/com.tencent.ig/cache/vulkan_pipeline/* 2>/dev/null"
                    );
                    Toast.makeText(this, "🧹 Vulkan Shader Cache Flushed!", Toast.LENGTH_SHORT).show();
                });
            }

            mWindowManager.addView(mOverlayView, params);

        } catch (Throwable t) {
            Log.e(TAG, "Failed initializing floating overlay HUD: " + t.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOverlayView != null && mWindowManager != null) {
            try {
                mWindowManager.removeView(mOverlayView);
            } catch (Throwable ignored) {}
        }
    }
}
