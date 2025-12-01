package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
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
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake data source to be injected into the ViewModel.
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        // Initialize the data source with no reminders
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun onClear_clearsAllLiveData() {
        // GIVEN the ViewModel has some data
        saveReminderViewModel.reminderTitle.value = "Test Title"
        saveReminderViewModel.reminderDescription.value = "Test Description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Test Location"
        saveReminderViewModel.latitude.value = 10.0
        saveReminderViewModel.longitude.value = 20.0
        saveReminderViewModel.radius.value = 100.0

        // WHEN onClear is called
        saveReminderViewModel.onClear()

        // THEN all relevant LiveData fields are cleared (set to null)
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.radius.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun populateWithReminder_populatesLiveDataWithReminderData() {
        // GIVEN a reminder data item
        val reminder = ReminderDataItem(
            title = "Test Title",
            description = "Test Description",
            location = "Test Location",
            latitude = 10.0,
            longitude = 20.0,
            radius = 100.0,
            id = "12345"
        )

        // WHEN populateWithReminder is called with the reminder
        saveReminderViewModel.populateWithReminder(reminder)

        // THEN the ViewModel's LiveData is populated with the reminder's data
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(reminder.title))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(reminder.description))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(reminder.location))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(reminder.latitude))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(reminder.longitude))
        assertThat(saveReminderViewModel.radius.getOrAwaitValue(), `is`(reminder.radius))
    }

    @Test
    fun saveReminder_showsLoading() = runTest {
        // GIVEN a new reminder
        val reminder = ReminderDataItem("title", "description", "location", 1.0, 1.0, 100.0)

        // WHEN saving the reminder
        saveReminderViewModel.saveReminder(reminder)

        // THEN the loading indicator is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Allow the coroutine to complete
        advanceUntilIdle()

        // THEN the loading indicator is hidden
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun saveReminder_showsSuccessToastAndNavigates() = runTest {
        // GIVEN a new reminder
        val reminder = ReminderDataItem("title", "description", "location", 1.0, 1.0, 100.0)

        // WHEN saving the reminder
        saveReminderViewModel.saveReminder(reminder)
        advanceUntilIdle()

        // THEN a success toast is shown
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        // AND the navigation event is triggered
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue().javaClass.name, `is`("com.udacity.project4.base.NavigationCommand\$Back"))
    }

    @Test
    fun validateEnteredData_withMissingTitle_returnsFalseAndShowsError() {
        // GIVEN a reminder with a missing title
        val reminder = ReminderDataItem(null, "description", "location", 1.0, 1.0, 100.0)

        // WHEN validating the reminder
        val isValid = saveReminderViewModel.validateEnteredData(reminder)

        // THEN the validation fails and the corresponding error is shown
        assertThat(isValid, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_withMissingLocation_returnsFalseAndShowsError() {
        // GIVEN a reminder with a missing location
        val reminder = ReminderDataItem("title", "description", null, 1.0, 1.0, 100.0)

        // WHEN validating the reminder
        val isValid = saveReminderViewModel.validateEnteredData(reminder)

        // THEN the validation fails and the corresponding error is shown
        assertThat(isValid, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun validateAndSaveReminder_withInvalidData_doesNotSaveAndShowsError() = runTest {
        // GIVEN a reminder with invalid data (missing title)
        val reminder = ReminderDataItem(null, "description", "location", 1.0, 1.0, 100.0)

        // WHEN attempting to save the reminder
        saveReminderViewModel.validateAndSaveReminder(reminder)
        advanceUntilIdle()

        // THEN the reminder is not saved in the data source
        assertThat((fakeDataSource.getReminders() as Result.Success).data.size, `is`(0))
        // AND a snackbar error is shown for the missing title
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }
}