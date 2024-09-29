package com.jojodev.taipeitrash.TrashCarScreen


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.data.TrashCar
import com.jojodev.taipeitrash.data.network.TrashApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//https://stackoverflow.com/questions/67128991/android-get-response-status-code-using-retrofit-and-coroutines
class TrashCarViewModel : ViewModel() {

    private val _uistate: MutableStateFlow<ApiStatus> = MutableStateFlow(ApiStatus.LOADING)
    val uistate = _uistate.asStateFlow()

    private val _trashCar = MutableStateFlow(emptyList<TrashCar>())
    val trashCar = _trashCar
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
        Log.v("TrashCarViewModel", "init")
    }
    private fun getAllTrashCars(): Job {
        return viewModelScope.launch {
            var offset = 0
            val list = mutableListOf<TrashCar>()
            val limit = 1000
            var count = -1
            do {
                try {
                    _uistate.value = ApiStatus.LOADING
                    val listResult = TrashApi.retrofitService.getTrashCar(offset = offset)
                    if (count < 0) {
                        if (listResult.result.count == 0) {
                            throw Exception("No data")
                        }
                        count = listResult.result.count
                        _importDate.value = listResult.result.trashCars[0]._importdate.date
                    }
                    list.addAll(listResult.result.trashCars)
                    offset += limit
                } catch (e: Exception) {
                    Log.e("TrashCarViewModel", "getAllTrashCars: ${e.message}")
                    _trashCar.value = listOf()
                    _uistate.value = ApiStatus.ERROR
                    return@launch
                }
            } while (offset < count)
            _trashCar.value = list
            _uistate.value = ApiStatus.DONE
            Log.i("TrashCarViewModel", "getAllTrashCars: ${_trashCar.value.size}")
        }
    }

    fun fetchData() {
        fetchDataJob?.cancel()
        fetchDataJob = getAllTrashCars()
        Log.i("TrashCarViewModel", "fetchData: Job started")
    }

    fun cancelFetchData() {
        fetchDataJob?.cancel()
        clearTrashCar()
        Log.i("TrashCarViewModel", "cancelFetchData: Job cancelled")
    }

    override fun onCleared() {
        super.onCleared()
        Log.v("TrashCarViewModel", "onCleared")
    }

    fun clearTrashCar() {
        _trashCar.value = listOf()
        _uistate.value = ApiStatus.LOADING
    }
}

sealed class ApiStatus {
    data object LOADING : ApiStatus()
    data object ERROR : ApiStatus()
    data object DONE : ApiStatus()
}
