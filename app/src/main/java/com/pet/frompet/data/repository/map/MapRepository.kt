package com.pet.frompet.data.repository.map

import com.naver.maps.geometry.LatLngBounds
import com.pet.frompet.data.model.UserLocation
import com.pet.frompet.data.model.UserLocationInfo


interface MapRepository {

      suspend fun getCurrentUserId():String
      suspend fun getUserLocation(): UserLocation
      suspend fun updateUserLocation(userLocation: UserLocation)
      suspend fun getOtherUserLocation():List<UserLocation>
      suspend fun getloadLocationData(bounds: LatLngBounds): UserLocationInfo

}
