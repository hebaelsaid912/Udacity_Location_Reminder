package com.udacity.project4.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.udacity.project4.permissionRequestList

object Permissions {
     fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    fun checkBackgroundLocationPermission(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    }
    fun requestLocationPermission(){
        permissionRequestList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionRequestList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    fun requestBackgroundLocationPermission(){
        permissionRequestList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}