package com.nourgad.sahtein.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.data.Stats
import com.nourgad.sahtein.data.Weekly
import com.nourgad.sahtein.data.dayTarget
import com.nourgad.sahtein.domain.S
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(vm: AppVM, profile: Profile, padding: PaddingValues, onDay: (Long) -> Unit, onWeek: (Long) -> Unit) {
    val slots by vm.slots.collectAsStateWithLifecycle()
    val logs by vm.allLogs.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val plan = remember(profile) { profile.plan() }
    var monthOffset by rememberSaveable { mutableStateOf(0L) }
    val month = YearMonth.now().plusMonths(monthOffset)
    val lang = LocalLang.current
    val stats = remember(logs, slots, sessions, profile) {
        Stats.allDays(logs, slots, plan, profile, sessions).associateBy { it.day }
    }
    val today = LocalDate.now()

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Button(onClick = { onWeek(Weekly.weekStart(today).toEpochDay()) }, modifier = Modifier.fillMaxWidth()) {
                Text("📊 ${tr(S.weeklyReport)}")
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { monthOffset-- }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
                    Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = { if (monthOffset < 0) monthOffset++ }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
                }
                // Week starts Saturday
                val names = if (lang.arabic) listOf("س", "ح", "ن", "ث", "ر", "خ", "ج") else listOf("Sa", "Su", "Mo", "Tu", "We", "Th", "Fr")
                Row { names.forEach { Text(it, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall) } }
                val first = month.atDay(1)
                val lead = (first.dayOfWeek.value - 6 + 7) % 7
                val cells = lead + month.lengthOfMonth()
                val rows = (cells + 6) / 7
                for (r in 0 until rows) {
                    Row {
                        for (c in 0 until 7) {
                            val idx = r * 7 + c - lead + 1
                            Box(Modifier.weight(1f).aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
                                if (idx in 1..month.lengthOfMonth()) {
                                    val date = month.atDay(idx)
                                    val st = stats[date.toEpochDay()]
                                    val color = when {
                                        st == null || st.kcal <= 0.0 -> Color.Transparent
                                        else -> {
                                            val t = dayTarget(plan, profile, sessions.filter { it.epochDay == st.day }.sumOf { it.kcal })
                                            val off = abs(st.kcal - t) / t
                                            when { st.onTarget -> Green; off <= 0.2 -> Amber; else -> Red }
                                        }
                                    }
                                    Box(
                                        Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                                            .background(if (color == Color.Transparent) MaterialTheme.colorScheme.surface else color.copy(alpha = 0.25f))
                                            .clickable(enabled = !date.isAfter(today)) { onDay(date.toEpochDay()) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("$idx", fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                                            color = if (date.isAfter(today)) Grey else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(lang.pick("on target", "في الهدف"), Green)
                    Pill(lang.pick("10–20% off", "بعيد ١٠-٢٠٪"), Amber)
                    Pill(lang.pick(">20% off", "بعيد أكتر من ٢٠٪"), Red)
                }
            }
        }
    }
}

@Composable
fun DayScreen(vm: AppVM, profile: Profile, day: Long, padding: PaddingValues, onLog: (Long) -> Unit) {
    val slots by vm.slots.collectAsStateWithLifecycle()
    val logs by vm.allLogs.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val plan = remember(profile) { profile.plan() }
    val dayLogs = logs.filter { it.epochDay == day }
    val target = dayTarget(plan, profile, sessions.filter { it.epochDay == day }.sumOf { it.kcal })
    val st = Stats.day(day, logs, slots, plan, profile, sessions)
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            AppCard {
                StatRow(tr(S.total), "${st.kcal.roundToInt()} / $target ${tr(S.kcal)}")
                StatRow(tr(S.protein), "${st.protein.roundToInt()} / ${plan.proteinG} ${tr(S.g)}")
                StatRow(tr(S.carbs), "${st.carbs.roundToInt()} / ${plan.carbsG} ${tr(S.g)}")
                StatRow(tr(S.fat), "${st.fat.roundToInt()} / ${plan.fatG} ${tr(S.g)}")
                StatRow("XP", "+${st.xp}")
            }
        }
        items(slots.size) { i ->
            MealCard(slots[i], slots, dayLogs, target, plan, lang, onAdd = { onLog(slots[i].id) }, onDelete = { vm.deleteLog(it) })
        }
    }
}

@Composable
fun WeekScreen(vm: AppVM, profile: Profile, startEpoch: Long, padding: PaddingValues) {
    val slots by vm.slots.collectAsStateWithLifecycle()
    val logs by vm.allLogs.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val weights by vm.weights.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val plan = remember(profile) { profile.plan() }
    var start by rememberSaveable { mutableStateOf(startEpoch) }
    val s = LocalDate.ofEpochDay(start)
    val r = Weekly.report(s, logs, slots, plan, profile, sessions, weights, lang.arabic)
    val prev = Weekly.report(s.minusDays(7), logs, slots, plan, profile, sessions, weights, lang.arabic)
    val fmtD = DateTimeFormatter.ofPattern("d MMM")

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { start -= 7 }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(tr(S.weeklyReport), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${r.start.format(fmtD)} – ${r.end.format(fmtD)}")
                }
                IconButton(onClick = { if (s.plusDays(7) <= LocalDate.now()) start += 7 }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
        }
        if (r.loggedDays == 0 && r.workouts == 0) {
            item { Text(tr(S.noData)) }
        } else {
            item {
                AppCard(color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(tr(S.adherence), style = MaterialTheme.typography.labelLarge)
                    Text("${r.adherencePct}%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    val diff = r.adherencePct - prev.adherencePct
                    Text("${if (diff >= 0) "▲" else "▼"} ${abs(diff)}% ${tr(S.vsLastWeek)}")
                }
            }
            item {
                AppCard {
                    StatRow(tr(S.total), "${r.totalKcal.roundToInt()} ${tr(S.kcal)}")
                    StatRow(tr(S.avgPerDay), "${r.avgKcal.roundToInt()} / ${r.target}")
                    StatRow(tr(S.daysOnTarget), "${r.daysOnTarget} / 7")
                    StatRow(tr(S.avgProtein), "${r.avgProtein.roundToInt()} / ${plan.proteinG} ${tr(S.g)}")
                    r.weightChange?.let { StatRow(tr(S.weightChange), "${if (it > 0) "+" else ""}${"%.1f".format(it)} ${tr(S.kg)}") }
                    r.bestDay?.let { StatRow(tr(S.bestDay), it.format(DateTimeFormatter.ofPattern("EEEE"))) }
                    StatRow(tr(S.workoutsDone), "${r.workouts}")
                }
            }
            if (r.topFoods.isNotEmpty()) item {
                AppCard {
                    Text(tr(S.topFoods), fontWeight = FontWeight.SemiBold)
                    r.topFoods.forEach { StatRow(it.name, "×${it.times}") }
                }
            }
            item {
                AppCard {
                    Text(lang.pick("Calories per day", "السعرات في اليوم"), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    WeekBars(start, logs.filter { it.epochDay in start..start + 6 }.groupBy { it.epochDay }.mapValues { e -> e.value.sumOf { it.kcal } }, r.target, lang.arabic)
                }
            }
        }
    }
}

@Composable
private fun WeekBars(start: Long, perDay: Map<Long, Double>, target: Int, arabic: Boolean) {
    val names = if (arabic) listOf("س", "ح", "ن", "ث", "ر", "خ", "ج") else listOf("Sa", "Su", "Mo", "Tu", "We", "Th", "Fr")
    val max = maxOf(target * 1.3, perDay.values.maxOrNull() ?: 0.0)
    Row(Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        for (i in 0..6) {
            val v = perDay[start + i] ?: 0.0
            val frac = (v / max).toFloat()
            val c = when {
                v <= 0 -> Grey
                v > target * 1.1 -> Red
                v < target * 0.9 -> Amber
                else -> Green
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height((110 * frac).dp.coerceAtLeast(2.dp)).clip(RoundedCornerShape(6.dp)).background(c))
                Text(names[i], style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
