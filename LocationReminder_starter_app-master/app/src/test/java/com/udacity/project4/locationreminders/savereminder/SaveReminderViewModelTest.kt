package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private var firstReminder = ReminderDataItem(
        "location_title",
        "description",
        "selected_location",
        (-50..50).random().toDouble(),
        (-50..50).random().toDouble()
    )

    private lateinit var fakeDataSource: FakeDataSource
    private var saveReminderViewModel: SaveReminderViewModel? = null

    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
        fakeDataSource = FakeDataSource(null)
        saveReminderViewModel = null
    }

    @Test
    fun validateAndSaveReminder() {
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel!!.validateAndSaveReminder(firstReminder)
        assertThat(saveReminderViewModel!!.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel!!.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `validate entered data with correct title and location returns true`() {
        firstReminder.title = "location_title"
        firstReminder.location = "selected_location"
        val result = saveReminderViewModel!!.validateEnteredData(firstReminder)
        Assert.assertEquals(true, result)
    }

    @Test
    fun `validate entered data with null title, returns snackbar with error message`() {
        firstReminder.title = null
        saveReminderViewModel!!.validateEnteredData(firstReminder)
        assertThat(
            saveReminderViewModel!!.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun `validate entered data with empty title, returns false`() {
        firstReminder.title = ""
        val result = saveReminderViewModel!!.validateEnteredData(firstReminder)
        Assert.assertEquals(false, result)
    }

    @Test
    fun `validate entered data with null location, returns snackbar with error message`() {
        firstReminder.title = "location_title"
        firstReminder.location = null
        saveReminderViewModel!!.validateEnteredData(firstReminder)
        assertThat(
            saveReminderViewModel!!.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun `validate entered data with empty location, returns snackbar with error message`() {
        firstReminder.location = ""
        saveReminderViewModel!!.validateEnteredData(firstReminder)
        assertThat(
            saveReminderViewModel!!.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun testTest() {
        Assert.assertFalse(false)
    }

}