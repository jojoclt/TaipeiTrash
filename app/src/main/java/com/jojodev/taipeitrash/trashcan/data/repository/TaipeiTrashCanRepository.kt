package com.jojodev.taipeitrash.trashcan.data.repository

import android.util.Log
import com.jojodev.taipeitrash.trash.data.network.NetworkTaipeiDataSource
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcan.data.local.dao.TrashCanDao
import com.jojodev.taipeitrash.trashcan.data.mapper.asEntity
import com.jojodev.taipeitrash.trashcan.data.mapper.asExternalModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class TaipeiTrashCanRepository @Inject constructor(
    private val networkTaipeiDataSource: NetworkTaipeiDataSource,
    private val trashCanDao: TrashCanDao
) : BaseTrashCan {
    private val TAG = "TaipeiTrashCanRepository"

    override suspend fun getTrashCans(
        forceUpdate: Boolean,
        onProgress: (Float) -> Unit
    ): List<TrashCan> {
        var results = emptyList<TrashCan>()

        if (forceUpdate) return updateTrashCan(onProgress)

        withContext(Dispatchers.IO) {
            results = trashCanDao.getTrashCan().map { it.asExternalModel() }
        }

        if (results.isEmpty()) results = updateTrashCan(onProgress)

        return results
    }

    override suspend fun updateTrashCan(onProgress: (Float) -> Unit): List<TrashCan> {
        try {
            networkTaipeiDataSource.clearCache()
            val trashCans = networkTaipeiDataSource.getTrashCans(onProgress).filter {
                it.latitude.toDoubleOrNull() != null && it.longitude.toDoubleOrNull() != null
            }
            withContext(Dispatchers.IO) {
                trashCanDao.update(trashCans.map { it.asEntity() })
            }
            return trashCans.map { it.asEntity().asExternalModel() }
        } catch (e: IOException) {
            Log.e(TAG, "Network Error: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
        }
        return emptyList()
    }


}