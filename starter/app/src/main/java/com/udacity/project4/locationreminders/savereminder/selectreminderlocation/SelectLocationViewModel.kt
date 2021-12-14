package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver.Companion.GEOFENCE_RADIUS_IN_METERS

class SelectLocationViewModel : ViewModel() {

    private val _selectedLocation = MutableLiveData<PointOfInterest>()

    var zoom = 15f

    val selectedLocation: LiveData<PointOfInterest>
        get() = _selectedLocation

    fun defineSelectedLocation(poi: PointOfInterest?, latLgn: LatLng?) {
        if (latLgn != null) {
            _selectedLocation.value = PointOfInterest(latLgn, null, null)
        } else if (poi != null) {
            _selectedLocation.value = poi
        }
    }

//    private val _radiusSelectorOpen = MutableLiveData(false)

//
//    val radiusSelectorOpen: LiveData<Boolean>
//        get() = _radiusSelectorOpen

//    val radius = MutableLiveData(GEOFENCE_RADIUS_IN_METERS)

//    fun toggleRadiusSelector() {
//        _radiusSelectorOpen.value = _radiusSelectorOpen.value?.not()
//    }
//
//    fun closeRadiusSelector() {
//        _radiusSelectorOpen.value = false
//    }
}