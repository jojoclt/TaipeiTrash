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
    val latitude: Float,
    @Json(name = "緯度")
    val longitude: Float,
    @Json(name = "行政區")
    val district: String
)

@Serializable
@JsonClass(generateAdapter = true)
data class Importdate(
    val date: String,
    val timezone: String,
    val timezone_type: Int
)