package com.nourgad.sahtein.domain

import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class Activity(val factor: Double, val label: Tx) {
    SEDENTARY(1.2, Tx("Sedentary (desk job, little exercise)", "قليل الحركة (مكتب ومفيش رياضة)")),
    LIGHT(1.375, Tx("Light (exercise 1–3 days/week)", "خفيف (رياضة ١-٣ أيام في الأسبوع)")),
    MODERATE(1.55, Tx("Moderate (exercise 3–5 days/week)", "متوسط (رياضة ٣-٥ أيام في الأسبوع)")),
    ACTIVE(1.725, Tx("Very active (6–7 days/week)", "نشيط جداً (٦-٧ أيام في الأسبوع)")),
    ATHLETE(1.9, Tx("Athlete / physical job", "رياضي أو شغل مجهود")),
}

data class Plan(
    val bmr: Int,
    val tdee: Int,
    val targetKcal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val floorKcal: Int,
    val paceKgWeek: Double,
    val paceCapped: Boolean,
    val maintenance: Boolean,
    val under18: Boolean,
    val goalDate: LocalDate?,
)

object Calc {
    const val KCAL_PER_KG = 7700.0

    fun age(birthYear: Int, today: LocalDate = LocalDate.now()) = today.year - birthYear

    /** Mifflin-St Jeor. */
    fun bmr(female: Boolean, kg: Double, cm: Double, age: Int): Double =
        10 * kg + 6.25 * cm - 5 * age + if (female) -161 else 5

    fun plan(
        female: Boolean,
        birthYear: Int,
        heightCm: Double,
        weightKg: Double,
        goalKg: Double,
        activity: Activity,
        paceKgWeek: Double,
        today: LocalDate = LocalDate.now(),
    ): Plan {
        val age = age(birthYear, today)
        val bmr = bmr(female, weightKg, heightCm, age)
        val tdee = bmr * activity.factor
        val floor = max(bmr, if (female) 1200.0 else 1500.0)
        val under18 = age < 18
        val wantsLoss = goalKg < weightKg - 0.1 && !under18
        val maxPace = weightKg * 0.01
        val pace = if (wantsLoss) min(paceKgWeek, maxPace) else 0.0
        val capped = wantsLoss && paceKgWeek > maxPace
        var target = tdee - pace * KCAL_PER_KG / 7.0
        target = max(target, if (wantsLoss) floor else tdee)
        // Real pace after the floor is applied
        val realPace = max(0.0, (tdee - target) * 7.0 / KCAL_PER_KG)
        val weeks = if (realPace > 0.01) (weightKg - goalKg) / realPace else null
        val goalDate = weeks?.let { today.plusDays((it * 7).roundToInt().toLong()) }

        val protein = 1.8 * min(goalKg, weightKg).coerceAtLeast(40.0)
        val fat = target * 0.28 / 9.0
        val carbs = max(0.0, (target - protein * 4 - fat * 9) / 4.0)
        return Plan(
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            targetKcal = target.roundToInt(),
            proteinG = protein.roundToInt(),
            carbsG = carbs.roundToInt(),
            fatG = fat.roundToInt(),
            floorKcal = floor.roundToInt(),
            paceKgWeek = realPace,
            paceCapped = capped,
            maintenance = !wantsLoss,
            under18 = under18,
            goalDate = goalDate,
        )
    }

    /** kcal = MET × 3.5 × kg ÷ 200 × minutes */
    fun workoutKcal(met: Double, kg: Double, minutes: Double) = met * 3.5 * kg / 200.0 * minutes
}

enum class VerdictKind { ON_TRACK, OVER, UNDER, EMPTY }

data class MealVerdict(
    val kind: VerdictKind,
    val budget: Int,
    val eaten: Int,
    val lowProtein: Boolean,
    /** For OVER: the item to cut and by how many units. */
    val cutItemName: String? = null,
    val cutUnits: Double = 0.0,
    val cutUnit: String? = null,
    val cutSaves: Int = 0,
)

data class SlotInfo(val id: Long, val pct: Int, val order: Int)
data class ItemInfo(val slotId: Long, val name: String, val qty: Double, val unit: String, val kcal: Double, val protein: Double)

object Verdicts {
    /**
     * Budget for a meal = what's left of the day (after other meals already logged),
     * shared among this meal and the later meals that are still empty, by their percentages.
     */
    fun budgetFor(slot: SlotInfo, slots: List<SlotInfo>, items: List<ItemInfo>, dayTarget: Int): Double {
        val eatenElsewhere = items.filter { it.slotId != slot.id }.sumOf { it.kcal }
        val loggedSlots = items.map { it.slotId }.toSet()
        val sharing = slots.filter { it.id == slot.id || (it.order > slot.order && it.id !in loggedSlots) }
        val pctSum = sharing.sumOf { it.pct }.coerceAtLeast(1)
        val left = max(0.0, dayTarget - eatenElsewhere)
        // Never let a single meal budget fall below 40% of its plain share, so verdicts stay sane.
        val plain = dayTarget * slot.pct / 100.0
        return max(left * slot.pct / pctSum, plain * 0.4)
    }

    fun verdict(slot: SlotInfo, slots: List<SlotInfo>, items: List<ItemInfo>, dayTarget: Int, proteinTarget: Int): MealVerdict {
        val mine = items.filter { it.slotId == slot.id }
        val budget = budgetFor(slot, slots, items, dayTarget)
        val eaten = mine.sumOf { it.kcal }
        if (mine.isEmpty()) return MealVerdict(VerdictKind.EMPTY, budget.roundToInt(), 0, false)
        val protein = mine.sumOf { it.protein }
        val lowProtein = protein < 0.7 * proteinTarget * slot.pct / 100.0
        val kind = when {
            eaten > budget * 1.10 -> VerdictKind.OVER
            eaten < budget * 0.80 -> VerdictKind.UNDER
            else -> VerdictKind.ON_TRACK
        }
        if (kind != VerdictKind.OVER) return MealVerdict(kind, budget.roundToInt(), eaten.roundToInt(), lowProtein)
        // Suggest cutting the biggest item by whole (or half) units.
        val overBy = eaten - budget
        val big = mine.maxBy { it.kcal }
        val perUnit = if (big.qty > 0) big.kcal / big.qty else big.kcal
        val step = if (big.unit == "g") 10.0 else if (big.qty <= 1.0) 0.5 else 1.0
        val units = min(big.qty, ceil(overBy / perUnit / step) * step)
        return MealVerdict(
            kind, budget.roundToInt(), eaten.roundToInt(), lowProtein,
            cutItemName = big.name, cutUnits = units, cutUnit = big.unit, cutSaves = (units * perUnit).roundToInt(),
        )
    }

    /** Whole-day status: within ±10% of target and not below the floor. */
    fun dayOnTarget(eaten: Double, target: Int, floor: Int): Boolean =
        eaten >= floor * 0.95 && eaten in target * 0.9..target * 1.1
}
