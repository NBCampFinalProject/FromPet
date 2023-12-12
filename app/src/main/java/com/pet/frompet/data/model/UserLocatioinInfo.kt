package com.pet.frompet.data.model

data class UserLocationInfo(
    val userUid: List<String> = emptyList(),
    val userLocations: List<UserLocation> = emptyList()
)
