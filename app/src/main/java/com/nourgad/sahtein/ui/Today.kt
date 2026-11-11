package com.nourgad.sahtein.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nourgad.sahtein.data.FoodLog
import com.nourgad.sahtein.data.MealSlot
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.data.Stats
import com.nourgad.sahtein.data.WorkoutSession
import com.nourgad.sahtein.data.dayTarget
import com.nourgad.sahtein.data.displayName
import com.nourgad.sahtein.data.info
import com.nourgad.sahtein.domain.LangCtx
import com.nourgad.sahtein.domain.Plan
import com.nourgad.sahtein.domain.S
import com.nourgad.sahtein.domain.Units
import com.nourgad.sahtein.domain.Verdicts
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun TodayScreen(vm: AppVM, profile: Profile, padding: PaddingValues, onLog: (Long) -> Unit, onSummary: () -> Unit) {
    val slots by vm.slots.collectAsStateWithLifecycle()
    val allLogs by vm.allLogs.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val today = vm.today()
    val plan = remember(profile) { profile.plan() }
    val logs = allLogs.filter { it.epochDay == today }
    val todaySessions = sessions.filter { it.epochDay == today }
    val target = dayTarget(plan, profile, todaySessions.sumOf { it.kcal })
    val stats = remember(allLogs, slots, sessions, profile) { Stats.allDays(allLogs, slots, plan, profile, sessions) }
    val streak = Stats.streak(stats.filter { it.slotsLogged > 0 }.map { it.day }.toSet(), today)
    val xp = stats.sumOf { it.xp }
    val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
    val next = slots.firstOrNull { s -> s.minuteOfDay > nowMin && logs.none { it.slotId == s.id } }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMM")), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tr(S.hello), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Pill("🔥 $streak", Amber)
                    Spacer(Modifier.height(4.dp))
                    Pill("⭐ ${tr(S.levelLbl)} ${Stats.level(xp)}", Purple)
                }
            }
        }
        item {
            AppCard {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CalorieRing(logs.sumOf { it.kcal }.roundToInt(), target)
                }
                Spacer(Modifier.height(12.dp))
                MacroBar(tr(S.protein), logs.sumOf { it.protein }, plan.proteinG, Purple)
                Spacer(Modifier.height(8.dp))
                MacroBar(tr(S.carbs), logs.sumOf { it.carbs }, plan.carbsG, Blue)
                Spacer(Modifier.height(8.dp))
                MacroBar(tr(S.fat), logs.sumOf { it.fat }, plan.fatG, Amber)
                if (next != null) {
                    Spacer(Modifier.height(10.dp))
                    val mins = next.minuteOfDay - nowMin
                    Text(
                        lang.pick("Next: ${next.name} in ${mins / 60}h ${mins % 60}m", "الجاية: ${next.name} بعد ${mins / 60} س ${mins % 60} د"),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        items(slots.size) { i ->
            MealCard(slots[i], slots, logs, target, plan, lang, onAdd = { onLog(slots[i].id) }, onDelete = { vm.deleteLog(it) })
        }
        if (todaySessions.isNotEmpty()) item {
            AppCard {
                Text("🏋️ ${tr(S.workoutsDone)}", fontWeight = FontWeight.SemiBold)
                todaySessions.forEach { StatRow(it.title, "${it.minutes} min · ${it.kcal.roundToInt()} ${tr(S.kcal)} ${tr(S.burned)}") }
            }
        }
        item {
            Button(onClick = onSummary, modifier = Modifier.fillMaxWidth()) { Text(tr(S.closeDay)) }
        }
    }
}

@Composable
fun MealCard(
    slot: MealSlot, slots: List<MealSlot>, logs: List<FoodLog>, target: Int, plan: Plan, lang: LangCtx,
    onAdd: () -> Unit, onDelete: (FoodLog) -> Unit,
) {
    val mine = logs.filter { it.slotId == slot.id }
    val verdict = Verdicts.verdict(slot.info(), slots.map { it.info() }, logs.map { it.info(lang.arabic) }, target, plan.proteinG)
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(slot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${minuteLabel(slot.minuteOfDay)} · ${tr(S.budget)} ${verdict.budget} ${tr(S.kcal)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalIconButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = tr(S.add)) }
        }
        if (mine.isEmpty()) {
            Text(tr(S.notLogged), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Spacer(Modifier.height(6.dp))
            mine.forEach { LogRow(it, lang, onDelete) }
            Spacer(Modifier.height(8.dp))
            VerdictView(verdict)
        }
    }
}

@Composable
fun LogRow(log: FoodLog, lang: LangCtx, onDelete: ((FoodLog) -> Unit)?) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(log.displayName(lang.arabic), style = MaterialTheme.typography.bodyMedium)
            if (log.foodId != "custom") Text("${fmt(log.qty)} ${Units.label(log.unit, lang)}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("${log.kcal.roundToInt()}", fontWeight = FontWeight.SemiBold)
        if (onDelete != null) {
            Spacer(Modifier.width(4.dp))
            androidx.compose.material3.IconButton(onClick = { onDelete(log) }) {
                Icon(Icons.Default.Close, contentDescription = lang.t(S.delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun summaryText(
    day: Long, profile: Profile, plan: Plan, slots: List<MealSlot>, logs: List<FoodLog>, sessions: List<WorkoutSession>, lang: LangCtx, streak: Int,
): String {
    val st = Stats.day(day, logs, slots, plan, profile, sessions)
    val target = dayTarget(plan, profile, sessions.filter { it.epochDay == day }.sumOf { it.kcal })
    val sb = StringBuilder()
    sb.append("📋 ").append(lang.t(S.dailySummary)).append(" · ")
        .append(LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("d MMM"))).append("\n\n")
    slots.forEach { s ->
        val items = logs.filter { it.epochDay == day && it.slotId == s.id }
        sb.append("🍽️ ").append(s.name).append(": ").append(items.sumOf { it.kcal }.roundToInt()).append(" ").append(lang.t(S.kcal)).append("\n")
        items.forEach {
            sb.append("   • ").append(it.displayName(lang.arabic))
            if (it.foodId != "custom") sb.append(" (").append(fmt(it.qty)).append(" ").append(Units.label(it.unit, lang)).append(")")
            sb.append(" – ").append(it.kcal.roundToInt()).append("\n")
        }
    }
    sessions.filter { it.epochDay == day }.forEach {
        sb.append("🏋️ ").append(it.title).append(": ").append(it.minutes).append(" min, ").append(it.kcal.roundToInt()).append(" ")
            .append(lang.t(S.kcal)).append(" ").append(lang.t(S.burned)).append("\n")
    }
    sb.append("\n").append(lang.t(S.total)).append(": ").append(st.kcal.roundToInt()).append(" / ").append(target).append(" ").append(lang.t(S.kcal)).append("\n")
    sb.append(lang.t(S.protein)).append(" ").append(st.protein.roundToInt()).append("/").append(plan.proteinG)
        .append(" · ").append(lang.t(S.carbs)).append(" ").append(st.carbs.roundToInt()).append("/").append(plan.carbsG)
        .append(" · ").append(lang.t(S.fat)).append(" ").append(st.fat.roundToInt()).append("/").append(plan.fatG).append(" ").append(lang.t(S.g)).append("\n")
    if (st.kcal > 0) {
        val pPct = (st.protein * 4 / st.kcal * 100).roundToInt()
        sb.append(lang.pick("Protein share: $pPct% of calories", "نسبة البروتين: $pPct٪ من السعرات")).append("\n")
    }
    sb.append("⭐ +").append(st.xp).append(" XP · 🔥 ").append(streak).append(" ").append(lang.t(S.streakDays)).append("\n\n")
    sb.append(if (st.onTarget) lang.t(S.greatDay) else lang.t(S.tomorrowReset))
    return sb.toString()
}

@Composable
fun SummaryScreen(vm: AppVM, profile: Profile, day: Long, padding: PaddingValues) {
    val slots by vm.slots.collectAsStateWithLifecycle()
    val logs by vm.allLogs.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val plan = remember(profile) { profile.plan() }
    val stats = Stats.allDays(logs, slots, plan, profile, sessions)
    val streak = Stats.streak(stats.filter { it.slotsLogged > 0 }.map { it.day }.toSet(), day)
    val text = summaryText(day, profile, plan, slots, logs, sessions, lang, streak)
    val ctx = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp,
        bottom = padding.calculateBottomPadding() + 16.dp)) {
        item {
            AppCard { Text(text, style = MaterialTheme.typography.bodyLarge) }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {
                val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
                ctx.startActivity(Intent.createChooser(send, lang.t(S.share)))
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text(tr(S.share))
            }
        }
    }
}
