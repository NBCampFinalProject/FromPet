@file:Suppress("UNCHECKED_CAST")

package com.pet.frompet.ui.commnunity.community


import android.media.metrics.Event
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pet.frompet.SingleLiveEvent
import com.pet.frompet.data.model.CommunityData
import com.pet.frompet.ui.commnunity.communityhome.CategoryClick
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pet.frompet.data.repository.CommunityRepository
import com.pet.frompet.data.repository.CommunityRepositoryImpl
import kotlinx.coroutines.launch
import java.lang.Exception

class CommunityViewModel(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    // 데이터 가져오기
    private val communitydb = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Firebase 현재 사용자 가져오기
    private val currentUserId = auth.currentUser?.uid
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val _communityList = MutableLiveData<List<CommunityData>?>()
    val communityList: MutableLiveData<List<CommunityData>?> = _communityList
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult


    private val _filteredCommunityList = MutableLiveData<List<CommunityData>?>()
    val filteredCommunityList: LiveData<List<CommunityData>?> = _filteredCommunityList

    private val _event: SingleLiveEvent<CategoryClick> = SingleLiveEvent()
    val event: LiveData<CategoryClick> get() = _event
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()


    // repo 로
    // livedata는 이미 비동기적으로 동작하도록 설계돼있음
    // livedata를 반환하는 함수엔 suspend 사용할 필요 없음
    fun getCommunityData(petType: String): LiveData<List<CommunityData>> {
        return communityRepository.getCommunityData(petType)
    }


    fun getFilterCommunityData(petType: String, filter: String): LiveData<List<CommunityData>> {
        val liveData = MutableLiveData<List<CommunityData>>()

        var query: Query = firestore.collection("Community").whereEqualTo("petType", petType)

        if (filter != "전체") {
            query = query.whereEqualTo("tag", filter)
        }
        query.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { docs ->
                val communityDataList = docs.mapNotNull { document ->
                    document.toObject(CommunityData::class.java)
                }
                liveData.value = communityDataList
            }
            .addOnFailureListener {}
        return liveData
    }

    fun deleteCommunityData(docsId: String) {
        viewModelScope.launch {
            try {
                val delete = communityRepository.deleteCommunityData(docsId)
                _deleteResult.value = (delete != null)
            } catch (e: Exception) {
                _deleteResult.value = false
            }
        }
    }
}

class CommunityViewModelFactory(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            return CommunityViewModel(CommunityRepositoryImpl(firestore)) as T
        } else {
            throw IllegalArgumentException("Not found ViewModel")
        }
    }
}


