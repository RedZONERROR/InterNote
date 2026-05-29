package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class NoteListWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_note_list)

            // 1. Plus header button: Opens a fresh new note editor
            val createIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("action_trigger_new_note", true)
            }
            val createPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 300,
                createIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.btn_widget_header_new, createPendingIntent)

            // 2. Set up the collection adapter pointing to RemoteViewsService
            val serviceIntent = Intent(context, NoteListWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            remoteViews.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
            remoteViews.setEmptyView(R.id.widget_list_view, android.R.id.empty)

            // 3. Set up click PendingIntent template for elements inside the ListView
            val clickIntentTemplate = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val clickPendingIntentTemplate = PendingIntent.getActivity(
                context,
                appWidgetId + 400,
                clickIntentTemplate,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE // MUST be mutable to receive custom extras appended on each row
            )
            remoteViews.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntentTemplate)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
        }
    }
}
