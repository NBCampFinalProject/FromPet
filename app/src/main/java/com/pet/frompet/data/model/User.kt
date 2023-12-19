package com.pet.frompet.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val petAge: Int = 0,
    val petDescription: String = "",
    val petGender: String = "",
    val petIntroduction: String = "",
    val petName: String = "",
    val petProfile: String?= null,
    val petType: String = "",
    val petNeuter: String = "",
    var uid:String = "",
    val userEmail:String = "",
    var userLocation: UserLocation? = null
): Parcelable
