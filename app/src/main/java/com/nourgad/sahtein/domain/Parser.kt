package com.nourgad.sahtein.domain

object Text {
    private val tashkeel = Regex("[\\u064B-\\u0652\\u0640]")

    fun normalize(s: String): String {
        val sb = StringBuilder()
        for (ch in s.lowercase()) {
            sb.append(
                when (ch) {
                    'أ', 'إ', 'آ' -> 'ا'
                    'ة' -> 'ه'
                    'ى' -> 'ي'
                    in '٠'..'٩' -> '0' + (ch - '٠')
                    '،', ',', '+', '.', '؛', ';', '-' -> ' '
                    else -> ch
                }
            )
        }
        return tashkeel.replace(sb.toString(), "").replace(Regex("\\s+"), " ").trim()
    }
}

data class ParsedItem(val food: Food, val qty: Double, val unit: String)
data class ParseResult(val items: List<ParsedItem>, val unmatched: List<String>)

/**
 * Rule-based meal parser (no AI). Understands Egyptian Arabic and English such as
 * "رغيفين فول وبيضة مسلوقة" or "2 loaves foul and an egg".
 */
object MealParser {
    private val numberWords: Map<String, Double> = mapOf(
        "واحد" to 1.0, "واحده" to 1.0, "اتنين" to 2.0, "اثنين" to 2.0, "اتنينه" to 2.0,
        "تلاته" to 3.0, "ثلاثه" to 3.0, "تلات" to 3.0, "اربعه" to 4.0, "اربع" to 4.0,
        "خمسه" to 5.0, "خمس" to 5.0, "سته" to 6.0, "ست" to 6.0, "سبعه" to 7.0, "سبع" to 7.0,
        "تمنيه" to 8.0, "ثمانيه" to 8.0, "تمن" to 8.0, "تسعه" to 9.0, "تسع" to 9.0, "عشره" to 10.0,
        "نص" to 0.5, "نصف" to 0.5, "ربع" to 0.25, "تلت" to 0.33, "ثلث" to 0.33,
        "one" to 1.0, "a" to 1.0, "an" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0,
        "five" to 5.0, "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
        "half" to 0.5, "quarter" to 0.25,
    )

    private val unitWords: Map<String, String> = mapOf(
        "رغيف" to "loaf", "رغفان" to "loaf", "عيشه" to "loaf", "loaf" to "loaf", "loaves" to "loaf",
        "حبه" to "piece", "حبات" to "piece", "قطعه" to "piece", "حته" to "piece", "piece" to "piece", "pieces" to "piece",
        "كوب" to "cup", "كوبايه" to "glass", "كباية" to "glass", "كبايه" to "glass", "cup" to "cup", "cups" to "cup", "glass" to "glass",
        "معلقه" to "tbsp", "معالق" to "tbsp", "tbsp" to "tbsp", "spoon" to "tbsp", "spoons" to "tbsp",
        "معلقه صغيره" to "tsp", "tsp" to "tsp", "teaspoon" to "tsp",
        "طبق" to "plate", "اطباق" to "plate", "plate" to "plate", "plates" to "plate",
        "شريحه" to "slice", "شرايح" to "slice", "slice" to "slice", "slices" to "slice",
        "علبه" to "can", "can" to "can", "كانز" to "can",
        "ساندوتش" to "sandwich", "سندوتش" to "sandwich", "sandwich" to "sandwich",
        "سكوب" to "scoop", "scoop" to "scoop", "كيس" to "bag", "bag" to "bag",
        "حفنه" to "handful", "handful" to "handful",
        "جرام" to "g", "جم" to "g", "gram" to "g", "grams" to "g", "g" to "g",
    )

    private val fillers = setOf(
        "و", "and", "with", "مع", "ب", "كلت", "اكلت", "كلتي", "اكلتي", "شربت", "شربتي", "i", "ate", "had",
        "of", "the", "some", "شويه", "حوالي", "تقريبا", "على", "علي", "في", "فطرت", "اتغديت", "اتعشيت",
    )

    private val foodKeys: List<Pair<String, Food>> by lazy {
        FoodDb.all.flatMap { food ->
            (listOf(food.en, food.ar) + food.synonyms).map { Text.normalize(it) to food }
        }.sortedByDescending { it.first.split(' ').size }
    }

    private fun matchFood(tokens: List<String>, start: Int): Pair<Food, Int>? {
        for (len in 3 downTo 1) {
            if (start + len > tokens.size) continue
            val phrase = tokens.subList(start, start + len).joinToString(" ")
            foodKeys.firstOrNull { it.first == phrase }?.let { return it.second to len }
        }
        return null
    }

    /** Handles Arabic dual forms: رغيفين -> (رغيف, 2), بيضتين -> (بيضه, 2). */
    private fun dual(token: String): String? = when {
        token.length > 4 && token.endsWith("تين") -> token.dropLast(3) + "ه"
        token.length > 3 && token.endsWith("ين") -> token.dropLast(2)
        else -> null
    }

    private fun isKnown(w: String): Boolean =
        numberWords.containsKey(w) || unitWords.containsKey(w) ||
            foodKeys.any { it.first == w || it.first.startsWith("$w ") } ||
            dual(w)?.let { d -> unitWords.containsKey(d) || foodKeys.any { it.first == d } } == true

    private val prefixes = listOf("وبال", "وال", "بال", "وب", "ال", "و", "ب")

    /** Splits Arabic attached prefixes: وبيضة -> و بيضه, بمعلقتين -> ب معلقتين, العيش -> عيش. */
    private fun splitPrefixes(tokens: List<String>): List<String> = tokens.flatMap { t ->
        if (isKnown(t)) return@flatMap listOf(t)
        val p = prefixes.firstOrNull { t.length > it.length + 1 && t.startsWith(it) && isKnown(t.drop(it.length)) }
        if (p != null) listOf("و", t.drop(p.length)) else listOf(t)
    }

    fun parse(input: String): ParseResult {
        val tokens = splitPrefixes(Text.normalize(input).split(' ').filter { it.isNotBlank() })
        val items = mutableListOf<ParsedItem>()
        val unmatched = mutableListOf<String>()
        var qty: Double? = null
        var unit: String? = null
        var i = 0
        while (i < tokens.size) {
            val t = tokens[i]
            // 1) food phrase
            val food = matchFood(tokens, i)
            if (food != null) {
                val (f, len) = food
                if (f.id == "oil" && qty == null && items.lastOrNull()?.food?.id == "foul") {
                    i += len; continue // "فول بالزيت": the oil is already in the foul entry
                }
                val pending = unit
                val n = qty ?: 1.0
                if (pending == "loaf" && f.units.none { it.first == "loaf" }) {
                    // Egyptian style: "رغيفين فول" = two loaves filled with foul
                    items += ParsedItem(FoodDb.byId.getValue("baladi"), n, "loaf")
                    val fillUnit = if (f.units.any { it.first == "sandwich" }) "sandwich" else f.units.first().first
                    items += ParsedItem(f, if (fillUnit == "sandwich") n else 1.0, fillUnit)
                } else {
                    val wanted = if (f.id == "sugar" && pending == "tbsp") "tsp" else pending // "معلقتين سكر" in tea = teaspoons
                    val u = if (wanted != null && f.units.any { it.first == wanted }) wanted else f.units.first().first
                    items += ParsedItem(f, n, u)
                }
                qty = null; unit = null; i += len
                continue
            }
            // 2) number
            val num = t.toDoubleOrNull()
            if (num != null) {
                qty = num; i++; continue
            }
            val word = numberWords[t]
            if (word != null) {
                val current = qty
                qty = if (current != null && word < 1) current + word else word // "واحد ونص"
                i++; continue
            }
            // 3) unit (two-word first)
            val twoWord = if (i + 1 < tokens.size) unitWords["$t ${tokens[i + 1]}"] else null
            if (twoWord != null) {
                unit = twoWord; i += 2; continue
            }
            val oneWord = unitWords[t]
            if (oneWord != null) {
                unit = oneWord; i++; continue
            }
            // 4) dual forms
            val d = dual(t)
            if (d != null) {
                val dualUnit = unitWords[d]
                if (dualUnit != null) {
                    unit = dualUnit; qty = 2.0; i++; continue
                }
                val dualFood = foodKeys.firstOrNull { it.first == d }?.second
                if (dualFood != null) {
                    val pending = unit
                    val u = if (pending != null && dualFood.units.any { it.first == pending }) pending else dualFood.units.first().first
                    items += ParsedItem(dualFood, 2.0, u)
                    qty = null; unit = null; i++; continue
                }
            }
            if (t !in fillers) unmatched += t
            i++
        }
        return ParseResult(merge(items), unmatched)
    }

    private fun merge(items: List<ParsedItem>): List<ParsedItem> =
        items.groupBy { it.food.id to it.unit }.map { (_, list) -> list.first().copy(qty = list.sumOf { it.qty }) }
}
