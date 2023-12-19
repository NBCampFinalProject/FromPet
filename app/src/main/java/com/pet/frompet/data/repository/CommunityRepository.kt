package com.pet.frompet.data.repository

import androidx.lifecycle.LiveData
import com.pet.frompet.data.model.CommunityData

interface CommunityRepository {
    fun getCommunityData(petType: String): LiveData<List<CommunityData>>

    suspend fun deleteCommunityData(docsId: String)

}