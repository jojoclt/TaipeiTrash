package com.jojodev.taipeitrash.trashcar.data

import android.util.Log
import com.jojodev.taipeitrash.trashcar.data.local.dao.TrashCarDao
import com.jojodev.taipeitrash.trashcar.data.network.NetworkTrashCarDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class TrashCarRepository @Inject constructor(
    private val networkDataSource: NetworkTrashCarDataSource,
    private val localDataSource: TrashCarDao
) {
    suspend fun getTrashCars(forceUpdate: Boolean = false): List<TrashCar> {
        var results = emptyList<TrashCar>()
//        check network is available
//        catch in vm
        if (forceUpdate) {
            return updateTrashCar()
        }
        withContext(Dispatchers.IO) {
            results = localDataSource.getTrashCar().map { it.asExternalModel() }
        }
        if (results.isEmpty()) {
            results = updateTrashCar()
        }
//        always get data from dao
        return results
    }

    private suspend fun updateTrashCar(): List<TrashCar> {
        try {
            val trashCars = networkDataSource.getTrashCars().filter {
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
