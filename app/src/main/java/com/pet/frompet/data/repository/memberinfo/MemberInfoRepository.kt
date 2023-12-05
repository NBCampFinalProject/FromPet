package com.pet.frompet.data.repository.memberinfo

import com.pet.frompet.data.model.CommunityHomeData

interface MemberInfoRepository {

    suspend fun getMemberSpinner(): List<CommunityHomeData>
}