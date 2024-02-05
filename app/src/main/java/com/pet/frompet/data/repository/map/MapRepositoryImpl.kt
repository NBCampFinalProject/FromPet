package com.pet.frompet.data.repository.map

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.pet.frompet.data.model.UserLocation
import com.pet.frompet.data.model.UserLocationInfo
import com.pet.frompet.ui.map.MapViewModel
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MapRepositoryImpl(
    private val firebaseDatabase: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) : MapRepository {

    companion object {
        private const val TAG = "MapRepository"
    }
    private val locationRef = firebaseDatabase.getReference("location")
    override suspend fun getCurrentUserId(): String {
        Log.d("LoadLocationData", "1getCurrentUserId : 시작")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d("LoadLocationData", "1getCurrentUserId : 끝 $uid")
        return uid
    }

    override suspend fun getUserLocation(): UserLocation {
        Log.d("LoadLocationData", "2getUserLocation : 시작" )
        val currentUserId = getCurrentUserId()
        val getuser = firebaseDatabase.getReference("location")
            .child(currentUserId)
            .get()
            .await()
            .getValue(UserLocation::class.java)
            ?: UserLocation()
        Log.d("LoadLocationData", "2getUserLocation : 끝 $getuser" )
        return getuser
    }

    override suspend fun updateUserLocation(userLocation: UserLocation) {
        Log.d("LoadLocationData", "3 위치 업데이트 : 시작" )
        val currentUserId = getCurrentUserId()
        firebaseDatabase.getReference("location")
            .child(currentUserId)
            .setValue(userLocation)
            .await()
        Log.d("LoadLocationData", "3 위치 업데이트 : 끝 $currentUserId" )
    }

    override suspend fun getOtherUserLocation(): List<UserLocation> {
        Log.d("LoadLocationData", "4 타사용자_위치조회 : 시작" )
        val currentUserId = getCurrentUserId()
        val snapshot = firebaseDatabase.getReference("location")
            .get()
            .await()

        val otherUserLocations = mutableListOf<UserLocation>()
        for (locationSnapshot in snapshot.children) {
            val location = locationSnapshot.getValue(UserLocation::class.java)
            val userUid = locationSnapshot.key

            if (location != null && userUid != null && userUid != currentUserId) {
                otherUserLocations.add(location)
            }
        }
        Log.d("LoadLocationData", "4 타사용자_위치조회 : 끝 $otherUserLocations" )
        return otherUserLocations
    }

    override suspend fun getloadLocationData(bounds: LatLngBounds): UserLocationInfo {
        Log.d("LoadLocationData", "5 특정 영역 내 사용자들 위치 정보 가져옴 : 시작" )
        return suspendCoroutine { continuation ->
            val userUids = mutableListOf<String>()
            val locationList = mutableListOf<UserLocation>()

            locationRef.get().addOnSuccessListener { snapshot ->
                for (locationSnapshot in snapshot.children) {
                    val location =
                        locationSnapshot.getValue(UserLocation::class.java)
                    if (location != null && bounds.contains(
                            LatLng(
                                location.latitude,
                                location.longitude
                            )
                        )
                    ) {
                        Log.d("LoadLocationData", "유저 아이디: ${locationSnapshot.key}")
                        // 지도 영역에 포함되는 위치만 처리
                        // null 방지 위해 orEmpty()
                        val userUid = locationSnapshot.key.orEmpty()
                        userUids.add(userUid)

                        // 위치 정보를 리스트에 추가
                        locationList.add(UserLocation(location.latitude, location.longitude))
                    }
                }

                val userLocationInfo = UserLocationInfo(userUids, locationList)
                continuation.resume(userLocationInfo)
                Log.d("LoadLocationData", "5 특정 영역 내 사용자들 위치 정보 가져옴 : 끝 $userLocationInfo" )
            }
        }
    }
}