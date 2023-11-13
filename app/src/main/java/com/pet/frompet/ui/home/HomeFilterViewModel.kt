package com.pet.frompet.ui.home

import android.app.Application
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pet.frompet.data.model.Filter
import com.pet.frompet.data.model.User
import com.pet.frompet.data.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pet.frompet.util.showToast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest

class HomeFilterViewModel(private val app :Application): ViewModel() {
    private val _filteredUsers = MutableLiveData<List<User>>()
    val filteredUsers: MutableLiveData<List<User>> get() = _filteredUsers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid?:""
    private val swipedUsersRef = database.getReference("swipedUsers").child(currentUser)
    private val filteredUsersRef = database.getReference("filteredUsers").child(currentUser)
    private val usersLocationRef = database.getReference("locations")
    var currentFilter: Filter? = null
    private var lastFilter: Filter? = null
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)


    private var currentUserLocation: Location? = null

    init {
        getCurrentUserLocation()
    }
    fun getCurrentUserLocation() {
        try {
            val task: Task<Location> = fusedLocationClient.lastLocation
            task.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLocation = UserLocation(location.latitude, location.longitude)
                    updateUserLocationFirestore(currentUser, userLocation)
                    currentUserLocation = location
                    currentFilter?.let { filter ->
                        filterUsers(filter)
                    }
                }
            }.addOnFailureListener {
                app.showToast("위치권한을 가져오는데 실패 했습니다.", Toast.LENGTH_SHORT)
            }
        } catch (e: SecurityException) {
            app.showToast("위치권한이 없습니다.", Toast.LENGTH_SHORT)
        }
    }


    fun filterUsers(filter: Filter) {
        lastFilter = filter
        var query: Query = store.collection("User")
        Log.d("Filter", " query: $query")


        if (filter.petGender != "all") {
            filter.petGender?.let {
                val genderValue = when (it) {
                    "수컷" -> "수컷"
                    "암컷" -> "암컷"
                    else -> it
                }
                query = query.whereEqualTo("petGender", genderValue)
            }
        }
        if (filter.petNeuter != "상관없음") {
            filter.petNeuter?.let {
                val genderValue = when (it) {
                    "중성화" -> "중성화"
                    "중성화 안함" -> "중성화 안함"
                    else -> it
                }
                query = query.whereEqualTo("petNeuter", genderValue)
            }
        }
        if (filter.petType != "전체") {
            filter.petType?.let { query = query.whereEqualTo("petType", it) }
        }
        query.get().addOnSuccessListener { documents ->
            val users = documents.map { it.toObject(User::class.java) }
                .filter { it.uid != currentUser }

            // 스와이프한 사용자를 제외하는로직입니다
            excludeSwipedUsers(users) { filteredUsersWithoutSwipes ->
                // 거리 필터링을 적용합니다.
                val usersWithinDistance = applyDistanceFilter(filteredUsersWithoutSwipes, filter)
                // 최종적으로 필터링된 사용자 목록을 LiveData에 설정합니다.
                _filteredUsers.value = usersWithinDistance
                // 필터링된 사용자 목록을 업데이트합니다.
                updateFilteredUsers(usersWithinDistance)
            }
        }
    }
    fun loadFilteredUsers() {
        _isLoading.value = true
        filteredUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                _filteredUsers.value = users
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        })
    }
    private fun excludeSwipedUsers(users: List<User>, callback: (List<User>) -> Unit) {
        swipedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val swipedUserIds = snapshot.children.map { it.key ?: "" }.toSet()
                val filteredList = users.filter { !swipedUserIds.contains(it.uid) }
                callback(filteredList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun applyDistanceFilter(users: List<User>, filter: Filter): List<User> {
        // 현재 사용자 위치가 없으면 빈 목록을 반환합니다
        val currentUserLocation = currentUserLocation ?: return emptyList()
        // 거리 필터링을 적용합니다
        return users.filter { user ->
            user.userLocation?.let { userLocation ->
                val otherUserLocation = Location("").apply {
                    latitude = userLocation.latitude
                    longitude = userLocation.longitude
                }
                val distance = currentUserLocation.distanceTo(otherUserLocation) / 1000 // km 단위
                distance <= filter.distanceFrom
            } ?: false
        }
    }

    // 사용자가 카드를 스와이프할 때 호출할 함수입니다:박세준
    fun userSwiped(userId: String) {
        // 스와이프한 사용자를 스와이프 노드에 추가합니다
        swipedUsersRef.child(userId).setValue(true)

        // 필터링된 사용자 목록에서 스와이프한 사용자를 제거합니당
        _filteredUsers.value = _filteredUsers.value?.filterNot { it.uid == userId }

        // 필터유저 노드에서 스와이프한 사용자를 제거합니다
        filteredUsersRef.child(userId).removeValue()
    }



    // 사용자의 위치 정보가 업데이트 될 때 파이어스토어 User 문서에도 반영하는 함수입니다
    fun updateUserLocationFirestore(userId: String, userLocation: UserLocation) {
        val userRef = store.collection("User").document(userId)
        userRef.update("userLocation", userLocation)
            .addOnSuccessListener {
                Log.d("Filter", "User location updat Firestore  $userId")
            }
    }

    // 필터링된 사용자 목록을 업데이트하는 함수입니당
    private fun updateFilteredUsers(users: List<User>) {
    //이 트랜잭션을 사용하는 이유: 여러 사용자가 동시에 같은 데이터를 수정할 때 발생할 수 있는 충돌을 방지하기 위해(현재상태기반으로 데이터를 읽고 쓰는 방식)
        filteredUsersRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                mutableData.value = users.associateBy { it.uid }
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                b: Boolean,
                dataSnapshot: DataSnapshot?
            ) {
                if (databaseError != null) {
                    Log.e("Filter", "Failed users", databaseError.toException())
                } else {
                    Log.d("Filter", "Updated ${users.size} users")
                }
            }
        })
    }

    //    fun loadLocationUsers(filteredUsers: List<User>, filter: Filter) {
//        _isLoading.value = true
//
//        val currentUserLocation = currentUserLocation ?: run {
//            _isLoading.value = false
//            return
//        }
//
//        // 이미 필터링된 사용자 목록에 대해서만 거리 필터링을 적용합니다
//        val usersWithinDistance = filteredUsers.filter { user ->
//            user.userLocation?.let { userLocation ->
//                val otherUserLocation = Location("").apply {
//                    latitude = userLocation.latitude
//                    longitude = userLocation.longitude
//                }
//                val distanceInMeters = currentUserLocation.distanceTo(otherUserLocation)
//                val distanceInKm = distanceInMeters / 1000 // 미터를 킬로미터로 변환
//                distanceInKm <= filter.distanceFrom
//            } ?: false
//        }
//
//        Log.d("Filter", "Filtering with distance up to ${filter.distanceFrom} km")
//
//        _filteredUsers.value = usersWithinDistance
//        _isLoading.value = false
//    }


}




    class HomeFilterViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFilterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeFilterViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}





