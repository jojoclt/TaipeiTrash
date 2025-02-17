package com.jojodev.taipeitrash.trashcar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.core.Results
import com.jojodev.taipeitrash.trashcan.data.TrashCar
import com.jojodev.taipeitrash.trashcar.data.TrashCarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//https://stackoverflow.com/questions/67128991/android-get-response-status-code-using-retrofit-and-coroutines
@HiltViewModel
class TrashCarViewModel @Inject constructor(private val trashCarRepository: TrashCarRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow<Results<List<TrashCar>>>(Results.Loading)
    val uiState = _uiState.onStart { fetchData() }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        Results.Loading
    )

    private val _importDate = MutableSharedFlow<String>()
    val importDate = _importDate.asSharedFlow()


    private var fetchDataJob: Job? = null

    init {
        Log.v("TrashCarViewModel", "init")
    }

    private fun getAllTrashCars(): Job {
        return viewModelScope.launch {
            try {
                _uiState.value = Results.Loading
                val results = trashCarRepository.getTrashCars()
                _importDate.emit(results[0].importDate)
                _uiState.value = Results.Success(results)
            } catch (e: Exception) {
                _uiState.value = Results.Error(e)
            }
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
        fetchDataJob?.cancel()
        Log.v("TrashCarViewModel", "onCleared")
    }

    private fun clearTrashCar() {
        _uiState.value = Results.Loading
    }
}

