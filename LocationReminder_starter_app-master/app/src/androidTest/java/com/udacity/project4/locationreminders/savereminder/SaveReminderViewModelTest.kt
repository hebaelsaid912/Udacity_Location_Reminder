package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private var list = listOf<ReminderDataItem>(ReminderDataItem("location_title", "description","selected_location",(-50..50).random().toDouble(),(-50..50).random().toDouble()))
    private var firstReminder = list[0]

    private lateinit var fakeDataSource: FakeDataSource
    private  var application: Application?=null
    private  var saveReminderViewModel: SaveReminderViewModel?=null
    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        application = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        fakeDataSource = FakeDataSource(null)
        saveReminderViewModel = null
        application = null
    }

    @Test
    fun validateAndSaveReminder() {
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel!!.validateAndSaveReminder(firstReminder)
        assertThat(saveReminderViewModel!!.showLoading.getOrAwaitValue(),`is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel!!.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateEnteredDataWithCorrectTitleAndLocationReturnsTrue() {
        firstReminder.title = "location_title"
        firstReminder.location = "selected_location"
        val result = saveReminderViewModel!!.validateEnteredData(firstReminder)
        Assert.assertEquals(true,result)
    }
    @Test
    fun validateEnteredDataWithNullTitleReturnsSnackbarWithErrorMessage() {
        firstReminder.title = null
        saveReminderViewModel!!.validateEnteredData(firstReminder)
        assertThat(saveReminderViewModel!!.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }
    @Test
    fun validateEnteredDataWithEmptyTitleReturnsSnackbarWithErrorMessage() {
        firstReminder.title = ""
        val result = saveReminderViewModel!!.validateEnteredData(firstReminder)
        Assert.assertEquals(false,result)
    }
    @Test
    fun validateEnteredDataWithNullLocationReturnsSnackbarWithErrorMessage() {
        firstReminder.title = "location_title"
        firstReminder.location = null
        saveReminderViewModel!!.validateEnteredData(firstReminder)
        assertThat(saveReminderViewModel!!.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }
    @Test
    fun validateEnteredDataWithEmptyLocationReturnsSnackbarWithErrorMessage() {
        firstReminder.location = ""
        saveReminderViewModel!!.validateEnteredData(firstReminder)
        assertThat(saveReminderViewModel!!.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }
    @Test
    fun testTest(){
        Assert.assertFalse(false)
    }

}