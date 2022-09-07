package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.permissionRequestList
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

private const val TAG = "ReminderListFragment"
class ReminderListFragment : BaseFragment() {
    // access location
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isLocationPermissionGranted = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            })
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.d(TAG, "onCreateView: permissionLauncher ")
                isLocationPermissionGranted =
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                        ?: isLocationPermissionGranted
                if (isLocationPermissionGranted) {
                    getCurrentLocation()
                }
            }
        getCurrentLocation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
//              TODO: add the logout implementation
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(), "You logout success", Toast.LENGTH_LONG).show()
                startActivity(Intent(requireActivity(), AuthenticationActivity::class.java))
                activity?.finish()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }
    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { location ->
                val geocoder =
                    Geocoder(requireContext(), Locale.ENGLISH)
                val address = geocoder.getFromLocation(
                    location.result.latitude,
                    location.result.longitude,
                    1
                )
                Log.d(
                    TAG,
                    "getCurrentLocation: latitude,longitude: ${address[0].latitude},${address[0].longitude}"
                )
                Log.d(TAG, "getCurrentLocation: latitude: ${address[0].latitude}")
                Log.d(TAG, "getCurrentLocation: longitude: ${address[0].longitude}")
            }
        }else {
            AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle("App Permission")
                .setMessage("This app need to access your current location")
                .setPositiveButton("ok"){ dialog, which ->
                    fetchLocationPermission()
                    dialog.cancel()
                }
                .setNegativeButton("cancel"){ dialog, which ->
                    dialog.cancel()
                }
                .create()
                .show()

            Log.d(TAG, "onCreateView: ")
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun fetchLocationPermission() {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!isLocationPermissionGranted) {
            permissionRequestList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionRequestList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionRequestList.isNotEmpty()) {
            permissionLauncher.launch(permissionRequestList.toTypedArray())
        }
    }
}
