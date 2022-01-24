package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofenceEvent = GeofencingEvent.fromIntent(intent)

        if (geofenceEvent.hasError()) {
            Toast.makeText(context, "Error on receiving geofence.", Toast.LENGTH_LONG).show()
        } else if (geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        } else {
            return
        }

//        if (intent.action == ACTION_GEOFENCE_EVENT) {
//            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
//        }
    }
}