package com.jojodev.taipeitrash.trashcar.data.repository

import com.jojodev.taipeitrash.core.model.City
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import javax.inject.Inject

class TrashCarRepository @Inject constructor(
    private val taipeiTrashCarRepository: TaipeiTrashCarRepository,
    private val hsinchuTrashCarRepository: HsinchuTrashCarRepository,
) {
    suspend fun getTrashCars(
        city: City = City.TAIPEI,
        forceUpdate: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): List<TrashCar> = when (city) {
        City.TAIPEI -> taipeiTrashCarRepository.getTrashCars(forceUpdate, onProgress)
        City.HSINCHU -> hsinchuTrashCarRepository.getTrashCars(forceUpdate, onProgress)
    }
}