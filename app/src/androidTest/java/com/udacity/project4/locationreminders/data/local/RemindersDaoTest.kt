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
    fun getReminders_successful() = runTest {
        // GIVEN - database has two entries
        val reminder1 = ReminderDTO(
            title = "Millennium Centre",
            description = "Description",
            location = "Location",
            latitude = 45.toDouble(),
            longitude = 12.toDouble()
        )

        val reminder2 = ReminderDTO(
            title = "Millennium Stadium",
            description = "Description",
            location = "Location",
            latitude = 45.toDouble(),
            longitude = 12.toDouble()
        )

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - users tries to retrieve all entries
        val remindersList = database.reminderDao().getReminders()

        // THEN - all reminders should be displayed
        assertThat(remindersList).hasSize(2)
        assertThat(remindersList).contains(reminder1)
        assertThat(remindersList).contains(reminder2)
    }

    @Test
    fun getReminders_withSaveStrategyResolution() = runTest {
        // GIVEN - database receives two entries with the same id
        val reminder1 = reminderDTO.copy(title = "Millennium Centre")
        val reminder2 = reminderDTO.copy(title = "Millennium Stadium")
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - users tries to retrieve all entries
        val remindersList = database.reminderDao().getReminders()

        // THEN - only the second saved reminder should be presented
        assertThat(remindersList).hasSize(1)
        assertThat(remindersList).contains(reminder2)
    }

    @Test
    fun getReminderById_successful() = runTest {
        // GIVEN - database has two entries
        val reminder1 = ReminderDTO(
            title = "Millennium Centre",
            description = "Description",
            location = "Location",
            latitude = 45.toDouble(),
            longitude = 12.toDouble()
        )

        val reminder2 = ReminderDTO(
            title = "Millennium Stadium",
            description = "Description",
            location = "Location",
            latitude = 45.toDouble(),
            longitude = 12.toDouble()
        )

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - users tries to retrieve all entries
        val retrievedReminder = database.reminderDao().getReminderById(reminder1.id)

        // THEN - all reminders should be displayed
        assertThat(retrievedReminder).isEqualTo(reminder1)
    }

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