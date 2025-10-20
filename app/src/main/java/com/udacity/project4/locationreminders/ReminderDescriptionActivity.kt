package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding

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

        val reminder = intent.getParcelableExtraCompat<ReminderDataItem>(EXTRA_REMINDER_DATA_ITEM)
        if (reminder != null) {
            binding.reminderDataItem = reminder
        }
        binding.executePendingBindings()
    }
}

/**
 * This extension function checks the Android version and uses the appropriate
 * getParcelableExtra method to avoid the NoSuchMethodError on devices
 * running Android versions older than TIRAMISU (API 33).
 */
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key) as? T
    }
}