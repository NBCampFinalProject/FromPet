package com.pet.frompet.data.repository.map

import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.overlay.Marker
import com.pet.frompet.data.model.UserLocation
import com.pet.frompet.data.model.UserLocationInfo
import com.pet.frompet.ui.map.MapViewModel

interface MapRepository {

      suspend fun getCurrentUserId() : String

      suspend fun getUserLocation() : UserLocation

      suspend fun updateUserLocation(userLocation: UserLocation)

      suspend fun getOtherUserLocations() : List<UserLocation>

      suspend fun getloadLocationData(bounds: LatLngBounds) : UserLocationInfo
}

//      suspend fun currentLocationUpload(latitude : Double, longitude : Double)
// currentLocationUpload -> updateUserLocatuon 함수명 변경