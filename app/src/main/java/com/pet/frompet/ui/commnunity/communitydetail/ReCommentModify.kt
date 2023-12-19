package com.pet.frompet.ui.commnunity.communitydetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pet.frompet.R
import com.pet.frompet.data.model.ReCommentData
import com.pet.frompet.data.model.CommunityData
import com.google.firebase.firestore.FirebaseFirestore

class ReCommentModify : AppCompatActivity() { //대댓글 수정 화면
    private lateinit var etReComment: EditText
    private var reCommentData: ReCommentData? = null
    private var communityData: CommunityData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_re_comment_modify)

        etReComment = findViewById(R.id.etComment)


        reCommentData = intent.getParcelableExtra("reCommentData")
        communityData = intent.getParcelableExtra("communityData")
        val reCommentText = intent.getStringExtra("reCommentText")
        etReComment.setText(reCommentText)

        val btModify = findViewById<Button>(R.id.bt_modify2)
        btModify.setOnClickListener {
            val modifiedReComment = etReComment.text.toString()

            if (modifiedReComment.isNotEmpty()) {
                val store = FirebaseFirestore.getInstance()
                val reCommentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData?.commentId ?: "")
                    .collection("ReComment")
                    .document(reCommentData?.reCommentId ?: "")

                reCommentDocumentRef.update("content", modifiedReComment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "대댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        val dataIntent = Intent().apply {
                            putExtra("modifiedReComment", modifiedReComment)
                        }
                        setResult(Activity.RESULT_OK, dataIntent)
                        Log.d("ReCommentModify", "docId: ${communityData?.docsId}, commentId: ${reCommentData?.commentId}, reCommentId: ${reCommentData?.reCommentId}")
                        finish()

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "대댓글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        Log.d("ReCommentModify", "docId: ${communityData?.docsId}, commentId: ${reCommentData?.commentId}, reCommentId: ${reCommentData?.reCommentId}")
                    }
            } else {
                Toast.makeText(this, "대댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        val backBtn = findViewById<ImageView>(R.id.back_btn)
        backBtn.setOnClickListener {
            finish()
        }
    }
}
