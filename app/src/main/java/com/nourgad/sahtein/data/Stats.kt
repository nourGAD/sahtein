package com.nourgad.sahtein.data

import com.nourgad.sahtein.domain.ExerciseDb
import com.nourgad.sahtein.domain.ItemInfo
import com.nourgad.sahtein.domain.Muscle
import com.nourgad.sahtein.domain.Plan
import com.nourgad.sahtein.domain.SlotInfo
import com.nourgad.sahtein.domain.Tx
import com.nourgad.sahtein.domain.VerdictKind
import com.nourgad.sahtein.domain.Verdicts
import java.time.LocalDate

fun MealSlot.info() = SlotInfo(id, pct, sortOrder)
fun FoodLog.info(arabic: Boolean) = ItemInfo(slotId, displayName(arabic), qty, unit, kcal, protein)

data class DayStat(
    val day: Long,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val slotsLogged: Int,
    val slotsOnTarget: Int,
    val onTarget: Boolean,
    val proteinHit: Boolean,
    val fullDay: Boolean,
    val workouts: Int,
    val xp: Int,
)

/** Daily target, optionally raised by workout calories (setting). */
fun dayTarget(plan: Plan, profile: Profile, workoutKcal: Double): Int =
    plan.targetKcal + (workoutKcal * profile.workoutAddBackPct / 100.0).toInt()

object Stats {
    fun day(
        day: Long, logs: List<FoodLog>, slots: List<MealSlot>, plan: Plan, profile: Profile, sessions: List<WorkoutSession>,
    ): DayStat {
        val mine = logs.filter { it.epochDay == day }
        val ws = sessions.filter { it.epochDay == day }
        val target = dayTarget(plan, profile, ws.sumOf { it.kcal })
        val infos = slots.map { it.info() }
        val items = mine.map { it.info(false) }
        val logged = infos.count { s -> items.any { it.slotId == s.id } }
        val onTargetSlots = infos.count { s ->
            Verdicts.verdict(s, infos, items, target, plan.proteinG).kind == VerdictKind.ON_TRACK
        }
        val kcal = mine.sumOf { it.kcal }
        val protein = mine.sumOf { it.protein }
        val proteinHit = protein >= plan.proteinG * 0.9
        val full = slots.isNotEmpty() && logged == slots.size
        val onTarget = mine.isNotEmpty() && Verdicts.dayOnTarget(kcal, target, plan.floorKcal)
        val xp = logged * 10 + onTargetSlots * 15 + (if (proteinHit) 10 else 0) + (if (full) 25 else 0) + ws.size * 20
        return DayStat(day, kcal, protein, mine.sumOf { it.carbs }, mine.sumOf { it.fat }, logged, onTargetSlots,
            onTarget, proteinHit, full, ws.size, xp)
    }

    fun allDays(logs: List<FoodLog>, slots: List<MealSlot>, plan: Plan, profile: Profile, sessions: List<WorkoutSession>): List<DayStat> {
        val days = (logs.map { it.epochDay } + sessions.map { it.epochDay }).toSortedSet()
        return days.map { day(it, logs, slots, plan, profile, sessions) }
    }

    fun streak(days: Set<Long>, today: Long): Int {
        var d = if (today in days) today else today - 1
        var n = 0
        while (d in days) { n++; d-- }
        return n
    }

    fun level(xp: Int) = 1 + xp / 500
    fun levelProgress(xp: Int) = (xp % 500) / 500f
}

data class Badge(val id: String, val emoji: String, val name: Tx, val earned: Boolean)

object Badges {
    fun compute(
        stats: List<DayStat>, logs: List<FoodLog>, sessions: List<WorkoutSession>, sets: List<SetLog>,
        profile: Profile, today: Long,
    ): List<Badge> {
        val logDays = stats.filter { it.slotsLogged > 0 }.map { it.day }.toSet()
        val bestStreak = run {
            var best = 0; var cur = 0; var prev = Long.MIN_VALUE
            for (d in logDays.sorted()) { cur = if (d == prev + 1) cur + 1 else 1; best = maxOf(best, cur); prev = d }
            best
        }
        val weekAgo = today - 6
        val musclesThisWeek = sets.filter { it.epochDay >= weekAgo }
            .mapNotNull { ExerciseDb.byId[it.exerciseId]?.muscle }.toSet()
        val lost = profile.startWeight - profile.currentWeight
        return listOf(
            Badge("first_log", "🍽️", Tx("First meal logged", "أول وجبة"), logs.isNotEmpty()),
            Badge("week", "📅", Tx("7 days logged", "٧ أيام تسجيل"), logDays.size >= 7),
            Badge("streak7", "🔥", Tx("7-day streak", "٧ أيام ورا بعض"), bestStreak >= 7),
            Badge("streak30", "🏆", Tx("30-day streak", "٣٠ يوم ورا بعض"), bestStreak >= 30),
            Badge("protein", "💪", Tx("Protein Pro (5 days)", "بطل البروتين (٥ أيام)"), stats.count { it.proteinHit } >= 5),
            Badge("on_target", "🎯", Tx("5 on-target days", "٥ أيام في الهدف"), stats.count { it.onTarget } >= 5),
            Badge("voice", "🎙️", Tx("Voice logger (10 items)", "سجلت بالصوت ١٠ مرات", "سجلتي بالصوت ١٠ مرات"), logs.count { it.method == "voice" } >= 10),
            Badge("first_workout", "🏋️", Tx("First workout", "أول تمرين"), sessions.isNotEmpty()),
            Badge("workouts10", "⚡", Tx("10 workouts", "١٠ تمارين"), sessions.size >= 10),
            Badge("all_muscles", "🧩", Tx("Every muscle in a week", "كل العضلات في أسبوع"), musclesThisWeek.size == Muscle.entries.size),
            Badge("kg5", "⬇️", Tx("5 kg down", "نزلت ٥ كيلو", "نزلتي ٥ كيلو"), lost >= 5),
            Badge("kg10", "🥇", Tx("10 kg down", "نزلت ١٠ كيلو", "نزلتي ١٠ كيلو"), lost >= 10),
        )
    }
}

data class FoodCount(val name: String, val times: Int)

data class WeekReport(
    val start: LocalDate,
    val end: LocalDate,
    val totalKcal: Double,
    val loggedDays: Int,
    val avgKcal: Double,
    val target: Int,
    val daysOnTarget: Int,
    val avgProtein: Double,
    val weightChange: Double?,
    val bestDay: LocalDate?,
    val topFoods: List<FoodCount>,
    val workouts: Int,
    val adherencePct: Int,
)

object Weekly {
    /** Start of the week containing [date]; weeks start on Saturday in Egypt. */
    fun weekStart(date: LocalDate): LocalDate {
        val dow = date.dayOfWeek.value // Mon=1..Sun=7, Sat=6
        val back = (dow - 6 + 7) % 7
        return date.minusDays(back.toLong())
    }

    fun report(
        start: LocalDate, logs: List<FoodLog>, slots: List<MealSlot>, plan: Plan, profile: Profile,
        sessions: List<WorkoutSession>, weights: List<WeightEntry>, arabic: Boolean,
    ): WeekReport {
        val end = start.plusDays(6)
        val from = start.toEpochDay(); val to = end.toEpochDay()
        val wLogs = logs.filter { it.epochDay in from..to }
        val wSessions = sessions.filter { it.epochDay in from..to }
        val stats = (from..to).map { Stats.day(it, wLogs, slots, plan, profile, wSessions) }.filter { it.kcal > 0 }
        val total = stats.sumOf { it.kcal }
        val best = stats.minByOrNull { kotlin.math.abs(it.kcal - plan.targetKcal) }?.day?.let(LocalDate::ofEpochDay)
        val wWeights = weights.filter { it.epochDay in from..to }
        val change = if (wWeights.size >= 2) wWeights.last().kg - wWeights.first().kg else null
        val top = wLogs.groupBy { it.displayName(arabic) }.map { FoodCount(it.key, it.value.size) }
            .sortedByDescending { it.times }.take(3)
        val onTarget = stats.count { it.onTarget }
        return WeekReport(
            start, end, total, stats.size, if (stats.isEmpty()) 0.0 else total / stats.size, plan.targetKcal,
            onTarget, if (stats.isEmpty()) 0.0 else stats.sumOf { it.protein } / stats.size, change, best, top,
            wSessions.size, (onTarget * 100) / 7,
        )
    }
}
