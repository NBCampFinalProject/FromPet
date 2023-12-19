package com.pet.frompet.ui.commnunity.communitydetail
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.pet.frompet.R
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.pet.frompet.data.model.CommentData
import com.pet.frompet.data.model.CommunityData
import com.google.firebase.firestore.FirebaseFirestore


class CommentModify : AppCompatActivity() { //댓글 수정 화면
    private lateinit var etComment: EditText
    private var commentData: CommentData? = null
    private var communityData: CommunityData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_modify)

        etComment = findViewById(R.id.etComment)

        // CommentModify 액티비티로 전달된 데이터 가져오기
        commentData = intent.getParcelableExtra("commentData")
        communityData = intent.getParcelableExtra("communityData")
        val commentText = intent.getStringExtra("commentText")
        etComment.setText(commentText)

        val btModify = findViewById<Button>(R.id.bt_modify2)
        btModify.setOnClickListener {
            val modifiedComment = etComment.text.toString()

            if (modifiedComment.isNotEmpty()) {

                val store = FirebaseFirestore.getInstance()
                val commentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(commentData?.commentId ?: "")

                commentDocumentRef.update("content", modifiedComment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        val dataIntent = Intent().apply {
                            putExtra("modifiedComment", modifiedComment)
                        }
                        setResult(Activity.RESULT_OK, dataIntent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "댓글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
        val backBtn = findViewById<ImageView>(R.id.back_btn)
        backBtn.setOnClickListener {
            finish()
        }
    }
}
