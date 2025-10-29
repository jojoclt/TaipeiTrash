package com.jojodev.taipeitrash.startup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.trashcan.data.TrashCanRepository
import com.jojodev.taipeitrash.trashcar.data.TrashCarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val trashCarRepository: TrashCarRepository,
    private val trashCanRepository: TrashCanRepository
) : ViewModel() {

    private val _trashCarProgress = MutableStateFlow(0f)
    private val _trashCanProgress = MutableStateFlow(0f)

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    val loadingProgress: StateFlow<Float> = combine(
        _trashCarProgress,
        _trashCanProgress
    ) { carProgress, canProgress ->
        (carProgress + canProgress) / 2f
    }.onStart {
        loadData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0f
    )

    private fun loadData() {
        viewModelScope.launch {
            try {
                coroutineScope {
                    launch {
                        trashCarRepository.getTrashCars { progress ->
                            _trashCarProgress.value = progress
                        }
                    }

                    launch {
                        trashCanRepository.getTrashCans { progress ->
                            _trashCanProgress.value = progress
                        }
                    }
                }

                _isLoaded.value = true
            } catch (e: Exception) {
                Log.e("StartupViewModel", "Failed to preload data", e)
            }
        }
    }
}
