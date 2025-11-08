package com.jojodev.taipeitrash.trash.data.network

import com.jojodev.taipeitrash.core.HsinchuApiService
import com.jojodev.taipeitrash.trashcar.data.network.models.HsinchuPointData
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkHsinchuDataSource @Inject constructor(private val hsinchuApiService: HsinchuApiService) {
    private var trashCarCached: List<HsinchuPointData>? = null

    suspend fun getTrashCars(onProgress: (Float) -> Unit = {}): List<HsinchuPointData> {
        if (trashCarCached != null) {
            onProgress(1f)
            return trashCarCached!!
        }

        return coroutineScope {
            val response = hsinchuApiService.getTrashCar().data
            val totalCount = response.total
            if (totalCount == 0) return@coroutineScope emptyList()

            val data = response.data
            trashCarCached = data
            onProgress(1f)
            data
        }
    }

    fun clearCache() {
        trashCarCached = null
    }
}