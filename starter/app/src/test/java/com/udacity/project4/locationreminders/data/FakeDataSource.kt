package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    val reminderData = ReminderDataItem(
        title = "Title",
        description = "Description",
        location = "Location",
        latitude = 45.toDouble(),
        longitude = 12.toDouble()
    )

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(remindersList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val chosenReminder = remindersList.find { it.id == id }

        return if (chosenReminder != null) {
            Result.Success(chosenReminder)
        } else {
            Result.Error("Reminder not found.")
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }
}