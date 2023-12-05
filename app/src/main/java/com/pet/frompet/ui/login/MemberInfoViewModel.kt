package com.pet.frompet.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pet.frompet.data.model.CommunityHomeData
import com.pet.frompet.data.repository.memberinfo.MemberInfoRepository
import com.pet.frompet.data.repository.memberinfo.MemberInfoRepositoryImp
import kotlinx.coroutines.launch

class MemberInfoViewModel(
    private val memberInfoRepository: MemberInfoRepository
) : ViewModel() {

    private val _petSpinnerType: MutableLiveData<List<CommunityHomeData>> = MutableLiveData()
    val petSpinnerType: LiveData<List<CommunityHomeData>> = _petSpinnerType

    private val _selectPetType = MutableLiveData<CommunityHomeData>()
    val selectPetType:LiveData<CommunityHomeData> = _selectPetType


    fun getMemberInfoSpinner() {
        viewModelScope.launch {
            val communityHomeData = memberInfoRepository.getMemberSpinner()
            _petSpinnerType.postValue(communityHomeData)
        }
    }

    fun setSelectType(item: CommunityHomeData){
        _selectPetType.value = item
    }
}

class MemberInfoViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemberInfoViewModel::class.java)) {
            return MemberInfoViewModel(MemberInfoRepositoryImp(context)) as T
        } else {
            throw IllegalArgumentException("Not found ViewModel class.")
        }
    }
}