package com.gamebooster.app.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.core.AppExecutors;
import java.util.Calendar;

/**
 * GameSchedulerService triggers automatic EXTREME boost profile during configured peak gaming hours.
 */
public class GameSchedulerService extends BroadcastReceiver {

    private static final String TAG = "GameSchedulerService";
    private static final int REQUEST_CODE = 3003;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;

        if (!GameSchedulerPreferences.isSchedulerEnabled(context)) {
            Log.d(TAG, "Scheduler is disabled in preferences");
            return;
        }

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int startHour = GameSchedulerPreferences.getStartHour(context);
        int endHour = GameSchedulerPreferences.getEndHour(context);

        Log.d(TAG, "Scheduler check: currentHour=" + currentHour + " (window=" + startHour + "-" + endHour + ")");

        AppExecutors.getInstance().executeCommand(() -> {
            if (currentHour >= startHour && currentHour < endHour) {
                Log.i(TAG, "⚡ Inside gaming window — Auto-activating EXTREME Performance!");
                MaxHzForceChannel.forceApply(165);
                PerformanceChannel.applyProfileWithResult(context, PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                TouchLatencyChannel.enableUltraTouchResponse();
            } else {
                Log.i(TAG, "Outside gaming window — Reverting to Balanced mode");
                PerformanceChannel.applyProfileWithResult(context, PerformanceChannel.Profile.BALANCED);
            }
        });
    }

    public static void scheduleNextAlarm(Context context) {
        if (context == null) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, GameSchedulerService.class);
        PendingIntent pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Check every 30 minutes
        long intervalMs = 30 * 60 * 1000L;
        long triggerAtMs = SystemClock.elapsedRealtime() + intervalMs;

        am.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMs,
            intervalMs,
            pi
        );
        Log.i(TAG, "Scheduled repeating gaming window check every 30 minutes");
    }

    public static void cancelAlarm(Context context) {
        if (context == null) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, GameSchedulerService.class);
        PendingIntent pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
            Log.i(TAG, "Cancelled gaming window scheduler alarm");
        }
    }
}
