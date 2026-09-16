package com.dk.together.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dk.together.MainActivity
import com.dk.together.R
import com.dk.together.model.RelationshipMath

class RelationshipWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateOne(context, appWidgetManager, id) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, RelationshipWidgetProvider::class.java))
            ids.forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE)
            val you = prefs.getString("you", "You") ?: "You"
            val partner = prefs.getString("partner", "Partner") ?: "Partner"
            val startEpochDay = prefs.getLong("start_epoch_day", java.time.LocalDate.now().toEpochDay())
            val note = prefs.getString("note", "Thinking of you 💜") ?: "Thinking of you 💜"
            val days = RelationshipMath.daysTogether(startEpochDay)

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.relationship_widget).apply {
                setTextViewText(R.id.widget_title, "$you + $partner")
                setTextViewText(R.id.widget_counter, "$days days together")
                setTextViewText(R.id.widget_note, note)
                setOnClickPendingIntent(R.id.widget_title, pendingIntent)
                setOnClickPendingIntent(R.id.widget_counter, pendingIntent)
                setOnClickPendingIntent(R.id.widget_note, pendingIntent)
            }
            manager.updateAppWidget(appWidgetId, views)
        }
    }
}
