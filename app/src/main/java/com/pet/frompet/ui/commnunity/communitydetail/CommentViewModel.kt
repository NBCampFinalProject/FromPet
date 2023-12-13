package com.pet.frompet.ui.commnunity.communitydetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.data.model.CommentData
import com.pet.frompet.data.model.ReCommentData

class CommentViewModel():ViewModel() {

    private val store = FirebaseFirestore.getInstance()
    private val _reCommentAdded = MutableLiveData<Boolean>()
    val reCommentAdded: LiveData<Boolean> = _reCommentAdded

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