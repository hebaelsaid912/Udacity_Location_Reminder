package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Permissions
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val TAG = "SelectLocationFragment"
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var selectedLocName: String = ""
    private var selectedLocLat: Double = 0.0
    private var selectedLocLng: Double = 0.0
    private var activeMarker: Marker? = null

    private lateinit var cancellationSource: CancellationTokenSource
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        /*requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.d(TAG, "onCreateView: permissionLauncher ")
             val isLocationGranted =  permissions[Manifest.permission.ACCESS_FINE_LOCATION]
            if (!isLocationGranted!!) {
                Toast.makeText(
                    requireContext(),
                    " location permission is NOT granted!",
                    Toast.LENGTH_LONG
                )
                    .show()
                //fetchLocationPermission()
            }else {
                selectCurrentLocationOnMap()
            }

        }*/

//        TODO: add the map setup implementation
        val mapFragment =
            childFragmentManager.findFragmentById(binding.googleMap.id) as SupportMapFragment
        mapFragment.getMapAsync(this)
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        // Add clickListener to the confirmButton
        binding.confirmButton.setOnClickListener {
            if(!checkLocationPermission()) {
                if (selectedLocName.isEmpty()) {
                    Toast.makeText(requireContext(), "No location selected!", Toast.LENGTH_LONG)
                        .show()
                }
                Snackbar.make(
                    view!!,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    fetchLocationPermission()
                }.show()
            }else {
                onLocationSelected()
            }
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.reminderSelectedLocationStr.value = selectedLocName
        _viewModel.latitude.value = selectedLocLat
        _viewModel.longitude.value = selectedLocLng
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    private fun setLocationData(name: String, lat: Double, lng: Double) {
        selectedLocName = name
        selectedLocLat = lat
        selectedLocLng = lng
    }

    override fun onStart() {
        super.onStart()
        cancellationSource = CancellationTokenSource()
    }
    override fun onStop() {
        super.onStop()
        cancellationSource.cancel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        setPoiClick(map)
        selectCurrentLocationOnMap()
    }
    private fun selectCurrentLocationOnMap() {
        if (checkLocationPermission()) {
            updateMap()
            getCurrentDeviceLocation {
                val zoomLevel = 15f
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, zoomLevel))
                setLocationData("Current Location", it.latitude, it.longitude)
                activeMarker?.remove()
                activeMarker = map.addMarker(MarkerOptions().position(it))
            }
        } else {
            updateMap()
        }

        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            setLocationData("Custom Selected Location", latLng.latitude, latLng.longitude)
            activeMarker?.remove()
            activeMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(selectedLocName)
                    .snippet(snippet)
            )
        }
    }
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            activeMarker?.remove()
            activeMarker= map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            val zoomLevel = 15f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, zoomLevel))
            activeMarker!!.showInfoWindow()
            setLocationData(poi.name, poi.latLng.latitude, poi.latLng.longitude)
        }
    }

    private fun updateMap() {
        try {
            map.isMyLocationEnabled = checkLocationPermission()
            map.uiSettings.isMyLocationButtonEnabled = checkLocationPermission()
        } catch (e: SecurityException) {
            Log.d(TAG, "Exception: ${e.message}")
        }
    }
    private fun getCurrentDeviceLocation(callback: (LatLng) -> Unit) {
        try {
            val getCurrentLocation = fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationSource.token
            )
            getCurrentLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val latLng = LatLng(task.result.latitude, task.result.longitude)
                    callback(latLng)
                }
            }
        } catch (ex: SecurityException) {
            Log.d(TAG, "getCurrentDeviceLocation: ${ex.message}")
        }
    }
    private fun checkLocationPermission(): Boolean {
        return Permissions.checkLocationPermission(requireContext())
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Toast.makeText(context,"Style parsing failed.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(context, "error $e",Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchLocationPermission() {
        if (!checkLocationPermission()) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}
