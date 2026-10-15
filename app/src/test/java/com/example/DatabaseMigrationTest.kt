package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {
    @Test fun upgradesIdenticalV1SchemaWithoutLosingProfileAndRemovesLegacyPassword() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-regression.db"
        context.deleteDatabase(name)
        // The entities in the last v1 commit are byte-for-byte identical to v2.
        val old = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        old.appDao().insertOrUpdateProfile(UserProfile(name = "Patrick", weightKg = 82f, passwordHash = "legacy-secret"))
        old.appDao().insertWorkoutLog(WorkoutLog(routineId = 1L, routineName = "Treino A",
            dateMillis = 1000L, durationMinutes = 30, totalVolumeKg = 200f, totalSetsCompleted = 5))
        old.close()
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { sqlite ->
            sqlite.version = 1
            sqlite.execSQL("DELETE FROM room_master_table")
        }
        val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_1_2).build()
        try {
            val profile = upgraded.appDao().getUserProfileDirect(1)!!
            assertEquals("Patrick", profile.name)
            assertEquals(82f, profile.weightKg, 0f)
            assertEquals("", profile.passwordHash)
            assertEquals("Treino A", upgraded.appDao().getAllWorkoutLogs().first().single().routineName)
        } finally {
            upgraded.close()
            context.deleteDatabase(name)
        }
    }
}
