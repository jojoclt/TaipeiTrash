package com.jojodev.taipeitrash.trashcan.data

import android.util.Log
import com.jojodev.taipeitrash.trashcan.data.local.dao.TrashCanDao
import com.jojodev.taipeitrash.trashcan.data.network.NetworkTrashCanDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class TrashCanRepository @Inject constructor(
    private val networkDataSource: NetworkTrashCanDataSource,
    private val localDataSource: TrashCanDao
) {
    suspend fun getTrashCans(): List<TrashCan> {
        var results = emptyList<TrashCan>()
//        check network is available
//        catch in vm
        withContext(Dispatchers.IO) {
            results = localDataSource.getTrashCan().map { it.asExternalModel() }
        }
        if (results.isEmpty()) {
            results = updateTrashCan()
        }
//        always get data from dao
        return results
    }

    suspend fun updateTrashCan(): List<TrashCan> {
        try {
            val trashCans = networkDataSource.getTrashCans()
            withContext(Dispatchers.IO) {
                localDataSource.updateTrash(trashCans.map { it.asEntity() })
            }
            return trashCans.map { it.asEntity().asExternalModel() }
        } catch (e: IOException) {
            Log.e("Network Error", e.message.toString())
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
        }
        return emptyList()
    }
}