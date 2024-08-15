package com.jojodev.taipeitrash.data.network

import com.jojodev.taipeitrash.data.TrashCan
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL =
    "https://data.taipei/api/v1/dataset/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface TrashApiService {
    @GET("267d550f-c6ec-46e0-b8af-fd5a464eb098")
    suspend fun getTrashCan(
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0,
        @Query("scope") scope: String = "resourceAquire"
    ): TrashCan
}

object TrashApi {
    val retrofitService : TrashApiService by lazy {
        retrofit.create(TrashApiService::class.java)
    }
}