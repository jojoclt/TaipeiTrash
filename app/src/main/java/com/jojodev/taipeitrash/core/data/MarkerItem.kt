package com.jojodev.taipeitrash.core.data

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class MarkerItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val itemZIndex: Float = 0f
) : ClusterItem {
    override fun getPosition(): LatLng =
        itemPosition

    override fun getTitle(): String =
        itemTitle

    override fun getSnippet(): String =
        itemSnippet

    override fun getZIndex(): Float =
        itemZIndex
}