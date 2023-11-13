package com.pet.frompet.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.pet.frompet.R
import com.pet.frompet.data.model.User
import com.pet.frompet.data.model.UserLocation
import com.pet.frompet.databinding.FragmentMapBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ted.gun0912.clustering.naver.TedNaverClustering
import java.lang.NumberFormatException

class NaverMapFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: MapViewModel by viewModels()

    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var naverMap: NaverMap? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val database = Firebase.database
    private val locationRef = database.getReference("location")

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initMapView()
            }
        }

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // 현재 uid 갖고 옴

    private val markers = mutableListOf<Marker>()


    /** onCreateView 에서 권한 확인+ 위치 권한 없을 시, 사용자에게 권한 요청 **/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasPermission()) {
            requestMultiplePermissions.launch(PERMISSIONS)
        } else {
            initMapView()
        }
    }

    private fun initMapView() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.naver_map_fragment) as MapFragment?
                ?: MapFragment.newInstance().also {
                    childFragmentManager.beginTransaction().add(R.id.naver_map_fragment, it)
                        .commit()
                }

        // fragment의 getMapAsync() 메서드로 OnMapReadyCallBack 콜백을 등록하면, 비동기로 NaverMap 객체를 얻을 수 있음
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        observeViewModel()

    }

    private fun observeViewModel() {

        viewModel.userLocation.observe(viewLifecycleOwner) {
            it?.let { userLocationInfo ->
                val userUid = userLocationInfo.userUid
                val userLocation = userLocationInfo.userLocations

                if (userUid.isNotEmpty() && userLocation.isNotEmpty()) {
                    for (i in userUid.indices) setMark(userUid[i], userLocation[i])
                }
            }
        }
    }

    // hasPermission()에서는 위치 권한 있 -> true , 없 -> false
    private fun hasPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (activity?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        permission
                    )
                } != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        setUpMap()

        // Firebase
        val database = Firebase.database
        val locationRef = database.getReference("location")

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext()) // 초기화
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {

                Log.d("뷰모델", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                // 사용자 현재 위치 파베에 업로드
//                val userLocation = UserLocation(location.latitude, location.longitude)
//                locationRef.child(currentUserId).setValue(userLocation) //viewmodel로 1 --
                viewModel.currentLocationUpload(location.latitude, location.longitude)

                val cameraUpdate =
                    CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))
                        .animate(CameraAnimation.Easing, 2000).reason(CameraUpdate.REASON_GESTURE)
                naverMap.moveCamera(cameraUpdate)

                naverMap.addOnCameraIdleListener {
                    resetMarker()
                    viewModel.getloadLocationData(naverMap.contentBounds)
                }
            }
        }

        viewModel.otherUserLocation.observe(viewLifecycleOwner, Observer { userLocations ->
            for (userLocationInfo in userLocations) {
                for (userUid in userLocationInfo.userUid) {
                    for (location in userLocationInfo.userLocations) {
                        setMark(userUid, location)
                    }
                }
            }
        })
    }


    private fun addMarker(marker: Marker) {
        markers.add(marker)
        Log.d("LoadLocationData", "마커 저장 띵 ${marker}")
    }

    private fun removeMarkers() {
        markers.forEach { marker ->
            marker.map = null //  마커 삭제
            Log.d("LoadLocationData", "마커 삭제 떽 ${marker}")
        }
        markers.clear()
        Log.d("LoadLocationData", "마커 삭제 떽 ${markers.size}")
    }

    private fun resetMarker() {
        removeMarkers()
    }

    private fun setUpMap() {
        naverMap?.locationSource = locationSource //현위치
        naverMap?.uiSettings?.isLocationButtonEnabled = true // 현 위치 버튼 기능
        naverMap?.locationTrackingMode = LocationTrackingMode.Follow // 위치를 추적하면서 카메라도 같이 움직임
        // 줌
        naverMap?.maxZoom = 13.0  // (최대 21)
        naverMap?.minZoom = 10.0
    }


    private fun setMark(userUid: String, location: UserLocation) = lifecycleScope.launch {
        if (!isAdded) return@launch //프래그먼트에서 액티비티가 연결되어 있는지 확인 만약 연결되어 있지 않다면 빠르게 종료해서 requireContext호출을 방지
        if (userUid != currentUserId) {
            val marker = createMarker(location, userUid)
            setUserProfileImage(userUid, marker)
            addMarker(marker)
            Log.d("LoadLocationData", "마커 저장 띵 ${markers.size}")
        }
    }

    /** userlocation, useruid 받아서 naver 지도에 마커 생성, 반환 **/
    private fun createMarker(location: UserLocation, userUid: String): Marker {
        val marker = Marker()

        // 기본 네이버 초록 마커
        if (location != null && location != UserLocation()) {
            marker.position = LatLng(location.latitude, location.longitude)
        } // 마커 위치
        marker.apply {
//            zIndex = 10 // 마커 우선순위
            map = naverMap
            isIconPerspectiveEnabled = true
            alpha = 1.0f
            width = 200
            height = 200
            setIcon(OverlayImage.fromResource(R.drawable.reset))
            onClickListener = Overlay.OnClickListener {
                markerClick(userUid)
                true
            }

            return marker
        }
    }

    //viewmodel
    /** 마커 클릭 시, 프로필 띄우기 **/
    private fun markerClick(userUid: String) {
        lifecycleScope.launch {
            val userDocument = firestore.collection("User").document(userUid).get()
                .await() //컬렉셕에 사용자 uid로 접근하고 비동기로 동작 데이터 가져올때까지 기달
            val user = userDocument.toObject(User::class.java) //위에서 얻은 문서들을 user클래스의 인스턴스로 변환

            val intent = Intent(context, MapUserDetailActivity::class.java)
            intent.putExtra(MapUserDetailActivity.USER, user)
            startActivity(intent)

        }
    }

    /** 특정 사용자의 프로필 이미지 -> 마커 아이콘 **/
    private fun setUserProfileImage(userUid: String, marker: Marker) = lifecycleScope.launch {
        val userDocument = firestore.collection("User").document(userUid).get()
            .await() //컬렉셕에 사용자 uid로 접근하고 비동기로 동작 데이터 가져올때까지 기달
        val user = userDocument.toObject(User::class.java) //위에서 얻은 문서들을 user클래스의 인스턴스로 변환
        val profileUrl = user?.petProfile //유저인스턴스에 해당 사용자들의 프로필 사진 변수

        if (profileUrl != null) {
            val imageLoader = context?.let { Coil.imageLoader(it) }
            val request = ImageRequest.Builder(requireActivity()).data(profileUrl).size(800, 800)
                .transformations(
                    CircleCropTransformation(), MapMakerBorder(requireContext(), 15f)
                ) //이미지동그랗게
                .target {
                    val bitmap = (it as BitmapDrawable).bitmap
                    val imageOverlay = OverlayImage.fromBitmap(bitmap)
                    marker.icon = imageOverlay
                }.build()

            imageLoader?.execute(request)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}