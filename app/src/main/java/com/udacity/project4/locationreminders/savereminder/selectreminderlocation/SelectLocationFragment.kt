package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val baseViewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    private lateinit var lastKnownLocation: Location

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        with(binding) {
            viewModel = baseViewModel
            lifecycleOwner = this@SelectLocationFragment
            btnSaveThisLocation.isExtended = false
            btnSaveThisLocation.setOnClickListener {
                baseViewModel.navigationCommand.value =
                    NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            }
        }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun onLocationSelected(poi: PointOfInterest) {
        with(baseViewModel) {
            latitude.value = poi.latLng.latitude
            longitude.value = poi.latLng.longitude
            reminderSelectedLocationStr.value = poi.name
            selectedPOI.value = poi
            reminderTitle.value = poi.name
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapStartingPosition(map)

        setMapStyle(map)

        setPoiClick(map)

        setMapLongClick(map)

        getDeviceLocation()
    }

    private fun setMapStartingPosition(map: GoogleMap) {
        val cardiff = LatLng(51.4815067, -3.1794096)
        val zoom = 15f

        with(map) {
            addMarker(MarkerOptions().position(cardiff).title("Marker in Cardiff, Wales"))
            moveCamera(CameraUpdateFactory.newLatLngZoom(cardiff, zoom))
            uiSettings.isZoomControlsEnabled = true
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
                    .snippet("${it.latLng.latitude},${it.latLng.longitude}")
                    .draggable(true)
            )

            map.addCircle(
                CircleOptions()
                    .center(it.latLng)
                    .radius(200.0)
                    .strokeColor(Color.argb(255, 255, 0, 0))
                    .fillColor(Color.argb(64, 255, 0, 0)).strokeWidth(4F)
            )

            poiMarker?.showInfoWindow()
            onLocationSelected(it)
            binding.btnSaveThisLocation.isExtended = true
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { coordinates ->
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                coordinates.latitude,
                coordinates.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(coordinates)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            )

            with(baseViewModel) {
                latitude.value = coordinates.latitude
                longitude.value = coordinates.longitude
                reminderSelectedLocationStr.value = getString(R.string.dropped_pin)
                selectedPOI.value =
                    PointOfInterest(LatLng(coordinates.latitude, coordinates.longitude), "", "")
            }

            binding.btnSaveThisLocation.isExtended = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager
                    .PERMISSION_GRANTED
            ) {
                getDeviceLocation()
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Displays App settings screen.
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        try {
            if (isPermissionGranted()) {
                map.isMyLocationEnabled = true
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        if (task.result != null) {
                            lastKnownLocation = task.result!!
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation.latitude,
                                        lastKnownLocation.longitude
                                    ),
                                    15f
                                )
                            )
                        }
                    } else {
                        Log.e(TAG, "Exception: %s", task.exception)
                        setMapStartingPosition(map)
                    }
                }
            } else {
                requestPermissions(
                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val FINE_LOCATION_ACCESS_REQUEST_CODE = 2
        private val TAG = SelectLocationFragment::class.java.simpleName
    }
}
