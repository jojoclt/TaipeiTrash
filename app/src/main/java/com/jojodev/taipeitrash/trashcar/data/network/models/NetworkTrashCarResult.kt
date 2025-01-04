package com.jojodev.taipeitrash.trashcar.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkTrashCarResult(
    val result: TrashCarResult
)

@Serializable
data class TrashCarResult(
    val count: Int,
    val limit: Int,
    val offset: Int,
    @SerialName("results")
    val trashCans: List<NetworkTrashCan>,
    val sort: String
)

@Serializable
data class NetworkTrashCan(
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
//2024-08-22 13:15:11.877  E  getAllTrashCans: Expected a double but was ?121.595369 at path $.result.results[113].經度

@Serializable
data class Importdate(
    val date: String,
    val timezone: String,
    val timezone_type: Int
)