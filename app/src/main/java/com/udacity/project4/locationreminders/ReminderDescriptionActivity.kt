package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.getParcelableExtraCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding
    private val viewModel: RemindersListViewModel by viewModel()
    private var reminderDataItem: ReminderDataItem? = null

    companion object {
        private const val EXTRA_REMINDER_DATA_ITEM = "EXTRA_REMINDER_DATA_ITEM"

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_REMINDER_DATA_ITEM, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)

        // Enable the back button on the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        reminderDataItem = intent.getParcelableExtraCompat(EXTRA_REMINDER_DATA_ITEM)
        if (reminderDataItem != null) {
            binding.reminderDataItem = reminderDataItem
        } else {
            // If no reminder data, close the activity
            finish()
            return
        }
        binding.executePendingBindings()

        binding.deleteReminderButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        addMenu()
    }

    private fun addMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.reminder_description_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    android.R.id.home -> {
                        onBackPressedDispatcher.onBackPressed()
                        true
                    }
                    R.id.action_edit -> {
                        // Navigate to SaveReminderFragment to edit the reminder
                        val intent = Intent(this@ReminderDescriptionActivity, RemindersActivity::class.java)
                        intent.putExtra(SaveReminderFragment.EXTRA_EDIT_REMINDER, reminderDataItem)
                        startActivity(intent)
                        finish() // Finish this activity so the user returns to the list after saving
                        true
                    }
                    else -> false
                }
            }
        }, this, Lifecycle.State.RESUMED)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_reminder)
            .setMessage(R.string.delete_confirmation_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteReminder()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteReminder() {
        reminderDataItem?.let {
            viewModel.deleteReminder(it)
            finish()
        }
    }
}