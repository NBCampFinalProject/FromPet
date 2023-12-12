package com.pet.frompet.data.repository.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
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

    //    private val database = Firebase.database
    private val locationRef = firebaseDatabase.getReference("location")


    override suspend fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "" // 현재 uid 갖고 옴
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

//    override suspend fun currentLocationUpload(latitude: Double, longitude: Double) {
//        val userLocation = UserLocation(latitude, longitude)
//        locationRef.child(currentUserId).setValue(userLocation)
//    }

    override suspend fun getOtherUserLocations(): List<UserLocation> {
        val currentUserId = getCurrentUserId()
        val snapshot = firebaseDatabase.getReference("location")
            .get()
            .await()

        val otherUserLocations = mutableListOf<UserLocation>()
        for (locationSnapshot in snapshot.children) {
            val location = locationSnapshot.getValue(UserLocation::class.java)
            val userUid = locationSnapshot.key

            if (location != null && userUid != currentUserId) {
                otherUserLocations.add(location)
            }
        }
        return otherUserLocations
    }


//        locationRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val userLocationList = mutableListOf<UserLocationInfo>()
//                for (snapshots in snapshot.children) {
//                    val location = snapshots.getValue(UserLocation::class.java)
//                    val userUid = snapshots.key
//
//                    if (location != null && userUid != null && userUid != currentUserId) {
//                        userLocationList.add(
//                            UserLocationInfo(
//                                listOf(userUid),
//                                listOf(location)
//                            )
//                        )
//                    }
//                }
//                _otherUserLocation.value = userLocationList
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })


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