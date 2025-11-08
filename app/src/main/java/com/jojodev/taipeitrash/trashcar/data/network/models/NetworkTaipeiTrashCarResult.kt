package com.jojodev.taipeitrash.trashcar.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkTaipeiTrashCarResult(
    @SerialName("result")
    val result: NetworkTaipeiTrashCarResultBody
)

@Serializable
data class NetworkTaipeiTrashCarResultBody(
    @SerialName("limit")
    val limit: Int,
    @SerialName("offset")
    val offset: Int,
    @SerialName("count")
    val count: Int,
    @SerialName("sort")
    val sort: String,
    @SerialName("results")
    val results: List<TaipeiTrashCars>
)

@Serializable
data class TaipeiTrashCars(
    @SerialName("_id")
    val id: Int,
    @SerialName("_importdate")
    val importdate: Importdate,
    @SerialName("行政區")
    val district: String,
    @SerialName("里別")
    val 里別: String,
    @SerialName("分隊")
    val 分隊: String,
    @SerialName("局編")
    val 局編: String,
    @SerialName("車號")
    val 車號: String,
    @SerialName("路線")
    val 路線: String,
    @SerialName("車次")
    val 車次: String,
    @SerialName("抵達時間")
    val timeArrive: String,
    @SerialName("離開時間")
    val timeLeave: String,
    @SerialName("地點")
    val address: String,
    @SerialName("經度")
    val longitude: String,
    @SerialName("緯度")
    val latitude: String,
)

@Serializable
data class Importdate(
    @SerialName("date")
    val date: String,
    @SerialName("timezone_type")
    val timezoneType: Int,
    @SerialName("timezone")
    val timezone: String
)