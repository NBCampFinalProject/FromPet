package com.pet.frompet.data.repository.memberinfo

import android.content.Context
import android.util.Log
import com.pet.frompet.R
import com.pet.frompet.data.model.CommunityHomeData

class MemberInfoRepositoryImp(
    private val context: Context
):MemberInfoRepository {
    override suspend fun getMemberSpinner(): List<CommunityHomeData> {
        val categories = listOf(
            "category_dog", "category_cat", "category_raccoon", "category_fox",
            "category_chick", "category_pig", "category_snake", "category_fish"
        )
        Log.e("sshImp", "getCategory called with categories: $categories")
        return categories.map { categoryString ->
            val stringResourceId = context.resources.getIdentifier(categoryString, "string", context.packageName)
            val categoryText = if (stringResourceId != 0) {
                context.getString(stringResourceId)
            } else {
                "Category Not Found"
            }

            CommunityHomeData(
                getAnimalImage(categoryString), // 이미지 리소스 ID 가져오기
                categoryText
            )
        }
    }

    private fun getAnimalImage(categoryString: String): Int {
        val resourceName = categoryString.split("_").last()
        val resourceIdName =
            context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (resourceIdName != 0) resourceIdName else R.drawable.frog
    }
}