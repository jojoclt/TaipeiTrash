package com.jojodev.taipeitrash.trashcan.data.repository

import com.jojodev.taipeitrash.trashcan.data.TrashCan

interface BaseTrashCan {
    suspend fun getTrashCans(
        forceUpdate: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): List<TrashCan>

    suspend fun updateTrashCan(onProgress: (Float) -> Unit = {}): List<TrashCan>
}