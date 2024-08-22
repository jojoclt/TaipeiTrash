package com.jojodev.taipeitrash.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
data class TrashResults(
    val result: Result
)

@Serializable
@JsonClass(generateAdapter = true)
data class Result(
    val count: Int,
    val limit: Int,
    val offset: Int,
    @Json(name = "results")
    val trashCans: List<TrashCan>,
    val sort: String
)

@Serializable
@JsonClass(generateAdapter = true)
data class TrashCan(
    val _id: Int,
    val _importdate: Importdate,
    @Json(name = "備註")
    val remark: String,
    @Json(name = "地址")
    val address: String,
    @Json(name = "經度")
    val longitude: String,
    @Json(name = "緯度")
    val latitude: String,
    @Json(name = "行政區")
    val district: String
)
//2024-08-22 13:15:11.877  E  getAllTrashCans: Expected a double but was ?121.595369 at path $.result.results[113].經度

@Serializable
@JsonClass(generateAdapter = true)
data class Importdate(
    val date: String,
    val timezone: String,
    val timezone_type: Int
)