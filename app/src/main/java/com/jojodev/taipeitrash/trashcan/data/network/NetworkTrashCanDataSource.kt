package com.jojodev.taipeitrash.trashcan.data.network

import android.util.Log
import com.jojodev.taipeitrash.core.TrashApiService
import com.jojodev.taipeitrash.trashcan.data.network.models.NetworkTrashCan
import javax.inject.Inject

class NetworkTrashCanDataSource @Inject constructor(private val trashApiService: TrashApiService) {
    suspend fun getTrashCans(): List<NetworkTrashCan> = fetchAllTrashCans()

    private suspend fun fetchAllTrashCans(
        offset: Int = 0,
        limit: Int = 1000,
    ): List<NetworkTrashCan> {
        Log.i("TrashCanViewModel", "FetchAllTrashCans: offset=$offset, limit=$limit")

        val listResult = trashApiService.getTrashCan(offset = offset, limit = limit).result
        if (listResult.count == 0 && offset == 0) {
            throw Exception("No data")
        }
        var result = listResult.trashCans
        if (offset + limit < listResult.count)
            result = result + fetchAllTrashCans(offset + limit, limit)
        return result
    }
}