package com.pet.frompet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pet.frompet.data.model.User
import com.pet.frompet.ui.home.HomeFilterViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class MatchSharedViewModel : ViewModel() {

    private val _likeList :MutableLiveData<List<User>?> = MutableLiveData()
    val likeList : MutableLiveData<List<User>?> get() = _likeList
//    private val _disLikeList : MutableLiveData<List<UserModel>> = MutableLiveData()
//    val disLikeList : MutableLiveData<List<UserModel>>  get() =  _disLikeList
    private val _matchedList : MutableLiveData<List<User>> = MutableLiveData()
    val matchedList : MutableLiveData<List<User>> get() = _matchedList

    private val database = FirebaseDatabase.getInstance().getReference("likeUsers")
    private val swipeUsersDb = FirebaseDatabase.getInstance().getReference("swipedUsers")
    private val disLikeDb = FirebaseDatabase.getInstance().getReference("dislikeList")
    private val filterDb =FirebaseDatabase.getInstance().getReference("filteredUsers")

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun like(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        database.child(targetUserId).child("likedBy").child(currentUserId).setValue(true)
//        filterDb.child(currentUserId).child(targetUserId).removeValue()
    }

    fun dislike(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        database.child(currentUserId).child("likedBy").child(targetUserId).removeValue()
        disLikeDb.child(currentUserId).child(targetUserId).setValue(true)
    }

    fun loadlike() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.child(currentUserId).child("likedBy").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedUserIds = snapshot.children.mapNotNull { it.key }
                val likedUsers = mutableListOf<User>()

                likedUserIds.forEach { userId ->
                    database.child(currentUserId).child("matched").child(userId)
                        .addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(matchedSnapshot: DataSnapshot) {
                                if (!matchedSnapshot.exists()) { // 매치된 사용자가 아닌 경우에만 추가
                                    firestore.collection("User").document(userId)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            val user = document.toObject(User::class.java)
                                            user?.let {
                                                // 중복 체크: 이미 likedUsers에 있는 사용자는 추가하지 않음
                                                if (likedUsers.none { existingUser -> existingUser.uid == user.uid }) {
                                                    likedUsers.add(it)
                                                    _likeList.value = likedUsers.toList()
                                                    Log.d("jun", "매치되기전라이크리스트${_likeList.value}")
                                                }
                                            }
                                        }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

//    fun loadAlreadyActionUsers(load: (List<String>) -> Unit) {
//        val currentUserId = auth.currentUser?.uid ?: return
//        val exceptIds = mutableListOf<String>()
//
//        disLikeDb.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {  //디스라이크유저 불러오기
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                snapshot.children.forEach { childSnapshot ->
//                    val userId = childSnapshot.key
//                    userId?.let { exceptIds.add(it) }
//                }
//                database.addListenerForSingleValueEvent(object : ValueEventListener { //likeby노드에 내유아디가 들어가서 전체순회
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        snapshot.children.forEach { userSnapshot ->
//                            val userId = userSnapshot.key
//                            if (userSnapshot.child("likedBy").hasChild(currentUserId)) {
//                                userId?.let { exceptIds.add(it) }
//
//                            }
//                        }
//                        database.child(currentUserId).child("matched").addListenerForSingleValueEvent(object : ValueEventListener { //매치된유저불러오기
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                snapshot.children.forEach { childSnapshot ->
//                                    val userId = childSnapshot.key
//                                    userId?.let { exceptIds.add(it) }
//                                }
//                                load(exceptIds)
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {}
//                        })
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {}
//                })
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//    }
//
//
//    fun getExceptDislikeAndMe(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
//        val allUsersData = mutableListOf<User>()
//        val currentUserId = auth.currentUser?.uid
//
//        loadAlreadyActionUsers { exceptionUsers ->
//
//            firestore.collection("User")
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    if (querySnapshot.isEmpty.not()) {
//                        for (document in querySnapshot.documents) {
//                            val user = document.toObject(User::class.java)
//                            user?.let {
//                                if (it.uid != currentUserId && it.uid !in exceptionUsers) {
//                                    allUsersData.add(it)
//                                }
//                            }
//                        }
//                        onSuccess(allUsersData)
//                    }
//                }
//                .addOnFailureListener { e ->
//                    onFailure(e)
//                }
//        }
//    }

    fun matchUser(otherUserUid: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentTime = System.currentTimeMillis()
        // 서로 like한 경우
        Log.d("jun", "매치 유저 uid: $otherUserUid")
        database.child(currentUserId).child("matched").child(otherUserUid).setValue(true)
        database.child(otherUserUid).child("matched").child(currentUserId).setValue(true)
        database.child(currentUserId).child("likedBy").child(otherUserUid).removeValue()
        database.child(otherUserUid).child("likedBy").child(currentUserId).removeValue()
        database.child(currentUserId).child("matched").child(otherUserUid).setValue(currentTime)
        database.child(otherUserUid).child("matched").child(currentUserId).setValue(currentTime)

        val currentLikes = _likeList.value?.toMutableList() ?: mutableListOf()
        currentLikes?.removeIf { it.uid == otherUserUid }
        _likeList.value = currentLikes
        Log.d("jun", "매치된후라이크리스트:${_likeList.value}")
        loadlike()
    }

    fun loadMatchedUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.child(currentUserId).child("matched").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matchedUserIds = snapshot.children.mapNotNull { it.key }
                val matchedUsers = mutableListOf<User>()
                if (matchedUserIds.isEmpty()) {
                    _matchedList.value = listOf()
                    return //이 부분 추가해서 매치리스트가 빈null일때도 ui업뎃하게함..
                }

                matchedUserIds.forEach { userId ->
                    firestore.collection("User").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            val user = document.toObject(User::class.java)
                            user?.let {
                                matchedUsers.add(it)
                                _matchedList.value = matchedUsers.toList()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("jun", "Error  ", exception)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    fun removeMatchedUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        database.child(currentUserId).child("matched").child(targetUserId).removeValue()
        database.child(targetUserId).child("matched").child(currentUserId).removeValue()
        database.child(targetUserId).child("likeBy").child(currentUserId).removeValue()
        database.child(currentUserId).child("likeBy").child(targetUserId).removeValue()
        swipeUsersDb.child(currentUserId).child(targetUserId).removeValue()
        swipeUsersDb.child(targetUserId).child(currentUserId).removeValue()
        loadMatchedUsers()
        loadlike()

        Log.d("jun", "삭제한 후 매치리스트  : ${_matchedList.value}")
    }
    // 순위 매기기
    // 상위 5명의 프로필 사진 가져오기
    // 1주일 전의 시간을 밀리초로 계산
    // 현재시간에서 1주일 시간뺀 만들고 매치노드 자식중에서 이후에 매칭된 데이터만 선택필터
    //공동 등수는 매치된 시간순으로
    // Firestore 호출의 Task를 저장할 리스트
    // 모든 Task가 완료될 때까지 기다림 여러 호출을 동시에 시작하면 완료 순서가 예측할 수 없는 걸 막기위해서
    fun getTopMatchedUsersThisWeek(onSuccess: (List<User>) -> Unit) {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        val matchedCountsThisWeek = mutableMapOf<String, Int>()
        val matchedTimeThisWeek = mutableMapOf<String, Long>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { userSnapshot ->
                    val userId = userSnapshot.key
                    val matchedThisWeek = userSnapshot.child("matched")
                        .children
                        .filter {
                            val value = it.getValue(Long::class.java)
                            value != null && value > oneWeekAgo
                        }

                    val matchedThisWeekCount = matchedThisWeek.count()
                    val earliestMatchThisWeek = matchedThisWeek
                        .mapNotNull { it.getValue(Long::class.java) }
                        .minOrNull() ?: Long.MAX_VALUE

                    userId?.let {
                        matchedCountsThisWeek[it] = matchedThisWeekCount
                        matchedTimeThisWeek[it] = earliestMatchThisWeek
                    }
                }
                val sortedUsers = matchedCountsThisWeek.toList()
                    .sortedWith(compareByDescending<Pair<String, Int>> { it.second }
                        .thenBy { matchedTimeThisWeek[it.first] })
                    .map { it.first }
                    .take(5)

                val tasks = mutableListOf<Task<DocumentSnapshot>>()

                sortedUsers.forEach { userId ->
                    val task = firestore.collection("User").document(userId).get()
                    tasks.add(task)
                }
                Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
                    val topUsers = documents.mapNotNull { it.toObject(User::class.java) }
                    onSuccess(topUsers)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    fun getTotalMatchedCount(onSuccess: (Int)-> Unit){
        var totalMatchedCount = 0
        database.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{ userSnapshot->
                    val matchedCountUser = userSnapshot.child("matched").children.count()
                    totalMatchedCount += matchedCountUser
                }
                onSuccess(totalMatchedCount / 2)
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}














