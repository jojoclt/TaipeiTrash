package com.jojodev.taipeitrash.core

import com.jojodev.taipeitrash.trashcar.data.network.models.NetworkHsinchuTrashCarResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// Forward declaration - actual models are in hsinchu package
// This is defined here to avoid circular dependencies
interface HsinchuApiService {
    @FormUrlEncoded
    @POST("getPointData")
    suspend fun getTrashCar(
        @Field("address") address: String = ""
    ): NetworkHsinchuTrashCarResult
}

