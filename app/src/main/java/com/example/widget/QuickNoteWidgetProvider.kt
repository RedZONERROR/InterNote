package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class QuickNoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_quick_note)

            // Intent when clicking launcher element opens MainActivity triggering a new note compile
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("action_trigger_new_note", true)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 200,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            remoteViews.setOnClickPendingIntent(R.id.btn_widget_new_note, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }
}
