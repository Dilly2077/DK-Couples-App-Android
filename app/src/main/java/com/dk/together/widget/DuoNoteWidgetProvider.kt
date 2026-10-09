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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DuoNoteWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateOne(context, appWidgetManager, it) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DuoNoteWidgetProvider::class.java))
            ids.forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("duo_note_widget", Context.MODE_PRIVATE)
            val sender = prefs.getString("sender", "").orEmpty()
            val note = prefs.getString("note", "").orEmpty()
            val sentAt = prefs.getLong("sent_at", 0L)

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val stamp = if (sentAt > 0) {
                Instant.ofEpochMilli(sentAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("d MMM · HH:mm"))
            } else ""

            val views = RemoteViews(context.packageName, R.layout.duo_note_widget).apply {
                setTextViewText(R.id.duo_widget_title, if (sender.isBlank()) "Duo note" else "from $sender")
                setTextViewText(R.id.duo_widget_note, if (note.isBlank()) "No note from your partner yet ♡" else note)
                setTextViewText(R.id.duo_widget_time, stamp)
                setOnClickPendingIntent(R.id.duo_widget_root, pendingIntent)
            }
            manager.updateAppWidget(appWidgetId, views)
        }
    }
}
