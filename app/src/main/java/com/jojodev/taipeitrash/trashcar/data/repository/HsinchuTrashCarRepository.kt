package com.jojodev.taipeitrash.trashcar.data.repository

import android.util.Log
import com.jojodev.taipeitrash.trash.data.network.NetworkHsinchuDataSource
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.trashcar.data.local.dao.TrashCarDao
import com.jojodev.taipeitrash.trashcar.data.mapper.asEntity
import com.jojodev.taipeitrash.trashcar.data.mapper.asExternalModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class HsinchuTrashCarRepository @Inject constructor(
    private val networkHsinchuDataSource: NetworkHsinchuDataSource,
    private val trashCarDao: TrashCarDao
) : BaseTrashCar {
    private val TAG = "HsinchuTrashCarRepository"

    override suspend fun getTrashCars(
        forceUpdate: Boolean,
        onProgress: (Float) -> Unit
    ): List<TrashCar> {
        var results = emptyList<TrashCar>()

        if (forceUpdate) return updateTrashCar(onProgress)

        withContext(Dispatchers.IO) {
            results = trashCarDao.getTrashCar().map { it.asExternalModel() }
        }

        if (results.isEmpty()) results = updateTrashCar(onProgress)

        return results
    }

    override suspend fun updateTrashCar(onProgress: (Float) -> Unit): List<TrashCar> {
        try {
            networkHsinchuDataSource.clearCache()
            val trashCars = networkHsinchuDataSource.getTrashCars(onProgress).filter {
                it.latitude.toDoubleOrNull() != null && it.longitude.toDoubleOrNull() != null
            }
            withContext(Dispatchers.IO) {
                trashCarDao.update(trashCars.map { it.asEntity() })
            }
            return trashCars.map { it.asEntity().asExternalModel() }
        } catch (e: IOException) {
            Log.e(TAG, "Network Error: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
        }
        return emptyList()
    }
}