package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var remindersList: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    val reminderData = ReminderDataItem(
        title = "Title",
        description = "Description",
        location = "Location",
        latitude = 45.toDouble(),
        longitude = 12.toDouble()
    )

    val reminderDTO = ReminderDTO(
        title = "Title",
        description = "Description",
        location = "Location",
        latitude = 45.toDouble(),
        longitude = 12.toDouble()
    )

    var shouldReturnError = false

    fun shouldReturnError(shouldReturnError: Boolean) {
        this.shouldReturnError = shouldReturnError
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val chosenReminder = remindersList.find { it.id == id }

        return if (shouldReturnError) {
            Result.Error("Error")
        } else {
            if (chosenReminder != null) {
                Result.Success(chosenReminder)
            } else {
                Result.Error("Item not found.")
            }
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("Error")
        } else {
            Result.Success(remindersList)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }
}