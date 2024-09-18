package com.jojodev.taipeitrash.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
data class TrashCarResults(
    val result: TrashCarResult
)

@Serializable
@JsonClass(generateAdapter = true)
data class TrashCarResult(
    val count: Int,
    val limit: Int,
    val offset: Int,
    val results: List<TrashCar>,
    val sort: String
)

@Serializable
@JsonClass(generateAdapter = true)
data class TrashCar(
    val _id: Int,
    val _importdate: Importdate,
    @Json(name = "分隊")
    val unit: String,
    @Json(name = "地點")
    val location: String,
    val 局編: String,
    @Json(name = "抵達時間")
    val arriveTime: String,
    @Json(name = "經度")
    val longitude: String,
    @Json(name = "緯度")
    val latitude: String,
    @Json(name = "行政區")
    val district: String,
    @Json(name = "路線")
    val line: String,
    @Json(name = "車次")
    val carNo: String,
    @Json(name = "車號")
    val carPlate: String,
    val 里別: String,
    @Json(name = "離開時間")
    val departTime: String
)

//data class Importdate(
//    val date: String,
//    val timezone: String,
//    val timezone_type: Int
//)