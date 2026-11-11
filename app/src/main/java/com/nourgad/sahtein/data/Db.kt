package com.nourgad.sahtein.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Upsert
import com.nourgad.sahtein.domain.Activity
import com.nourgad.sahtein.domain.Calc
import com.nourgad.sahtein.domain.Plan
import kotlinx.coroutines.flow.Flow

@Entity
data class Profile(
    @PrimaryKey val id: Int = 1,
    val female: Boolean,
    val birthYear: Int,
    val heightCm: Double,
    val startWeight: Double,
    val currentWeight: Double,
    val goalWeight: Double,
    val activity: Int,
    val paceKgWeek: Double,
    val summaryMinute: Int = 22 * 60,
    val workoutAddBackPct: Int = 0,
    val startEpochDay: Long,
) {
    fun plan(): Plan = Calc.plan(
        female, birthYear, heightCm, currentWeight, goalWeight,
        Activity.entries[activity.coerceIn(0, Activity.entries.size - 1)], paceKgWeek,
    )
}

@Entity
data class MealSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val minuteOfDay: Int,
    val pct: Int,
    val reminderOn: Boolean = true,
    val sortOrder: Int,
)

@Entity
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val slotId: Long,
    val foodId: String, // FoodDb id, or "custom"
    val name: String,
    val qty: Double,
    val unit: String,
    val grams: Double,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val method: String, // search / voice / recent / custom / copy
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity
data class WeightEntry(
    @PrimaryKey val epochDay: Long,
    val kg: Double,
)

@Entity
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val title: String,
    val minutes: Int,
    val kcal: Double,
)

@Entity
data class SetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val epochDay: Long,
    val exerciseId: String,
    val setNo: Int,
    val reps: Int,
    val weightKg: Double,
)

@Dao
interface AppDao {
    // Profile
    @Query("SELECT * FROM Profile WHERE id = 1")
    fun profile(): Flow<Profile?>

    @Query("SELECT * FROM Profile WHERE id = 1")
    suspend fun profileNow(): Profile?

    @Upsert
    suspend fun saveProfile(p: Profile)

    // Slots
    @Query("SELECT * FROM MealSlot ORDER BY sortOrder")
    fun slots(): Flow<List<MealSlot>>

    @Query("SELECT * FROM MealSlot ORDER BY sortOrder")
    suspend fun slotsNow(): List<MealSlot>

    @Query("SELECT * FROM MealSlot WHERE id = :id")
    suspend fun slot(id: Long): MealSlot?

    @Insert
    suspend fun insertSlots(slots: List<MealSlot>)

    @Update
    suspend fun updateSlot(slot: MealSlot)

    @Delete
    suspend fun deleteSlot(slot: MealSlot)

    @Query("DELETE FROM MealSlot")
    suspend fun clearSlots()

    // Food logs
    @Query("SELECT * FROM FoodLog WHERE epochDay = :day ORDER BY createdAt")
    fun logsFor(day: Long): Flow<List<FoodLog>>

    @Query("SELECT * FROM FoodLog WHERE epochDay = :day ORDER BY createdAt")
    suspend fun logsForNow(day: Long): List<FoodLog>

    @Query("SELECT * FROM FoodLog WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay, createdAt")
    fun logsBetween(from: Long, to: Long): Flow<List<FoodLog>>

    @Query("SELECT * FROM FoodLog WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay, createdAt")
    suspend fun logsBetweenNow(from: Long, to: Long): List<FoodLog>

    @Query("SELECT * FROM FoodLog ORDER BY createdAt DESC LIMIT 200")
    fun recentLogs(): Flow<List<FoodLog>>

    @Query("SELECT DISTINCT epochDay FROM FoodLog ORDER BY epochDay DESC")
    fun loggedDays(): Flow<List<Long>>

    @Insert
    suspend fun insertLogs(logs: List<FoodLog>)

    @Delete
    suspend fun deleteLog(log: FoodLog)

    // Weight
    @Query("SELECT * FROM WeightEntry ORDER BY epochDay")
    fun weights(): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeight(w: WeightEntry)

    // Workouts
    @Query("SELECT * FROM WorkoutSession ORDER BY epochDay DESC, id DESC")
    fun sessions(): Flow<List<WorkoutSession>>

    @Insert
    suspend fun insertSession(s: WorkoutSession): Long

    @Insert
    suspend fun insertSets(sets: List<SetLog>)

    @Query("SELECT * FROM SetLog WHERE exerciseId = :exerciseId AND sessionId = (SELECT MAX(sessionId) FROM SetLog WHERE exerciseId = :exerciseId) ORDER BY setNo")
    suspend fun lastSetsFor(exerciseId: String): List<SetLog>

    @Query("SELECT MAX(weightKg) FROM SetLog WHERE exerciseId = :exerciseId")
    suspend fun bestWeight(exerciseId: String): Double?

    @Query("SELECT * FROM SetLog")
    fun allSets(): Flow<List<SetLog>>

    // Reset
    @Query("DELETE FROM FoodLog") suspend fun clearLogs()
    @Query("DELETE FROM WeightEntry") suspend fun clearWeights()
    @Query("DELETE FROM WorkoutSession") suspend fun clearSessions()
    @Query("DELETE FROM SetLog") suspend fun clearSets()
    @Query("DELETE FROM Profile") suspend fun clearProfile()
}

@Database(
    entities = [Profile::class, MealSlot::class, FoodLog::class, WeightEntry::class, WorkoutSession::class, SetLog::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile private var instance: AppDb? = null

        fun get(context: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDb::class.java, "sahtein.db")
                .build().also { instance = it }
        }
    }
}
