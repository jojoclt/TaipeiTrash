package com.jojodev.taipeitrash.trashcar.data

import android.util.Log
import com.jojodev.taipeitrash.trashcar.data.local.dao.TrashCarDao
import com.jojodev.taipeitrash.trashcar.data.mapper.asEntity
import com.jojodev.taipeitrash.trashcar.data.mapper.asExternalModel
import com.jojodev.taipeitrash.trashcar.data.network.NetworkTrashCarDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class TrashCarRepository @Inject constructor(
    private val networkDataSource: NetworkTrashCarDataSource,
    private val localDataSource: TrashCarDao
) {
    suspend fun getTrashCars(
        forceUpdate: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): List<TrashCar> {
        var results = emptyList<TrashCar>()
//        check network is available
//        catch in vm
        if (forceUpdate) {
            return updateTrashCar(onProgress)
        }
        withContext(Dispatchers.IO) {
            results = localDataSource.getTrashCar().map { it.asExternalModel() }
        }
        if (results.isEmpty()) {
            Log.d("TrashCarRepository", "Local data empty, updating from network")
            results = updateTrashCar(onProgress)
        }
//        always get data from dao
        return results
    }

    private suspend fun updateTrashCar(onProgress: (Float) -> Unit = {}): List<TrashCar> {
        try {
            val trashCars = networkDataSource.getTrashCars(onProgress).filter {
                it.latitude.toDoubleOrNull() != null && it.longitude.toDoubleOrNull() != null
            }
            withContext(Dispatchers.IO) {
                localDataSource.updateTrashCar(trashCars.map { it.asEntity() })
            }
            return trashCars.map { it.asEntity().asExternalModel() }
        } catch (e: IOException) {
            Log.e("Network Error", e.message.toString())
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
        }
        return emptyList()
    }
}
