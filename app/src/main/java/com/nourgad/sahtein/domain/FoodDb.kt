package com.nourgad.sahtein.domain

/**
 * Built-in food database. Values are per 100 g (or 100 ml for drinks).
 * Sources: USDA FoodData Central for generic foods; typical Egyptian recipes
 * for local dishes. Must be reviewed by a nutritionist before public release.
 *
 * units: household unit key -> grams for ONE unit. The first unit is the default.
 */
data class Food(
    val id: String,
    val en: String,
    val ar: String,
    val kcal: Double,
    val p: Double,
    val c: Double,
    val f: Double,
    val units: List<Pair<String, Double>>,
    val synonyms: List<String> = emptyList(),
) {
    fun gramsFor(unit: String, qty: Double): Double =
        (units.firstOrNull { it.first == unit }?.second ?: 1.0) * qty

    fun name(lang: LangCtx) = if (lang.arabic) ar else en
}

object Units {
    val labels: Map<String, Tx> = mapOf(
        "g" to Tx("g", "جم"),
        "piece" to Tx("piece", "حبة"),
        "loaf" to Tx("loaf", "رغيف"),
        "cup" to Tx("cup", "كوب"),
        "glass" to Tx("glass", "كوباية"),
        "tbsp" to Tx("tbsp", "معلقة كبيرة"),
        "tsp" to Tx("tsp", "معلقة صغيرة"),
        "plate" to Tx("plate", "طبق"),
        "bowl" to Tx("bowl", "طبق شوربة"),
        "slice" to Tx("slice", "شريحة"),
        "handful" to Tx("handful", "حفنة"),
        "can" to Tx("can", "علبة"),
        "scoop" to Tx("scoop", "سكوب"),
        "sandwich" to Tx("sandwich", "ساندوتش"),
        "bag" to Tx("bag", "كيس"),
    )

    fun label(unit: String, lang: LangCtx) = labels[unit]?.let { lang.t(it) } ?: unit
}

private fun u(vararg pairs: Pair<String, Double>): List<Pair<String, Double>> = pairs.toList() + ("g" to 1.0)

object FoodDb {
    val all: List<Food> = listOf(
        // ---------- Bread & breakfast ----------
        Food("baladi", "Baladi bread", "عيش بلدي", 250.0, 9.0, 52.0, 1.5, u("loaf" to 100.0), listOf("عيش", "خبز", "bread", "3eish", "aish", "baladi")),
        Food("fino", "Fino bread", "عيش فينو", 280.0, 9.0, 55.0, 3.0, u("piece" to 60.0), listOf("فينو", "fino")),
        Food("toast", "Toast bread", "عيش توست", 265.0, 9.0, 49.0, 3.2, u("slice" to 25.0), listOf("توست", "toast")),
        Food("croissant", "Croissant", "كرواسون", 406.0, 8.0, 46.0, 21.0, u("piece" to 60.0), listOf("كرواسون", "croissant")),
        Food("foul", "Foul medames (with oil)", "فول مدمس بالزيت", 150.0, 7.0, 17.0, 6.0, u("plate" to 250.0, "cup" to 170.0, "tbsp" to 15.0, "sandwich" to 120.0), listOf("فول", "foul", "ful", "fava")),
        Food("foul_plain", "Foul medames (plain)", "فول سادة", 110.0, 7.6, 19.6, 0.5, u("plate" to 250.0, "cup" to 170.0, "tbsp" to 15.0), listOf("فول سادة", "فول من غير زيت")),
        Food("tameya", "Ta'meya (falafel)", "طعمية", 330.0, 13.0, 30.0, 18.0, u("piece" to 25.0, "sandwich" to 75.0), listOf("طعمية", "فلافل", "tameya", "ta3meya", "falafel")),
        Food("bessara", "Bessara", "بصارة", 110.0, 6.0, 15.0, 3.0, u("bowl" to 250.0, "tbsp" to 20.0), listOf("بصارة", "bessara")),
        Food("shakshouka", "Shakshouka", "شكشوكة", 110.0, 6.5, 5.0, 7.0, u("plate" to 250.0), listOf("شكشوكة", "shakshuka")),
        Food("egg_boiled", "Boiled egg", "بيض مسلوق", 155.0, 13.0, 1.1, 11.0, u("piece" to 50.0), listOf("بيضة", "بيض", "egg", "eggs", "بيضه مسلوقة")),
        Food("egg_fried", "Fried egg", "بيض مقلي", 196.0, 13.6, 0.8, 15.0, u("piece" to 46.0), listOf("بيض مقلي", "بيضة مقلية", "fried egg")),
        Food("omelette", "Omelette", "أومليت", 154.0, 11.0, 1.0, 12.0, u("piece" to 60.0), listOf("اومليت", "omelette", "omelet", "عجة")),
        Food("white_cheese", "White cheese (Domiati)", "جبنة بيضا", 260.0, 14.0, 3.0, 21.0, u("piece" to 30.0, "tbsp" to 20.0), listOf("جبنة", "جبنه", "دمياطي", "cheese", "gebna", "feta")),
        Food("roumi", "Roumi cheese", "جبنة رومي", 390.0, 27.0, 2.0, 31.0, u("slice" to 20.0), listOf("رومي", "roumi")),
        Food("areesh", "Cottage cheese (areesh)", "جبنة قريش", 98.0, 11.0, 3.4, 4.3, u("tbsp" to 20.0, "cup" to 220.0), listOf("قريش", "cottage")),
        Food("halawa", "Halawa (tahini halva)", "حلاوة طحينية", 520.0, 12.0, 50.0, 31.0, u("tbsp" to 20.0), listOf("حلاوة", "halawa", "halva")),
        Food("honey", "Honey", "عسل نحل", 304.0, 0.3, 82.0, 0.0, u("tbsp" to 21.0, "tsp" to 7.0), listOf("عسل", "honey")),
        Food("jam", "Jam", "مربى", 250.0, 0.4, 65.0, 0.1, u("tbsp" to 20.0), listOf("مربى", "مربة", "jam")),
        Food("oats", "Oats (dry)", "شوفان", 389.0, 17.0, 66.0, 7.0, u("cup" to 80.0, "tbsp" to 10.0), listOf("شوفان", "oats", "oatmeal")),
        Food("peanut_butter", "Peanut butter", "زبدة فول سوداني", 588.0, 25.0, 20.0, 50.0, u("tbsp" to 16.0), listOf("زبدة فول سوداني", "peanut butter")),

        // ---------- Egyptian mains ----------
        Food("koshary", "Koshary", "كشري", 150.0, 5.0, 27.0, 2.5, u("plate" to 450.0, "bowl" to 350.0), listOf("كشري", "كشرى", "koshary", "koshari", "kushari")),
        Food("mahshi", "Mahshi (stuffed vegetables)", "محشي", 150.0, 3.0, 22.0, 5.5, u("piece" to 30.0, "plate" to 300.0), listOf("محشي", "ورق عنب", "كرنب", "mahshi")),
        Food("molokhia", "Molokhia", "ملوخية", 45.0, 2.5, 4.0, 2.2, u("bowl" to 250.0), listOf("ملوخية", "ملوخيه", "molokhia")),
        Food("fatta", "Fatta", "فتة", 230.0, 10.0, 24.0, 11.0, u("plate" to 350.0), listOf("فتة", "فته", "fatta")),
        Food("bechamel", "Macarona bechamel", "مكرونة بشاميل", 210.0, 9.0, 20.0, 10.0, u("piece" to 200.0), listOf("بشاميل", "bechamel")),
        Food("hawawshi", "Hawawshi", "حواوشي", 260.0, 12.0, 22.0, 14.0, u("piece" to 250.0), listOf("حواوشي", "hawawshi")),
        Food("feteer", "Feteer meshaltet", "فطير مشلتت", 400.0, 7.0, 40.0, 24.0, u("slice" to 80.0), listOf("فطير", "فطيرة", "feteer")),
        Food("mesaqaa", "Mesaqaa (moussaka)", "مسقعة", 120.0, 2.0, 8.0, 9.0, u("plate" to 250.0), listOf("مسقعة", "moussaka")),
        Food("bamia", "Okra with meat", "بامية باللحمة", 110.0, 7.0, 6.0, 6.5, u("plate" to 300.0), listOf("بامية", "bamia", "okra")),
        Food("fasolia", "Green beans with meat", "فاصوليا خضرا", 90.0, 5.0, 7.0, 5.0, u("plate" to 300.0), listOf("فاصوليا", "green beans")),
        Food("lentil_soup", "Lentil soup", "شوربة عدس", 70.0, 4.5, 11.0, 1.2, u("bowl" to 300.0), listOf("عدس", "شوربة عدس", "lentil")),
        Food("chicken_soup", "Chicken soup", "شوربة فراخ", 35.0, 3.0, 2.0, 1.5, u("bowl" to 300.0), listOf("شوربة", "شوربة فراخ", "soup")),
        Food("hamam", "Stuffed pigeon", "حمام محشي", 250.0, 15.0, 15.0, 15.0, u("piece" to 300.0), listOf("حمام", "pigeon")),
        Food("kebda", "Alexandrian liver", "كبدة اسكندراني", 175.0, 20.0, 5.0, 8.0, u("sandwich" to 80.0, "plate" to 200.0), listOf("كبدة", "kebda", "liver")),
        Food("shawerma", "Chicken shawerma sandwich", "شاورما فراخ", 210.0, 13.0, 18.0, 9.0, u("sandwich" to 250.0), listOf("شاورما", "شاورمة", "shawarma", "shawerma")),

        // ---------- Rice, pasta, potatoes ----------
        Food("rice_egy", "Egyptian rice with vermicelli", "رز بالشعرية", 170.0, 3.0, 30.0, 4.5, u("plate" to 250.0, "cup" to 160.0, "tbsp" to 15.0), listOf("رز", "ارز", "رز بالشعرية", "rice")),
        Food("rice_white", "Plain white rice", "رز أبيض سادة", 130.0, 2.7, 28.0, 0.3, u("plate" to 250.0, "cup" to 160.0, "tbsp" to 15.0), listOf("رز ابيض", "رز سادة", "white rice")),
        Food("pasta", "Boiled pasta", "مكرونة مسلوقة", 158.0, 5.8, 31.0, 0.9, u("plate" to 250.0, "cup" to 140.0), listOf("مكرونة", "مكرونه", "pasta", "macaroni")),
        Food("pasta_sauce", "Pasta with red sauce", "مكرونة بالصلصة", 130.0, 4.0, 22.0, 3.0, u("plate" to 300.0), listOf("مكرونة بالصلصة", "مكرونة صلصة")),
        Food("fries", "French fries", "بطاطس محمرة", 312.0, 3.4, 41.0, 15.0, u("plate" to 150.0, "cup" to 100.0), listOf("بطاطس محمرة", "بطاطس", "فرايز", "fries", "chips")),
        Food("potato_boiled", "Boiled potato", "بطاطس مسلوقة", 87.0, 1.9, 20.0, 0.1, u("piece" to 150.0), listOf("بطاطس مسلوقة", "boiled potato")),
        Food("sweet_potato", "Baked sweet potato", "بطاطا مشوية", 90.0, 2.0, 21.0, 0.2, u("piece" to 200.0), listOf("بطاطا", "sweet potato")),
        Food("corn", "Grilled corn", "درة مشوي", 96.0, 3.4, 21.0, 1.5, u("piece" to 100.0), listOf("درة", "دره", "corn")),

        // ---------- Protein ----------
        Food("chicken_breast", "Grilled chicken breast", "صدور فراخ مشوية", 165.0, 31.0, 0.0, 3.6, u("piece" to 120.0), listOf("صدور", "صدر فراخ", "فراخ", "chicken", "chicken breast")),
        Food("chicken_quarter", "Grilled chicken quarter", "ربع فرخة مشوي", 215.0, 27.0, 0.0, 12.0, u("piece" to 200.0), listOf("ربع فرخة", "فرخة", "chicken quarter")),
        Food("pane", "Chicken pané", "فراخ بانيه", 260.0, 22.0, 12.0, 14.0, u("piece" to 100.0), listOf("بانيه", "pane", "schnitzel")),
        Food("beef", "Cooked beef", "لحمة", 250.0, 26.0, 0.0, 15.0, u("piece" to 50.0), listOf("لحمة", "لحمه", "beef", "meat")),
        Food("kofta", "Grilled kofta", "كفتة مشوية", 270.0, 18.0, 4.0, 20.0, u("piece" to 40.0), listOf("كفتة", "كفته", "kofta")),
        Food("fish_grilled", "Grilled fish (tilapia)", "سمك مشوي", 128.0, 26.0, 0.0, 2.7, u("piece" to 200.0), listOf("سمك", "بلطي", "fish")),
        Food("fish_fried", "Fried fish", "سمك مقلي", 230.0, 20.0, 8.0, 13.0, u("piece" to 200.0), listOf("سمك مقلي", "fried fish")),
        Food("tuna", "Tuna in oil (drained)", "تونة", 200.0, 29.0, 0.0, 8.0, u("can" to 120.0), listOf("تونة", "تونه", "tuna")),
        Food("whey", "Whey protein", "بروتين واي", 400.0, 80.0, 8.0, 6.0, u("scoop" to 30.0), listOf("بروتين", "واي", "whey", "protein")),
        Food("termes", "Lupini beans", "ترمس", 119.0, 15.6, 10.0, 2.9, u("cup" to 160.0), listOf("ترمس", "lupini")),

        // ---------- Dairy ----------
        Food("milk", "Full-fat milk", "لبن كامل الدسم", 61.0, 3.2, 4.8, 3.3, u("glass" to 250.0, "cup" to 240.0), listOf("لبن", "حليب", "milk")),
        Food("yoghurt", "Plain yoghurt", "زبادي", 61.0, 3.5, 4.7, 3.3, u("piece" to 105.0), listOf("زبادي", "زبادى", "yoghurt", "yogurt")),
        Food("greek_yoghurt", "Greek yoghurt", "زبادي يوناني", 97.0, 9.0, 4.0, 5.0, u("piece" to 170.0), listOf("يوناني", "greek")),

        // ---------- Dips & salads ----------
        Food("tahini_salad", "Tahini salad", "سلطة طحينة", 230.0, 6.0, 7.0, 20.0, u("tbsp" to 15.0), listOf("طحينة", "طحينه", "tahini")),
        Food("baba", "Baba ghanoug", "بابا غنوج", 120.0, 2.0, 8.0, 9.0, u("tbsp" to 20.0), listOf("بابا غنوج", "baba")),
        Food("salad", "Green salad (no oil)", "سلطة خضرا", 25.0, 1.0, 5.0, 0.2, u("plate" to 200.0), listOf("سلطة", "سلطه", "salad")),
        Food("cucumber", "Cucumber", "خيار", 15.0, 0.7, 3.6, 0.1, u("piece" to 120.0), listOf("خيار", "خياره", "cucumber")),
        Food("tomato", "Tomato", "طماطم", 18.0, 0.9, 3.9, 0.2, u("piece" to 120.0), listOf("طماطم", "طماطمة", "tomato")),

        // ---------- Fruit ----------
        Food("banana", "Banana", "موز", 89.0, 1.1, 23.0, 0.3, u("piece" to 120.0), listOf("موز", "موزة", "banana")),
        Food("apple", "Apple", "تفاح", 52.0, 0.3, 14.0, 0.2, u("piece" to 180.0), listOf("تفاح", "تفاحة", "apple")),
        Food("orange", "Orange", "برتقال", 47.0, 0.9, 12.0, 0.1, u("piece" to 150.0), listOf("برتقال", "برتقانة", "برتقالة", "orange")),
        Food("dates", "Dates", "بلح", 282.0, 2.5, 75.0, 0.4, u("piece" to 8.0), listOf("بلح", "تمر", "بلحة", "dates")),
        Food("guava", "Guava", "جوافة", 68.0, 2.6, 14.0, 1.0, u("piece" to 100.0), listOf("جوافة", "جوافه", "guava")),
        Food("mango", "Mango", "مانجا", 60.0, 0.8, 15.0, 0.4, u("piece" to 250.0), listOf("مانجا", "مانجو", "mango")),
        Food("watermelon", "Watermelon", "بطيخ", 30.0, 0.6, 7.6, 0.2, u("slice" to 300.0), listOf("بطيخ", "watermelon")),
        Food("grapes", "Grapes", "عنب", 69.0, 0.7, 18.0, 0.2, u("cup" to 150.0), listOf("عنب", "grapes")),
        Food("strawberry", "Strawberries", "فراولة", 32.0, 0.7, 7.7, 0.3, u("cup" to 150.0), listOf("فراولة", "فراوله", "strawberry")),

        // ---------- Nuts & snacks ----------
        Food("peanuts", "Peanuts", "سوداني", 567.0, 26.0, 16.0, 49.0, u("handful" to 30.0), listOf("سوداني", "فول سوداني", "peanuts")),
        Food("almonds", "Almonds", "لوز", 579.0, 21.0, 22.0, 50.0, u("handful" to 30.0, "piece" to 1.2), listOf("لوز", "almonds")),
        Food("chocolate", "Chocolate bar", "شوكولاتة", 535.0, 7.6, 59.0, 30.0, u("piece" to 40.0), listOf("شوكولاتة", "شوكولاته", "chocolate")),
        Food("biscuits", "Biscuits", "بسكويت", 480.0, 6.0, 68.0, 20.0, u("piece" to 10.0), listOf("بسكوت", "بسكويت", "biscuits", "cookies")),
        Food("chipsy", "Potato chips (bag)", "شيبسي", 536.0, 7.0, 53.0, 34.0, u("bag" to 35.0), listOf("شيبسي", "chipsy", "crisps")),

        // ---------- Sweets ----------
        Food("basbousa", "Basbousa", "بسبوسة", 360.0, 4.0, 55.0, 14.0, u("piece" to 70.0), listOf("بسبوسة", "basbousa")),
        Food("konafa", "Konafa", "كنافة", 390.0, 5.0, 50.0, 19.0, u("piece" to 100.0), listOf("كنافة", "konafa", "kunafa")),
        Food("roz_laban", "Rice pudding", "رز بلبن", 130.0, 3.5, 22.0, 3.0, u("cup" to 200.0), listOf("رز بلبن", "rice pudding")),
        Food("om_ali", "Om Ali", "أم علي", 250.0, 6.0, 28.0, 13.0, u("bowl" to 200.0), listOf("ام علي", "om ali")),

        // ---------- Fast food ----------
        Food("pizza", "Pizza", "بيتزا", 266.0, 11.0, 33.0, 10.0, u("slice" to 110.0), listOf("بيتزا", "pizza")),
        Food("burger", "Burger sandwich", "برجر", 250.0, 13.0, 24.0, 11.0, u("sandwich" to 220.0), listOf("برجر", "برجر ساندوتش", "burger")),

        // ---------- Drinks ----------
        Food("tea", "Tea (no sugar)", "شاي من غير سكر", 1.0, 0.0, 0.3, 0.0, u("cup" to 200.0), listOf("شاي", "شاى", "tea")),
        Food("coffee", "Turkish coffee (no sugar)", "قهوة سادة", 2.0, 0.1, 0.3, 0.0, u("cup" to 60.0), listOf("قهوة", "قهوه", "coffee")),
        Food("sugar", "Sugar", "سكر", 400.0, 0.0, 100.0, 0.0, u("tsp" to 5.0, "tbsp" to 12.0), listOf("سكر", "sugar")),
        Food("soda", "Soft drink (can)", "مشروب غازي", 42.0, 0.0, 10.6, 0.0, u("can" to 330.0), listOf("بيبسي", "كوكاكولا", "pepsi", "cola", "soda")),
        Food("orange_juice", "Fresh orange juice", "عصير برتقال", 45.0, 0.7, 10.4, 0.2, u("glass" to 250.0), listOf("عصير برتقال", "orange juice", "عصير")),
        Food("cane_juice", "Sugarcane juice", "عصير قصب", 73.0, 0.2, 18.0, 0.0, u("glass" to 300.0), listOf("قصب", "sugarcane")),

        // ---------- Fats ----------
        Food("oil", "Oil / olive oil", "زيت", 884.0, 0.0, 0.0, 100.0, u("tbsp" to 13.5, "tsp" to 4.5), listOf("زيت", "زيت زيتون", "oil", "olive oil")),
        Food("butter", "Butter", "زبدة", 717.0, 0.9, 0.1, 81.0, u("tbsp" to 14.0), listOf("زبدة", "زبده", "butter")),
        Food("ghee", "Ghee", "سمنة", 900.0, 0.0, 0.0, 100.0, u("tbsp" to 13.0), listOf("سمنة", "سمنه", "ghee")),
    )

    val byId: Map<String, Food> = all.associateBy { it.id }

    /** Fuzzy search over names and synonyms in both languages. */
    fun search(query: String, limit: Int = 30): List<Food> {
        val q = Text.normalize(query)
        if (q.isBlank()) return all.take(limit)
        return all.mapNotNull { food ->
            val keys = (listOf(food.en, food.ar) + food.synonyms).map(Text::normalize)
            val score = keys.maxOf { k ->
                when {
                    k == q -> 100
                    k.startsWith(q) -> 80
                    k.split(' ').any { it.startsWith(q) } -> 60
                    k.contains(q) -> 40
                    else -> 0
                }
            }
            if (score > 0) food to score else null
        }.sortedByDescending { it.second }.map { it.first }.take(limit)
    }
}
