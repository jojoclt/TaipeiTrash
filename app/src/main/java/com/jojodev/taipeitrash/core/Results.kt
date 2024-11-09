package com.jojodev.taipeitrash.core

sealed class Results<out T> {
    data object Loading : Results<Nothing>()
    data class Success<T>(val data: T) : Results<T>()
    data class Error(val error: Throwable?) : Results<Nothing>()
}