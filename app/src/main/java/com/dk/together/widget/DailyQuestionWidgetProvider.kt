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
import com.dk.together.model.ContentBanks
import com.dk.together.model.QuestionContent
import org.json.JSONArray
import java.time.LocalDate

class DailyQuestionWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateOne(context, appWidgetManager, it) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DailyQuestionWidgetProvider::class.java))
            ids.forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val question = dailyQuestion(context)
            val launch = PendingIntent.getActivity(
                context,
                1,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val views = RemoteViews(context.packageName, R.layout.daily_question_widget).apply {
                setTextViewText(R.id.daily_widget_category, question.category)
                setTextViewText(R.id.daily_widget_question, question.prompt)
                setOnClickPendingIntent(R.id.daily_widget_root, launch)
            }
            manager.updateAppWidget(id, views)
        }

        private fun dailyQuestion(context: Context): QuestionContent {
            val raw = context.assets.open("questions.json").bufferedReader().use { it.readText() }
            val array = JSONArray(raw)
            val base = List(array.length()) { i ->
                val o = array.getJSONObject(i)
                QuestionContent(o.getString("id"), o.getString("category"), o.getString("prompt"))
            }
            val all = ContentBanks.expandedQuestions(base)
            val day = LocalDate.now().toEpochDay()
            val index = ((day % all.size) + all.size).toInt() % all.size
            return all[index]
        }
    }
}
