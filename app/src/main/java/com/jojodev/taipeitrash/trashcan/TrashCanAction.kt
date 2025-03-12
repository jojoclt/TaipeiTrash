package com.jojodev.taipeitrash.trashcan

sealed class TrashCanAction {
    data object FetchData : TrashCanAction()
    data object CancelFetchData : TrashCanAction()
}