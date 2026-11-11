package com.nourgad.sahtein.ui

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nourgad.sahtein.data.MealSlot
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.data.todayEpoch
import com.nourgad.sahtein.domain.Activity
import com.nourgad.sahtein.domain.Calc
import com.nourgad.sahtein.domain.LangCtx
import com.nourgad.sahtein.domain.S
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun pickTime(context: Context, minute: Int, onPicked: (Int) -> Unit) {
    TimePickerDialog(context, { _, h, m -> onPicked(h * 60 + m) }, minute / 60, minute % 60, false).show()
}

data class SlotDraft(val name: String, val minute: Int, val pct: Int)

fun defaultSlots(lang: LangCtx, withSnack: Boolean): List<SlotDraft> = if (withSnack) listOf(
    SlotDraft(lang.t(S.breakfast), 9 * 60, 25),
    SlotDraft(lang.t(S.lunch), 15 * 60, 35),
    SlotDraft(lang.t(S.snack), 18 * 60, 10),
    SlotDraft(lang.t(S.dinner), 21 * 60, 30),
) else listOf(
    SlotDraft(lang.t(S.breakfast), 9 * 60, 30),
    SlotDraft(lang.t(S.lunch), 15 * 60, 40),
    SlotDraft(lang.t(S.dinner), 21 * 60, 30),
)

@Composable
fun OnboardingScreen(vm: AppVM, onRequestNotifications: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var female by remember { mutableStateOf(false) }
    var birthYear by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf(Activity.LIGHT) }
    var pace by remember { mutableStateOf(0.5) }
    var withSnack by remember { mutableStateOf(false) }
    val lang = LangCtx(LangCtx.deviceIsArabic(), female)
    var slots by remember { mutableStateOf(defaultSlots(lang, false)) }
    val ctx = LocalContext.current
    val total = 7

    val valid = when (step) {
        1 -> birthYear.toIntOrNull()?.let { it in 1920..LocalDate.now().year - 10 } == true &&
            height.toDoubleOrNull()?.let { it in 120.0..230.0 } == true
        2 -> weight.toDoubleOrNull()?.let { it in 30.0..300.0 } == true &&
            goal.toDoubleOrNull()?.let { it in 30.0..300.0 } == true
        5 -> slots.sumOf { it.pct } == 100
        else -> true
    }

    CompositionLocalProvider(LocalLang provides lang) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(20.dp)
        ) {
            LinearProgressIndicator(progress = { (step + 1f) / total }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (step) {
                    0 -> {
                        Text("🍽️", style = MaterialTheme.typography.displayMedium)
                        Text(tr(S.welcome), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(tr(S.welcomeSub), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        Text(tr(S.sex), style = MaterialTheme.typography.titleMedium)
                        Segmented(listOf(false to tr(S.male), true to tr(S.female)), female, {
                            female = it
                            slots = defaultSlots(LangCtx(lang.arabic, it), withSnack)
                        })
                        Text(tr(S.sexNote), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    1 -> {
                        NumberField(tr(S.birthYear), birthYear, { birthYear = it.take(4) })
                        NumberField(tr(S.height), height, { height = it.take(5) })
                    }
                    2 -> {
                        NumberField(tr(S.currentWeight), weight, { weight = it.take(5) })
                        NumberField(tr(S.goalWeight), goal, { goal = it.take(5) })
                    }
                    3 -> {
                        Text(tr(S.activity), style = MaterialTheme.typography.titleMedium)
                        Activity.entries.forEach { a ->
                            Row(Modifier.fillMaxWidth().clickable { activity = a }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = activity == a, onClick = { activity = a })
                                Text(tr(a.label))
                            }
                        }
                    }
                    4 -> {
                        Text(tr(S.pace), style = MaterialTheme.typography.titleMedium)
                        Segmented(listOf(0.25 to "0.25 ${tr(S.kg)}", 0.5 to "0.5 ${tr(S.kg)}", 0.75 to "0.75 ${tr(S.kg)}"), pace, { pace = it })
                        Text(tr(S.floorNote), style = MaterialTheme.typography.bodySmall)
                    }
                    5 -> {
                        Text(tr(S.meals), style = MaterialTheme.typography.titleMedium)
                        Text(tr(S.mealsSub), style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = withSnack, onCheckedChange = { withSnack = it; slots = defaultSlots(lang, it) })
                            Spacer(Modifier.width(8.dp)); Text(tr(S.addSnack))
                        }
                        slots.forEachIndexed { i, s ->
                            AppCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(s.name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                    OutlinedButton(onClick = {
                                        pickTime(ctx, s.minute) { m -> slots = slots.toMutableList().also { it[i] = s.copy(minute = m) } }
                                    }) { Text(minuteLabel(s.minute)) }
                                    Spacer(Modifier.width(8.dp))
                                    Text("${s.pct}%")
                                }
                            }
                        }
                        if (slots.sumOf { it.pct } != 100) Text(tr(S.share100), color = Red)
                    }
                    6 -> PlanSummary(
                        female, birthYear.toInt(), height.toDouble(), weight.toDouble(), goal.toDouble(), activity, pace,
                        slots.map { it.name to it.pct },
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (step > 0) TextButton(onClick = { step-- }) { Text(tr(S.back)) } else Spacer(Modifier.width(1.dp))
                Button(enabled = valid, onClick = {
                    if (step < total - 1) step++ else {
                        val w = weight.toDouble()
                        val profile = Profile(
                            female = female, birthYear = birthYear.toInt(), heightCm = height.toDouble(),
                            startWeight = w, currentWeight = w, goalWeight = goal.toDouble(),
                            activity = activity.ordinal, paceKgWeek = pace, startEpochDay = todayEpoch(),
                        )
                        val mealSlots = slots.sortedBy { it.minute }.mapIndexed { idx, s ->
                            MealSlot(name = s.name, minuteOfDay = s.minute, pct = s.pct, sortOrder = idx)
                        }
                        vm.completeOnboarding(profile, mealSlots)
                        onRequestNotifications()
                    }
                }) { Text(if (step < total - 1) tr(S.next) else tr(S.imIn)) }
            }
        }
    }
}

@Composable
fun PlanSummary(
    female: Boolean, birthYear: Int, heightCm: Double, weight: Double, goal: Double, activity: Activity, pace: Double,
    slots: List<Pair<String, Int>>,
) {
    val plan = Calc.plan(female, birthYear, heightCm, weight, goal, activity, pace)
    val lang = LocalLang.current
    Text(tr(S.yourPlan), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    AppCard(color = MaterialTheme.colorScheme.primaryContainer) {
        Text(tr(S.dailyTarget), style = MaterialTheme.typography.labelLarge)
        Text("${plan.targetKcal} ${tr(S.kcal)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        if (plan.maintenance) Text(tr(S.maintenance))
        Spacer(Modifier.height(8.dp))
        StatRow(tr(S.protein), "${plan.proteinG} ${tr(S.g)}")
        StatRow(tr(S.carbs), "${plan.carbsG} ${tr(S.g)}")
        StatRow(tr(S.fat), "${plan.fatG} ${tr(S.g)}")
        plan.goalDate?.let {
            StatRow(tr(S.goalDate), it.format(DateTimeFormatter.ofPattern("d MMM yyyy")))
            StatRow(tr(S.pace), "${"%.2f".format(plan.paceKgWeek)} ${tr(S.kg)}")
        }
    }
    AppCard {
        Text(tr(S.meals), fontWeight = FontWeight.SemiBold)
        slots.forEach { (name, pct) -> StatRow(name, "${plan.targetKcal * pct / 100} ${tr(S.kcal)}") }
    }
    if (plan.under18) Text(tr(S.under18), color = Red)
    if (plan.paceCapped) Text(tr(S.paceCapped), color = Amber)
    Text(lang.t(S.floorNote) + " (${plan.floorKcal} ${tr(S.kcal)})", style = MaterialTheme.typography.bodySmall)
    Text(tr(S.disclaimer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
