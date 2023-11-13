package com.pet.frompet.ui.commnunity.community


import android.media.metrics.Event
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pet.frompet.SingleLiveEvent
import com.pet.frompet.data.model.CommunityData
import com.pet.frompet.ui.commnunity.communityhome.CategoryClick
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pet.frompet.data.model.CommentData
import com.pet.frompet.data.model.ReCommentData

class CommunityViewModel(
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
    private val store = FirebaseFirestore.getInstance()
    private val _reCommentAdded = MutableLiveData<Boolean>()
    val reCommentAdded: LiveData<Boolean> = _reCommentAdded


    fun getCommunityData(petType: String): LiveData<List<CommunityData>> {
        val liveData = MutableLiveData<List<CommunityData>>()
        firestore.collection("Community")
            .whereEqualTo("petType", petType)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("zzzzzz", "Documents: $documents")

                val communityDataList = documents.mapNotNull { document ->
                    document.toObject(CommunityData::class.java)
                }
                liveData.value = communityDataList
            }
            .addOnFailureListener {}
        return liveData
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
        firestore.collection("Community").document(docsId).delete()
            .addOnCompleteListener { task ->
                _deleteResult.value = task.isSuccessful
            }
    }
    fun addReComment(receivedCommentData: CommentData, reCommentText: String) {
        val reCommentId = store.collection("Community")
            .document(receivedCommentData.postDocumentId)
            .collection("Comment")
            .document(receivedCommentData.commentId)
            .collection("ReComment")
            .document().id

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            val reCommentData = ReCommentData(
                reCommentId,
                receivedCommentData.commentId,
                reCommentText,
                uid,
                System.currentTimeMillis()
            )

            store.collection("Community")
                .document(receivedCommentData.postDocumentId)
                .collection("Comment")
                .document(receivedCommentData.commentId)
                .collection("ReComment")
                .document(reCommentId)
                .set(reCommentData)
                .addOnSuccessListener {
                    _reCommentAdded.value = true
                }
                .addOnFailureListener {
                    _reCommentAdded.value = false
                }
        } else {
            _reCommentAdded.value = false
        }
    }

}


