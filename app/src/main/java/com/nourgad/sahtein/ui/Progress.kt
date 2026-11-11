package com.nourgad.sahtein.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nourgad.sahtein.data.Badges
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.data.Stats
import com.nourgad.sahtein.data.WeightEntry
import com.nourgad.sahtein.domain.S

@Composable
fun ProgressScreen(vm: AppVM, profile: Profile, padding: PaddingValues) {
    val slots by vm.slots.collectAsStateWithLifecycle()
    val logs by vm.allLogs.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val sets by vm.sets.collectAsStateWithLifecycle()
    val weights by vm.weights.collectAsStateWithLifecycle()
    val plan = remember(profile) { profile.plan() }
    val today = vm.today()
    val stats = remember(logs, slots, sessions, profile) { Stats.allDays(logs, slots, plan, profile, sessions) }
    val xp = stats.sumOf { it.xp }
    val logStreak = Stats.streak(stats.filter { it.slotsLogged > 0 }.map { it.day }.toSet(), today)
    val targetStreak = Stats.streak(stats.filter { it.onTarget }.map { it.day }.toSet(), today)
    val badges = Badges.compute(stats, logs, sessions, sets, profile, today)
    var w by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard(color = Purple.copy(alpha = 0.12f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${tr(S.levelLbl)} ${Stats.level(xp)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Text("$xp XP")
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { Stats.levelProgress(xp) },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = Purple, drawStopIndicator = {})
            }
        }
        item {
            AppCard {
                Text(tr(S.streaks), fontWeight = FontWeight.SemiBold)
                StatRow("🔥 ${tr(S.loggingStreak)}", "$logStreak")
                StatRow("🎯 ${tr(S.targetStreak)}", "$targetStreak")
                StatRow("🏋️ ${tr(S.workoutsDone)}", "${sessions.size}")
            }
        }
        item {
            AppCard {
                Text(tr(S.weight), fontWeight = FontWeight.SemiBold)
                StatRow(tr(S.currentWeight), "${fmt(profile.currentWeight)} ${tr(S.kg)}")
                StatRow(tr(S.goalWeight), "${fmt(profile.goalWeight)} ${tr(S.kg)}")
                val lost = profile.startWeight - profile.currentWeight
                StatRow(LocalLang.current.pick("Change since start", "التغير من البداية"),
                    "${if (lost >= 0) "-" else "+"}${fmt(kotlin.math.abs(lost))} ${tr(S.kg)}")
                if (weights.size >= 2) {
                    Spacer(Modifier.height(8.dp))
                    WeightChart(weights)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(tr(S.kg), w, { w = it.take(5) }, Modifier.weight(1f))
                    Button(enabled = w.toDoubleOrNull()?.let { it in 30.0..300.0 } == true, onClick = {
                        vm.logWeight(w.toDouble()); w = ""
                    }) { Text(tr(S.save)) }
                }
                Text(tr(S.logWeight), style = MaterialTheme.typography.bodySmall)
            }
        }
        item { SectionTitle(tr(S.badges)) }
        badges.chunked(3).forEach { row ->
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { b ->
                        AppCard(modifier = Modifier.weight(1f).alpha(if (b.earned) 1f else 0.35f)) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(b.emoji, style = MaterialTheme.typography.headlineMedium)
                                Text(tr(b.name), style = MaterialTheme.typography.labelSmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun WeightChart(weights: List<WeightEntry>) {
    val color = MaterialTheme.colorScheme.primary
    val pts = weights.takeLast(60)
    val min = pts.minOf { it.kg } - 0.5
    val max = pts.maxOf { it.kg } + 0.5
    val d0 = pts.first().epochDay
    val span = (pts.last().epochDay - d0).coerceAtLeast(1)
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(fmt(pts.first().kg), style = MaterialTheme.typography.labelSmall)
            Text(fmt(pts.last().kg), style = MaterialTheme.typography.labelSmall)
        }
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            val path = Path()
            pts.forEachIndexed { i, p ->
                val x = size.width * (p.epochDay - d0) / span
                val y = size.height * (1 - ((p.kg - min) / (max - min))).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(color, 4.dp.toPx(), Offset(x, y))
            }
            drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
        }
    }
    Spacer(Modifier.width(1.dp))
}
