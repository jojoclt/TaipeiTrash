package com.jojodev.taipeitrash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.data.TrashCan
import com.jojodev.taipeitrash.data.TrashResults
import com.jojodev.taipeitrash.data.network.TrashApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//https://stackoverflow.com/questions/67128991/android-get-response-status-code-using-retrofit-and-coroutines
class MainViewModel: ViewModel() {
//    var uistate by mutableStateOf<ApiStatus>(ApiStatus.LOADING)
//        private set
//
//    var response by mutableStateOf<TrashResults?>(null)
//        private set
//    var trashCan by mutableStateOf<List<TrashCan>>(listOf())
//        private set
//
//    var importDate by mutableStateOf<String>("")
//        private set
    private val _uistate: MutableStateFlow<ApiStatus> = MutableStateFlow(ApiStatus.LOADING)
    val uistate = _uistate.asStateFlow()

    private val _response = MutableStateFlow<TrashResults?> (null)
    val response = _response.asStateFlow()

    private val _trashCan = MutableStateFlow(emptyList<TrashCan>())
    val trashCan = _trashCan.asStateFlow()

    private val _importDate = MutableStateFlow("")
    val importDate = _importDate.asStateFlow()

//    val importDate: StateFlow<String>
//    field = MutableStateFlow("")

//    init {
//        Log.v("MainViewModel", "init")
//        getTrashCan()
//    }

    fun getTrashCan() {
        viewModelScope.launch {
            try {
                val listResult = TrashApi.retrofitService.getTrashCan()
                _response.value = listResult
                _uistate.value = ApiStatus.DONE
            } catch (e: Exception) {
                Log.e("MainViewModel", "getTrash: ${e.message}")
                _response.value = null
                _uistate.value = ApiStatus.ERROR
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
                    if (count < 0) {
                        if (listResult.result.count == 0) {
                            throw Exception("No data")
                        }
                        count = listResult.result.count
                        _importDate.value = listResult.result.trashCans[0]._importdate.date
                    }
                    list.addAll(listResult.result.trashCans)
                    offset += limit
                } catch (e: Exception) {
                    Log.e("MainViewModel", "getAllTrashCans: ${e.message}")
                    _trashCan.value = listOf()
                    _uistate.value = ApiStatus.ERROR
                    return@launch
                }
            } while (offset < count)
            _trashCan.value = list
            _uistate.value = ApiStatus.DONE
            Log.i("MainViewModel", "getAllTrashCans: ${_trashCan.value.size}")
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

    fun clearTrashCan() {
        _trashCan.value = listOf()
        _uistate.value = ApiStatus.LOADING
    }
}

sealed class ApiStatus {
    data object LOADING: ApiStatus()
    data object ERROR: ApiStatus()
    data object DONE: ApiStatus()
}
