package com.udacity.project4.locationreminders.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R

class GeofenceUtils(private val geofenceContext: Context) : ContextWrapper(geofenceContext) {

    var pendingIntent: PendingIntent? = null

    fun createGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()
    }

    fun getGeofence(
        geofenceId: String,
        coordinates: LatLng,
        transitionTypes: Int
    ): Geofence {
        return Geofence.Builder()
            .setCircularRegion(coordinates.latitude, coordinates.longitude, GEOFENCE_RADIUS_IN_METERS)
            .setRequestId(geofenceId)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(4000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    fun geofencePendingIntent(): PendingIntent? {
        if (pendingIntent != null) return pendingIntent

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            geofenceContext,
            GEOFENCE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return pendingIntent
    }

    fun geofenceError(e: Exception): String {
        if (e is ApiException) {
            when (e.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return getString(R.string.geofence_not_available)
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return getString(R.string.geofence_too_many_geofences)
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return getString(R.string.geofence_too_many_pending_intents)
            }
        }
        return e.localizedMessage ?: "Something went wrong."
    }

    companion object {
        private const val TAG = "GeofenceUtil"
        private const val GEOFENCE_REQUEST_CODE = 777
        const val GEOFENCE_RADIUS_IN_METERS = 300f
        internal const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
    }


}