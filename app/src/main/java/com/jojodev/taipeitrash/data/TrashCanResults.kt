package com.jojodev.taipeitrash.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrashCanResults(
    val result: TrashCanResult
)

@Serializable
data class TrashCanResult(
    val count: Int,
    val limit: Int,
    val offset: Int,
    @SerialName("results")
    val trashCans: List<TrashCan>,
    val sort: String
)

@Serializable
data class TrashCan(
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