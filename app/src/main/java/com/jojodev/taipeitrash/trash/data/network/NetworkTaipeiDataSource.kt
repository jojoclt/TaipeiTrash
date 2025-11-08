package com.jojodev.taipeitrash.trash.data.network

import android.util.Log
import com.jojodev.taipeitrash.core.TaipeiApiService
import com.jojodev.taipeitrash.trashcan.data.network.models.TaipeiTrashCans
import com.jojodev.taipeitrash.trashcar.data.network.models.TaipeiTrashCars
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkTaipeiDataSource @Inject constructor(private val taipeiApiService: TaipeiApiService) {
    private var trashCanCached: List<TaipeiTrashCans>? = null
    private var trashCarCached: List<TaipeiTrashCars>? = null

    suspend fun getTrashCans(onProgress: (Float) -> Unit = {}): List<TaipeiTrashCans> {
        if (trashCanCached != null) {
            onProgress(1f)
            return trashCanCached!!
        }

        return coroutineScope {
            val firstResponse = taipeiApiService.getTrashCan(offset = 0).result
            val totalCount = firstResponse.count
            if (totalCount == 0) return@coroutineScope emptyList()

            val firstBatch = firstResponse.trashCans
            val remainingOffsets = (1000 until totalCount step 1000).toList()
            val totalPages = remainingOffsets.size + 1
            var completedPages = 1

            onProgress(completedPages.toFloat() / totalPages)

            val deferreds = remainingOffsets.map { offset ->
                async {
                    Log.i("TrashCanNetwork", "Fetching page at offset=$offset")
                    val result = taipeiApiService.getTrashCan(offset = offset).result.trashCans
                    completedPages++
                    onProgress(completedPages.toFloat() / totalPages)
                    result
                }
            }

            val remainingBatches = deferreds.awaitAll().flatten()
            val data = firstBatch + remainingBatches
            trashCanCached = data
            data
        }
    }



    suspend fun getTrashCars(onProgress: (Float) -> Unit = {}): List<TaipeiTrashCars> {
        if (trashCarCached != null) {
            onProgress(1f)
            return trashCarCached!!
        }

        return coroutineScope {
            val firstResponse = taipeiApiService.getTrashCar(offset = 0).result
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
                    val result = taipeiApiService.getTrashCar(offset = offset).result.results
                    completedPages++
                    onProgress(completedPages.toFloat() / totalPages)
                    result
                }
            }

            val remainingBatches = deferreds.awaitAll().flatten()
            val data = firstBatch + remainingBatches
            trashCarCached = data
            data
        }
    }

    fun clearCache() {
        trashCarCached = null
        trashCanCached = null

    }
}