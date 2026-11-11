package com.nourgad.sahtein.data

import android.content.Context
import androidx.room.withTransaction
import com.nourgad.sahtein.domain.Calc
import com.nourgad.sahtein.domain.ExerciseDb
import com.nourgad.sahtein.domain.Food
import com.nourgad.sahtein.notify.Alarms
import java.time.LocalDate

fun todayEpoch(): Long = LocalDate.now().toEpochDay()

data class SetDraft(val exerciseId: String, val reps: Int, val weightKg: Double)

class Repo(private val context: Context) {
    private val db = AppDb.get(context)
    val dao = db.dao()

    val profile = dao.profile()
    val slots = dao.slots()

    suspend fun completeOnboarding(profile: Profile, slots: List<MealSlot>) {
        db.withTransaction {
            dao.saveProfile(profile)
            dao.clearSlots()
            dao.insertSlots(slots)
            dao.saveWeight(WeightEntry(profile.startEpochDay, profile.currentWeight))
        }
        Alarms.scheduleAll(context)
    }

    suspend fun saveProfile(p: Profile) = dao.saveProfile(p)

    suspend fun saveSlots(slots: List<MealSlot>) {
        // Keep existing slot ids so past logs stay linked to their meal.
        db.withTransaction {
            val keep = slots.filter { it.id != 0L }.map { it.id }.toSet()
            dao.slotsNow().filter { it.id !in keep }.forEach { dao.deleteSlot(it) }
            val ordered = slots.mapIndexed { i, s -> s.copy(sortOrder = i) }
            ordered.filter { it.id != 0L }.forEach { dao.updateSlot(it) }
            dao.insertSlots(ordered.filter { it.id == 0L })
        }
        Alarms.scheduleAll(context)
    }

    fun foodLog(day: Long, slotId: Long, food: Food, qty: Double, unit: String, method: String): FoodLog {
        val grams = food.gramsFor(unit, qty)
        val k = grams / 100.0
        return FoodLog(
            epochDay = day, slotId = slotId, foodId = food.id, name = food.ar + "|" + food.en,
            qty = qty, unit = unit, grams = grams,
            kcal = food.kcal * k, protein = food.p * k, carbs = food.c * k, fat = food.f * k, method = method,
        )
    }

    suspend fun addLogs(logs: List<FoodLog>) = dao.insertLogs(logs)

    suspend fun addCustom(day: Long, slotId: Long, name: String, kcal: Double, p: Double, c: Double, f: Double) =
        dao.insertLogs(listOf(FoodLog(epochDay = day, slotId = slotId, foodId = "custom", name = "$name|$name",
            qty = 1.0, unit = "piece", grams = 0.0, kcal = kcal, protein = p, carbs = c, fat = f, method = "custom")))

    suspend fun copyFromDay(fromDay: Long, toDay: Long, slotId: Long): Int {
        val src = dao.logsForNow(fromDay).filter { it.slotId == slotId }
        dao.insertLogs(src.map { it.copy(id = 0, epochDay = toDay, method = "copy", createdAt = System.currentTimeMillis()) })
        return src.size
    }

    suspend fun deleteLog(log: FoodLog) = dao.deleteLog(log)

    suspend fun logWeight(kg: Double) {
        dao.saveWeight(WeightEntry(todayEpoch(), kg))
        dao.profileNow()?.let { dao.saveProfile(it.copy(currentWeight = kg)) }
    }

    suspend fun saveWorkout(title: String, minutes: Int, sets: List<SetDraft>): Double {
        val p = dao.profileNow() ?: return 0.0
        val mets = sets.mapNotNull { ExerciseDb.byId[it.exerciseId]?.met }
        val met = if (mets.isEmpty()) 5.0 else mets.average()
        val kcal = Calc.workoutKcal(met, p.currentWeight, minutes.toDouble())
        val day = todayEpoch()
        val id = dao.insertSession(WorkoutSession(epochDay = day, title = title, minutes = minutes, kcal = kcal))
        val numbering = HashMap<String, Int>()
        dao.insertSets(sets.map { s ->
            val n = (numbering[s.exerciseId] ?: 0) + 1
            numbering[s.exerciseId] = n
            SetLog(sessionId = id, epochDay = day, exerciseId = s.exerciseId, setNo = n, reps = s.reps, weightKg = s.weightKg)
        })
        return kcal
    }

    suspend fun lastSets(exerciseId: String) = dao.lastSetsFor(exerciseId)

    suspend fun wipe() {
        db.withTransaction {
            dao.clearLogs(); dao.clearWeights(); dao.clearSessions(); dao.clearSets(); dao.clearSlots(); dao.clearProfile()
        }
        Alarms.cancelAll(context)
    }
}

/** FoodLog.name stores "arabic|english"; pick by language. */
fun FoodLog.displayName(arabic: Boolean): String {
    val parts = name.split('|')
    return if (arabic) parts.first() else parts.getOrElse(1) { parts.first() }
}
