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
import com.dk.together.data.AppStore
import com.dk.together.model.PetMath
import com.dk.together.model.PetStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PetWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateOne(context, appWidgetManager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action != ACTION_FEED && action != ACTION_CLEAN && action != ACTION_PLAY) return

        val snapshot = context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE)
        val current = PetStats(
            hunger = snapshot.getInt("pet_hunger", 72),
            happiness = snapshot.getInt("pet_happiness", 82),
            cleanliness = snapshot.getInt("pet_cleanliness", 78),
            energy = snapshot.getInt("pet_energy", 70),
            affection = snapshot.getInt("pet_affection", 86)
        )
        val updated = when (action) {
            ACTION_FEED -> current.copy(hunger = current.hunger + 18, happiness = current.happiness + 2)
            ACTION_CLEAN -> current.copy(cleanliness = current.cleanliness + 22)
            else -> current.copy(happiness = current.happiness + 12, energy = current.energy - 5, affection = current.affection + 3)
        }.clamped()

        snapshot.edit()
            .putInt("pet_hunger", updated.hunger)
            .putInt("pet_happiness", updated.happiness)
            .putInt("pet_cleanliness", updated.cleanliness)
            .putInt("pet_energy", updated.energy)
            .putInt("pet_affection", updated.affection)
            .apply()

        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                AppStore(context).setPet(updated)
            } finally {
                updateAll(context)
                result.finish()
            }
        }
    }

    companion object {
        private const val ACTION_FEED = "com.dk.together.widget.FEED"
        private const val ACTION_CLEAN = "com.dk.together.widget.CLEAN"
        private const val ACTION_PLAY = "com.dk.together.widget.PLAY"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PetWidgetProvider::class.java))
            ids.forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val snapshot = context.getSharedPreferences("widget_snapshot", Context.MODE_PRIVATE)
            val name = snapshot.getString("pet_name", "Nova") ?: "Nova"
            val room = snapshot.getString("pet_room", "Living room") ?: "Living room"
            val stats = PetStats(
                hunger = snapshot.getInt("pet_hunger", 72),
                happiness = snapshot.getInt("pet_happiness", 82),
                cleanliness = snapshot.getInt("pet_cleanliness", 78),
                energy = snapshot.getInt("pet_energy", 70),
                affection = snapshot.getInt("pet_affection", 86)
            )

            val launchPending = PendingIntent.getActivity(
                context,
                appWidgetId,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            fun actionPending(action: String, suffix: Int): PendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + suffix,
                Intent(context, PetWidgetProvider::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.pet_widget).apply {
                setTextViewText(R.id.pet_widget_name, name)
                setTextViewText(R.id.pet_widget_room, room)
                setTextViewText(R.id.pet_widget_thought, PetMath.thought(stats))
                setTextViewText(R.id.pet_widget_needs, "🍓 ${stats.hunger}%   🫧 ${stats.cleanliness}%   ✨ ${stats.happiness}%")
                setOnClickPendingIntent(R.id.pet_widget_root, launchPending)
                setOnClickPendingIntent(R.id.pet_widget_feed, actionPending(ACTION_FEED, 1))
                setOnClickPendingIntent(R.id.pet_widget_clean, actionPending(ACTION_CLEAN, 2))
                setOnClickPendingIntent(R.id.pet_widget_play, actionPending(ACTION_PLAY, 3))
            }
            manager.updateAppWidget(appWidgetId, views)
        }

        private fun PetStats.clamped() = copy(
            hunger = hunger.coerceIn(0, 100),
            happiness = happiness.coerceIn(0, 100),
            cleanliness = cleanliness.coerceIn(0, 100),
            energy = energy.coerceIn(0, 100),
            affection = affection.coerceIn(0, 100)
        )
    }
}
