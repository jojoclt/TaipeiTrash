package com.jojodev.taipeitrash.trashcan

import com.jojodev.taipeitrash.trashcan.data.TrashCan

sealed class TrashCanAction {
    data object FetchData : TrashCanAction()
    data object CancelFetchData : TrashCanAction()
    data class ShowDetail(val trashCan: TrashCan) : TrashCanAction()
}