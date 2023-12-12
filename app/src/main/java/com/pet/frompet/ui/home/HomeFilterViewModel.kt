package com.pet.frompet.ui.home

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pet.frompet.data.model.Filter
import com.pet.frompet.data.model.User
import com.pet.frompet.data.model.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.pet.frompet.util.showToast
import com.pet.frompet.data.repository.home.HomeFilterRepository
import kotlinx.coroutines.launch

class HomeFilterViewModel(
    private val app: Application,
    private val homeFilterRepository: HomeFilterRepository
) : ViewModel() {

    private val _filteredUsers = MutableLiveData<List<User>?>()
    val filteredUsers: MutableLiveData<List<User>?> = _filteredUsers

    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
     val currentFilter:Filter? = null

    init {
        loadCurrentUserLocationAndUpdate()
    }

     fun loadCurrentUserLocationAndUpdate() {
        viewModelScope.launch {
            try {
                val location = homeFilterRepository.getCurrentUserLocation()
                location?.let {
                    val userLocation = UserLocation(it.latitude, it.longitude)
                    homeFilterRepository.updateUserLocationFirestore(currentUser, userLocation)
                    currentFilter?.let { filter ->
                        filterUsers(filter)
                    }
                }
            } catch (e: SecurityException) {
                app.showToast("위치권한이 없습니다.", Toast.LENGTH_SHORT)
            }
        }
    }

    fun filterUsers(filter: Filter) {
        viewModelScope.launch {

            val users = homeFilterRepository.filterUsers(filter, currentUser)

            val filteredUsersWithoutSwipes = homeFilterRepository.excludeSwipedUsers(currentUser, users)

            val location = homeFilterRepository.getCurrentUserLocation() ?: return@launch

            val usersWithinDistance = homeFilterRepository.applyDistanceFilter(filteredUsersWithoutSwipes, filter, location)

            _filteredUsers.value = usersWithinDistance
            homeFilterRepository.updateFilteredUsers(currentUser, usersWithinDistance)

        }
    }
    fun loadFilteredUsers() {
        viewModelScope.launch {
            val users = homeFilterRepository.loadFilteredUsers(currentUser)
            _filteredUsers.value = users
        }
    }

    fun userSwiped(swipedUserId: String) {
        viewModelScope.launch {
            homeFilterRepository.userSwiped(currentUser, swipedUserId)
            val updateUsers = _filteredUsers.value?.filterNot { it.uid == swipedUserId }
            _filteredUsers.postValue(updateUsers)
        }
    }

}

class HomeFilterViewModelFactory(
    private val application: Application,
    private val homeFilterRepository: HomeFilterRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFilterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeFilterViewModel(application, homeFilterRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
