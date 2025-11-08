package com.jojodev.taipeitrash.trashcan.data.repository

import com.jojodev.taipeitrash.core.model.City
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import javax.inject.Inject

class TrashCanRepository @Inject constructor(
    private val taipeiTrashCanRepository: TaipeiTrashCanRepository
) {
    suspend fun getTrashCans(
        city: City = City.TAIPEI,
        forceUpdate: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): List<TrashCan> = when (city) {
        City.TAIPEI -> taipeiTrashCanRepository.getTrashCans(forceUpdate, onProgress)
        else -> {
            onProgress(1f)
            emptyList()
        }
    }
}