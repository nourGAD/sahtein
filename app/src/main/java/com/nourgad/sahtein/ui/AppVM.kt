package com.nourgad.sahtein.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nourgad.sahtein.data.FoodLog
import com.nourgad.sahtein.data.MealSlot
import com.nourgad.sahtein.data.Profile
import com.nourgad.sahtein.data.Repo
import com.nourgad.sahtein.data.SetDraft
import com.nourgad.sahtein.data.SetLog
import com.nourgad.sahtein.data.WeightEntry
import com.nourgad.sahtein.data.WorkoutSession
import com.nourgad.sahtein.data.todayEpoch
import com.nourgad.sahtein.domain.Food
import com.nourgad.sahtein.notify.Alarms
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileState(val loaded: Boolean, val profile: Profile?)

/** One row of the active workout. */
class ActiveSet(val exerciseId: String, reps: String, kg: String, done: Boolean = false) {
    val reps = mutableStateOf(reps)
    val kg = mutableStateOf(kg)
    val done = mutableStateOf(done)
}

class AppVM(app: Application) : AndroidViewModel(app) {
    val repo = Repo(app)
    private val dao = repo.dao

    val profile: StateFlow<ProfileState> = repo.profile.map { ProfileState(true, it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProfileState(false, null))
    val slots: StateFlow<List<MealSlot>> = repo.slots.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allLogs: StateFlow<List<FoodLog>> = dao.logsBetween(0, Long.MAX_VALUE).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val recent: StateFlow<List<FoodLog>> = dao.recentLogs().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val weights: StateFlow<List<WeightEntry>> = dao.weights().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val sessions: StateFlow<List<WorkoutSession>> = dao.sessions().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val sets: StateFlow<List<SetLog>> = dao.allSets().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun completeOnboarding(p: Profile, slots: List<MealSlot>) = viewModelScope.launch { repo.completeOnboarding(p, slots) }
    fun saveProfile(p: Profile) = viewModelScope.launch { repo.saveProfile(p); Alarms.scheduleAll(getApplication()) }
    fun saveSlots(s: List<MealSlot>) = viewModelScope.launch { repo.saveSlots(s) }

    fun addFood(day: Long, slotId: Long, food: Food, qty: Double, unit: String, method: String) = viewModelScope.launch {
        repo.addLogs(listOf(repo.foodLog(day, slotId, food, qty, unit, method)))
        Alarms.cancelFollowUp(getApplication(), slotId)
    }

    fun addMany(day: Long, slotId: Long, items: List<Triple<Food, Double, String>>, method: String) = viewModelScope.launch {
        repo.addLogs(items.map { (f, q, u) -> repo.foodLog(day, slotId, f, q, u, method) })
        Alarms.cancelFollowUp(getApplication(), slotId)
    }

    fun readd(day: Long, slotId: Long, log: FoodLog) = viewModelScope.launch {
        repo.addLogs(listOf(log.copy(id = 0, epochDay = day, slotId = slotId, method = "recent", createdAt = System.currentTimeMillis())))
    }

    fun addCustom(day: Long, slotId: Long, name: String, kcal: Double, p: Double, c: Double, f: Double) =
        viewModelScope.launch { repo.addCustom(day, slotId, name, kcal, p, c, f) }

    fun copyYesterday(day: Long, slotId: Long) = viewModelScope.launch { repo.copyFromDay(day - 1, day, slotId) }
    fun deleteLog(log: FoodLog) = viewModelScope.launch { repo.deleteLog(log) }
    fun logWeight(kg: Double) = viewModelScope.launch { repo.logWeight(kg) }
    fun wipe() = viewModelScope.launch { repo.wipe() }

    // ---------- Active workout ----------
    val activeTitle = mutableStateOf("")
    val activeSets = mutableStateListOf<ActiveSet>()
    val activeStart = mutableStateOf(0L)
    val lastSets = mutableStateOf<Map<String, List<SetLog>>>(emptyMap())

    fun startWorkout(title: String, exerciseIds: List<String>, defaultSets: (String) -> Int) {
        activeTitle.value = title
        activeSets.clear()
        activeStart.value = System.currentTimeMillis()
        viewModelScope.launch {
            val last = exerciseIds.associateWith { repo.lastSets(it) }
            lastSets.value = last
            exerciseIds.forEach { id ->
                val prev = last[id].orEmpty()
                repeat(defaultSets(id)) { i ->
                    val p = prev.getOrNull(i)
                    activeSets += ActiveSet(id, p?.reps?.toString() ?: "", p?.weightKg?.let { if (it > 0) fmt(it) else "" } ?: "")
                }
            }
        }
    }

    fun addSet(exerciseId: String) {
        val idx = activeSets.indexOfLast { it.exerciseId == exerciseId }
        val prev = activeSets.getOrNull(idx)
        activeSets.add(idx + 1, ActiveSet(exerciseId, prev?.reps?.value ?: "", prev?.kg?.value ?: ""))
    }

    fun finishWorkout(onSaved: (Double) -> Unit) {
        val done = activeSets.filter { it.done.value }.map {
            SetDraft(it.exerciseId, it.reps.value.toIntOrNull() ?: 0, it.kg.value.toDoubleOrNull() ?: 0.0)
        }
        val minutes = ((System.currentTimeMillis() - activeStart.value) / 60_000L).toInt().coerceAtLeast(1)
        val title = activeTitle.value
        viewModelScope.launch {
            val kcal = if (done.isEmpty()) 0.0 else repo.saveWorkout(title, minutes, done)
            activeSets.clear()
            activeTitle.value = ""
            onSaved(kcal)
        }
    }

    fun today() = todayEpoch()
}
