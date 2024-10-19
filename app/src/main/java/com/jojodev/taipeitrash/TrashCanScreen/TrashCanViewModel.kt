package com.jojodev.taipeitrash.TrashCanScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.data.TrashCan
import com.jojodev.taipeitrash.data.network.TrashApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//https://stackoverflow.com/questions/67128991/android-get-response-status-code-using-retrofit-and-coroutines
class TrashCanViewModel : ViewModel() {

    private val _uistate: MutableStateFlow<ApiStatus> = MutableStateFlow(ApiStatus.LOADING)
    val uistate = _uistate.asStateFlow()

    private val _trashCan = MutableStateFlow(emptyList<TrashCan>())
    val trashCan = _trashCan
        .onStart { fetchData() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            emptyList()
        )

    private val _importDate = MutableStateFlow("")
    val importDate = _importDate.asStateFlow()

    private var fetchDataJob: Job? = null

    init {
        Log.v("MainViewModel", "init")
    }
    private fun getAllTrashCans(): Job {
        return viewModelScope.launch {
            var offset = 0
            val list = mutableListOf<TrashCan>()
            _uistate.value = ApiStatus.DONE
            val limit = 1000
            var count = -1
            do {
                try {
                    _uistate.value = ApiStatus.LOADING
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

    fun fetchData() {
        fetchDataJob?.cancel()
        fetchDataJob = getAllTrashCans()
        Log.i("MainViewModel", "fetchData: Job started")
    }

    fun cancelFetchData() {
        fetchDataJob?.cancel()
        clearTrashCan()
        Log.i("MainViewModel", "cancelFetchData: Job cancelled")
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
    data object LOADING : ApiStatus()
    data object ERROR : ApiStatus()
    data object DONE : ApiStatus()
}
