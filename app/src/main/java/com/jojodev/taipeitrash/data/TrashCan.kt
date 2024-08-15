package com.jojodev.taipeitrash.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrashCan(
    val result: Result
)
@JsonClass(generateAdapter = true)
data class Result(
    val count: Int,
    val limit: Int,
    val offset: Int,
    val results: List<ResultX>,
    val sort: String
)
@JsonClass(generateAdapter = true)
data class ResultX(
    val _id: Int,
    val _importdate: Importdate,
    @Json(name = "備註")
    val remark: String,
    @Json(name = "地址")
    val address: String,
    @Json(name = "經度")
    val latitude: String,
    @Json(name = "緯度")
    val longtitude: String,
    @Json(name = "行政區")
    val district: String
)
@JsonClass(generateAdapter = true)
data class Importdate(
    val date: String,
    val timezone: String,
    val timezone_type: Int
)