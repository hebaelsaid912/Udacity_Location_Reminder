package com.udacity.project4.locationreminders.data.local


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private var repository: RemindersLocalRepository? = null
    private var firstReminder = ReminderDTO(
        "location_title",
        "description",
        "selected_location",
        (-50..50).random().toDouble(),
        (-50..50).random().toDouble()
    )
    private var secondReminder = ReminderDTO(
        "location_title",
        "description",
        "selected_location",
        (-50..50).random().toDouble(),
        (-50..50).random().toDouble()
    )
    private var thirdReminder = ReminderDTO(
        "location_title",
        "description",
        "selected_location",
        (-50..50).random().toDouble(),
        (-50..50).random().toDouble()
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        repository = RemindersLocalRepository(database.reminderDao())
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
            repository = null
        }
    }

    @Test
    fun getRemindersReturnCorrectRemindersListSize() = runBlocking {
        repository!!.saveReminder(firstReminder)
        repository!!.saveReminder(secondReminder)
        repository!!.saveReminder(thirdReminder)
        val results = repository!!.getReminders() as Result.Success

        assertThat(results.data, notNullValue())
        assertThat(results.data.size, `is`(3))
    }

    @Test
    fun saveReminderAndReturnReminder() = runBlocking {
        repository!!.saveReminder(firstReminder)

        val result = repository!!.getReminder(firstReminder.id) as Result.Success

        assertThat(result.data, notNullValue())
        assertThat(result.data.id, `is`(firstReminder.id))
        assertThat(result.data.title, `is`(firstReminder.title))
        assertThat(result.data.description, `is`(firstReminder.description))
        assertThat(result.data.location, `is`(firstReminder.location))
        assertThat(result.data.latitude, `is`(firstReminder.latitude))
        assertThat(result.data.longitude, `is`(firstReminder.longitude))
    }

    @Test
    fun deleteAllRemindersDeletedSuccessfullyAndCheckGetRemindersReturnEmptyListOfReminders() =
        runBlocking {
            repository!!.saveReminder(firstReminder)
            repository!!.saveReminder(secondReminder)
            repository!!.saveReminder(thirdReminder)
            repository!!.deleteAllReminders()
            val deletedResults = repository!!.getReminders() as Result.Success
            assertThat(deletedResults.data, notNullValue())
            assertThat(deletedResults.data.size, `is`(0))
        }

    @Test
    fun deleteAllRemindersDeletedSuccessfullyAndCheckGetReminderWithDeletedIdReturnErrorMessage() =
        runBlocking {
            repository!!.saveReminder(firstReminder)
            repository!!.deleteAllReminders()
            val deletedResults = repository!!.getReminder(firstReminder.id) as Result.Error
            assertThat(deletedResults.message, `is`("Reminder not found!"))
        }
}