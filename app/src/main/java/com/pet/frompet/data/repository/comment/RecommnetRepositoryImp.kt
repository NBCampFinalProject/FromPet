package com.pet.frompet.data.repository.comment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.data.model.ReCommentData
import com.pet.frompet.util.showToast

class RecommnetRepositoryImp(
    private val firestore: FirebaseFirestore
):RecommnetRepository {

    override suspend fun addReCommnet(
        postDocumentId: String,
        commentId: String,
        reCommentText: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val reCommentId = firestore.collection("Community")
            .document(postDocumentId)
            .collection("Comment")
            .document(commentId)
            .collection("ReComment")
            .document().id

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            val reCommentData = ReCommentData(
                reCommentId,
                commentId,
                reCommentText,
                uid,
                System.currentTimeMillis()
            )

            firestore.collection("Community")
                .document(postDocumentId)
                .collection("Comment")
                .document(commentId)
                .collection("ReComment")
                .document(reCommentId)
                .set(reCommentData)
                .addOnSuccessListener {
                    val commentDocumentRef = firestore.collection("Community")
                        .document(postDocumentId)
                        .collection("Comment")
                        .document(commentId)
                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(commentDocumentRef)
                        val newReCommentCount = (snapshot.getLong("reCommentCount") ?: 0) + 1
                        transaction.update(commentDocumentRef, "reCommentCount", newReCommentCount)
                        null
                    }.addOnSuccessListener {
                        onSuccess.invoke()
                    }.addOnFailureListener {
                        onFailure.invoke("대댓글 추가에 실패했습니다")
                    }
                }.addOnFailureListener {
                    onFailure.invoke("대댓글 추가에 실패했습니다")
                }
        } else {
            onFailure.invoke("사용자 정보를 불러오는데 실패했습니다.")
        }
    }
}