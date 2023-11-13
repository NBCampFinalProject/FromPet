package com.pet.frompet.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng

@Parcelize
data class UserLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
): Parcelable
//    , TedClusterItem {
//    override fun getTedLatLng(): TedLatLng {
//        return TedLatLng(latitude, longitude)
//    }
//}
