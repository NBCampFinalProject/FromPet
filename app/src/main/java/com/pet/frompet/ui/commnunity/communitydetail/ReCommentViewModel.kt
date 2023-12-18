package com.pet.frompet.ui.commnunity.communitydetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.data.model.CommentData
import com.pet.frompet.data.model.CommunityData
import com.pet.frompet.data.model.ReCommentData

class ReCommentViewModel : ViewModel() {

    private val store = FirebaseFirestore.getInstance()
    private var communityData: CommunityData? = null

    private val _addReCommentResult = MutableLiveData<List<ReCommentData>>()
    val addReCommentResult: LiveData<List<ReCommentData>> get() = _addReCommentResult

    // 대댓글 수 카운트
    private val _reCommentCount = MutableLiveData<Int>()
    val reCommentCount: LiveData<Int> get() = _reCommentCount

//    fun addReComment(receiveCommentData: CommentData, reCommentText: String) {
//
//    }

//    fun  likeComment(commentData: CommentData) {
//        val likeUsers = commentData.likeUsers
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        val commentDocumentRef = store.collection("Community")
//            .document(communityData?.docsId ?: "")
//            .collection("Comment")
//            .document(commentData.commentId)
//        store.runTransaction { transaction ->
//            val snapshot = transaction.get(commentDocumentRef)
//            val newLikeCount: Long
//            val newLikeUsers: List<String>
//            if (likeUsers.contains(uid)) {
//                newLikeCount = (snapshot.getLong("likeCount") ?: 1) - 1
//                newLikeUsers = likeUsers - uid
//            } else {
//                newLikeCount = (snapshot.getLong("likeCount") ?: 0) + 1
//                newLikeUsers = likeUsers + uid
//            }
//            transaction.update(commentDocumentRef, "likeCount", newLikeCount)
//            transaction.update(commentDocumentRef, "likeUsers", newLikeUsers)
//            null
//        }
//    }

    // 액비티비에서 fb 에 직접 접근하는 대신, viewmodel 에서 ref 생성이 바람직
    // 댓글 삭제
    fun deleteComment(
        communityDocId: String,
        communityId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val commentDocumentRef = store
            .collection("Community")
            .document(communityDocId)
            .collection("Comment")
            .document(communityId)

        commentDocumentRef.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }

    // 댓글 신고
    fun reportComment(
        communityDocId: String,
        communityId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val commentDocumentRef = store
            .collection("Community")
            .document(communityDocId)
            .collection("Comment")
            .document(communityId)

        store.runTransaction { transaction ->
            val snapshot = transaction.get(commentDocumentRef)
            val newReportCount = snapshot.getLong("reportCount")?.plus(1) ?: 1
            transaction.update(commentDocumentRef, "reportCount", newReportCount)

            // 신고 횟수가 10회 이상이면 해당 댓글 삭제, 나중에 신고는 개인 당 한 번만 할 수 있게 바꿀 예정
            if (newReportCount >= 10) {
                transaction.delete(commentDocumentRef)
            }
            null
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure()
        }
    }

    // 대댓글 수 로드
    fun loadReCommentCount(communityDocId: String, communityId: String) {
        val reCommentRef = store
            .collection("Community")
            .document(communityDocId)
            .collection("Comment")
            .document(communityId)
            .collection("ReComment")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val count = snapshot?.documents?.size ?: 0
                _reCommentCount.value = count
            }
    }
}
