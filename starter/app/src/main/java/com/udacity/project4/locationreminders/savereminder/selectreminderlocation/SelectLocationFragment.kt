package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver.Companion.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import java.util.*
import java.util.concurrent.Executors
import java.util.function.Consumer
import org.koin.android.ext.android.inject
import org.koin.core.context.GlobalContext

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private val selectLocationViewModel: SelectLocationViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var selectedLocationMarker: Marker
    private lateinit var selectedLocationCircle: Circle

    private var permissionsGranted: Boolean = false
    private val locationManager: LocationManager?
        get() =
            GlobalContext.getOrNull()
                ?.koin
                ?.get<Application>()
                ?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val requestExecutor by lazy { Executors.newSingleThreadExecutor() }

    //    private val TAG = SelectLocationFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        with(binding) {
            saveReminderViewModel = _viewModel
            lifecycleOwner = this@SelectLocationFragment
            btnSaveThisLocation.setOnClickListener { onLocationSelected() }
        }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        settingUpGoogleMaps()

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.run {
            setSelectedLocation(selectLocationViewModel.selectedLocation.value!!)
            navigationCommand.postValue(NavigationCommand.Back)

        }
    }

    private fun settingUpGoogleMaps() {
        val fragment =
            childFragmentManager.findFragmentByTag(getString(R.string.reminders_mapFragment)) as? SupportMapFragment
                ?: return

        selectedLocationCircle.radius = GEOFENCE_RADIUS_IN_METERS.toDouble()

        selectLocationViewModel.selectedLocation.observe(viewLifecycleOwner, Observer { poi ->
            selectedLocationMarker.position = poi.latLng
            selectedLocationCircle.center = poi.latLng
        })

        fragment.getMapAsync(this)
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style
            )
        )

        setMapStartingPosition {
            selectLocationViewModel.defineSelectedLocation(null, LatLng(it.latitude, it.longitude))
        }

        val definedLatLng = selectLocationViewModel.selectedLocation.value!!.latLng

        val markerOptions = MarkerOptions()
            .position(definedLatLng)
            .title(getString(R.string.dropped_pin))
            .draggable(true)

        val circleOptions = CircleOptions()
            .center(map.cameraPosition.target)
            .fillColor(ResourcesCompat.getColor(resources, R.color.mapRadiusFillColor, null))
            .strokeColor(ResourcesCompat.getColor(resources, R.color.mapRadiusStrokeColor, null))
            .strokeWidth(4f)
            .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())

        selectedLocationMarker = map.addMarker(markerOptions)
        selectedLocationCircle = map.addCircle(circleOptions)

        selectLocationViewModel.selectedLocation.value.let {
            selectLocationViewModel.defineSelectedLocation(
                it ?: PointOfInterest(map.cameraPosition.target, null, null), null
            )

            if (it == null) {
                setMapStartingPosition {}
            }
        }

        with(map) {
            setOnMapLongClickListener {
                selectLocationViewModel.defineSelectedLocation(null, it)
            }

            setOnPoiClickListener {
                selectLocationViewModel.defineSelectedLocation(it, null)
            }

            setOnCameraMoveListener {
                selectLocationViewModel.zoom = map.cameraPosition.zoom
            }
        }

        isMyLocationEnabled()
    }

    @SuppressLint("MissingPermission")
    private fun setMapStartingPosition(block: (Location) -> Unit) {
        if (permissionsGranted) {
            locationManager?.getLastKnownLocation(GPS_PROVIDER)?.let {
                block(it)
                return
            }
        }

        locationManager?.getCurrentLocation(
            GPS_PROVIDER,
            null,
            requestExecutor,
            Consumer<Location> {
                Handler(Looper.getMainLooper()).post { block(it) }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                isMyLocationEnabled()
            }
        }
    }

    private fun isMyLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            permissionsGranted = true
            map.isMyLocationEnabled = true
        }
    }

    companion object {
        private const val GPS_PROVIDER = LocationManager.GPS_PROVIDER
        private const val REQUEST_LOCATION_PERMISSION = 1
//        private val TAG = SelectLocationFragment::class.java.simpleName
    }
}
