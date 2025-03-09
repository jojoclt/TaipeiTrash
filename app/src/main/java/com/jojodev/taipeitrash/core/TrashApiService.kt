package com.jojodev.taipeitrash.core

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