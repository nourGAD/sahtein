package com.nourgad.sahtein.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.domain.Exercise
import com.nourgad.sahtein.domain.ExerciseDb
import com.nourgad.sahtein.domain.Muscle
import com.nourgad.sahtein.domain.MuscleGroup
import com.nourgad.sahtein.domain.Place
import com.nourgad.sahtein.domain.S
import com.nourgad.sahtein.domain.WorkoutPlans
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun WorkoutsScreen(
    profile: Profile, padding: PaddingValues, place: Place, onPlace: (Place) -> Unit,
    onMuscle: (Muscle) -> Unit, onPlan: (String) -> Unit, hasActive: Boolean, onResume: () -> Unit,
) {
    val rec = WorkoutPlans.recommended(profile.female, place)
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Segmented(listOf(Place.GYM to "🏋️ ${tr(S.gym)}", Place.HOME to "🏠 ${tr(S.home)}"), place, onPlace) }
        if (hasActive) item {
            Button(onClick = onResume, modifier = Modifier.fillMaxWidth()) { Text("▶ ${tr(S.startWorkout)}") }
        }
        item {
            AppCard(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.clickable { onPlan(rec.id) }) {
                Text(tr(S.recommended), style = MaterialTheme.typography.labelLarge)
                Text(tr(rec.name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${rec.perWeek}× / ${LocalLang.current.pick("week", "أسبوع")} · ${rec.days.size} ${LocalLang.current.pick("day types", "أنواع أيام")}")
            }
        }
        item { SectionTitle(tr(S.plans)) }
        WorkoutPlans.all.filter { it.place == place && it.id != rec.id }.forEach { p ->
            item {
                AppCard(modifier = Modifier.clickable { onPlan(p.id) }) {
                    Text(tr(p.name), fontWeight = FontWeight.SemiBold)
                    Text("${p.perWeek}× / ${LocalLang.current.pick("week", "أسبوع")}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { SectionTitle(tr(S.muscles)) }
        MuscleGroup.entries.forEach { g ->
            item { Text(tr(g.label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
            val ms = Muscle.entries.filter { it.group == g }
            ms.chunked(2).forEach { pair ->
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { m ->
                            AppCard(modifier = Modifier.weight(1f).clickable { onMuscle(m) }) {
                                Text(m.emoji, style = MaterialTheme.typography.headlineSmall)
                                Text(tr(m.label), fontWeight = FontWeight.SemiBold)
                                Text("${ExerciseDb.forMuscle(m, place).size} ${LocalLang.current.pick("exercises", "تمارين")}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleScreen(muscle: Muscle, place: Place, padding: PaddingValues, onExercise: (String) -> Unit) {
    val lang = LocalLang.current
    val list = ExerciseDb.forMuscle(muscle, place)
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("${muscle.emoji} ${tr(muscle.label)} · ${if (place == Place.GYM) tr(S.gym) else tr(S.home)}",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        list.forEach { e ->
            item {
                AppCard(modifier = Modifier.clickable { onExercise(e.id) }) {
                    Text(tr(e.name), fontWeight = FontWeight.SemiBold)
                    Text("${tr(e.equipment)} · ${levelLabel(e.level, lang.arabic)} · ${e.sets} × ${e.reps}",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

fun levelLabel(level: Int, arabic: Boolean) = when (level) {
    1 -> if (arabic) "مبتدئ" else "Beginner"
    2 -> if (arabic) "متوسط" else "Intermediate"
    else -> if (arabic) "متقدم" else "Advanced"
}

fun openYoutube(context: android.content.Context, e: Exercise) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(e.youtubeUrl())))
    } catch (_: Exception) {
        Toast.makeText(context, "YouTube", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ExerciseScreen(id: String, padding: PaddingValues, onLogThis: (Exercise) -> Unit) {
    val e = ExerciseDb.byId[id] ?: return
    val ctx = LocalContext.current
    val lang = LocalLang.current
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(tr(e.name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${e.muscle.emoji} ${tr(e.muscle.label)}", color = MaterialTheme.colorScheme.primary)
        }
        item {
            Button(onClick = { openYoutube(ctx, e) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text(tr(S.watchYoutube))
            }
        }
        item {
            AppCard {
                StatRow(tr(S.equipment), tr(e.equipment))
                StatRow(tr(S.level), levelLabel(e.level, lang.arabic))
                StatRow(tr(S.setsReps), "${e.sets} × ${e.reps}")
                StatRow(tr(S.rest), "${e.restSec} s")
            }
        }
        item {
            AppCard {
                Text(tr(S.steps), fontWeight = FontWeight.SemiBold)
                e.steps.forEachIndexed { i, s -> Text("${i + 1}. ${tr(s)}", Modifier.padding(top = 6.dp)) }
            }
        }
        item {
            AppCard(color = Red.copy(alpha = 0.08f)) {
                Text("⚠️ ${tr(S.mistakes)}", fontWeight = FontWeight.SemiBold)
                e.avoid.forEach { Text("• ${tr(it)}", Modifier.padding(top = 6.dp)) }
            }
        }
        item {
            OutlinedButton(onClick = { onLogThis(e) }, modifier = Modifier.fillMaxWidth()) { Text(tr(S.logThis)) }
        }
    }
}

@Composable
fun PlanScreen(planId: String, padding: PaddingValues, onStart: (String, List<String>) -> Unit, onExercise: (String) -> Unit) {
    val plan = WorkoutPlans.byId[planId] ?: return
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(tr(plan.name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${plan.perWeek}× / ${LocalLang.current.pick("week", "أسبوع")}")
        }
        plan.days.forEach { d ->
            item {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tr(d.title), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        val title = "${tr(plan.name)} · ${tr(d.title)}"
                        Button(onClick = { onStart(title, d.exerciseIds) }) { Text(tr(S.startWorkout)) }
                    }
                    d.exerciseIds.mapNotNull { ExerciseDb.byId[it] }.forEach { e ->
                        Row(Modifier.fillMaxWidth().clickable { onExercise(e.id) }.padding(vertical = 6.dp)) {
                            Text(tr(e.name), Modifier.weight(1f))
                            Text("${e.sets} × ${e.reps}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveWorkoutScreen(vm: AppVM, padding: PaddingValues, onDone: () -> Unit) {
    val lang = LocalLang.current
    val ctx = LocalContext.current
    var restLeft by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(restLeft) {
        if (restLeft > 0) { delay(1000); restLeft-- }
    }
    val groups = vm.activeSets.groupBy { it.exerciseId }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(vm.activeTitle.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏱ ${tr(S.restTimer)}: ", Modifier.weight(1f))
                if (restLeft > 0) Text("${restLeft}s", style = MaterialTheme.typography.titleLarge, color = Green)
                TextButton(onClick = { restLeft = 60 }) { Text("60s") }
                TextButton(onClick = { restLeft = 90 }) { Text("90s") }
            }
        }
        groups.forEach { (exId, rows) ->
            val e = ExerciseDb.byId[exId] ?: return@forEach
            item {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tr(e.name), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { openYoutube(ctx, e) }) { Text("▶") }
                    }
                    val last = vm.lastSets.value[exId].orEmpty()
                    if (last.isNotEmpty()) Text("${tr(S.previous)}: " + last.joinToString("  ") { "${it.reps}×${fmt(it.weightKg)}" },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    rows.forEachIndexed { i, s ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${i + 1}", Modifier.width(20.dp))
                            OutlinedTextField(s.reps.value, { v -> s.reps.value = v.filter { it.isDigit() }.take(3) }, Modifier.weight(1f),
                                label = { Text(tr(S.reps)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(s.kg.value, { v -> s.kg.value = v.filter { it.isDigit() || it == '.' }.take(5) }, Modifier.weight(1f),
                                label = { Text(tr(S.kg)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            Checkbox(checked = s.done.value, onCheckedChange = {
                                s.done.value = it
                                if (it) restLeft = e.restSec
                            })
                        }
                    }
                    TextButton(onClick = { vm.addSet(exId) }) { Text("+ ${tr(S.addSet)}") }
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Button(onClick = {
                vm.finishWorkout { kcal ->
                    Toast.makeText(ctx, "${lang.t(S.workoutSaved)} · ${kcal.roundToInt()} ${lang.t(S.kcal)} ${lang.t(S.burned)}", Toast.LENGTH_LONG).show()
                    onDone()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(tr(S.finish)) }
        }
    }
}
