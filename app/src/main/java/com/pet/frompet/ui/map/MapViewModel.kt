package com.pet.frompet.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pet.frompet.data.model.UserLocation
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.pet.frompet.data.repository.map.MapRepository
import com.pet.frompet.data.repository.map.MapRepositoryImpl

class MapViewModel(private val mapRepository: MapRepository) : ViewModel() {

    private val _navigateToDetails = SingleLiveEvent<Void>()
    val navigateToDetails : LiveData<Void> get() = _navigateToDetails

    private val database = Firebase.database

    private val _userLocationInfo = MutableLiveData<UserLocationInfo>()
    val userLocation : LiveData<UserLocationInfo> get() = _userLocationInfo

    private val _otherUserLocation = MutableLiveData<List<UserLocationInfo>>()
    val otherUserLocation : MutableLiveData<List<UserLocationInfo>> get() = _otherUserLocation

    // 상태 클래스 (상태패턴)
    data class UserLocationInfo(
        val userUid : List<String> = emptyList(),
        val userLocations : List<UserLocation> = emptyList()
    )

    //싱글라이브이벤트
    fun moveTheCamera() {
        _navigateToDetails.call()
    }



    suspend fun getloadLocationData(bounds: LatLngBounds) {

        mapRepository.getloadLocationData()

//        val userUids = mutableListOf<String>()
//        val locationList = mutableListOf<UserLocation>()
//        var userLocationInfo = UserLocationInfo()
//
//        locationRef.get().addOnSuccessListener { snapshot ->
//
//            for (locationSnapshot in snapshot.children) {
//                val location =
//                    locationSnapshot.getValue(com.pet.frompet.data.model.UserLocation::class.java)
//                if (location != null && bounds.contains(
//                        LatLng(
//                            location.latitude,
//                            location.longitude
//                        )
//                    )
//                ) {
//                    Log.d("LoadLocationData", "유저 아이디: ${locationSnapshot.key}")
//                    // 지도 영역에 포함되는 위치만 처리
//                    // null 방지 위해 orEmpty()
//                    val userUid = locationSnapshot.key.orEmpty()
//                    userUids.add(userUid)
//
//                    // 위치 정보를 리스트에 추가
//                    locationList.add(UserLocation(location.latitude, location.longitude))
//                }
//            }
//            userLocationInfo = userLocationInfo.copy(userUids, locationList)
//            _userLocationInfo.value = userLocationInfo
//        }
    }

    // 현재 사용자 위치 FB 업로드
    suspend fun currentLocationUpload(latitude : Double, longitude : Double) {

        mapRepository.currentLocationUpload(latitude : Double, longitude : Double)
//        val userLocation = UserLocation(latitude, longitude)
//        locationRef.child(currentUserId).setValue(userLocation)

    }

    // 사용자 위치 마커 표시
    suspend fun getOtherUserLocations() {

        mapRepository.getOtherUserLocations()
//        locationRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val userLocationList = mutableListOf<UserLocationInfo>()
//                for (snapshots in snapshot.children) {
//                    val location = snapshots.getValue(UserLocation::class.java)
//                    val userUid = snapshots.key
//
//                    if (location != null && userUid != null && userUid != currentUserId) {
//                        userLocationList.add(UserLocationInfo(listOf(userUid), listOf(location)))
//                    }
//                }
//                _otherUserLocation.value = userLocationList
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
    }
}