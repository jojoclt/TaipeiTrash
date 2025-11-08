package com.jojodev.taipeitrash.trashcan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.core.Results
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcan.data.repository.TrashCanRepository
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
class TrashCanViewModel @Inject constructor(private val trashCanRepository: TrashCanRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<Results<List<TrashCan>>>(Results.Loading)
    val uiState = _uiState.onStart { fetchData() }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        Results.Loading
    )

    private val _importDate = MutableSharedFlow<String>()
    val importDate = _importDate.asSharedFlow()

    private var fetchDataJob: Job? = null

    init {
        Log.i("TrashCanViewModel", "init")
    }

    private fun getAllTrashCans(forceUpdate: Boolean = false): Job {
        Log.i("TrashCanViewModel", "getAllTrashCans")

        return viewModelScope.launch {
            try {
                _uiState.value = Results.Loading
                val results = trashCanRepository.getTrashCans(forceUpdate = forceUpdate)
                _importDate.emit(results[0].importDate)
                _uiState.value = Results.Success(results)
            } catch (e: Exception) {
                _uiState.value = Results.Error(e)
            }
        }
    }

    fun fetchData(forceUpdate: Boolean = false) {
        fetchDataJob?.cancel()
        fetchDataJob = getAllTrashCans(forceUpdate)
        Log.i("TrashCanViewModel", "fetchData: Job started")
    }

    fun cancelFetchData() {
        fetchDataJob?.cancel()
        clearTrashCan()
        Log.i("TrashCanViewModel", "cancelFetchData: Job cancelled")
    }

    override fun onCleared() {
        super.onCleared()
        fetchDataJob?.cancel()
        Log.v("TrashCanViewModel", "onCleared")
    }

    private fun clearTrashCan() {
        _uiState.value = Results.Loading
    }
}

