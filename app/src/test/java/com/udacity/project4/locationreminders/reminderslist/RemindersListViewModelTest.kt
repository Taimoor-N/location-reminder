package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Rule to execute tasks synchronously for LiveData
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Rule to handle coroutines for testing with the new TestDispatcher
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_showsLoading() = runTest {
        // When loading reminders is called
        remindersListViewModel.loadReminders()

        // Then the initial state of showLoading should be true
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Allow the coroutine to complete
        advanceUntilIdle()

        // Then the final state of showLoading should be false
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_whenRemindersAreUnavailable_showsError() = runTest {
        // Given the data source will return an error
        fakeDataSource.setShouldReturnError(true)

        // When loading reminders
        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        // Then an error message should be shown
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test exception: Error getting reminders"))
    }

    @Test
    fun loadReminders_withNoReminders_showsNoData() = runTest {
        // Given the data source is empty
        fakeDataSource.deleteAllReminders()

        // When loading reminders
        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        // Then the reminders list should be empty and showNoData should be true
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(reminders.isEmpty(), `is`(true))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_withRemindersInDataSource_updatesLiveData() = runTest {
        // Given the data source has reminders
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 10.0, 10.0, 100.0)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 20.0, 20.0, 100.0)
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        // When loading reminders
        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        // Then the remindersList LiveData should be updated with the correct data
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(reminders.size, `is`(2))
        assertThat(reminders[0].title, `is`(reminder1.title))
        assertThat(reminders[1].description, `is`(reminder2.description))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun deleteReminder_removesReminderFromList() = runTest {
        // Given a reminder exists in the data source
        val reminderToDelete = ReminderDTO("Delete Me", "Description", "Location", 30.0, 30.0, 100.0)
        fakeDataSource.saveReminder(reminderToDelete)
        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(1))

        // When a reminder is deleted
        val reminderDataItem = ReminderDataItem(
            reminderToDelete.title,
            reminderToDelete.description,
            reminderToDelete.location,
            reminderToDelete.latitude,
            reminderToDelete.longitude,
            reminderToDelete.radius,
            reminderToDelete.id
        )
        remindersListViewModel.deleteReminder(reminderDataItem)
        advanceUntilIdle()

        // Then the list should be empty and showNoData should be true
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(true))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

}