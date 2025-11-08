package com.jojodev.taipeitrash.trashcan.data.network.models

import com.jojodev.taipeitrash.trashcar.data.network.models.Importdate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkTaipeiTrashCanResult(
    val result: NetworkTaipeiTrashCanResultBody
)

@Serializable
data class NetworkTaipeiTrashCanResultBody(
    val count: Int,
    val limit: Int,
    val offset: Int,
    @SerialName("results")
    val trashCans: List<TaipeiTrashCans>,
    val sort: String
)

@Serializable
data class TaipeiTrashCans(
    val _id: Int,
    val _importdate: Importdate,
    @SerialName("備註")
    val remark: String,
    @SerialName("地址")
    val address: String,
    @SerialName("經度")
    val longitude: String,
    @SerialName("緯度")
    val latitude: String,
    @SerialName("行政區")
    val district: String
)