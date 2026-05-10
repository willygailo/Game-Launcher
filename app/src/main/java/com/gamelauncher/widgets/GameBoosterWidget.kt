package com.gamelauncher.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.app.PendingIntent
import com.gamelauncher.R
import com.gamelauncher.services.GameBoosterService
import com.gamelauncher.ui.MainActivity

class GameBoosterWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }
}

fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_game_booster)
    
    val openIntent = Intent(context, MainActivity::class.java)
    val openPendingIntent = PendingIntent.getActivity(
        context, 0, openIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.btn_open, openPendingIntent)
    
    val boostIntent = Intent(context, GameBoosterService::class.java).apply {
        action = GameBoosterService.ACTION_START_BOOST
    }

    val boostPendingIntent = PendingIntent.getService(
        context, 1, boostIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.btn_boost, boostPendingIntent)
    
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
