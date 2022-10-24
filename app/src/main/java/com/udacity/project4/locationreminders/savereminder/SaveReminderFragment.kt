package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
private const val TAG = "SaveReminderFragment"
class SaveReminderFragment : BaseFragment() {
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private val runningQOrLater = Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
    private var reminderFromViewModel = ReminderDataItem("", "", "", 0.0, 0.0)
    private val requestDeviceLocationOn = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { permission ->
        if( permission.resultCode == 1) {
            checkDeviceLocationSettings()
        }else {
            createGeofenceRequest(reminderFromViewModel)
        }
    }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.d(TAG, "onCreateView: permissionLauncher ")
            val isLocationGranted =  permissions[Manifest.permission.ACCESS_FINE_LOCATION]
            if (!isLocationGranted!!) {
                Toast.makeText(
                    requireContext(),
                    " location permission is NOT granted!",
                    Toast.LENGTH_LONG
                ).show()

            }else {
                requestPermissions()
            }

        }
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            reminderFromViewModel = ReminderDataItem(title, description, location, latitude, longitude)

            if (_viewModel.validateEnteredData(reminderFromViewModel)) {
                if (isForegroundAndBackgroundLocationPermissionOk()) {
                    checkDeviceLocationSettings()
                } else {
                    requestPermissions()
                }
            }

        }
    }

    private fun createGeofenceRequest(
        reminder: ReminderDataItem
    ) {
        val requestID = reminder.id
        val longitude = reminder.longitude!!
        val latitude = reminder.latitude!!
        val geofence = Geofence.Builder()
            .setRequestId(requestID)
            .setCircularRegion(
                latitude,
                longitude,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setNotificationResponsiveness(300000)
            .build()

        val geofenceRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                _viewModel.saveReminder(reminderFromViewModel)
            }
            addOnFailureListener { ex ->
                Toast.makeText(
                    requireContext(), "Failed to add geofence",
                    Toast.LENGTH_SHORT
                ).show()
                if (ex.message != null) {
                    Log.d(TAG, ex.message!!)
                }
            }
        }
    }

    private fun checkDeviceLocationSettings() {
        Log.d(TAG, "checkDeviceLocationSettings: 1")
        val requestLocation = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(requestLocation)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { ex ->
            Log.d(TAG, "checkDeviceLocationSettings: 2")
            if (ex is ResolvableApiException ) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(ex.resolution).build()
                    Log.d(TAG, "checkDeviceLocationSettings: 3")
                    requestDeviceLocationOn.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        TAG,
                        "Error open location settings : " + sendEx.message
                    )
                    Log.d(TAG, "checkDeviceLocationSettings: 4")
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
                Log.d(TAG, "checkDeviceLocationSettings: 5")
            }
        }
        locationSettingsResponseTask.addOnCompleteListener { locationSettingResponse ->
            if (locationSettingResponse.isSuccessful) {
                createGeofenceRequest(reminderFromViewModel)
                Log.d(TAG, "checkDeviceLocationSettings: 6")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestPermissions() {
        if (isForegroundAndBackgroundLocationPermissionOk())
            return
        var array = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (runningQOrLater) {
                array += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                1
            }
            else
                2
        requestPermissionLauncher.launch(array)

    }
    @RequiresApi(Build.VERSION_CODES.Q)
    @TargetApi(29)
    private fun isForegroundAndBackgroundLocationPermissionOk(): Boolean {
        val isLocationOk = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val isBackgroundLocationOk =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return isLocationOk && isBackgroundLocationOk
    }
    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }
}
