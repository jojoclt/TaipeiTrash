package com.jojodev.taipeitrash.trashcar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.core.Results
import com.jojodev.taipeitrash.core.data.TrashCar
import com.jojodev.taipeitrash.core.data.network.TrashApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//https://stackoverflow.com/questions/67128991/android-get-response-status-code-using-retrofit-and-coroutines
class TrashCarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<Results<List<TrashCar>>>(Results.Loading)
    val uiState = _uiState.onStart { fetchData() }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        Results.Loading
    )

    private val _importDate = MutableStateFlow("")
    val importDate = _importDate.asStateFlow()

    private var fetchDataJob: Job? = null

    init {
        Log.v("TrashCarViewModel", "init")
    }

    private fun getAllTrashCars(): Job {
        return viewModelScope.launch {
            try {
                _uiState.value = Results.Loading
                val results = fetchAllTrashCars(offset = 0, limit = 1000)
                _importDate.value = results[0]._importdate.date
                _uiState.value = Results.Success(results)
            } catch (e: Exception) {
                _uiState.value = Results.Error(e)
            }
        }
    }

    private suspend fun fetchAllTrashCars(
        offset: Int = 0,
        limit: Int = 1000,
    ): List<TrashCar> {
        val listResult = TrashApi.retrofitService.getTrashCar(offset = offset, limit = limit).result
        if (listResult.count == 0 && offset == 0) {
            throw Exception("No data")
        }
        var result = listResult.trashCars
        if (offset + limit < listResult.count)
            result = result + fetchAllTrashCars(offset + limit, limit)
        return result
    }

    fun fetchData() {
        fetchDataJob?.cancel()
        fetchDataJob = getAllTrashCars()
        Log.i("TrashCarViewModel", "fetchData: Job started")
    }

    fun cancelFetchData() {
        fetchDataJob?.cancel()
        clearTrashCan()
        Log.i("TrashCarViewModel", "cancelFetchData: Job cancelled")
    }

    override fun onCleared() {
        super.onCleared()
        fetchDataJob?.cancel()
        Log.v("TrashCarViewModel", "onCleared")
    }

    private fun clearTrashCan() {
        _uiState.value = Results.Loading
    }
}

