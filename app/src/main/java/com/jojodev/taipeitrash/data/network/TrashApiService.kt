package com.jojodev.taipeitrash.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.jojodev.taipeitrash.data.TrashCanResults
import com.jojodev.taipeitrash.data.TrashCarResults
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL =
    "https://data.taipei/api/v1/dataset/"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(
        Json.asConverterFactory(
            "application/json; charset=UTF8".toMediaType()))
    .build()

interface TrashApiService {
    @GET("267d550f-c6ec-46e0-b8af-fd5a464eb098")
    suspend fun getTrashCan(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1000,
        @Query("scope") scope: String = "resourceAquire"
    ): TrashCanResults

    @GET("a6e90031-7ec4-4089-afb5-361a4efe7202")
    suspend fun getTrashCar(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1000,
        @Query("scope") scope: String = "resourceAquire"
    ): TrashCarResults
}

object TrashApi {
    val retrofitService: TrashApiService by lazy {
        retrofit.create(TrashApiService::class.java)
    }
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