package com.jojodev.taipeitrash.trashcar.data.repository

import com.jojodev.taipeitrash.trashcar.data.TrashCar

interface BaseTrashCar {
    suspend fun getTrashCars(
        forceUpdate: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): List<TrashCar>

    suspend fun updateTrashCar(onProgress: (Float) -> Unit = {}): List<TrashCar>
}