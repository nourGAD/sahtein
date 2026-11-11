package com.nourgad.sahtein.domain

import java.net.URLEncoder

enum class MuscleGroup(val label: Tx) {
    PUSH(Tx("Upper body: push", "الجزء العلوي: دفع")),
    PULL(Tx("Upper body: pull", "الجزء العلوي: سحب")),
    CORE(Tx("Core", "البطن والوسط")),
    LEGS(Tx("Lower body", "الجزء السفلي")),
}

enum class Muscle(val label: Tx, val group: MuscleGroup, val emoji: String) {
    CHEST(Tx("Chest", "الصدر"), MuscleGroup.PUSH, "🫁"),
    SHOULDERS(Tx("Shoulders", "الكتف"), MuscleGroup.PUSH, "🏋️"),
    TRICEPS(Tx("Triceps", "التراي"), MuscleGroup.PUSH, "💪"),
    LATS(Tx("Back (lats)", "الظهر (المجنص)"), MuscleGroup.PULL, "🦅"),
    UPPER_BACK(Tx("Upper back & traps", "أعلى الظهر والترابيس"), MuscleGroup.PULL, "🧗"),
    LOWER_BACK(Tx("Lower back", "أسفل الظهر"), MuscleGroup.PULL, "🧱"),
    BICEPS(Tx("Biceps", "البايسبس"), MuscleGroup.PULL, "💪"),
    FOREARMS(Tx("Forearms", "السواعد"), MuscleGroup.PULL, "✊"),
    ABS(Tx("Abs", "البطن"), MuscleGroup.CORE, "🔥"),
    OBLIQUES(Tx("Obliques", "الجوانب"), MuscleGroup.CORE, "🌀"),
    GLUTES(Tx("Glutes", "المؤخرة (الجلوتس)"), MuscleGroup.LEGS, "🍑"),
    QUADS(Tx("Quads", "الفخذ الأمامي"), MuscleGroup.LEGS, "🦵"),
    HAMSTRINGS(Tx("Hamstrings", "الفخذ الخلفي"), MuscleGroup.LEGS, "🦿"),
    CALVES(Tx("Calves", "السمانة"), MuscleGroup.LEGS, "🦶"),
}

enum class Place { GYM, HOME }

data class Exercise(
    val id: String,
    val name: Tx,
    val muscle: Muscle,
    val place: Place,
    val equipment: Tx,
    val level: Int, // 1 beginner, 2 intermediate, 3 advanced
    val steps: List<Tx>,
    val avoid: List<Tx>,
    val sets: Int = 3,
    val reps: String = "10–12",
    val restSec: Int = 75,
    val met: Double = 5.0,
) {
    /** v1: YouTube search, so links never break. v1.1 will use curated video IDs. */
    fun youtubeUrl(): String =
        "https://www.youtube.com/results?search_query=" + URLEncoder.encode("${name.en} proper form", "UTF-8")
}

private val BODYWEIGHT = Tx("Bodyweight", "وزن الجسم")
private val DUMBBELLS = Tx("Dumbbells", "دامبلز")
private val BARBELL = Tx("Barbell", "بار")
private val MACHINE = Tx("Machine", "جهاز")
private val CABLE = Tx("Cable machine", "جهاز كابل")
private val BAND = Tx("Resistance band", "حبل مطاط")
private val BENCH = Tx("Bench / chair", "بنش أو كرسي")
private val BAR = Tx("Pull-up bar", "عقلة")

private fun t(en: String, ar: String) = Tx(en, ar)

object ExerciseDb {
    val all: List<Exercise> = listOf(
        // ---------------- CHEST ----------------
        Exercise("bench_press", t("Barbell bench press", "بنش برس بالبار"), Muscle.CHEST, Place.GYM, BARBELL, 2,
            listOf(t("Lie on the bench, eyes under the bar, feet flat.", "نام على البنش وعينك تحت البار ورجلك على الأرض."),
                t("Lower the bar to mid-chest with elbows at ~45°.", "نزل البار لنص الصدر والكوع بزاوية حوالي ٤٥°."),
                t("Press up until arms are straight.", "ادفع لفوق لحد ما دراعك يتفرد.")),
            listOf(t("Bouncing the bar off your chest", "خبط البار في صدرك"), t("Lifting your hips off the bench", "رفع الوسط من على البنش")),
            sets = 4, reps = "8–10", restSec = 90, met = 6.0),
        Exercise("pec_deck", t("Pec deck fly", "فراشة صدر (جهاز)"), Muscle.CHEST, Place.GYM, MACHINE, 1,
            listOf(t("Sit tall, back against the pad.", "اقعد مفرود وضهرك على المسند."),
                t("Bring the handles together in front of your chest.", "قفل الدراعات قدام صدرك."),
                t("Open slowly until you feel a stretch.", "افتح ببطء لحد ما تحس بشد في الصدر.")),
            listOf(t("Using momentum", "المرجحة"), t("Shrugging shoulders up", "رفع الكتف لفوق"))),
        Exercise("push_up", t("Push-up", "ضغط"), Muscle.CHEST, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Hands a bit wider than shoulders, body in a straight line.", "إيديك أوسع شوية من كتفك وجسمك على خط مستقيم."),
                t("Lower your chest close to the floor.", "نزل صدرك قريب من الأرض."),
                t("Push back up. Too hard? Do it on your knees.", "اطلع تاني. صعب؟ اعمله على ركبك.")),
            listOf(t("Sagging hips", "الوسط نازل"), t("Elbows flared straight out", "الكوع مفتوح على الجنب خالص")),
            reps = "8–15", met = 3.8),
        Exercise("db_floor_press", t("Dumbbell floor press", "ضغط دامبلز على الأرض"), Muscle.CHEST, Place.HOME, DUMBBELLS, 1,
            listOf(t("Lie on the floor, knees bent, a dumbbell in each hand.", "نام على الأرض ركبك مثنية ودامبل في كل إيد."),
                t("Lower until your upper arms touch the floor.", "نزل لحد ما ضهر دراعك يلمس الأرض."),
                t("Press up and squeeze your chest.", "ادفع لفوق واعصر صدرك.")),
            listOf(t("Dropping the weights fast", "تنزيل الوزن بسرعة"))),

        // ---------------- SHOULDERS ----------------
        Exercise("shoulder_press_db", t("Seated dumbbell shoulder press", "ضغط كتف دامبلز وانت قاعد"), Muscle.SHOULDERS, Place.GYM, DUMBBELLS, 1,
            listOf(t("Sit with back support, dumbbells at ear level.", "اقعد وضهرك مسنود والدامبلز عند ودنك."),
                t("Press overhead without locking elbows hard.", "ادفع لفوق من غير ما تقفل الكوع جامد."),
                t("Lower under control.", "نزل بتحكم.")),
            listOf(t("Arching your lower back", "تقويس أسفل الضهر"))),
        Exercise("lateral_raise", t("Lateral raise", "رفرفة جانبي"), Muscle.SHOULDERS, Place.GYM, DUMBBELLS, 1,
            listOf(t("Stand with light dumbbells at your sides.", "اقف ودامبلز خفاف جنبك."),
                t("Raise arms out to shoulder height, elbows soft.", "ارفع دراعك على الجنب لمستوى الكتف والكوع مثني سنة."),
                t("Lower slowly.", "نزل ببطء.")),
            listOf(t("Swinging the body", "مرجحة الجسم"), t("Going too heavy", "وزن تقيل زيادة")), reps = "12–15", restSec = 60),
        Exercise("pike_push_up", t("Pike push-up", "ضغط بايك للكتف"), Muscle.SHOULDERS, Place.HOME, BODYWEIGHT, 2,
            listOf(t("Make an upside-down V with hips high.", "اعمل شكل V مقلوبة ووسطك لفوق."),
                t("Bend elbows to bring your head toward the floor.", "اثني الكوع ونزل راسك ناحية الأرض."),
                t("Push back up.", "ادفع تاني لفوق.")),
            listOf(t("Letting hips drop", "نزول الوسط")), reps = "6–10", met = 4.0),
        Exercise("band_lateral_raise", t("Band lateral raise", "رفرفة جانبي بالحبل"), Muscle.SHOULDERS, Place.HOME, BAND, 1,
            listOf(t("Stand on the band, ends in each hand.", "اقف على الحبل وطرفه في كل إيد."),
                t("Raise arms to shoulder height.", "ارفع دراعك لمستوى الكتف."),
                t("Lower slowly against the band.", "نزل ببطء ضد شد الحبل.")),
            listOf(t("Shrugging", "رفع الكتف لودنك")), reps = "12–15", restSec = 60, met = 3.5),

        // ---------------- TRICEPS ----------------
        Exercise("cable_pushdown", t("Cable triceps pushdown", "تراي كابل لتحت"), Muscle.TRICEPS, Place.GYM, CABLE, 1,
            listOf(t("Hold the bar or rope, elbows pinned to your sides.", "امسك البار أو الحبل والكوع لازق في جنبك."),
                t("Push down until arms are straight.", "ادفع لتحت لحد ما دراعك يتفرد."),
                t("Return slowly to 90°.", "ارجع ببطء لزاوية ٩٠°.")),
            listOf(t("Elbows drifting forward", "الكوع بيتحرك لقدام"))),
        Exercise("skull_crusher", t("EZ-bar skull crusher", "سكال كراشر بالبار"), Muscle.TRICEPS, Place.GYM, BARBELL, 2,
            listOf(t("Lie on a bench, bar above your chest.", "نام على البنش والبار فوق صدرك."),
                t("Bend elbows to lower the bar toward your forehead.", "اثني الكوع ونزل البار ناحية جبهتك."),
                t("Extend back up.", "افرد دراعك تاني.")),
            listOf(t("Flaring elbows", "فتح الكوع"))),
        Exercise("bench_dip", t("Bench / chair dip", "ديبس على كرسي"), Muscle.TRICEPS, Place.HOME, BENCH, 1,
            listOf(t("Hands on the edge of a chair, legs forward.", "إيديك على طرف الكرسي ورجلك لقدام."),
                t("Bend elbows to lower your hips.", "اثني الكوع ونزل وسطك."),
                t("Push back up.", "ادفع لفوق.")),
            listOf(t("Going too deep (shoulder pain)", "النزول زيادة (وجع في الكتف)")), met = 3.8),
        Exercise("diamond_push_up", t("Diamond push-up", "ضغط دايموند"), Muscle.TRICEPS, Place.HOME, BODYWEIGHT, 2,
            listOf(t("Hands together under your chest, making a diamond.", "إيديك جنب بعض تحت صدرك على شكل معين."),
                t("Lower your chest to your hands.", "نزل صدرك لإيديك."),
                t("Push up keeping elbows close.", "اطلع والكوع قريب من جسمك.")),
            listOf(t("Sagging hips", "نزول الوسط")), reps = "6–12", met = 4.0),

        // ---------------- LATS ----------------
        Exercise("lat_pulldown", t("Lat pulldown", "سحب أمامي"), Muscle.LATS, Place.GYM, CABLE, 1,
            listOf(t("Grip the bar a bit wider than shoulders.", "امسك البار أوسع شوية من كتفك."),
                t("Pull the bar to your upper chest, chest up.", "اسحب البار لأعلى صدرك وصدرك لفوق."),
                t("Let it rise slowly.", "سيبه يطلع ببطء.")),
            listOf(t("Leaning far back", "الرجوع بضهرك لورا زيادة"), t("Pulling behind the neck", "السحب ورا الرقبة"))),
        Exercise("seated_row", t("Seated cable row", "سحب أرضي"), Muscle.LATS, Place.GYM, CABLE, 1,
            listOf(t("Sit tall, knees slightly bent.", "اقعد مفرود وركبك مثنية سنة."),
                t("Pull the handle to your belly, squeeze shoulder blades.", "اسحب لبطنك واقفل لوح الكتف."),
                t("Return with arms straight.", "ارجع ودراعك مفرود.")),
            listOf(t("Rounding your back", "تقويس الضهر"))),
        Exercise("pull_up", t("Pull-up (or negatives)", "عقلة (أو نزول بطيء)"), Muscle.LATS, Place.HOME, BAR, 3,
            listOf(t("Hang from the bar, hands shoulder-width.", "اتعلق في العقلة وإيديك على عرض كتفك."),
                t("Pull until your chin passes the bar.", "اسحب لحد ما دقنك تعدي العقلة."),
                t("Can't yet? Jump up and lower slowly for 5 s.", "مش قادر؟ اطلع بنطة وانزل ببطء ٥ ثواني.")),
            listOf(t("Kipping / swinging", "المرجحة")), reps = "AMRAP", restSec = 90, met = 5.0),
        Exercise("db_row", t("One-arm dumbbell row", "سحب دامبل بإيد واحدة"), Muscle.LATS, Place.HOME, DUMBBELLS, 1,
            listOf(t("One hand and knee on a chair, back flat.", "إيد وركبة على كرسي وضهرك مفرود."),
                t("Row the dumbbell to your hip.", "اسحب الدامبل ناحية وسطك."),
                t("Lower until the arm is straight.", "نزل لحد ما الدراع يتفرد.")),
            listOf(t("Twisting the torso", "لف الجسم"))),

        // ---------------- UPPER BACK ----------------
        Exercise("face_pull", t("Face pull", "فيس بول"), Muscle.UPPER_BACK, Place.GYM, CABLE, 1,
            listOf(t("Rope at face height, palms in.", "الحبل في مستوى وشك."),
                t("Pull toward your eyes, elbows high and wide.", "اسحب ناحية عينك والكوع عالي ومفتوح."),
                t("Return slowly.", "ارجع ببطء.")),
            listOf(t("Using too much weight", "وزن تقيل زيادة")), reps = "12–15", restSec = 60),
        Exercise("barbell_shrug", t("Barbell shrug", "ترابيس بالبار"), Muscle.UPPER_BACK, Place.GYM, BARBELL, 1,
            listOf(t("Hold the bar at your thighs.", "امسك البار عند فخدك."),
                t("Lift shoulders straight up toward ears.", "ارفع كتفك لفوق ناحية ودنك."),
                t("Hold 1 s, lower.", "اثبت ثانية ونزل.")),
            listOf(t("Rolling the shoulders", "لف الكتف"))),
        Exercise("band_pull_apart", t("Band pull-apart", "فتح الحبل المطاط"), Muscle.UPPER_BACK, Place.HOME, BAND, 1,
            listOf(t("Hold the band at shoulder height, arms straight.", "امسك الحبل في مستوى الكتف ودراعك مفرود."),
                t("Pull it apart until it touches your chest.", "افتحه لحد ما يلمس صدرك."),
                t("Return slowly.", "ارجع ببطء.")),
            listOf(t("Bending elbows a lot", "ثني الكوع كتير")), reps = "15–20", restSec = 45, met = 3.0),
        Exercise("prone_ytw", t("Prone Y-T-W raises", "رفع Y-T-W على البطن"), Muscle.UPPER_BACK, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Lie face down, arms forward like a Y.", "نام على بطنك ودراعك لقدام زي حرف Y."),
                t("Lift arms, then move to T and W shapes.", "ارفع دراعك وبعدين اعمل T و W."),
                t("Squeeze shoulder blades each time.", "اقفل لوح الكتف كل مرة.")),
            listOf(t("Lifting your head high", "رفع راسك لفوق")), reps = "8 each", restSec = 45, met = 2.8),

        // ---------------- LOWER BACK ----------------
        Exercise("back_extension", t("Back extension (45° bench)", "باك إكستنشن"), Muscle.LOWER_BACK, Place.GYM, MACHINE, 1,
            listOf(t("Hips on the pad, feet locked.", "وسطك على المسند ورجلك مثبتة."),
                t("Lower your upper body slowly.", "نزل الجزء العلوي ببطء."),
                t("Rise until your body is straight, not past it.", "اطلع لحد ما جسمك يبقى مستقيم مش أكتر.")),
            listOf(t("Over-arching at the top", "تقويس زيادة فوق"))),
        Exercise("romanian_deadlift", t("Romanian deadlift", "ديدليفت روماني"), Muscle.LOWER_BACK, Place.GYM, BARBELL, 2,
            listOf(t("Hold the bar, knees soft.", "امسك البار وركبك مثنية سنة."),
                t("Push hips back, bar sliding down your legs.", "زق وسطك لورا والبار ماشي على رجلك."),
                t("Stand up by squeezing glutes.", "اطلع وانت بتعصر الجلوتس.")),
            listOf(t("Rounding your back", "تقويس الضهر")), sets = 4, reps = "8–10", restSec = 90, met = 6.0),
        Exercise("superman", t("Superman hold", "سوبرمان"), Muscle.LOWER_BACK, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Lie face down, arms forward.", "نام على بطنك ودراعك لقدام."),
                t("Lift arms and legs a few cm off the floor.", "ارفع دراعك ورجلك كام سم من الأرض."),
                t("Hold 2–3 s, lower.", "اثبت ٢-٣ ثواني ونزل.")),
            listOf(t("Cranking the neck up", "رفع الرقبة زيادة")), reps = "10–12", restSec = 45, met = 2.8),
        Exercise("bird_dog", t("Bird dog", "بيرد دوج"), Muscle.LOWER_BACK, Place.HOME, BODYWEIGHT, 1,
            listOf(t("On hands and knees, back flat.", "على إيديك وركبك وضهرك مفرود."),
                t("Reach one arm forward and the opposite leg back.", "مد إيد لقدام والرجل العكسية لورا."),
                t("Hold 2 s, switch sides.", "اثبت ثانيتين وبدّل.")),
            listOf(t("Twisting the hips", "لف الوسط")), reps = "10 each", restSec = 45, met = 2.5),

        // ---------------- BICEPS ----------------
        Exercise("barbell_curl", t("Barbell curl", "بايسبس بالبار"), Muscle.BICEPS, Place.GYM, BARBELL, 1,
            listOf(t("Stand, bar at thighs, palms forward.", "اقف والبار عند فخدك وكفك لقدام."),
                t("Curl up keeping elbows still.", "ارفع والكوع ثابت."),
                t("Lower fully.", "نزل للآخر.")),
            listOf(t("Swinging the back", "مرجحة الضهر"))),
        Exercise("cable_curl", t("Cable curl", "بايسبس كابل"), Muscle.BICEPS, Place.GYM, CABLE, 1,
            listOf(t("Low pulley, straight bar.", "البكرة تحت والبار مستقيم."),
                t("Curl to your chest.", "ارفع لصدرك."),
                t("Lower slowly.", "نزل ببطء.")),
            listOf(t("Elbows moving forward", "الكوع بيتحرك لقدام")), reps = "12–15", restSec = 60),
        Exercise("db_curl", t("Dumbbell curl", "بايسبس دامبلز"), Muscle.BICEPS, Place.HOME, DUMBBELLS, 1,
            listOf(t("Dumbbells at your sides.", "الدامبلز جنبك."),
                t("Curl up, turning palms up.", "ارفع ولف كفك لفوق."),
                t("Lower slowly.", "نزل ببطء.")),
            listOf(t("Using momentum", "المرجحة")), met = 3.5),
        Exercise("band_curl", t("Band curl", "بايسبس بالحبل"), Muscle.BICEPS, Place.HOME, BAND, 1,
            listOf(t("Stand on the band, ends in hands.", "اقف على الحبل وطرفه في إيدك."),
                t("Curl up, elbows at sides.", "ارفع والكوع جنبك."),
                t("Lower against the band.", "نزل ضد الشد.")),
            listOf(t("Leaning back", "الرجوع لورا")), reps = "12–15", restSec = 60, met = 3.0),

        // ---------------- FOREARMS ----------------
        Exercise("wrist_curl", t("Barbell wrist curl", "رسغ بالبار"), Muscle.FOREARMS, Place.GYM, BARBELL, 1,
            listOf(t("Forearms on a bench, palms up.", "ساعدك على البنش وكفك لفوق."),
                t("Curl the bar with your wrists only.", "ارفع البار بالرسغ بس."),
                t("Lower slowly.", "نزل ببطء.")),
            listOf(t("Lifting the forearms", "رفع الساعد")), reps = "15–20", restSec = 45, met = 2.5),
        Exercise("farmer_carry", t("Farmer's carry", "مشي الفلاح"), Muscle.FOREARMS, Place.GYM, DUMBBELLS, 1,
            listOf(t("Hold heavy dumbbells at your sides.", "امسك دامبلز تقال جنبك."),
                t("Walk tall for 30–40 m.", "امشي مفرود ٣٠-٤٠ متر."),
                t("Rest, repeat.", "ارتاح وكرر.")),
            listOf(t("Leaning to one side", "الميل على جنب")), reps = "40 m", restSec = 60, met = 6.0),
        Exercise("dead_hang", t("Dead hang", "تعليق على العقلة"), Muscle.FOREARMS, Place.HOME, BAR, 1,
            listOf(t("Hang from a bar, arms straight.", "اتعلق في العقلة ودراعك مفرود."),
                t("Keep shoulders slightly engaged.", "خلي كتفك مشدود سنة."),
                t("Hold as long as you can.", "اثبت أطول وقت.")),
            listOf(t("Shoulders fully relaxed to the ears", "الكتف سايب لحد ودنك")), reps = "20–40 s", restSec = 60, met = 2.5),
        Exercise("towel_wring", t("Towel wringing", "عصر الفوطة"), Muscle.FOREARMS, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Roll a towel and hold it with both hands.", "لف فوطة وامسكها بإيديك الاتنين."),
                t("Twist hard in opposite directions.", "اعصرها جامد في اتجاهين عكس بعض."),
                t("Switch direction.", "اعكس الاتجاه.")),
            listOf(t("Holding your breath", "كتم النفس")), reps = "10 each way", restSec = 45, met = 2.0),

        // ---------------- ABS ----------------
        Exercise("cable_crunch", t("Cable crunch", "كرانش كابل"), Muscle.ABS, Place.GYM, CABLE, 2,
            listOf(t("Kneel facing the cable, rope by your head.", "اركع قدام الكابل والحبل جنب راسك."),
                t("Crunch down, ribs to hips.", "اثني لتحت وقرب ضلوعك من وسطك."),
                t("Return slowly.", "ارجع ببطء.")),
            listOf(t("Pulling with your arms", "السحب بدراعك")), reps = "12–15", restSec = 60),
        Exercise("hanging_knee_raise", t("Hanging knee raise", "رفع ركب وانت متعلق"), Muscle.ABS, Place.GYM, BAR, 2,
            listOf(t("Hang from the bar.", "اتعلق في البار."),
                t("Raise knees to chest.", "ارفع ركبك لصدرك."),
                t("Lower without swinging.", "نزل من غير مرجحة.")),
            listOf(t("Swinging", "المرجحة")), reps = "10–12", restSec = 60),
        Exercise("plank", t("Plank", "بلانك"), Muscle.ABS, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Forearms on the floor, body straight.", "ساعدك على الأرض وجسمك مستقيم."),
                t("Squeeze glutes and abs.", "اعصر الجلوتس والبطن."),
                t("Breathe and hold.", "خد نفسك واثبت.")),
            listOf(t("Hips too high or sagging", "الوسط عالي أو نازل")), reps = "30–60 s", restSec = 45, met = 3.0),
        Exercise("dead_bug", t("Dead bug", "ديد باج"), Muscle.ABS, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Lie on your back, arms up, knees at 90°.", "نام على ضهرك ودراعك لفوق وركبك ٩٠°."),
                t("Lower opposite arm and leg slowly.", "نزل إيد والرجل العكسية ببطء."),
                t("Keep lower back pressed down; switch.", "خلي أسفل ضهرك لازق في الأرض وبدّل.")),
            listOf(t("Lower back lifting off", "أسفل الضهر بيترفع")), reps = "10 each", restSec = 45, met = 2.8),

        // ---------------- OBLIQUES ----------------
        Exercise("pallof_press", t("Pallof press", "بالوف برس"), Muscle.OBLIQUES, Place.GYM, CABLE, 1,
            listOf(t("Stand side-on to the cable, handle at chest.", "اقف بجنبك للكابل والمقبض عند صدرك."),
                t("Press straight out, resist the twist.", "ادفع لقدام وقاوم اللف."),
                t("Hold 2 s, return.", "اثبت ثانيتين وارجع.")),
            listOf(t("Letting the torso rotate", "إنك تسيب جسمك يلف")), reps = "10 each", restSec = 45),
        Exercise("cable_woodchop", t("Cable woodchop", "وود تشوب كابل"), Muscle.OBLIQUES, Place.GYM, CABLE, 2,
            listOf(t("High pulley, both hands on the handle.", "البكرة فوق والمقبض بإيديك الاتنين."),
                t("Pull diagonally down across your body.", "اسحب بشكل مايل لتحت قدام جسمك."),
                t("Pivot the back foot; return slowly.", "لف الرجل اللي ورا وارجع ببطء.")),
            listOf(t("Bending arms a lot", "ثني الدراع كتير")), reps = "12 each", restSec = 45),
        Exercise("side_plank", t("Side plank", "بلانك جانبي"), Muscle.OBLIQUES, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Lie on your side, elbow under shoulder.", "نام على جنبك والكوع تحت الكتف."),
                t("Lift hips into a straight line.", "ارفع وسطك لحد ما جسمك يبقى مستقيم."),
                t("Hold, then switch sides.", "اثبت وبعدين بدّل.")),
            listOf(t("Hips dropping", "الوسط بينزل")), reps = "20–40 s each", restSec = 45, met = 3.0),
        Exercise("russian_twist", t("Russian twist", "رشن تويست"), Muscle.OBLIQUES, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Sit, lean back slightly, feet up or on floor.", "اقعد وارجع لورا سنة ورجلك فوق أو على الأرض."),
                t("Rotate your chest side to side.", "لف صدرك يمين وشمال."),
                t("Hold a bottle for extra load.", "امسك إزازة مية لو عايز تقل.")),
            listOf(t("Only moving the arms", "تحريك الدراع بس")), reps = "12 each", restSec = 45, met = 3.8),

        // ---------------- GLUTES ----------------
        Exercise("hip_thrust", t("Barbell hip thrust", "هيب ثرست بالبار"), Muscle.GLUTES, Place.GYM, BARBELL, 2,
            listOf(t("Upper back on a bench, bar on hips (padded).", "أعلى ضهرك على البنش والبار على وسطك بمخدة."),
                t("Drive hips up until your body is flat.", "ارفع وسطك لحد ما جسمك يبقى مستقيم."),
                t("Squeeze glutes 1 s at the top, lower.", "اعصر الجلوتس ثانية فوق ونزل.")),
            listOf(t("Arching the lower back at the top", "تقويس أسفل الضهر فوق")), sets = 4, reps = "8–12", restSec = 90, met = 5.5),
        Exercise("cable_kickback", t("Cable glute kickback", "كيك باك كابل"), Muscle.GLUTES, Place.GYM, CABLE, 1,
            listOf(t("Ankle strap on, hold the machine.", "اربط الحزام في رجلك وامسك الجهاز."),
                t("Kick the leg back and up.", "ارفع رجلك لورا ولفوق."),
                t("Return slowly.", "ارجع ببطء.")),
            listOf(t("Arching the back to lift higher", "تقويس الضهر عشان ترفع أكتر")), reps = "12–15 each", restSec = 45),
        Exercise("glute_bridge", t("Glute bridge", "جلوت بريدج"), Muscle.GLUTES, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Lie on your back, knees bent, feet flat.", "نام على ضهرك وركبك مثنية ورجلك على الأرض."),
                t("Push hips up, squeeze glutes.", "ارفع وسطك واعصر الجلوتس."),
                t("Lower slowly. Harder: one leg.", "نزل ببطء. أصعب: برجل واحدة.")),
            listOf(t("Pushing through the toes", "الضغط على صوابعك")), reps = "15–20", restSec = 45, met = 3.5),
        Exercise("bulgarian_split", t("Bulgarian split squat", "سكوات بلغاري"), Muscle.GLUTES, Place.HOME, BENCH, 2,
            listOf(t("Back foot on a chair, front foot forward.", "رجلك اللي ورا على كرسي واللي قدام لقدام."),
                t("Lower until the front thigh is parallel.", "نزل لحد ما فخدك اللي قدام يبقى موازي للأرض."),
                t("Drive up through the front heel.", "اطلع بالضغط على كعب الرجل اللي قدام.")),
            listOf(t("Front knee collapsing inward", "الركبة داخلة لجوه")), reps = "8–12 each", restSec = 75, met = 5.0),

        // ---------------- QUADS ----------------
        Exercise("back_squat", t("Barbell back squat", "سكوات بالبار"), Muscle.QUADS, Place.GYM, BARBELL, 2,
            listOf(t("Bar on upper back, feet shoulder-width.", "البار على أعلى ضهرك ورجلك على عرض كتفك."),
                t("Sit down and back, chest up.", "انزل كأنك هتقعد وصدرك لفوق."),
                t("Stand up through your whole foot.", "اطلع بالضغط على رجلك كلها.")),
            listOf(t("Knees caving in", "الركب داخلة لجوه"), t("Heels lifting", "الكعب بيترفع")), sets = 4, reps = "6–10", restSec = 120, met = 6.0),
        Exercise("leg_press", t("Leg press", "ليج برس"), Muscle.QUADS, Place.GYM, MACHINE, 1,
            listOf(t("Feet shoulder-width on the plate.", "رجلك على عرض كتفك على الجهاز."),
                t("Lower until knees reach ~90°.", "نزل لحد ما ركبك تبقى حوالي ٩٠°."),
                t("Press without locking knees.", "ادفع من غير ما تقفل ركبك.")),
            listOf(t("Lower back lifting off the seat", "أسفل الضهر بيترفع من الكرسي")), sets = 4, restSec = 90),
        Exercise("bodyweight_squat", t("Bodyweight squat", "سكوات بوزن الجسم"), Muscle.QUADS, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Feet shoulder-width, arms forward.", "رجلك على عرض كتفك ودراعك لقدام."),
                t("Sit down as low as you comfortably can.", "انزل لأوطى نقطة مريحة."),
                t("Stand up tall.", "اطلع مفرود.")),
            listOf(t("Knees caving in", "الركب داخلة لجوه")), reps = "15–20", restSec = 60, met = 5.0),
        Exercise("reverse_lunge", t("Reverse lunge", "لانچ لورا"), Muscle.QUADS, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Step back with one foot.", "ارجع خطوة لورا برجل واحدة."),
                t("Lower until both knees are at ~90°.", "انزل لحد ما الركبتين يبقوا حوالي ٩٠°."),
                t("Push back to standing; switch.", "ارجع واقف وبدّل.")),
            listOf(t("Leaning the torso forward", "ميل الجسم لقدام")), reps = "10 each", met = 4.0),

        // ---------------- HAMSTRINGS ----------------
        Exercise("lying_leg_curl", t("Lying leg curl", "رجل خلفي نايم"), Muscle.HAMSTRINGS, Place.GYM, MACHINE, 1,
            listOf(t("Lie face down, pad above your heels.", "نام على بطنك والمسند فوق كعبك."),
                t("Curl heels toward your glutes.", "اثني رجلك ناحية الجلوتس."),
                t("Lower slowly.", "نزل ببطء.")),
            listOf(t("Hips lifting off the pad", "الوسط بيترفع"))),
        Exercise("good_morning", t("Barbell good morning", "جود مورنينج"), Muscle.HAMSTRINGS, Place.GYM, BARBELL, 3,
            listOf(t("Light bar on upper back, knees soft.", "بار خفيف على ضهرك وركبك مثنية سنة."),
                t("Hinge forward at the hips, back flat.", "اثني لقدام من الوسط وضهرك مفرود."),
                t("Return by squeezing glutes.", "ارجع بعصر الجلوتس.")),
            listOf(t("Rounding the back", "تقويس الضهر"), t("Going heavy", "وزن تقيل")), reps = "8–10", restSec = 90, met = 5.0),
        Exercise("db_rdl", t("Dumbbell Romanian deadlift", "ديدليفت روماني دامبلز"), Muscle.HAMSTRINGS, Place.HOME, DUMBBELLS, 1,
            listOf(t("Dumbbells in front of thighs, knees soft.", "الدامبلز قدام فخدك وركبك مثنية سنة."),
                t("Push hips back until you feel a stretch.", "زق وسطك لورا لحد ما تحس بشد."),
                t("Stand up squeezing glutes.", "اطلع وانت بتعصر الجلوتس.")),
            listOf(t("Rounding your back", "تقويس الضهر")), met = 4.5),
        Exercise("slider_curl", t("Towel slider leg curl", "رجل خلفي بالفوطة"), Muscle.HAMSTRINGS, Place.HOME, BODYWEIGHT, 2,
            listOf(t("Lie on your back, heels on a towel on a smooth floor.", "نام على ضهرك وكعبك على فوطة على أرض ناعمة."),
                t("Lift hips, pull heels toward you.", "ارفع وسطك واسحب كعبك ناحيتك."),
                t("Slide out slowly.", "افرد رجلك ببطء.")),
            listOf(t("Dropping hips", "نزول الوسط")), reps = "8–12", met = 3.8),

        // ---------------- CALVES ----------------
        Exercise("standing_calf_machine", t("Standing calf raise (machine)", "سمانة واقف (جهاز)"), Muscle.CALVES, Place.GYM, MACHINE, 1,
            listOf(t("Balls of feet on the step, shoulders under pads.", "مشط رجلك على الحافة وكتفك تحت المساند."),
                t("Rise as high as you can.", "اطلع على صوابعك لأعلى نقطة."),
                t("Lower for a full stretch.", "انزل لآخر شد.")),
            listOf(t("Bouncing", "النط")), sets = 4, reps = "12–15", restSec = 45),
        Exercise("seated_calf", t("Seated calf raise", "سمانة قاعد"), Muscle.CALVES, Place.GYM, MACHINE, 1,
            listOf(t("Sit with knees under the pad.", "اقعد وركبك تحت المسند."),
                t("Raise heels high.", "ارفع كعبك لفوق."),
                t("Lower slowly.", "نزل ببطء.")),
            listOf(t("Short range of motion", "مدى حركة قصير")), reps = "15–20", restSec = 45),
        Exercise("step_calf_raise", t("Single-leg calf raise on a step", "سمانة برجل واحدة على سلمة"), Muscle.CALVES, Place.HOME, BODYWEIGHT, 1,
            listOf(t("Stand on one foot on a stair edge, hold the wall.", "اقف برجل واحدة على طرف سلمة وامسك الحيطة."),
                t("Rise up high.", "اطلع لفوق."),
                t("Lower below the step; switch.", "انزل تحت مستوى السلمة وبدّل.")),
            listOf(t("Bending the knee", "ثني الركبة")), reps = "12–15 each", restSec = 45, met = 3.0),
        Exercise("jump_rope", t("Jump rope", "نط الحبل"), Muscle.CALVES, Place.HOME, Tx("Jump rope", "حبل نط"), 1,
            listOf(t("Small jumps on the balls of your feet.", "نطات صغيرة على مشط رجلك."),
                t("Turn the rope with your wrists.", "لف الحبل برسغك."),
                t("Keep a steady rhythm.", "حافظ على إيقاع ثابت.")),
            listOf(t("Jumping too high", "النط عالي زيادة")), reps = "60 s", restSec = 60, met = 10.0),
    )

    val byId: Map<String, Exercise> = all.associateBy { it.id }

    fun forMuscle(m: Muscle, place: Place) = all.filter { it.muscle == m && it.place == place }
}

data class PlanDay(val title: Tx, val exerciseIds: List<String>)
data class WorkoutPlan(val id: String, val name: Tx, val place: Place, val perWeek: Int, val forFemale: Boolean?, val days: List<PlanDay>)

object WorkoutPlans {
    val all = listOf(
        WorkoutPlan("full_home", Tx("Full body at home", "جسم كامل في البيت"), Place.HOME, 3, null, listOf(
            PlanDay(Tx("Full body", "جسم كامل"), listOf("bodyweight_squat", "push_up", "db_row", "glute_bridge", "pike_push_up", "plank")),
        )),
        WorkoutPlan("quick15", Tx("15-minute no equipment", "١٥ دقيقة من غير أدوات"), Place.HOME, 4, null, listOf(
            PlanDay(Tx("Circuit ×3", "دايرة ×٣"), listOf("bodyweight_squat", "push_up", "reverse_lunge", "superman", "plank", "jump_rope")),
        )),
        WorkoutPlan("lower_core_home", Tx("Lower body & core (home)", "رجل وبطن (البيت)"), Place.HOME, 3, true, listOf(
            PlanDay(Tx("Glutes & legs", "جلوتس ورجل"), listOf("glute_bridge", "bulgarian_split", "db_rdl", "reverse_lunge", "step_calf_raise")),
            PlanDay(Tx("Core & upper", "بطن وجزء علوي"), listOf("dead_bug", "side_plank", "push_up", "db_row", "band_lateral_raise")),
        )),
        WorkoutPlan("upper_lower", Tx("Upper / lower split", "علوي / سفلي"), Place.GYM, 4, null, listOf(
            PlanDay(Tx("Upper", "علوي"), listOf("bench_press", "lat_pulldown", "shoulder_press_db", "seated_row", "cable_pushdown", "barbell_curl")),
            PlanDay(Tx("Lower", "سفلي"), listOf("back_squat", "romanian_deadlift", "leg_press", "lying_leg_curl", "standing_calf_machine", "cable_crunch")),
        )),
        WorkoutPlan("ppl", Tx("Push / pull / legs", "دفع / سحب / رجل"), Place.GYM, 6, false, listOf(
            PlanDay(Tx("Push", "دفع"), listOf("bench_press", "shoulder_press_db", "pec_deck", "lateral_raise", "cable_pushdown", "skull_crusher")),
            PlanDay(Tx("Pull", "سحب"), listOf("lat_pulldown", "seated_row", "face_pull", "barbell_shrug", "barbell_curl", "cable_curl")),
            PlanDay(Tx("Legs", "رجل"), listOf("back_squat", "leg_press", "romanian_deadlift", "lying_leg_curl", "standing_calf_machine", "hanging_knee_raise")),
        )),
        WorkoutPlan("glutes_gym", Tx("Glutes & legs focus (gym)", "تركيز جلوتس ورجل (جيم)"), Place.GYM, 4, true, listOf(
            PlanDay(Tx("Glutes", "جلوتس"), listOf("hip_thrust", "romanian_deadlift", "cable_kickback", "leg_press", "seated_calf")),
            PlanDay(Tx("Upper & core", "علوي وبطن"), listOf("lat_pulldown", "shoulder_press_db", "seated_row", "lateral_raise", "pallof_press", "cable_crunch")),
        )),
    )

    val byId = all.associateBy { it.id }

    /** Default suggestion by sex and place; every plan stays available to everyone. */
    fun recommended(female: Boolean, place: Place): WorkoutPlan =
        all.firstOrNull { it.place == place && it.forFemale == female }
            ?: all.first { it.place == place }
}
