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
    private val locationRef = firebaseDatabase.getReference("location")
    override suspend fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override suspend fun getUserLocation(): UserLocation {
        val currentUserId = getCurrentUserId()
        return firebaseDatabase.getReference("location")
            .child(currentUserId)
            .get()
            .await()
            .getValue(UserLocation::class.java)
            ?: UserLocation()
    }

    override suspend fun updateUserLocation(userLocation: UserLocation) {
        val currentUserId = getCurrentUserId()
        firebaseDatabase.getReference("location")
            .child(currentUserId)
            .setValue(userLocation)
            .await()
    }

    override suspend fun getOtherUserLocation(): List<UserLocation> {
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
        return otherUserLocations
    }

    override suspend fun getloadLocationData(bounds: LatLngBounds): UserLocationInfo {
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
            }
        }
    }
}
