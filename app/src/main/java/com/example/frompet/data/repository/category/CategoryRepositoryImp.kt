package com.example.frompet.data.repository.category

import android.content.Context
import android.util.Log
import com.example.frompet.R
import com.example.frompet.data.model.CommunityData
import com.example.frompet.data.model.CommunityHomeData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CategoryRepositoryImp(
    private val context: Context
) : CategoryRepository {
    override suspend fun getCategory(): List<CommunityHomeData> {
        val categories = listOf(
            "category_dog", "category_cat", "category_raccoon", "category_fox",
            "category_chick", "category_pig", "category_snake", "category_fish"
        )
        Log.e("zzzzz", "getCategory called with categories: $categories")
        return categories.map { categoryString ->
            CommunityHomeData(
                getAnimalImage(categoryString), // 이미지 리소스 ID 가져오기
                context.getString(
                    context.resources.getIdentifier(
                        categoryString,
                        "string",
                        context.packageName
                    )
                )
            )

        }
    }

    override suspend fun getCommunityData(petType:String): List<CommunityData> {
        val firestore = FirebaseFirestore.getInstance()
        val communityDataList = mutableListOf<CommunityData>()
        try {
            val querySnapshot = firestore.collection("Community")
                .whereEqualTo("petType",petType)
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .get()
                .await()
            Log.e("aaaaaa", "$petType")
            for (document in querySnapshot.documents) {
                val title = document.getString("title") ?: ""
                val tag = document.getString("tag") ?: ""
                val timestamp = document.getString("timestamp") ?: ""
                val contents = document.getString("contents") ?: ""
                val uid = document.getString("uid") ?: ""
                val docsId = document.id
                val petType = document.getString("petType") ?: ""

                val communityData = CommunityData(
                    title = title,
                    tag = tag,
                    timestamp = timestamp,
                    contents = contents,
                    uid = uid,
                    docsId = docsId,
                    petType = petType
                )
                communityDataList.add(communityData)
            }
            Log.e("aaaaa", "$communityDataList")

        } catch (e: Exception) {
            Log.e("aaaaaa", "error", e)
        }
        return communityDataList

    }


    private fun getAnimalImage(categoryString: String): Int {
        val resourceName = categoryString.split("_").last()
        val resourceIdName =
            context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (resourceIdName != 0) resourceIdName else R.drawable.frog
    }

}
