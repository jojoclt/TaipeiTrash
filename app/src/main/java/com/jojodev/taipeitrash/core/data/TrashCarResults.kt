package com.jojodev.taipeitrash.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrashCarResults(
    val result: TrashCarResult
)

@Serializable
data class TrashCarResult(
    val count: Int,
    val limit: Int,
    val offset: Int,
    @SerialName("results")
    val trashCars: List<TrashCar>,
    val sort: String
)

@Serializable
data class TrashCar(
    val _id: Int,
    val _importdate: Importdate,
    @SerialName("分隊")
    val unit: String,
    @SerialName("地點")
    val location: String,
    val 局編: String,
    @SerialName("抵達時間")
    val arriveTime: String,
    @SerialName("經度")
    val longitude: String,
    @SerialName("緯度")
    val latitude: String,
    @SerialName("行政區")
    val district: String,
    @SerialName("路線")
    val line: String,
    @SerialName("車次")
    val carNo: String,
    @SerialName("車號")
    val carPlate: String,
    val 里別: String,
    @SerialName("離開時間")
    val departTime: String
)

//data class Importdate(
//    val date: String,
//    val timezone: String,
//    val timezone_type: Int
//)