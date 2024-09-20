package com.jojodev.taipeitrash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class PermissionViewModel(context: Context) : ViewModel() {

    val permission = Manifest.permission.ACCESS_FINE_LOCATION

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted = _permissionGranted.onStart { getPermissionEnabled(context, permission) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            false
        )

    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted
    }

    private fun getPermissionEnabled(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED


}
