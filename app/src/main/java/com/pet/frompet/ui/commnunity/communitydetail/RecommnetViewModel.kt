package com.pet.frompet.ui.commnunity.communitydetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.data.model.ReCommentData
import com.pet.frompet.data.repository.comment.RecommnetRepository
import com.pet.frompet.data.repository.comment.RecommnetRepositoryImp
import kotlinx.coroutines.launch

class RecommnetViewModel(
    private val recommnetRepository: RecommnetRepository
):ViewModel() {
    private val _reComments = MutableLiveData<List<ReCommentData>>()
    val reComment: LiveData<List<ReCommentData>> = _reComments

    fun addReComment(
        postDocumentId:String,
        commentId:String,
        reCommentText: String,
        onSuccess: () -> Unit,
        onFailure:(String) -> Unit
    ){
        viewModelScope.launch {
            recommnetRepository.addReCommnet(
                postDocumentId, commentId, reCommentText,onSuccess={
                    onSuccess.invoke()
                },
                onFailure={
                    onFailure.invoke(it)
                }
            )
        }
    }

}
class RecommnetViewModelFactory(
    private val firestore: FirebaseFirestore
):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RecommnetViewModel::class.java)){
            return RecommnetViewModel(RecommnetRepositoryImp(firestore)) as T
        }else{
            throw IllegalArgumentException("Not found ViewModel")
        }
    }
}