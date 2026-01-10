package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        // Initializes an in-memory Room database. This database is temporary and exists only for
        // the duration of the test.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        // Closes the database connection after each test to release resources.
        database.close()
    }

    @Test
    fun saveReminder_getById_returnsSavedReminder() = runTest {
        // GIVEN a new reminder is created and saved to the repository
        val reminder = createReminder()
        repository.saveReminder(reminder)

        // WHEN the same reminder is retrieved from the repository using its ID.
        val result = repository.getReminder(reminder.id)

        // THEN the retrieved result is successful and its data matches the original reminder.
        assertThat(result is Result.Success, `is`(true))
        val successResult = result as Result.Success
        assertThat(successResult.data.id, `is`(reminder.id))
        assertThat(successResult.data.title, `is`("title"))
        assertThat(successResult.data.description, `is`("description"))
        assertThat(successResult.data.location, `is`("location"))
        assertThat(successResult.data.latitude, `is`(1.0))
        assertThat(successResult.data.longitude, `is`(1.0))
        assertThat(successResult.data.radius, `is`(100.0))
    }

    @Test
    fun getReminders_withMultipleReminders_returnsAllReminders() = runTest {
        // GIVEN two different reminders are created and saved.
        val reminder1 = createReminder()
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0, 200.0)
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        // WHEN the list of all reminders is requested.
        val result = repository.getReminders()

        // THEN the result is successful and contains both of the saved reminders.
        assertThat(result is Result.Success, `is`(true))
        val successResult = result as Result.Success
        assertThat(successResult.data.size, `is`(2))
        assertThat(successResult.data.any { it.id == reminder1.id }, `is`(true))
        assertThat(successResult.data.any { it.id == reminder2.id }, `is`(true))
    }

    @Test
    fun deleteAllReminders_getReminders_returnsEmptyList() = runTest {
        // GIVEN a reminder is saved to the repository.
        val reminder = createReminder()
        repository.saveReminder(reminder)

        // WHEN the deleteAllReminders function is called, and the list of reminders is fetched again.
        repository.deleteAllReminders()
        val result = repository.getReminders()

        // THEN the result is successful, and the returned list of reminders is empty.
        assertThat(result is Result.Success, `is`(true))
        val successResult = result as Result.Success
        assertThat(successResult.data.isEmpty(), `is`(true))
    }

    @Test
    fun getReminder_withInvalidId_returnsError() = runTest {
        // GIVEN an invalid ID that does not correspond to any saved reminder.
        val invalidId = "invalidId"

        // WHEN a reminder is requested using the invalid ID.
        val result = repository.getReminder(invalidId)

        // THEN the result is an error, with a "Reminder not found!" message.
        assertThat(result is Result.Error, `is`(true))
        val errorResult = result as Result.Error
        assertThat(errorResult.message, `is`("Reminder not found!"))
    }

    @Test
    fun deleteReminder_deletesCorrectReminder() = runTest {
        // GIVEN two reminders are saved in the repository.
        val reminder1 = createReminder()
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0, 200.0)
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        // WHEN the first reminder is deleted using its ID.
        repository.deleteReminder(reminder1.id)
        val remindersResult = repository.getReminders() as Result.Success

        // THEN the repository contains only the second reminder
        assertThat(remindersResult.data.size, `is`(1))
        assertThat(remindersResult.data.first().id, `is`(reminder2.id))
    }

    private fun createReminder(): ReminderDTO {
        return ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 1.0,
            longitude = 1.0,
            radius = 100.0
        )
    }

}