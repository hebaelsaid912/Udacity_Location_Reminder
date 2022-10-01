package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private var firstReminder = ReminderDTO("location_title", "description","selected_location",(-50..50).random().toDouble(),(-50..50).random().toDouble())
    private var remindersList = mutableListOf(firstReminder,firstReminder,firstReminder,firstReminder)
    private lateinit var fakeDataSource: FakeDataSource
    private  var reminderListViewModel: RemindersListViewModel?=null
    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource(remindersList)
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        fakeDataSource = FakeDataSource(null)
        reminderListViewModel = null
    }


    @Test
    fun loadRemindersWithCorrectRemindersReturnSuccess() {
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel!!.loadReminders()
        // check that loading indicator is shown
        assertThat(reminderListViewModel!!.showLoading.getOrAwaitValue(), `is`(true))
        // check indicator not showing
        mainCoroutineRule.resumeDispatcher()
        assertThat(reminderListViewModel!!.showLoading.getOrAwaitValue(), `is`(false))
    }
    @Test
    fun loadRemindersWithNullRemindersReturnNoRemindersFound() {
        fakeDataSource = FakeDataSource(null)
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        reminderListViewModel!!.loadReminders()
        assertThat(reminderListViewModel!!.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(reminderListViewModel!!.showSnackBar.getOrAwaitValue(), `is`("No reminders found"))
    }
    @Test
    fun loadRemindersWithListOfRemindersReturnNumberOfRemindersInList() {
        reminderListViewModel!!.loadReminders()
        assertThat(reminderListViewModel!!.remindersList.getOrAwaitValue().size, `is`(remindersList.size))
    }
}