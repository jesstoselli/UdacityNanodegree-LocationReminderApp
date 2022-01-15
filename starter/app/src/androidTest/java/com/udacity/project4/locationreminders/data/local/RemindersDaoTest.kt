package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() = database.close()

    private val reminderDTO = ReminderDTO(
        title = "Title",
        description = "Description",
        location = "Location",
        latitude = 45.toDouble(),
        longitude = 12.toDouble()
    )

    @Test
    fun saveReminder_successful() = runTest {
        // GIVEN - database is empty
        database.reminderDao().deleteAllReminders()

        // WHEN - one reminder is saved
        database.reminderDao().saveReminder(reminderDTO)

        // THEN - retrieving db should retrieve only that reminder
        val savedReminders = database.reminderDao().getReminders()

        assertThat(savedReminders[0]).isEqualTo(reminderDTO)
    }


    @Test
    fun deleteAllReminders_successful() = runTest {
        // GIVEN - database has one reminder
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - call deleteAllReminders()
        database.reminderDao().deleteAllReminders()

        val remindersList = database.reminderDao().getReminders()

        // THEN - there should be not reminders in db
        assertThat(remindersList).isEmpty()
    }
}