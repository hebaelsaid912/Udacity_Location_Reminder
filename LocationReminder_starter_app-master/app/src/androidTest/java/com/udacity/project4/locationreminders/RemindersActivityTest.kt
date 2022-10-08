package com.udacity.project4.locationreminders

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.util.DataBindingIdlingResource
import com.udacity.project4.locationreminders.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.`is`

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {
    private var repository: ReminderDataSource? = null
    private var application: Application? = null
    private var testModule: Module? = null


    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(RemindersActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val backgroundPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var decorView: View

    @Before
    fun setUp() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        testModule = module {
            viewModel {
                RemindersListViewModel(
                    application!!,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    application!!,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(application!!) }
        }
        startKoin {
            modules(listOf(testModule!!))
        }
        repository = get()

        runBlocking {
            repository!!.deleteAllReminders()
        }
        activityScenarioRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        application = null
        testModule = null
        repository = null
        activityScenarioRule.scenario.close()
    }

    @Test
    fun saveValidReminderWithCorrectTitleAndCurrentLocationSavedToDBSuccessSave() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        val reminderTitle = "Test Reminder Title"
        val reminderDesc = "Test Reminder Description"
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText(reminderTitle))
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText(reminderDesc), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.google_map))
            .check(matches(isDisplayed()))

        onView(withId(R.id.google_map)).perform(click())
        runBlocking {
            delay(1000L)
        }
        onView(withId(R.id.confirmButton)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(reminderTitle))
            .check(matches(isDisplayed()))
        onView(withText(reminderDesc))
            .check(matches(isDisplayed()))
        activityScenario.close()
    }
    @Test
    fun saveValidReminderWithCorrectTitleAndCurrentLocationSavedToDBSuccessSaveShowToastMessage() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle))
            .perform(replaceText("Test Reminder Title"))
        onView(withId(R.id.reminderDescription))
            .perform(replaceText("Test Reminder Description"))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.google_map)).perform(click())
        runBlocking {
            delay(1000L)
        }
        onView(withId(R.id.confirmButton)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(application!!.getString(R.string.reminder_saved))).inRoot(
            withDecorView(not(decorView))
        ).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun saveReminderWithInvalidReminderTitleShowSnackBarWithTitleError() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(application!!.getString(R.string.err_enter_title))).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(R.string.err_enter_title)).inRoot(withDecorView(not(decorView)))
            .check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun saveReminderWithInvalidReminderLocationShowSnackBarWithLocationError() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Test Reminder Title"))
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(application!!.getString(R.string.err_select_location))).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(R.string.err_select_location)).inRoot(withDecorView(not(decorView)))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun checkEmptyListWithDisplayNoDataView() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        activityScenario.close()
    }
}