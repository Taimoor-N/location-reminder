package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceTransitionsWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    companion object {
        const val TAG = "GeofenceTransitionsWorker"
        const val KEY_TRIGGERING_GEOFENCE_IDS = "triggered_geofence_ids"
        const val KEY_GEOFENCE_TRANSITION_TYPE = "geofence_transition_type"
    }

    private val remindersLocalRepository: ReminderDataSource by inject()

    override suspend fun doWork(): Result {
        Log.d(TAG, "WorkManager: Geofence transition work starting.")

        val geofenceIds = inputData.getStringArray(KEY_TRIGGERING_GEOFENCE_IDS)
        val transitionType = inputData.getInt(KEY_GEOFENCE_TRANSITION_TYPE, -1) // Default to invalid

        if (geofenceIds.isNullOrEmpty()) {
            Log.e(TAG, "No geofence IDs provided to worker.")
            return Result.failure()
        }

        if (transitionType != Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "Geofence transition type not 'ENTER': $transitionType. Work not needed.")
            return Result.success()
        }

        Log.d(TAG, "Geofence ENTER transition detected for IDs: ${geofenceIds.joinToString()}")
        sendNotificationForGeofences(geofenceIds.toList())

        return Result.success()
    }

    private suspend fun sendNotificationForGeofences(triggeringGeofenceIds: List<String>) {

    }
}