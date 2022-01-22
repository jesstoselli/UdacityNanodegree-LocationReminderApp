package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    private val reminderDTO = ReminderDTO(
        title = "Title",
        description = "Description",
        location = "Location",
        latitude = 45.toDouble(),
        longitude = 12.toDouble()
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

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

        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        // WHEN - users tries to retrieve all entries
        val remindersList = repository.getReminders()

        // THEN - all reminders should be displayed
        assertThat(remindersList).isInstanceOf(Result.Success::class.java)

        remindersList as Result.Success
        assertThat(remindersList.data).hasSize(2)
        assertThat(remindersList.data).contains(reminder1)
        assertThat(remindersList.data).contains(reminder2)
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

        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        // WHEN - user tries to retrieve an entry by its id
        val chosenReminder = repository.getReminder(reminder2.id)

        // THEN - reminder should be found and contain the same information provided
        assertThat(chosenReminder).isInstanceOf(Result.Success::class.java)

        chosenReminder as Result.Success
        assertThat(chosenReminder).isNotNull()
        assertThat(chosenReminder.data).isEqualTo(reminder2)
    }

    @Test
    fun getReminderById_failed() = runTest {
        // GIVEN - database is empty
        repository.deleteAllReminders()

        // WHEN - user tries to retrieve all entries
        val reminder = repository.getReminder(reminderDTO.id)

        // THEN - an error should be thrown
        assertThat(reminder).isInstanceOf(Result.Error::class.java)

        reminder as Result.Error
        assertThat(reminder.message).isEqualTo("Reminder not found!")
        assertThat(reminder.statusCode).isNull()
    }

    @Test
    fun saveReminder_successful() = runTest {
        // GIVEN - database is empty
        repository.deleteAllReminders()

        // WHEN - one reminder is saved
        repository.saveReminder(reminderDTO)

        val savedReminders = repository.getReminders()

        // THEN - retrieving db should retrieve only that reminder
        assertThat(savedReminders).isInstanceOf(Result.Success::class.java)

        savedReminders as Result.Success
        assertThat(savedReminders.data).hasSize(1)
        assertThat(savedReminders.data).contains(reminderDTO)
    }

    @Test
    fun deleteAllReminders_successful() = runTest {
        // GIVEN - database has one reminder
        repository.saveReminder(reminderDTO)

        // WHEN - call deleteAllReminders()
        repository.deleteAllReminders()

        val remindersListResult = repository.getReminders()

        // THEN - there should be not reminders in db
        assertThat(remindersListResult).isInstanceOf(Result.Success::class.java)

        remindersListResult as Result.Success
        assertThat(remindersListResult.data).isEmpty()
    }
}