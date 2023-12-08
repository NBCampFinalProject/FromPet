package com.pet.frompet.data.repository.memberinfo

import android.content.Context
import android.net.Uri
import android.provider.Settings.Global.getString
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pet.frompet.R
import com.pet.frompet.data.model.CommunityHomeData
import com.pet.frompet.data.model.User
import com.pet.frompet.ui.login.putFile
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resumeWithException

class MemberInfoRepositoryImp(
    private val context: Context,
    private val storage: FirebaseStorage,
    private val fireStore: FirebaseFirestore
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

    override suspend fun uploadPetProfile(uri: String): String {
        return suspendCancellableCoroutine { continuation ->
            uri?.let { petProfileUri ->
                val timestamp = SimpleDateFormat(
                    context.getString(R.string.member_info_timestamp),
                    Locale.getDefault()
                ).format(Date())
                val fileName = "IMAGE_$timestamp.png"

                val storageRef = storage.reference.child("images").child(fileName)

                val uploadTask = storageRef.putFile(Uri.parse(petProfileUri))

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    storageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val imageUrl = downloadUri.toString()
                        continuation.resume(imageUrl) {
                            // 취소 시 추가 작업 수행
                            uploadTask.cancel()
                        }
                    } else {
                        continuation.resumeWithException(task.exception ?: IllegalStateException("업로드 실패"))
                    }
                }

                uploadTask.addOnCanceledListener {
                    // 업로드 취소 처리
                    continuation.cancel()
                }
            } ?: continuation.resumeWithException(IllegalArgumentException("Uri가 null입니다"))
        }
    }

    override suspend fun saveToFireStore(user: User): Result<Unit> {
        return try{
            FirebaseFirestore.getInstance().collection("User")
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }


    private fun getAnimalImage(categoryString: String): Int {
        val resourceName = categoryString.split("_").last()
        val resourceIdName =
            context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (resourceIdName != 0) resourceIdName else R.drawable.frog
    }
}