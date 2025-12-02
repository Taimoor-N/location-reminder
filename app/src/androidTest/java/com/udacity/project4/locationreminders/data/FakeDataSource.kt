package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlin.collections.find

class FakeDataSource(private var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(shouldError: Boolean) {
        this.shouldReturnError = shouldError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception: Error getting reminders")
        }
        return Result.Success(ArrayList(reminders))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception: Error getting reminder with id $id")
        }
        val reminder = reminders.find { it.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found with id: $id")
        }
    }

    override suspend fun deleteReminder(id: String) {
        val reminder = reminders.find { it.id == id }
        reminder?.let {
            reminders.remove(it)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}