package com.jojodev.taipeitrash

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.data.TrashResults
import com.jojodev.taipeitrash.data.network.TrashApi
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    var uistate by mutableStateOf<ApiStatus>(ApiStatus.LOADING)
        private set

    var response by mutableStateOf<TrashResults?>(null)
        private set

    init {
        getTrashCan()
    }

    private fun getTrashCan() {
        viewModelScope.launch {
            try {
                val listResult = TrashApi.retrofitService.getTrashCan()
                uistate = ApiStatus.DONE
                response = listResult
            } catch (e: Exception) {
                Log.e("MainViewModel", "getTrash: ${e.message}")
                uistate = ApiStatus.ERROR
                response = null
            }
        }
    }

    private fun getMarsPhotos() {
//        viewModelScope.launch {
//            try {
//                val listResult = MarsApi.retrofitService.getPhotos()
//                uistate = ApiStatus.DONE
//               list = listResult
//            } catch (e: Exception) {
//                Log.e("MainViewModel", "getMarsPhotos: ${e.message}")
//                uistate = ApiStatus.ERROR
//                list = listOf()
//            }
//        }
    }
}

sealed class ApiStatus {
    data object LOADING: ApiStatus()
    data object ERROR: ApiStatus()
    data object DONE: ApiStatus()
}
