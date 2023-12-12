package com.pet.frompet.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.data.model.UserLocation
import com.naver.maps.geometry.LatLngBounds
import com.pet.frompet.data.model.UserLocationInfo
import com.pet.frompet.data.repository.map.MapRepository
import com.pet.frompet.data.repository.map.MapRepositoryImpl
import kotlinx.coroutines.launch

class MapViewModel(private val mapRepository: MapRepository) : ViewModel() {

    private val _userLocationInfo = MutableLiveData<UserLocationInfo>()
    val userLocation: LiveData<UserLocationInfo> get() = _userLocationInfo

    private val _otherUserLocation = MutableLiveData<List<UserLocationInfo>>()
    val otherUserLocation: MutableLiveData<List<UserLocationInfo>> get() = _otherUserLocation


    fun getloadLocationData(bounds: LatLngBounds) {
        viewModelScope.launch {
            val userLocationInfo = mapRepository.getloadLocationData(bounds)
            _userLocationInfo.value = userLocationInfo
        }
    }

    // 현재 사용자 위치 FB 업로드
    fun currentLocationUpload(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            mapRepository.updateUserLocation(UserLocation(latitude, longitude))
        }

    }

    // 사용자 위치 마커 표시
    fun getOtherUserLocations() {
        viewModelScope.launch {
            val otherUserLocations = mapRepository.getOtherUserLocations()
            val userLocationInfoList = otherUserLocations.map{UserLocationInfo(listOf(), listOf(it))}
            _otherUserLocation.value = userLocationInfoList
        }
    }
}

class MapViewModelFactory(
    private val firebaseDatabase: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(MapRepositoryImpl(firebaseDatabase, firestore)) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}