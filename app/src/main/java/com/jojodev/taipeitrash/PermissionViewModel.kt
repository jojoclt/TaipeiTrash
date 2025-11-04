package com.jojodev.taipeitrash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.core.data.PermissionPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PermissionViewModel(private val context: Context) : ViewModel() {

    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionPreferences = PermissionPreferences(context)

    private val _isLaunchedOnce = MutableStateFlow(false)
    val isLaunchedOnce = _isLaunchedOnce.asStateFlow()

    private val _permissionGranted = MutableStateFlow<Boolean?>(null)
    val permissionGranted = _permissionGranted.onStart {
        _permissionGranted.value = getPermissionEnabled(context, permission)
    }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            null
        )

    // Track if permission was ever denied (persisted across app launches)
    val wasPermissionDenied = permissionPreferences.wasPermissionDenied.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        false
    )

    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted

        if (!granted) {
            // User denied permission - persist this state
            viewModelScope.launch {
                permissionPreferences.setPermissionDenied(true)
            }
        } else {
            // User granted permission - clear the denial flag
            viewModelScope.launch {
                permissionPreferences.clearPermissionDenied()
            }
        }
    }

    fun setLaunchedOnce(launched: Boolean) {
        _isLaunchedOnce.value = launched
    }

    private fun getPermissionEnabled(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED


}
