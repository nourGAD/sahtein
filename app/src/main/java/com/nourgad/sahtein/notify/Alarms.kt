package com.nourgad.sahtein.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nourgad.sahtein.MainActivity
import com.nourgad.sahtein.R
import com.nourgad.sahtein.data.AppDb
import com.nourgad.sahtein.data.Stats
import com.nourgad.sahtein.data.todayEpoch
import com.nourgad.sahtein.domain.LangCtx
import com.nourgad.sahtein.domain.S
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

object Alarms {
    const val CHANNEL = "meals"
    const val EXTRA_TYPE = "type"
    const val EXTRA_SLOT = "slot"
    const val EXTRA_OPEN = "open"

    const val MEAL = "meal"
    const val SNOOZED = "snoozed"
    const val FOLLOW_UP = "followup"
    const val SUMMARY = "summary"
    const val ACTION_SNOOZE = "snooze"
    const val ACTION_SKIP = "skip"

    private const val SUMMARY_CODE = 900_000

    fun createChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val arabic = LangCtx.deviceIsArabic()
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, if (arabic) "تنبيهات الوجبات" else "Meal reminders", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun code(slotId: Long, kind: Int) = (slotId * 10 + kind).toInt()

    fun canExact(context: Context): Boolean {
        val am = context.getSystemService(AlarmManager::class.java)
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
    }

    private fun pending(context: Context, code: Int, type: String, slotId: Long): PendingIntent {
        val i = Intent(context, MealAlarmReceiver::class.java)
            .putExtra(EXTRA_TYPE, type).putExtra(EXTRA_SLOT, slotId)
            .setAction("sahtein.$type.$slotId")
        return PendingIntent.getBroadcast(context, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun setAt(context: Context, atMillis: Long, pi: PendingIntent) {
        val am = context.getSystemService(AlarmManager::class.java)
        if (canExact(context)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        } else {
            am.setWindow(AlarmManager.RTC_WAKEUP, atMillis, 5 * 60_000L, pi)
        }
    }

    private fun nextAt(minuteOfDay: Int): Long {
        val now = LocalDateTime.now()
        var t = now.toLocalDate().atTime(minuteOfDay / 60, minuteOfDay % 60)
        if (!t.isAfter(now)) t = t.plusDays(1)
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    suspend fun scheduleAll(context: Context) {
        val dao = AppDb.get(context).dao()
        val profile = dao.profileNow() ?: return
        val am = context.getSystemService(AlarmManager::class.java)
        for (slot in dao.slotsNow()) {
            val pi = pending(context, code(slot.id, 1), MEAL, slot.id)
            if (slot.reminderOn) setAt(context, nextAt(slot.minuteOfDay), pi) else am.cancel(pi)
        }
        setAt(context, nextAt(profile.summaryMinute), pending(context, SUMMARY_CODE, SUMMARY, 0))
    }

    fun scheduleIn(context: Context, slotId: Long, type: String, minutes: Int) {
        val kind = if (type == FOLLOW_UP) 2 else 3
        setAt(context, System.currentTimeMillis() + minutes * 60_000L, pending(context, code(slotId, kind), type, slotId))
    }

    fun cancelFollowUp(context: Context, slotId: Long) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context, code(slotId, 2), FOLLOW_UP, slotId))
    }

    suspend fun cancelAll(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        for (slot in AppDb.get(context).dao().slotsNow()) {
            am.cancel(pending(context, code(slot.id, 1), MEAL, slot.id))
            am.cancel(pending(context, code(slot.id, 2), FOLLOW_UP, slot.id))
            am.cancel(pending(context, code(slot.id, 3), SNOOZED, slot.id))
        }
        am.cancel(pending(context, SUMMARY_CODE, SUMMARY, 0))
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun openAppIntent(context: Context, open: String, code: Int): PendingIntent {
        val i = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN, open)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun actionIntent(context: Context, action: String, slotId: Long): PendingIntent {
        val i = Intent(context, MealAlarmReceiver::class.java)
            .putExtra(EXTRA_TYPE, action).putExtra(EXTRA_SLOT, slotId)
            .setAction("sahtein.$action.$slotId")
        return PendingIntent.getBroadcast(context, code(slotId, if (action == ACTION_SNOOZE) 4 else 5), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

class MealAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(Alarms.EXTRA_TYPE) ?: return
        val slotId = intent.getLongExtra(Alarms.EXTRA_SLOT, 0)
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(context, type, slotId)
            } finally {
                result.finish()
            }
        }
    }

    private suspend fun handle(context: Context, type: String, slotId: Long) {
        val dao = AppDb.get(context).dao()
        val profile = dao.profileNow() ?: return
        val lang = LangCtx(LangCtx.deviceIsArabic(), profile.female)
        val nm = NotificationManagerCompat.from(context)
        val notifId = slotId.toInt() + 1
        when (type) {
            Alarms.MEAL, Alarms.SNOOZED -> {
                val slot = dao.slot(slotId) ?: return
                if (dao.logsForNow(todayEpoch()).none { it.slotId == slotId }) {
                    notify(context, notifId, slot.name + lang.t(S.mealTime), lang.t(S.speak), "log:$slotId", slotId, lang, withActions = true)
                    Alarms.scheduleIn(context, slotId, Alarms.FOLLOW_UP, 60)
                }
                if (type == Alarms.MEAL) Alarms.scheduleAll(context) // next day's alarm
            }
            Alarms.FOLLOW_UP -> {
                val slot = dao.slot(slotId) ?: return
                if (dao.logsForNow(todayEpoch()).none { it.slotId == slotId }) {
                    notify(context, notifId, "${lang.t(S.followUp)} ${slot.name}", lang.t(S.speak), "log:$slotId", slotId, lang, withActions = false)
                }
            }
            Alarms.ACTION_SNOOZE -> {
                nm.cancel(notifId)
                Alarms.cancelFollowUp(context, slotId)
                Alarms.scheduleIn(context, slotId, Alarms.SNOOZED, 15)
            }
            Alarms.ACTION_SKIP -> {
                nm.cancel(notifId)
                Alarms.cancelFollowUp(context, slotId)
            }
            Alarms.SUMMARY -> {
                val today = todayEpoch()
                val logs = dao.logsForNow(today)
                if (logs.isNotEmpty()) {
                    val plan = profile.plan()
                    val st = Stats.day(today, logs, dao.slotsNow(), plan, profile, emptyList())
                    val body = "${st.kcal.toInt()} / ${plan.targetKcal} ${lang.t(S.kcal)} · ${lang.t(S.protein)} ${st.protein.toInt()}/${plan.proteinG} ${lang.t(S.g)}"
                    notify(context, 777, lang.t(S.dailySummary), body, "summary", 0, lang, withActions = false)
                }
                Alarms.scheduleAll(context)
            }
        }
    }

    private fun notify(
        context: Context, id: Int, title: String, body: String, open: String, slotId: Long, lang: LangCtx, withActions: Boolean,
    ) {
        if (!Alarms.canNotify(context)) return
        val b = NotificationCompat.Builder(context, Alarms.CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(Alarms.openAppIntent(context, open, id))
        if (withActions) {
            b.addAction(0, lang.t(S.snooze), Alarms.actionIntent(context, Alarms.ACTION_SNOOZE, slotId))
            b.addAction(0, lang.t(S.skipped), Alarms.actionIntent(context, Alarms.ACTION_SKIP, slotId))
        }
        try {
            NotificationManagerCompat.from(context).notify(id, b.build())
        } catch (_: SecurityException) {
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Alarms.scheduleAll(context)
            } finally {
                result.finish()
            }
        }
    }
}
