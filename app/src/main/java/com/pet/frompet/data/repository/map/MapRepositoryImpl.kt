package com.pet.frompet.data.repository.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.pet.frompet.data.model.User
import com.pet.frompet.data.model.UserLocation
import com.pet.frompet.data.repository.map.MapRepository
import com.pet.frompet.ui.map.MapMakerBorder
import com.pet.frompet.ui.map.MapUserDetailActivity
import com.pet.frompet.ui.map.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MapRepositoryImpl(
    private val context: Context,
    private val requireActivity: Activity,
    private val requireContext: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val startActivity: Activity
) : MapRepository {


    private val _otherUserLocation = MutableLiveData<List<MapViewModel.UserLocationInfo>>()
    val otherUserLocation : MutableLiveData<List<MapViewModel.UserLocationInfo>> get() = _otherUserLocation

    private val _userLocationInfo = MutableLiveData<MapViewModel.UserLocationInfo>()
    val userLocation : LiveData<MapViewModel.UserLocationInfo> get() = _userLocationInfo

    private val database = Firebase.database
    private val locationRef = database.getReference("location")
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun getUserLocation(): List<UserLocation> {
        TODO("Not yet implemented")
    }

    override suspend fun setUserProfileImage(userUid: String, marker: Marker) {

        val userDocument = firestore.collection("User").document(userUid).get()
            .await() //컬렉셕에 사용자 uid로 접근하고 비동기로 동작 데이터 가져올때까지 기달
        val user = userDocument.toObject(User::class.java) //위에서 얻은 문서들을 user클래스의 인스턴스로 변환
        val profileUrl = user?.petProfile //유저인스턴스에 해당 사용자들의 프로필 사진 변수

        if (profileUrl != null) {
            val imageLoader = context.let { Coil.imageLoader(it) }
            val request = ImageRequest.Builder(requireActivity).data(profileUrl).size(800, 800)
                .transformations(
                    CircleCropTransformation(), MapMakerBorder(requireContext, 15f)
                ) //이미지동그랗게
                .target {
                    val bitmap = (it as BitmapDrawable).bitmap
                    val imageOverlay = OverlayImage.fromBitmap(bitmap)
                    marker.icon = imageOverlay
                }.build()

            imageLoader.execute(request)
        }
    }

    // 스타트액티비티 어떻게 ??
    override suspend fun markerClick(userUid: String) {
//        lifecycleScope.launch {
//            val userDocument = firestore.collection("User").document(userUid).get()
//                .await() //컬렉셕에 사용자 uid로 접근하고 비동기로 동작 데이터 가져올때까지 기달
//            val user = userDocument.toObject(User::class.java) //위에서 얻은 문서들을 user클래스의 인스턴스로 변환
//
//            withContext(Dispatchers.Main) {
//                val intent = Intent(requireContext, MapUserDetailActivity::class.java)
//                intent.putExtra(MapUserDetailActivity.USER, user)
//                startActivity(intent)
//            }
//        }
    }

    override suspend fun getOtherUserLocations() {

        locationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userLocationList = mutableListOf<MapViewModel.UserLocationInfo>()
                for (snapshots in snapshot.children) {
                    val location = snapshots.getValue(UserLocation::class.java)
                    val userUid = snapshots.key

                    if (location != null && userUid != null && userUid != currentUserId) {
                        userLocationList.add(
                            MapViewModel.UserLocationInfo(
                                listOf(userUid),
                                listOf(location)
                            )
                        )
                    }
                }
                _otherUserLocation.value = userLocationList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override suspend fun getloadLocationData(bounds: LatLngBounds) {
        val userUids = mutableListOf<String>()
        val locationList = mutableListOf<UserLocation>()
        var userLocationInfo = MapViewModel.UserLocationInfo()

        locationRef.get().addOnSuccessListener { snapshot ->

            for (locationSnapshot in snapshot.children) {
                val location =
                    locationSnapshot.getValue(com.pet.frompet.data.model.UserLocation::class.java)
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
            userLocationInfo = userLocationInfo.copy(userUids, locationList)
            _userLocationInfo.value = userLocationInfo
        }
    }

    override suspend fun currentLocationUpload(latitude: Double, longitude: Double) {
        val userLocation = UserLocation(latitude, longitude)
        locationRef.child(currentUserId).setValue(userLocation)
    }
}