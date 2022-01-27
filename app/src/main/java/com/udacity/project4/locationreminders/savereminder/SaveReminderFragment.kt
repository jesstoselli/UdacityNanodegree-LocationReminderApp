package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceUtils
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val baseViewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var newReminder: ReminderDataItem

    private lateinit var geofenceUtils: GeofenceUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = baseViewModel

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.tvSelectLocation.setOnClickListener {
            baseViewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        geofenceUtils = GeofenceUtils(requireContext())

        populateTitleFieldWithPoiInfo()

        binding.fabSaveReminder.setOnClickListener {
            val title = baseViewModel.reminderTitle.value
            val description = baseViewModel.reminderDescription.value
            val location = baseViewModel.reminderSelectedLocationStr.value
            val latitude = baseViewModel.latitude.value
            val longitude = baseViewModel.longitude.value

            if (latitude != null && longitude != null && !title.isNullOrEmpty()) {
                createGeofence(LatLng(latitude, longitude), title)
            }

            newReminder = ReminderDataItem(title, description, location, latitude, longitude)
            baseViewModel.validateAndSaveReminder((newReminder))
        }
    }

    private fun populateTitleFieldWithPoiInfo() {
        if (baseViewModel.reminderTitle.value.isNullOrEmpty().not()) {
            binding.etReminderTitle.setText(baseViewModel.reminderTitle.value)
        }
    }

    @SuppressLint("MissingPermission")
    private fun createGeofence(latLng: LatLng, geofenceId: String) {
        val newGeofence =
            geofenceUtils.getGeofence(geofenceId, latLng, Geofence.GEOFENCE_TRANSITION_ENTER)

        val newGeofencingRequest = geofenceUtils.createGeofencingRequest(newGeofence)
        val pendingIntent = geofenceUtils.geofencePendingIntent()

        if (pendingIntent != null) {
            geofencingClient.addGeofences(newGeofencingRequest, pendingIntent)
                .addOnFailureListener {
                    geofenceUtils.geofenceError(it)
                    Toast.makeText(
                        context,
                        "Please, enable background location permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        baseViewModel.onClear()
    }
}
