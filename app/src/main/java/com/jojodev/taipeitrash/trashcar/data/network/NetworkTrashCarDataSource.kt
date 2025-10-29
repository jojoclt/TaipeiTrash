package com.jojodev.taipeitrash.trashcar.data.network

import android.util.Log
import com.jojodev.taipeitrash.core.TrashApiService
import com.jojodev.taipeitrash.trashcar.data.network.models.NetworkTrashCar
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkTrashCarDataSource @Inject constructor(private val trashApiService: TrashApiService) {

    private var cachedData: List<NetworkTrashCar>? = null

    suspend fun getTrashCars(onProgress: (Float) -> Unit = {}): List<NetworkTrashCar> {
        if (cachedData != null) {
            onProgress(1f)
            return cachedData!!
        }

        return coroutineScope {
            val firstResponse = trashApiService.getTrashCar(offset = 0).result
            val totalCount = firstResponse.count
            if (totalCount == 0) return@coroutineScope emptyList()

            val firstBatch = firstResponse.results
            val remainingOffsets = (1000 until totalCount step 1000).toList()
            val totalPages = remainingOffsets.size + 1
            var completedPages = 1

            onProgress(completedPages.toFloat() / totalPages)

            val deferreds = remainingOffsets.map { offset ->
                async {
                    Log.i("TrashCarNetwork", "Fetching page at offset=$offset")
                    val result = trashApiService.getTrashCar(offset = offset).result.results
                    completedPages++
                    onProgress(completedPages.toFloat() / totalPages)
                    result
                }
            }

            val remainingBatches = deferreds.awaitAll().flatten()
            val data = firstBatch + remainingBatches
            cachedData = data
            data
        }
    }
}
