package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao

class FakeReminderDao(private val reminders : MutableList<ReminderDTO>? = mutableListOf()) : RemindersDao {
    override suspend fun getReminders(): List<ReminderDTO> {
        reminders?.let {
            if(it.isEmpty())
                return  reminders
        }
        return reminders!!
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        reminders?.firstOrNull { it.id == reminderId }?.let { return it }
        return null
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}