package com.nourgad.sahtein.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nourgad.sahtein.data.MealSlot
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.domain.Activity
import com.nourgad.sahtein.domain.S
import com.nourgad.sahtein.notify.Alarms

@Composable
fun SettingsScreen(vm: AppVM, profile: Profile, padding: PaddingValues) {
    val savedSlots by vm.slots.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val lang = LocalLang.current
    var birth by remember(profile) { mutableStateOf(profile.birthYear.toString()) }
    var height by remember(profile) { mutableStateOf(fmt(profile.heightCm)) }
    var current by remember(profile) { mutableStateOf(fmt(profile.currentWeight)) }
    var goal by remember(profile) { mutableStateOf(fmt(profile.goalWeight)) }
    var slots by remember(savedSlots) { mutableStateOf(savedSlots) }
    var confirmWipe by remember { mutableStateOf(false) }
    val plan = profile.plan()

    fun save(p: Profile) = vm.saveProfile(p)

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard(color = MaterialTheme.colorScheme.primaryContainer) {
                Text(tr(S.dailyTarget), style = MaterialTheme.typography.labelLarge)
                Text("${plan.targetKcal} ${tr(S.kcal)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("${tr(S.protein)} ${plan.proteinG} · ${tr(S.carbs)} ${plan.carbsG} · ${tr(S.fat)} ${plan.fatG} ${tr(S.g)}")
            }
        }
        item { SectionTitle(tr(S.profile)) }
        item {
            AppCard {
                Text(tr(S.sex), fontWeight = FontWeight.SemiBold)
                Segmented(listOf(false to tr(S.male), true to tr(S.female)), profile.female, { save(profile.copy(female = it)) })
                Text(tr(S.sexNote), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(tr(S.birthYear), birth, { birth = it.take(4) })
                    NumberField(tr(S.height), height, { height = it.take(5) })
                    NumberField(tr(S.currentWeight), current, { current = it.take(5) })
                    NumberField(tr(S.goalWeight), goal, { goal = it.take(5) })
                    Button(onClick = {
                        val b = birth.toIntOrNull(); val h = height.toDoubleOrNull(); val c = current.toDoubleOrNull(); val g = goal.toDoubleOrNull()
                        if (b != null && h != null && c != null && g != null && h in 120.0..230.0 && c in 30.0..300.0 && g in 30.0..300.0) {
                            if (c != profile.currentWeight) vm.logWeight(c)
                            save(profile.copy(birthYear = b, heightCm = h, currentWeight = c, goalWeight = g))
                        }
                    }) { Text(tr(S.save)) }
                }
            }
        }
        item {
            AppCard {
                Text(tr(S.activity), fontWeight = FontWeight.SemiBold)
                Activity.entries.forEach { a ->
                    Row(Modifier.fillMaxWidth().clickable { save(profile.copy(activity = a.ordinal)) }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(profile.activity == a.ordinal, onClick = { save(profile.copy(activity = a.ordinal)) })
                        Text(tr(a.label), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(tr(S.pace), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                Segmented(listOf(0.25 to "0.25", 0.5 to "0.5", 0.75 to "0.75"), profile.paceKgWeek, { save(profile.copy(paceKgWeek = it)) })
            }
        }
        item { SectionTitle(tr(S.mealsAndReminders)) }
        item {
            AppCard {
                slots.forEachIndexed { i, s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(s.name, { v -> slots = slots.toMutableList().also { it[i] = s.copy(name = v) } },
                            Modifier.weight(1.4f), singleLine = true)
                        Spacer(Modifier.width(6.dp))
                        OutlinedButton(onClick = {
                            pickTime(ctx, s.minuteOfDay) { m -> slots = slots.toMutableList().also { it[i] = s.copy(minuteOfDay = m) } }
                        }) { Text(minuteLabel(s.minuteOfDay)) }
                        Spacer(Modifier.width(6.dp))
                        NumberField("%", s.pct.toString(), { v -> slots = slots.toMutableList().also { it[i] = s.copy(pct = v.filter(Char::isDigit).take(3).toIntOrNull() ?: 0) } },
                            Modifier.weight(0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tr(S.reminder), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Switch(s.reminderOn, { on -> slots = slots.toMutableList().also { it[i] = s.copy(reminderOn = on) } })
                        if (slots.size > 1) IconButton(onClick = { slots = slots.filterIndexed { j, _ -> j != i } }) {
                            Icon(Icons.Default.Delete, tr(S.delete))
                        }
                    }
                }
                TextButton(onClick = {
                    slots = slots + MealSlot(name = lang.t(S.snack), minuteOfDay = 18 * 60, pct = 0, sortOrder = slots.size)
                }) { Text("+ ${tr(S.addSnack)}") }
                val sum = slots.sumOf { it.pct }
                if (sum != 100) Text("${tr(S.share100)} ($sum%)", color = Red)
                Button(enabled = sum == 100 && slots.all { it.name.isNotBlank() }, onClick = {
                    vm.saveSlots(slots.sortedBy { it.minuteOfDay })
                }) { Text(tr(S.save)) }
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tr(S.summaryTime), Modifier.weight(1f))
                    OutlinedButton(onClick = { pickTime(ctx, profile.summaryMinute) { m -> save(profile.copy(summaryMinute = m)) } }) {
                        Text(minuteLabel(profile.summaryMinute))
                    }
                }
                Text(tr(S.addBack), Modifier.padding(top = 8.dp))
                Segmented(listOf(0 to tr(S.off), 50 to tr(S.half)), profile.workoutAddBackPct, { save(profile.copy(workoutAddBackPct = it)) })
                if (!Alarms.canExact(ctx) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Spacer(Modifier.width(1.dp))
                    OutlinedButton(onClick = {
                        ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")))
                    }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(tr(S.exactAlarm)) }
                    Text(tr(S.exactAlarmSub), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Text(tr(S.disclaimer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { confirmWipe = true }) {
                Text(lang.pick("Delete all my data", "امسح كل بياناتي"), color = Red)
            }
        }
    }

    if (confirmWipe) AlertDialog(
        onDismissRequest = { confirmWipe = false },
        title = { Text(lang.pick("Delete everything?", "تمسح كل حاجة؟")) },
        text = { Text(lang.pick("This removes your profile, logs, weights and workouts from this phone.", "ده هيمسح بياناتك وأكلك ووزنك وتمارينك من الموبايل.")) },
        confirmButton = { Button(onClick = { confirmWipe = false; vm.wipe() }) { Text(tr(S.delete)) } },
        dismissButton = { TextButton(onClick = { confirmWipe = false }) { Text(tr(S.cancel)) } },
    )
}
