package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
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

    @Before
    fun initDb() {
        // Initializes an in-memory Room database. This database is temporary and exists only for
        // the duration of the test.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminder_getById_returnsSameReminder() = runTest {
        // GIVEN a new reminder is inserted into the database.
        val reminder = createReminder()
        database.reminderDao().saveReminder(reminder)

        // WHEN the reminder is retrieved from the database by its ID.
        val dbReminder = database.reminderDao().getReminderById(reminder.id)

        // THEN the dbReminder's data matches the original reminder's data.
        assertThat(dbReminder as ReminderDTO, `is`(reminder))
        assertThat(dbReminder.id, `is`(reminder.id))
        assertThat(dbReminder.title, `is`(reminder.title))
        assertThat(dbReminder.description, `is`(reminder.description))
        assertThat(dbReminder.location, `is`(reminder.location))
        assertThat(dbReminder.latitude, `is`(reminder.latitude))
        assertThat(dbReminder.longitude, `is`(reminder.longitude))
        assertThat(dbReminder.radius, `is`(reminder.radius))
    }

    @Test
    fun getAllReminders_multipleReminders_returnsAll() = runTest {
        // GIVEN two reminders are inserted into the database.
        val reminder1 = createReminder()
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.0, 200.0)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN the list of all reminders is retrieved.
        val reminders = database.reminderDao().getReminders()

        // THEN the list contains exactly two reminders.
        assertThat(reminders.size, `is`(2))
    }

    @Test
    fun deleteAllReminders_clearsTheDatabase() = runTest {
        // GIVEN - A reminder is inserted into the database.
        val reminder = createReminder()
        database.reminderDao().saveReminder(reminder)

        // WHEN - All reminders are deleted from the database.
        database.reminderDao().deleteAllReminders()

        // THEN - The list of reminders is empty.
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))
    }

    @Test
    fun getReminderById_withInvalidId_returnsNull() = runTest {
        // GIVEN an invalid reminder ID.
        val invalidId = "invalidId"

        // WHEN a reminder is requested with the invalid ID.
        val loaded = database.reminderDao().getReminderById(invalidId)

        // THEN the result is null.
        assertThat(loaded, `is`(nullValue()))
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