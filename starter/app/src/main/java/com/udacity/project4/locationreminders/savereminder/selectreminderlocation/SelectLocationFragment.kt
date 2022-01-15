package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val baseViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var poi: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        with(binding) {
            viewModel = baseViewModel
            lifecycleOwner = this@SelectLocationFragment
            btnSaveThisLocation.setOnClickListener { onLocationSelected() }
        }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun onLocationSelected() {

        if (this::poi.isInitialized) {
            with(baseViewModel) {
                latitude.value = poi.latLng.latitude
                longitude.value = poi.latLng.longitude
                reminderSelectedLocationStr.value = poi.name
                selectedPOI.value = poi
                navigationCommand.value =
                    NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            }
        } else {
            Toast.makeText(context, "Please, select a location to proceed.", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapStartingPosition(map)

        setMapStyle(map)

        setPoiClick(map)

        setMapLongClick(map)

        isMyLocationEnabled()
    }

    private fun setMapStartingPosition(map: GoogleMap) {
        val sheffield = LatLng(53.3795459, -1.4779999)
        val zoom = 15f

        with(map) {
            addMarker(MarkerOptions().position(sheffield).title("Marker in Campinas, SP"))
            moveCamera(CameraUpdateFactory.newLatLngZoom(sheffield, zoom))
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
        map.setOnPoiClickListener { poi ->
            map.clear()
            this.poi = poi

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .draggable(true)
            )
            poiMarker?.showInfoWindow()

            map.addCircle(
                CircleOptions()
                    .center(poi.latLng)
                    .radius(177.0)
                    .strokeColor(Color.argb(10, 102, 103, 171))
                    .fillColor(Color.argb(1, 102, 103, 171))
                    .strokeWidth(3F)
            )
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { coordinates ->
            map.clear()
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
        }
    }

    private fun isMyLocationEnabled() {
        when {
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                map.isMyLocationEnabled = true
            }
            (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }
            else ->
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                isMyLocationEnabled()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission not granted.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
        private const val FINE_LOCATION_ACCESS_REQUEST_CODE = 1
        private val TAG = SelectLocationFragment::class.java.simpleName
    }
}