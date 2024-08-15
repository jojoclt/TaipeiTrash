package com.jojodev.taipeitrash.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Mars(
    val id: String = "",
    @Json(name = "img_src")
    val url: String = ""
)