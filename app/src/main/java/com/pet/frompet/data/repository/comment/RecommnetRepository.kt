package com.pet.frompet.data.repository.comment

interface RecommnetRepository {

    suspend fun addReCommnet(
        postDocumentId:String,
        commentId:String,
        reCommentText: String,
        onSuccess: () -> Unit,
        onFailure:(String) -> Unit
    )

}