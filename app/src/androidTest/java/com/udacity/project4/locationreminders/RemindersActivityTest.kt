package com.udacity.project4.locationreminders

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest // End-to-end test for the save reminders functionality.
class RemindersActivityTest : KoinTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Initializes Koin with a fake data source for testing and clears previous data.
     */
    @Before
    fun init() {
        stopKoin() // Stop the original app Koin module.
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            // Use a FakeDataSource for testing to ensure test isolation and speed.
            single<ReminderDataSource> { FakeDataSource() }
        }
        // Declare a new Koin module for testing.
        startKoin {
            modules(listOf(myModule))
        }
        // Get our fake data source.
        repository = get()

        // Clear the data to ensure a clean state before each test.
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Registers the Idling Resource before each test to synchronize Espresso with Data Binding.
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregisters the Idling Resource after each test to prevent memory leaks.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addReminder_andVerifyItIsDisplayedOnList() {
        // GIVEN - The RemindersActivity is launched.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // WHEN - The user clicks the "Add Reminder" FAB to navigate to the save reminder screen.
        onView(withId(R.id.addReminderFAB)).perform(click())

        // AND - The user enters the title and description for the new reminder.
        onView(withId(R.id.reminderTitle)).perform(replaceText("Buy Apples"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Remember to buy apples"))

        // AND - The user selects a location from the map.
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick()) // Long clicks the center of the map.
        onView(withId(R.id.confirm_selection_button)).perform(click())

        // AND - The user saves the reminder.
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - The new reminder is displayed on the reminder list screen.
        onView(withText("Buy Apples")).check(matches(isDisplayed()))
        onView(withText("Remember to buy apples")).check(matches(isDisplayed()))

        // Clean up the activity scenario.
        activityScenario.close()
    }

}