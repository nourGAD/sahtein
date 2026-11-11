package com.nourgad.sahtein.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.data.dayTarget
import com.nourgad.sahtein.data.displayName
import com.nourgad.sahtein.data.info
import com.nourgad.sahtein.domain.Food
import com.nourgad.sahtein.domain.FoodDb
import com.nourgad.sahtein.domain.MealParser
import com.nourgad.sahtein.domain.S
import com.nourgad.sahtein.domain.Units
import com.nourgad.sahtein.domain.Verdicts
import kotlin.math.roundToInt

private class Draft(val food: Food, qty: Double, unit: String) {
    var qty by mutableStateOf(qty)
    var unit by mutableStateOf(unit)
    fun kcal() = food.kcal * food.gramsFor(unit, qty) / 100.0
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(vm: AppVM, profile: Profile, slotId: Long, day: Long, padding: PaddingValues, startWithVoice: Boolean) {
    val slots by vm.slots.collectAsStateWithLifecycle()
    val allLogs by vm.allLogs.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val slot = slots.firstOrNull { it.id == slotId } ?: return
    val plan = remember(profile) { profile.plan() }
    val dayLogs = allLogs.filter { it.epochDay == day }
    val target = dayTarget(plan, profile, sessions.filter { it.epochDay == day }.sumOf { it.kcal })
    val verdict = Verdicts.verdict(slot.info(), slots.map { it.info() }, dayLogs.map { it.info(lang.arabic) }, target, plan.proteinG)
    var tab by rememberSaveable { mutableIntStateOf(if (startWithVoice) 1 else 0) }
    var adding by remember { mutableStateOf<Food?>(null) }

    Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("${slot.name} · ${tr(S.thisMeal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            val mine = dayLogs.filter { it.slotId == slotId }
            if (mine.isEmpty()) {
                Text("${tr(S.budget)}: ${verdict.budget} ${tr(S.kcal)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                VerdictView(verdict)
            }
        }
        PrimaryTabRow(selectedTabIndex = tab) {
            listOf(S.search, S.voice, S.recent, S.custom).forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(tr(t)) })
            }
        }
        when (tab) {
            0 -> SearchTab(onPick = { adding = it })
            1 -> VoiceTab(onAddAll = { items -> vm.addMany(day, slotId, items, "voice") })
            2 -> RecentTab(vm, onReadd = { vm.readd(day, slotId, it) }, onCopyYesterday = { vm.copyYesterday(day, slotId) })
            3 -> CustomTab(onAdd = { n, k, p, c, f -> vm.addCustom(day, slotId, n, k, p, c, f) })
        }
    }

    adding?.let { food ->
        AddFoodDialog(food, onDismiss = { adding = null }, onAdd = { q, u ->
            vm.addFood(day, slotId, food, q, u, "search"); adding = null
        })
    }
}

@Composable
private fun SearchTab(onPick: (Food) -> Unit) {
    var q by rememberSaveable { mutableStateOf("") }
    val results = remember(q) { FoodDb.search(q) }
    val lang = LocalLang.current
    Column {
        OutlinedTextField(
            q, { q = it }, Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text(tr(S.searchHint)) }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true,
        )
        if (results.isEmpty()) Text(tr(S.noResults), Modifier.padding(16.dp))
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(results, key = { it.id }) { f ->
                Row(Modifier.fillMaxWidth().clickable { onPick(f) }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(f.name(lang))
                        val (u, g) = f.units.first()
                        Text("1 ${Units.label(u, lang)} ≈ ${(f.kcal * g / 100).roundToInt()} ${tr(S.kcal)}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AddFoodDialog(food: Food, onDismiss: () -> Unit, onAdd: (Double, String) -> Unit) {
    val lang = LocalLang.current
    val draft = remember(food) { Draft(food, 1.0, food.units.first().first) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name(lang)) },
        text = { DraftEditor(draft, onRemove = null) },
        confirmButton = { Button(onClick = { onAdd(draft.qty, draft.unit) }) { Text(tr(S.add)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr(S.cancel)) } },
    )
}

@Composable
private fun DraftEditor(d: Draft, onRemove: (() -> Unit)?) {
    val lang = LocalLang.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onRemove != null) Text(d.food.name(lang), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            else Spacer(Modifier.weight(1f))
            Text("${d.kcal().roundToInt()} ${tr(S.kcal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (onRemove != null) IconButton(onClick = onRemove) { Icon(Icons.Default.Close, tr(S.delete)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val step = if (d.unit == "g") 10.0 else 0.5
            IconButton(onClick = { d.qty = (d.qty - step).coerceAtLeast(step) }) { Icon(Icons.Default.Remove, null) }
            Text(fmt(d.qty), style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(56.dp))
            IconButton(onClick = { d.qty += step }) { Icon(Icons.Default.Add, null) }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            d.food.units.forEach { (u, grams) ->
                FilterChip(selected = d.unit == u, onClick = {
                    if (u != d.unit) {
                        // keep roughly the same amount when switching unit
                        val g = d.food.gramsFor(d.unit, d.qty)
                        d.unit = u
                        d.qty = if (u == "g") (g / 10).roundToInt() * 10.0 else maxOf(0.5, (g / grams * 2).roundToInt() / 2.0)
                    }
                }, label = { Text(Units.label(u, lang)) })
            }
        }
    }
}

@Composable
private fun VoiceTab(onAddAll: (List<Triple<Food, Double, String>>) -> Unit) {
    val lang = LocalLang.current
    val ctx = LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    var drafts by remember { mutableStateOf<List<Draft>>(emptyList()) }
    var unmatched by remember { mutableStateOf<List<String>>(emptyList()) }

    fun runParse(s: String) {
        val r = MealParser.parse(s)
        drafts = r.items.map { Draft(it.food, it.qty, it.unit) }
        unmatched = r.unmatched
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val spoken = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) { text = spoken; runParse(spoken) }
        }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                LargeFloatingActionButton(onClick = {
                    val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang.arabic) "ar-EG" else "en-US")
                        .putExtra(RecognizerIntent.EXTRA_PROMPT, lang.t(S.speak))
                    try { launcher.launch(i) } catch (_: ActivityNotFoundException) {
                        Toast.makeText(ctx, lang.t(S.voiceUnavailable), Toast.LENGTH_LONG).show()
                    }
                }) { Icon(Icons.Default.Mic, tr(S.speak), Modifier.size(36.dp)) }
                Spacer(Modifier.height(8.dp))
                Text(tr(S.speak), style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), placeholder = { Text(tr(S.typeMeal)) }, minLines = 2)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { runParse(text) }, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(tr(S.parse)) }
        }
        items(drafts.size) { i ->
            val d = drafts[i]
            AppCard { DraftEditor(d, onRemove = { drafts = drafts.filterIndexed { j, _ -> j != i } }) }
        }
        if (unmatched.isNotEmpty()) item {
            Text("${tr(S.notUnderstood)} ${unmatched.joinToString("، ")}", color = Amber)
            Text(tr(S.searchThem), style = MaterialTheme.typography.bodySmall)
        }
        if (drafts.isNotEmpty()) item {
            val total = drafts.sumOf { it.kcal() }.roundToInt()
            Button(onClick = {
                onAddAll(drafts.map { Triple(it.food, it.qty, it.unit) })
                drafts = emptyList(); unmatched = emptyList(); text = ""
            }, modifier = Modifier.fillMaxWidth()) { Text("${tr(S.addAll)} · $total ${tr(S.kcal)}") }
        }
    }
}

@Composable
private fun RecentTab(vm: AppVM, onReadd: (com.nourgad.sahtein.data.FoodLog) -> Unit, onCopyYesterday: () -> Unit) {
    val recent by vm.recent.collectAsStateWithLifecycle()
    val lang = LocalLang.current
    val distinct = remember(recent) { recent.distinctBy { Triple(it.foodId, it.name, it.qty to it.unit) }.take(40) }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            OutlinedButton(onClick = onCopyYesterday, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text(tr(S.copyYesterday)) }
        }
        items(distinct, key = { it.id }) { log ->
            Row(Modifier.fillMaxWidth().clickable { onReadd(log) }.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(log.displayName(lang.arabic))
                    if (log.foodId != "custom") Text("${fmt(log.qty)} ${Units.label(log.unit, lang)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${log.kcal.roundToInt()} ${tr(S.kcal)}")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider()
        }
        if (distinct.isEmpty()) item { Text(tr(S.noData), Modifier.padding(16.dp)) }
    }
}

@Composable
private fun CustomTab(onAdd: (String, Double, Double, Double, Double) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var kcal by rememberSaveable { mutableStateOf("") }
    var p by rememberSaveable { mutableStateOf("") }
    var c by rememberSaveable { mutableStateOf("") }
    var f by rememberSaveable { mutableStateOf("") }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(tr(S.foodName)) }, singleLine = true)
        NumberField(tr(S.kcal), kcal, { kcal = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(tr(S.protein), p, { p = it }, Modifier.weight(1f))
            NumberField(tr(S.carbs), c, { c = it }, Modifier.weight(1f))
            NumberField(tr(S.fat), f, { f = it }, Modifier.weight(1f))
        }
        Button(
            enabled = name.isNotBlank() && kcal.toDoubleOrNull() != null,
            onClick = {
                onAdd(name.trim(), kcal.toDouble(), p.toDoubleOrNull() ?: 0.0, c.toDoubleOrNull() ?: 0.0, f.toDoubleOrNull() ?: 0.0)
                name = ""; kcal = ""; p = ""; c = ""; f = ""
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(tr(S.add)) }
    }
}
