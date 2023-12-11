package com.pet.frompet.data.repository.map

import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.overlay.Marker
import com.pet.frompet.data.model.UserLocation

interface MapRepository {

//      suspend fun getCommunityData(petType:String): List<CommunityData>
      suspend fun getUserLocation() : List<UserLocation>

      suspend fun setUserProfileImage(userUid: String, marker: Marker)

      suspend fun markerClick(userUid: String)

      suspend fun getOtherUserLocations()

      suspend fun getloadLocationData(bounds: LatLngBounds)

      suspend fun currentLocationUpload(latitude : Double, longitude : Double)


}
