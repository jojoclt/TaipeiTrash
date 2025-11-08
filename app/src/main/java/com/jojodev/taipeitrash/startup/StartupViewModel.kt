package com.jojodev.taipeitrash.startup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jojodev.taipeitrash.core.data.AppPreferencesDataStore
import com.jojodev.taipeitrash.core.model.City
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcan.data.repository.TrashCanRepository
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.trashcar.data.repository.TrashCarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val trashCarRepository: TrashCarRepository,
    private val trashCanRepository: TrashCanRepository,
    private val preferencesDataStore: AppPreferencesDataStore
) : ViewModel() {

    init {
        Log.d("StartupViewModel", "init:")
    }

    // Current selected city
    val selectedCity: StateFlow<City> = preferencesDataStore.selectedCity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = City.TAIPEI
        )
    private val _trashCarProgress = MutableStateFlow(0f)
    private val _trashCanProgress = MutableStateFlow(0f)

    private val _isLoaded =
        MutableStateFlow<Boolean?>(null) // null = initial, false = loading, true = loaded
    val isLoaded: StateFlow<Boolean?> = _isLoaded.asStateFlow()

    private val _lastRefresh = MutableStateFlow("")
    val lastRefresh: StateFlow<String> = _lastRefresh.asStateFlow()

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

    private val _trashCar = MutableStateFlow(emptyList<TrashCar>())
    val trashCar =
        _trashCar.onEach { Log.d("StartupViewModel", "trashCar for ${selectedCity.first()}: ${it.size}") }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )
    private val _trashCan = MutableStateFlow(emptyList<TrashCan>())
    val trashCan =
        _trashCan.onEach { Log.d("StartupViewModel", "trashCan for ${selectedCity.first()}: ${it.size}") }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )


    // Derived flows that expose the first item's importDate (or empty string)
    val trashCarLastRefresh: StateFlow<String> = trashCar
        .map { list ->
            val datetime = if (list.isNotEmpty()) list.first().importDate else ""
            datetime.parseDateTime()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ""
        )

    val trashCanLastRefresh: StateFlow<String> = trashCan
        .map { list ->
            val datetime = if (list.isNotEmpty()) list.first().importDate else ""
            datetime.parseDateTime()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ""
        )

    private fun String.parseDateTime() = runCatching {
        LocalDateTime.parse(this.replace(' ', 'T'))
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    }.getOrElse { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _isLoaded.value = false // Mark as loading
                val city = selectedCity.value

                // Load data with progress tracking
                // Repository will handle local cache vs network fetch
                coroutineScope {
                    launch {
                        _trashCar.value =
                            trashCarRepository.getTrashCars(
                                city = city,
                                forceUpdate = false
                            ) { progress ->
                                _trashCarProgress.value = progress
                            }
                    }

                    launch {
                        _trashCan.value =
                            trashCanRepository.getTrashCans(
                                city = city,
                                forceUpdate = false
                            ) { progress ->
                                _trashCanProgress.value = progress
                            }
                    }
                }

                _isLoaded.value = true
                _lastRefresh.value = nowString()
            } catch (e: Exception) {
                Log.e("StartupViewModel", "Failed to preload data", e)
                _isLoaded.value = true // Still mark as loaded to show UI
            }
        }
    }

    fun forceRefresh() {
        viewModelScope.launch {
            try {
                _isLoaded.value = false
                _trashCarProgress.value = 0f
                _trashCanProgress.value = 0f
                val city = selectedCity.value

                coroutineScope {
                    launch {
                        _trashCar.value =
                            trashCarRepository.getTrashCars(
                                city = city,
                                forceUpdate = true
                            ) { progress ->
                                _trashCarProgress.value = progress
                            }
                    }

                    launch {
                        _trashCan.value =
                            trashCanRepository.getTrashCans(
                                city = city,
                                forceUpdate = true
                            ) { progress ->
                                _trashCanProgress.value = progress
                            }
                    }
                }

                _isLoaded.value = true
                _lastRefresh.value = nowString()
            } catch (e: Exception) {
                Log.e("StartupViewModel", "Failed to refresh data", e)
                _isLoaded.value = true
            }
        }
    }

    fun setCity(city: City) {
        viewModelScope.launch {
            val previousCity = selectedCity.value
            if (previousCity != city) {
                preferencesDataStore.setSelectedCity(city)
                // Clear cached data and reload for the new city
                forceRefresh()
            }
        }
    }

    private fun nowString(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return fmt.format(Date())
    }
}
