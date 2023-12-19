package com.pet.frompet.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pet.frompet.data.model.CommunityData

class CommunityRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CommunityRepository{


    override fun getCommunityData(petType: String): LiveData<List<CommunityData>> {
        val liveData = MutableLiveData<List<CommunityData>>()
        firestore.collection("Community")
            .whereEqualTo("petType", petType)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("zzzzzz", "Documents: $documents")

                val communityDataList = documents.mapNotNull { document ->
                    document.toObject(CommunityData::class.java)
                }
                liveData.value = communityDataList
            }
            .addOnFailureListener {}
        return liveData
    }

    override suspend fun deleteCommunityData(docsId: String) {
//        val deleteResult = MutableLiveData<Boolean>()
        firestore.collection("Community").document(docsId).delete()
            .addOnCompleteListener { task ->
//                deleteResult.value = task.isSuccessful
            }
    }
}