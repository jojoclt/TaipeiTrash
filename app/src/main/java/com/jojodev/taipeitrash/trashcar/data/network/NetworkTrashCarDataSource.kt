package com.jojodev.taipeitrash.trashcar.data.network

import android.util.Log
import com.jojodev.taipeitrash.core.TrashApiService
import com.jojodev.taipeitrash.trashcar.data.network.models.NetworkTrashCar
import javax.inject.Inject

class NetworkTrashCarDataSource @Inject constructor(private val trashApiService: TrashApiService) {
    suspend fun getTrashCars(): List<NetworkTrashCar> = fetchAllTrashCars()

    private suspend fun fetchAllTrashCars(
        offset: Int = 0,
        limit: Int = 1000,
    ): List<NetworkTrashCar> {
        Log.i("TrashCarNetwork", "FetchAllTrashCars: offset=$offset, limit=$limit")

        val listResult = trashApiService.getTrashCar(offset = offset, limit = limit).result
        if (listResult.count == 0 && offset == 0) {
            throw Exception("No data")
        }
        var result = listResult.results
        if (offset + limit < listResult.count)
            result = result + fetchAllTrashCars(offset + limit, limit)
        return result
    }
}