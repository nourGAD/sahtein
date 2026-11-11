package com.nourgad.sahtein.domain

import java.util.Locale

/**
 * Tiny bilingual text holder. Arabic has a masculine and a feminine form,
 * because Egyptian Arabic addresses men and women differently (كلت / كلتي).
 */
data class Tx(val en: String, val arM: String, val arF: String = arM)

data class LangCtx(val arabic: Boolean, val female: Boolean) {
    fun t(tx: Tx): String = when {
        !arabic -> tx.en
        female -> tx.arF
        else -> tx.arM
    }

    fun pick(en: String, ar: String): String = if (arabic) ar else en

    companion object {
        fun deviceIsArabic(): Boolean = Locale.getDefault().language == "ar"
    }
}

/** All user-facing UI strings. */
object S {
    // General
    val appName = Tx("Sahtein", "صحتين")
    val today = Tx("Today", "النهارده")
    val history = Tx("History", "السجل")
    val log = Tx("Log", "سجّل", "سجّلي")
    val workouts = Tx("Workouts", "التمارين")
    val progress = Tx("Progress", "التقدم")
    val settings = Tx("Settings", "الإعدادات")
    val save = Tx("Save", "حفظ")
    val cancel = Tx("Cancel", "إلغاء")
    val add = Tx("Add", "ضيف", "ضيفي")
    val delete = Tx("Delete", "امسح", "امسحي")
    val next = Tx("Next", "التالي")
    val back = Tx("Back", "رجوع")
    val done = Tx("Done", "تم")
    val kcal = Tx("kcal", "سعر")
    val g = Tx("g", "جم")
    val kg = Tx("kg", "كجم")
    val cm = Tx("cm", "سم")
    val protein = Tx("Protein", "بروتين")
    val carbs = Tx("Carbs", "كارب")
    val fat = Tx("Fat", "دهون")
    val remaining = Tx("left", "فاضل")
    val over = Tx("over", "زيادة")
    val share = Tx("Share", "شارك", "شاركي")

    // Greetings
    val hello = Tx("Hi! Let's stay on plan today.", "أهلاً! يلا نلتزم النهارده.", "أهلاً! يلا نلتزم النهارده.")
    val streakDays = Tx("day streak", "يوم ورا بعض")

    // Onboarding
    val welcome = Tx("Welcome to Sahtein", "أهلاً بيك في صحتين", "أهلاً بيكي في صحتين")
    val welcomeSub = Tx(
        "We'll set your daily calories and remind you at every meal.",
        "هنحسب سعراتك اليومية ونفكرك في كل وجبة.",
    )
    val sex = Tx("Sex", "النوع")
    val male = Tx("Male", "ذكر")
    val female = Tx("Female", "أنثى")
    val sexNote = Tx(
        "Used for the calorie formula and how the app talks to you. You can change it any time in Settings.",
        "بيستخدم في معادلة السعرات وطريقة كلام الأبلكيشن معاك. تقدر تغيره في أي وقت من الإعدادات.",
        "بيستخدم في معادلة السعرات وطريقة كلام الأبلكيشن معاكي. تقدري تغيريه في أي وقت من الإعدادات.",
    )
    val birthYear = Tx("Birth year", "سنة الميلاد")
    val height = Tx("Height (cm)", "الطول (سم)")
    val currentWeight = Tx("Current weight (kg)", "الوزن الحالي (كجم)")
    val goalWeight = Tx("Goal weight (kg)", "الوزن المستهدف (كجم)")
    val activity = Tx("Activity level", "مستوى النشاط")
    val pace = Tx("Weekly pace", "معدل النزول في الأسبوع")
    val meals = Tx("Your meals", "وجباتك")
    val mealsSub = Tx("Set the time of each meal. We'll remind you then.", "حدد ميعاد كل وجبة وهنفكرك فيه.", "حددي ميعاد كل وجبة وهنفكرك فيه.")
    val addSnack = Tx("Add a snack", "ضيف سناك", "ضيفي سناك")
    val yourPlan = Tx("Your plan", "خطتك")
    val imIn = Tx("I'm in!", "أنا معاكم!", "أنا معاكم!")
    val dailyTarget = Tx("Daily target", "هدفك اليومي")
    val goalDate = Tx("Expected goal date", "التاريخ المتوقع للوصول للهدف")
    val maintenance = Tx("Maintenance", "تثبيت الوزن")
    val under18 = Tx(
        "Under 18: we only track maintenance calories. Please talk to a doctor before dieting.",
        "أقل من ١٨ سنة: هنتابع سعرات التثبيت بس. اتكلم مع دكتور قبل أي دايت.",
        "أقل من ١٨ سنة: هنتابع سعرات التثبيت بس. اتكلمي مع دكتور قبل أي دايت.",
    )
    val floorNote = Tx(
        "Your target never goes below a safe minimum.",
        "هدفك عمره ما هينزل تحت الحد الآمن.",
    )
    val paceCapped = Tx(
        "We lowered your pace to 1% of your body weight per week to keep it safe.",
        "قللنا المعدل لـ ١٪ من وزنك في الأسبوع عشان يبقى آمن.",
    )

    // Meals
    val breakfast = Tx("Breakfast", "فطار")
    val lunch = Tx("Lunch", "غدا")
    val dinner = Tx("Dinner", "عشا")
    val snack = Tx("Snack", "سناك")
    val budget = Tx("Budget", "المسموح")
    val notLogged = Tx("Not logged yet", "لسه ماسجلتش", "لسه ماسجلتيش")

    // Verdicts
    val onTrack = Tx("On track ✅", "تمام كده ✅")
    val overBudget = Tx("Over budget ⚠️", "زيادة ⚠️")
    val underBudget = Tx("Under budget 🔻", "أقل من المطلوب 🔻")
    val lowProtein = Tx("Low on protein 💪", "البروتين قليل 💪")
    val underTip = Tx(
        "Add some protein (eggs, foul, chicken, yoghurt) or carry the rest to your next meal.",
        "ضيف شوية بروتين (بيض، فول، فراخ، زبادي) أو خلي الباقي للوجبة الجاية.",
        "ضيفي شوية بروتين (بيض، فول، فراخ، زبادي) أو خلي الباقي للوجبة الجاية.",
    )
    val proteinTip = Tx(
        "Try adding an egg, a cup of yoghurt, or some chicken.",
        "جرب تضيف بيضة أو علبة زبادي أو حتة فراخ.",
        "جربي تضيفي بيضة أو علبة زبادي أو حتة فراخ.",
    )

    // Logging
    val search = Tx("Search", "بحث")
    val voice = Tx("Voice / text", "صوت / كتابة")
    val recent = Tx("Recent", "الأخيرة")
    val custom = Tx("Custom", "يدوي")
    val searchHint = Tx("Search food (Arabic or English)", "دور على أكلة (عربي أو إنجليزي)", "دوري على أكلة (عربي أو إنجليزي)")
    val speak = Tx("Tap and say what you ate", "دوس وقول كلت إيه", "دوسي وقولي كلتي إيه")
    val typeMeal = Tx("…or type it, e.g. 2 loaves foul and an egg", "…أو اكتبها، مثلاً رغيفين فول وبيضة", "…أو اكتبيها، مثلاً رغيفين فول وبيضة")
    val parse = Tx("Calculate", "احسب", "احسبي")
    val notUnderstood = Tx("Couldn't match:", "مافهمتش:")
    val searchThem = Tx("Search for them in the Search tab.", "دور عليهم في تبويب البحث.", "دوري عليهم في تبويب البحث.")
    val addAll = Tx("Add all", "ضيف الكل", "ضيفي الكل")
    val copyYesterday = Tx("Same as yesterday", "زي امبارح")
    val foodName = Tx("Food name", "اسم الأكلة")
    val quantity = Tx("Quantity", "الكمية")
    val unit = Tx("Unit", "الوحدة")
    val noResults = Tx("No results. Try another name or use Custom.", "مفيش نتايج. جرب اسم تاني أو استخدم يدوي.", "مفيش نتايج. جربي اسم تاني أو استخدمي يدوي.")
    val thisMeal = Tx("This meal", "الوجبة دي")
    val voiceUnavailable = Tx("Voice input isn't available on this phone. Type instead.", "الإدخال بالصوت مش متاح على الموبايل ده. اكتب بدله.", "الإدخال بالصوت مش متاح على الموبايل ده. اكتبي بدله.")

    // Summary
    val closeDay = Tx("Close day & summary", "اقفل اليوم وشوف الملخص", "اقفلي اليوم وشوفي الملخص")
    val dailySummary = Tx("Daily summary", "ملخص اليوم")
    val weeklyReport = Tx("Weekly report", "تقرير الأسبوع")
    val total = Tx("Total", "الإجمالي")
    val target = Tx("Target", "الهدف")
    val avgPerDay = Tx("Average / day", "المتوسط في اليوم")
    val daysOnTarget = Tx("Days on target", "أيام في الهدف")
    val avgProtein = Tx("Average protein", "متوسط البروتين")
    val weightChange = Tx("Weight change", "تغير الوزن")
    val bestDay = Tx("Best day", "أحسن يوم")
    val topFoods = Tx("Most eaten", "أكتر أكل")
    val adherence = Tx("Adherence", "الالتزام")
    val vsLastWeek = Tx("vs last week", "مقارنة بالأسبوع اللي فات")
    val workoutsDone = Tx("Workouts", "تمارين")
    val noData = Tx("No data yet", "لسه مفيش بيانات")
    val tomorrowReset = Tx("Tomorrow is a reset 💚", "بكره بداية جديدة 💚")
    val greatDay = Tx("Great day! Keep going 🔥", "يوم عظيم! كمل كده 🔥", "يوم عظيم! كملي كده 🔥")

    // Workouts
    val gym = Tx("Gym", "الجيم")
    val home = Tx("Home", "البيت")
    val recommended = Tx("Recommended for you", "مقترح ليك", "مقترح ليكي")
    val plans = Tx("Workout plans", "خطط التمرين")
    val muscles = Tx("Train by muscle", "تمرين لكل عضلة")
    val watchYoutube = Tx("Watch on YouTube", "اتفرج على يوتيوب", "اتفرجي على يوتيوب")
    val steps = Tx("How to do it", "طريقة الأداء")
    val mistakes = Tx("Avoid", "تجنب", "تجنبي")
    val setsReps = Tx("Sets × reps", "مجموعات × عدات")
    val rest = Tx("Rest", "راحة")
    val equipment = Tx("Equipment", "الأدوات")
    val level = Tx("Level", "المستوى")
    val startWorkout = Tx("Start workout", "ابدأ التمرين", "ابدئي التمرين")
    val logThis = Tx("Log this exercise", "سجل التمرين ده", "سجلي التمرين ده")
    val finish = Tx("Finish workout", "خلص التمرين", "خلصي التمرين")
    val reps = Tx("reps", "عدة")
    val addSet = Tx("Add set", "ضيف مجموعة", "ضيفي مجموعة")
    val restTimer = Tx("Rest timer", "مؤقت الراحة")
    val previous = Tx("Last time", "المرة اللي فاتت")
    val workoutSaved = Tx("Workout saved", "التمرين اتسجل")
    val burned = Tx("burned", "اتحرق")

    // Progress
    val logWeight = Tx("Log today's weight", "سجل وزن النهارده", "سجلي وزن النهارده")
    val levelLbl = Tx("Level", "المستوى")
    val badges = Tx("Badges", "الشارات")
    val streaks = Tx("Streaks", "السلاسل")
    val loggingStreak = Tx("Logging streak", "أيام التسجيل")
    val targetStreak = Tx("On-target streak", "أيام في الهدف")
    val weight = Tx("Weight", "الوزن")

    // Settings
    val profile = Tx("Profile", "البيانات")
    val mealsAndReminders = Tx("Meals & reminders", "الوجبات والتنبيهات")
    val summaryTime = Tx("Daily summary time", "ميعاد ملخص اليوم")
    val addBack = Tx("Add workout calories to food budget", "ضيف سعرات التمرين لميزانية الأكل", "ضيفي سعرات التمرين لميزانية الأكل")
    val off = Tx("Off", "لأ")
    val half = Tx("50%", "٥٠٪")
    val exactAlarm = Tx("Allow exact reminders", "اسمح بالتنبيهات في ميعادها بالظبط", "اسمحي بالتنبيهات في ميعادها بالظبط")
    val exactAlarmSub = Tx("Without this, reminders may arrive a few minutes late.", "من غير ده التنبيه ممكن يتأخر كام دقيقة.")
    val reminder = Tx("Reminder", "تنبيه")
    val share100 = Tx("Shares must add up to 100%", "النسب لازم مجموعها يبقى ١٠٠٪")
    val disclaimer = Tx(
        "Sahtein gives estimates and is not medical advice.",
        "صحتين بيدي تقديرات ومش بديل عن استشارة الدكتور.",
    )

    // Notifications
    val mealTime = Tx(" time! What are you having?", "! هتاكل إيه؟", "! هتاكلي إيه؟")
    val followUp = Tx("You haven't logged", "لسه ماسجلتش", "لسه ماسجلتيش")
    val snooze = Tx("Snooze 15 min", "أجّل ١٥ دقيقة", "أجّلي ١٥ دقيقة")
    val skipped = Tx("Skipped", "مكلتش", "مكلتيش")
}
