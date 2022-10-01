package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase
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

    }

    @After
    fun tearDown() {
        if (::database.isInitialized)
            database.close()

    }

    @Test
    fun insertReminderIntoDBAndReturnSameReminder() = runBlockingTest {
        database.reminderDao().saveReminder(firstReminder)

        val loaded = database.reminderDao().getReminderById(firstReminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(firstReminder.id))
        assertThat(loaded.title, `is`(firstReminder.title))
        assertThat(loaded.description, `is`(firstReminder.description))
        assertThat(loaded.location, `is`(firstReminder.location))
        assertThat(loaded.latitude, `is`(firstReminder.latitude))
        assertThat(loaded.longitude, `is`(firstReminder.longitude))
    }

    @Test
    fun getAllRemindersInDBCheckRetrievingRemindersAndRemindersListSize() = runBlockingTest {

        database.reminderDao().saveReminder(firstReminder)
        database.reminderDao().saveReminder(secondReminder)
        database.reminderDao().saveReminder(thirdReminder)

        val loaded = database.reminderDao().getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.size, `is`(3))
    }

    @Test
    fun getReminderByIdWithNotFoundIdReturnNull() = runBlockingTest {
        val loaded = database.reminderDao().getReminderById(secondReminder.id)
        assertThat(loaded, nullValue())
    }

    @Test
    fun deleteAllRemindersFromDBAreDeletedSuccessfullyAndCheckGetRemindersReturnEmptyListOfReminders() =
        runBlocking {
            database.reminderDao().saveReminder(firstReminder)
            database.reminderDao().saveReminder(secondReminder)
            database.reminderDao().saveReminder(thirdReminder)
            database.reminderDao().deleteAllReminders()
            val deletedResults = database.reminderDao().getReminders()
            assertThat(deletedResults.size, `is`(0))
        }

    @Test
    fun deleteAllRemindersFromDBAreDeletedSuccessfullyAndCheckGetReminderWithDeletedIdReturnErrorMessage() =
        runBlocking {
            database.reminderDao().saveReminder(firstReminder)
            database.reminderDao().deleteAllReminders()
            val deletedResults = database.reminderDao().getReminderById(firstReminder.id)
            assertThat(deletedResults, nullValue())
        }

}