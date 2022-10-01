package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.util.DataBindingIdlingResource
import com.udacity.project4.locationreminders.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {
    private var repository: ReminderDataSource? = null
    private var application: Application? = null
    private var testModule: Module? = null
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private var firstReminder = ReminderDTO(
        "first_location_title",
        "first_description",
        "first_selected_location",
        (-50..50).random().toDouble(),
        (-50..50).random().toDouble()
    )
    private var secondReminder = ReminderDTO(
        "second_location_title",
        "second_description",
        "second_selected_location",
        (-50..50).random().toDouble(),
        (-50..50).random().toDouble()
    )
    private var thirdReminder = ReminderDTO(
        "third_location_title",
        "third_description",
        "third_selected_location",
        (-50..50).random().toDouble(),
        (-50..50).random().toDouble()
    )

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
    }

    @After
    fun tearDown() {
        runBlocking {
            repository!!.deleteAllReminders()
        }
        stopKoin()
    }

    @Test
    fun checkEmptyViewDisplayOnUIWhenThereAreNoReminders() {
        val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragment)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
    @Test
    fun checkBehaviorWhenClickOnAddReminderFloatingActionButtonToNavigateSaveReminderFragment() {
        val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragment)

        val navController = mock(NavController::class.java)
        fragment.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }
    @Test
    fun saveRemindersAndCheckIfAllDisplay() {
        runBlocking {
            repository!!.saveReminder(firstReminder)
            repository!!.saveReminder(secondReminder)
            repository!!.saveReminder(thirdReminder)
        }
        val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragment)
        onView(withText(firstReminder.title)).check(matches(isDisplayed()))
        onView(withText(secondReminder.title)).check(matches(isDisplayed()))
        onView(withText(thirdReminder.title)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}