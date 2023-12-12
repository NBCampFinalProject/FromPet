package com.pet.frompet.data.repository.home

import android.location.Location
import com.pet.frompet.data.model.Filter
import com.pet.frompet.data.model.User
import com.pet.frompet.data.model.UserLocation

interface HomeFilterRepository {
    suspend fun getCurrentUserLocation(): Location?
    suspend fun updateUserLocationFirestore(userId: String, userLocation: UserLocation)
    suspend fun filterUsers(filter: Filter, userId: String): List<User>
    suspend fun loadFilteredUsers(userId: String): List<User>
    suspend fun excludeSwipedUsers(userId: String, users: List<User>): List<User>
    suspend fun userSwiped(userId: String, swipedUserId: String)
    suspend fun updateFilteredUsers(userId: String, users: List<User>)
    suspend fun applyDistanceFilter(users: List<User>, filter: Filter, currentUserLocation: Location): List<User>
}
