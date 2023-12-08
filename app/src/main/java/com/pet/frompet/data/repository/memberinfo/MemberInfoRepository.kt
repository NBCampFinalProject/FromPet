package com.pet.frompet.data.repository.memberinfo

import com.pet.frompet.data.model.CommunityHomeData
import com.pet.frompet.data.model.User

interface MemberInfoRepository {

    suspend fun getMemberSpinner(): List<CommunityHomeData>

    suspend fun uploadPetProfile(uri: String):String

    suspend fun saveToFireStore(user: User): Result<Unit>
}