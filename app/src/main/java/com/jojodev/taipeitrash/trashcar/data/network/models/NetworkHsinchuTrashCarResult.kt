package com.jojodev.taipeitrash.trashcar.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkHsinchuTrashCarResult(
    @SerialName("data")
    val data: HsinchuData
)

@Serializable
data class HsinchuData(
    @SerialName("total")
    val total: Int,
    @SerialName("cleanPoint")
    val data: List<HsinchuPointData>
)

@Serializable
data class HsinchuPointData(
    @SerialName("recycleDay")
    val recycleDay: String,  // "1,5" - Days when recycling is collected
    @SerialName("address")
    val address: String,
    @SerialName("pointName")
    val pointName: String,
    @SerialName("lon")
    val longitude: String,
    @SerialName("rcarNo")
    val rcarNo: String = "",
    @SerialName("holidayMemo")
    val holidayMemo: String = "",
    @SerialName("routeName")
    val routeName: String,
    @SerialName("taskType")
    val taskType: String = "-1",
    @SerialName("routeId")
    val routeId: String,
    @SerialName("pointId")
    val pointId: String,
    @SerialName("carNo")
    val carNo: String = "",
    @SerialName("district")
    val district: String,
    @SerialName("estimate")
    val estimate: String = "-1",
    @SerialName("historyTime")
    val historyTime: String = "",
    @SerialName("time")
    val time: String,  // "12:46~12:47" - Arrival and departure time
    @SerialName("attr")
    val attr: String = "1",
    @SerialName("trashDay")
    val trashDay: String,  // "1,2,4,5,6" - Days when trash is collected
    @SerialName("seq")
    val seq: String,
    @SerialName("lat")
    val latitude: String,
    @SerialName("status")
    val status: String = "1"
)
