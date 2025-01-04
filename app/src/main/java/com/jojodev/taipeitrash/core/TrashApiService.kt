package com.jojodev.taipeitrash.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.jojodev.taipeitrash.trashcan.data.network.models.NetworkTrashCanResult
import com.jojodev.taipeitrash.trashcar.data.network.models.NetworkTrashCarResult
import retrofit2.http.GET
import retrofit2.http.Query

interface TrashApiService {
    @GET("267d550f-c6ec-46e0-b8af-fd5a464eb098")
    suspend fun getTrashCan(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1000,
        @Query("scope") scope: String = "resourceAquire"
    ): NetworkTrashCanResult

    @GET("a6e90031-7ec4-4089-afb5-361a4efe7202")
    suspend fun getTrashCar(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1000,
        @Query("scope") scope: String = "resourceAquire"
    ): NetworkTrashCarResult
}

private fun isInternetAvailable(context: Context): Boolean {
    var result = false
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.run {
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
    }
    return result
}