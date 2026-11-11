package com.nourgad.sahtein.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nourgad.sahtein.domain.LangCtx
import com.nourgad.sahtein.domain.MealVerdict
import com.nourgad.sahtein.domain.S
import com.nourgad.sahtein.domain.Tx
import com.nourgad.sahtein.domain.Units
import com.nourgad.sahtein.domain.VerdictKind
import kotlin.math.roundToInt

val LocalLang = compositionLocalOf { LangCtx(arabic = false, female = false) }

@Composable
fun tr(tx: Tx): String = LocalLang.current.t(tx)

fun fmt(d: Double): String = if (d == d.roundToInt().toDouble()) d.roundToInt().toString() else "%.1f".format(d)

fun minuteLabel(m: Int): String = "%02d:%02d".format(m / 60, m % 60)

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp))
}

@Composable
fun AppCard(modifier: Modifier = Modifier, color: Color? = null, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun CalorieRing(eaten: Int, target: Int, modifier: Modifier = Modifier) {
    val frac = if (target > 0) eaten.toFloat() / target else 0f
    val color = when {
        frac > 1.1f -> Red
        frac > 0.9f -> Green
        else -> MaterialTheme.colorScheme.primary
    }
    val track = MaterialTheme.colorScheme.surfaceVariant
    val lang = LocalLang.current
    Box(modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(170.dp)) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            drawArc(track, 0f, 360f, false, style = stroke)
            drawArc(color, -90f, 360f * frac.coerceAtMost(1f), false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val left = target - eaten
            Text(if (left >= 0) "$left" else "+${-left}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("${lang.t(S.kcal)} ${if (left >= 0) lang.t(S.remaining) else lang.t(S.over)}", style = MaterialTheme.typography.bodyMedium)
            Text("$eaten / $target", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MacroBar(label: String, value: Double, target: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${value.roundToInt()}/${target}${tr(S.g)}", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (target > 0) (value / target).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            drawStopIndicator = {},
        )
    }
}

@Composable
fun VerdictView(v: MealVerdict) {
    val lang = LocalLang.current
    val (title, color) = when (v.kind) {
        VerdictKind.ON_TRACK -> lang.t(S.onTrack) to Green
        VerdictKind.OVER -> lang.t(S.overBudget) to Red
        VerdictKind.UNDER -> lang.t(S.underBudget) to Amber
        VerdictKind.EMPTY -> return
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.12f)).padding(12.dp)
    ) {
        Text(title, color = color, fontWeight = FontWeight.Bold)
        Text("${v.eaten} / ${v.budget} ${lang.t(S.kcal)}", style = MaterialTheme.typography.bodySmall)
        when (v.kind) {
            VerdictKind.OVER -> if (v.cutItemName != null) {
                val unit = Units.label(v.cutUnit ?: "g", lang)
                Text(
                    lang.pick(
                        "Tip: cut ${fmt(v.cutUnits)} $unit of ${v.cutItemName} to save ~${v.cutSaves} kcal.",
                        "اقتراح: قلل ${fmt(v.cutUnits)} $unit من ${v.cutItemName} وهتوفر حوالي ${v.cutSaves} سعر.",
                    ).let { if (lang.arabic && lang.female) it.replace("قلل", "قللي").replace("وهتوفر", "وهتوفري") else it },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            VerdictKind.UNDER -> Text(lang.t(S.underTip), style = MaterialTheme.typography.bodyMedium)
            else -> {}
        }
        if (v.lowProtein) {
            Spacer(Modifier.height(6.dp))
            Text(lang.t(S.lowProtein), color = Purple, fontWeight = FontWeight.SemiBold)
            Text(lang.t(S.proteinTip), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { s -> onChange(s.filter { it.isDigit() || it == '.' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        options.forEachIndexed { i, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(i, options.size),
            ) { Text(label) }
        }
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
