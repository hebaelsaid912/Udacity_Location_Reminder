package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(private val reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {
    private var shouldReturnError = false

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> =
        if (shouldReturnError) {
             Result.Error(Exception("Test exception").toString())
        }else {
              Result.Success(ArrayList(reminders))
        }



    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error(Exception("Test exception").toString())
        }
        reminders?.firstOrNull { it.id == id }?.let { return Result.Success(it) }
        return Result.Error("No element with id $id found")

    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}