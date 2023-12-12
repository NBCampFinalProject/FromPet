package com.pet.frompet.data.repository.home

import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pet.frompet.data.model.Filter
import com.pet.frompet.data.model.User
import com.pet.frompet.data.model.UserLocation
import kotlinx.coroutines.tasks.await

class HomeFilterRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val database: FirebaseDatabase,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : HomeFilterRepository {
    override suspend fun getCurrentUserLocation(): Location? {
        return fusedLocationProviderClient.lastLocation.await()
    }

    override suspend fun updateFilteredUsers(userId: String, users: List<User>) {
        val filteredUsersRef = database.getReference("filteredUsers").child(userId)
        val userMap = users.associateBy { it.uid }
        filteredUsersRef.setValue(userMap).await()
    }


    override suspend fun filterUsers(filter: Filter, userId: String): List<User> {
        var query: Query = firestore.collection("User")

        if (filter.petType != "전체") {
            query = query.whereEqualTo("petType", filter.petType)
        }
        if (filter.petGender != "all") {
            query = query.whereEqualTo("petGender", filter.petGender)
        }
        if (filter.petNeuter != "상관없음") {
            query = query.whereEqualTo("petNeuter", filter.petNeuter)
        }

        val result = query.get().await()
        val users = result.toObjects(User::class.java).filter { it.uid != userId }

        return users
    }



    override suspend fun updateUserLocationFirestore(userId: String, userLocation: UserLocation) {
        val userRef = firestore.collection("User").document(userId)
        userRef.update("userLocation", userLocation).await()
    }

    override suspend fun userSwiped(userId: String, swipedUserId: String) {
        val swipeRef = database.getReference("swipedUsers").child(userId).child(swipedUserId)
        val filteredUsersRef = database.getReference("filteredUsers").child(userId)
        filteredUsersRef.child(swipedUserId).removeValue()
        swipeRef.setValue(true).await()
    }


    override suspend fun excludeSwipedUsers(userId: String, users: List<User>): List<User> {
        val swipedUserRef = database.getReference("swipedUsers").child(userId)
        val swipedUserSnapShot = swipedUserRef.get().await()
        val swipeUserIds = swipedUserSnapShot.children.map { it.key ?: "" }.toSet()

        return users.filterNot { it.uid in swipeUserIds }
    }

    override suspend fun loadFilteredUsers(userId: String): List<User> {
        val filteredUsersRef = database.getReference("filteredUsers").child(userId)
        val snapshot = filteredUsersRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(User::class.java) }
    }

    override suspend fun applyDistanceFilter(
        users: List<User>,
        filter: Filter,
        currentUserLocation: Location
    ): List<User> {
        return users.filter { user ->
            user.userLocation?.let { userLocation ->
                if (userLocation.latitude in -90.0..90.0 && userLocation.longitude in -180.0..180.0) {
                    val otherUserLocation = Location("").apply {
                        latitude = userLocation.latitude
                        longitude = userLocation.longitude
                    }
                    val distance = currentUserLocation.distanceTo(otherUserLocation) / 1000 // km 단위
                    distance <= filter.distanceFrom
                } else {
                    false
                }
            } ?: false
        }
    }

}