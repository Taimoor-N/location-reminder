package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent == null) {
                Log.e(TAG, "GeofencingEvent is null. Cannot process.")
                return
            }

            if (geofencingEvent.hasError()) {
                val errorMessage = geofenceError(context, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.i(TAG, context.getString(R.string.geofence_entered))

                val triggeringGeofences = geofencingEvent.triggeringGeofences

                // Check if the list is not null AND not empty
                if (!triggeringGeofences.isNullOrEmpty()) {
                    // Extract all non-null and non-empty request IDs
                    val triggeringGeofenceIds = triggeringGeofences
                        .mapNotNull { it.requestId }
                        .filter { it.isNotEmpty() }


                    if (triggeringGeofenceIds.isNotEmpty()) {
                        Log.d(TAG, "Processing geofence IDs: ${triggeringGeofenceIds.joinToString()}")

                        val workData = Data.Builder()
                            .putStringArray(
                                GeofenceTransitionsWorker.KEY_TRIGGERING_GEOFENCE_IDS,
                                triggeringGeofenceIds.toTypedArray()
                            )
                            .putInt(
                                GeofenceTransitionsWorker.KEY_GEOFENCE_TRANSITION_TYPE,
                                geofencingEvent.geofenceTransition
                            )
                            .build()

                        val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceTransitionsWorker>()
                            .setInputData(workData)
                            .build()

                        WorkManager.getInstance(context.applicationContext).enqueue(geofenceWorkRequest)
                        Log.d(TAG, "Enqueued work to WorkManager for geofence IDs: ${triggeringGeofenceIds.joinToString()}")

                    } else {
                        Log.w(TAG, "All triggering geofences had null or empty IDs.")
                    }
                } else {
                    Log.w(TAG, "No triggering geofences found for ENTER transition.")
                }
            } else {
                Log.d(TAG, "Geofence transition type not 'ENTER': ${geofencingEvent.geofenceTransition}")
            }
        }
    }
}