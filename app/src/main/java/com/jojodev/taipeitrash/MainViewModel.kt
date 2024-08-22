package com.jojodev.taipeitrash

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.data.TrashCan
import com.jojodev.taipeitrash.data.TrashResults
import com.jojodev.taipeitrash.data.network.TrashApi
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    var uistate by mutableStateOf<ApiStatus>(ApiStatus.LOADING)
        private set

    var response by mutableStateOf<TrashResults?>(null)
        private set
    var trashCan by mutableStateOf<List<TrashCan>>(listOf())
        private set

//    init {
//        Log.v("MainViewModel", "init")
//        getTrashCan()
//    }

    fun getTrashCan() {
        viewModelScope.launch {
            try {
                val listResult = TrashApi.retrofitService.getTrashCan()
                response = listResult
                uistate = ApiStatus.DONE
            } catch (e: Exception) {
                Log.e("MainViewModel", "getTrash: ${e.message}")
                response = null
                uistate = ApiStatus.ERROR
            }
        }
    }

    fun getAllTrashCans() {
        viewModelScope.launch {
            var offset = 0
            val list = mutableListOf<TrashCan>()
            val limit = 1000
            var count = -1
            do {
                try {
                    val listResult = TrashApi.retrofitService.getTrashCan(offset = offset)
                    if (count < 0) count = listResult.result.count
                    list.addAll(listResult.result.trashCans)
                    offset += limit
                } catch (e: Exception) {
                    Log.e("MainViewModel", "getAllTrashCans: ${e.message}")
                    trashCan = listOf()
                    uistate = ApiStatus.ERROR
                    return@launch
                }
            } while (offset < count)
            trashCan = list
            uistate = ApiStatus.DONE
            Log.i("MainViewModel", "getAllTrashCans: ${trashCan.size}")
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

    override fun onCleared() {
        super.onCleared()
        Log.v("MainViewModel", "onCleared")
    }
}

sealed class ApiStatus {
    data object LOADING: ApiStatus()
    data object ERROR: ApiStatus()
    data object DONE: ApiStatus()
}
