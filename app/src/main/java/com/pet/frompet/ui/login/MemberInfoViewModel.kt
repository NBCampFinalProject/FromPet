package com.pet.frompet.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pet.frompet.data.model.CommunityHomeData
import com.pet.frompet.data.model.User
import com.pet.frompet.data.repository.memberinfo.MemberInfoRepository
import com.pet.frompet.data.repository.memberinfo.MemberInfoRepositoryImp
import kotlinx.coroutines.launch

class MemberInfoViewModel(
    private val memberInfoRepository: MemberInfoRepository
) : ViewModel() {

    private val _petSpinnerType: MutableLiveData<List<CommunityHomeData>> = MutableLiveData()
    val petSpinnerType: LiveData<List<CommunityHomeData>> = _petSpinnerType

    private val _selectPetType = MutableLiveData<CommunityHomeData>()
    val selectPetType: LiveData<CommunityHomeData> = _selectPetType

    private val _petProfileLiveData = MutableLiveData<String>()
    val petProfileLiveData: LiveData<String> = _petProfileLiveData

    private val _saveFireStore: MutableLiveData<Result<Unit>> = MutableLiveData()
    val saveFireStore: LiveData<Result<Unit>> = _saveFireStore


    fun getMemberInfoSpinner() {
        viewModelScope.launch {
            try{
            val communityHomeData = memberInfoRepository.getMemberSpinner()
            _petSpinnerType.postValue(communityHomeData)
        }catch (_: Exception){

        }
        }
    }
    fun saveToFireStore(user: User) {
        viewModelScope.launch {
            try {
                val result = memberInfoRepository.saveToFireStore(user)
                _saveFireStore.postValue(result)
            } catch (e: Exception) {
                _saveFireStore.postValue(Result.failure(e))
            }
        }
    }
    fun contentUpload(petProfile: String?) {
        viewModelScope.launch {
            try {
                if (petProfile == null) {
                    throw IllegalArgumentException("petProfile is null")
                }

                val imageUrl = memberInfoRepository.uploadPetProfile(petProfile)
                showToast("이미지 업로드 성공")

                // Set the value of the LiveData
                _petProfileLiveData.value = imageUrl
            } catch (e: Exception) {
                showToast("이미지 업로드 실패: ${e.message}")
            }
        }
    }
    private fun showToast(message: String) {
        /*  Toast.makeText(this, message, Toast.LENGTH_SHORT).show()*/
    }
}

class MemberInfoViewModelFactory(
    private val context: Context,
    private val storage: FirebaseStorage,
    private val fireStore: FirebaseFirestore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemberInfoViewModel::class.java)) {
            return MemberInfoViewModel(MemberInfoRepositoryImp(context, storage,fireStore)) as T
        } else {
            throw IllegalArgumentException("Not found ViewModel class.")
        }
    }
}