package com.pet.frompet.ui.commnunity.communitydetail

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.R
import com.pet.frompet.data.model.CommentData
import com.pet.frompet.data.model.CommunityData
import com.pet.frompet.data.model.ReCommentData
import com.pet.frompet.databinding.ActivityReCommentBinding
import com.pet.frompet.ui.map.MapViewModel
import com.pet.frompet.util.showToast

class ReCommentActivity : AppCompatActivity() { //대댓글 작성 화면

    private var _binding: ActivityReCommentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CommentAdapter
    /*private val firestore = FirebaseFirestore.getInstance()*/
    private var communityData: CommunityData? = null

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            RecommnetViewModelFactory(firestore)
        ).get(RecommnetViewModel::class.java)
    }
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityReCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)


        communityData = intent.getParcelableExtra<CommunityData>("communityData")



        adapter = CommentAdapter(
            modifyClick = { commentData ->
                showBottomSheet(commentData)
            },
            likeClick = { commentData, imageView, textView1, textView2 ->
                likeClick(commentData, imageView, textView1, textView2)
            },
            reReplyClick = { commentData ->

            },
            reCommentModifyClick = { reCommentData ->
                showReCommentBottomSheet(reCommentData)
            },
            reCommentLikeClick = { reCommentData, imageView, textView1, textView2 ->
                reCommentLikeClick(reCommentData, imageView, textView1, textView2)
            }
        )


        binding.rvReply.adapter = adapter
        binding.rvReply.layoutManager = LinearLayoutManager(this)


        val receivedCommentData = intent.getParcelableExtra<CommentData>("commentData")

        val commentDataList = listOf(receivedCommentData)

        adapter.submitList(commentDataList)

        val tvCountRe = findViewById<TextView>(R.id.tv_count_re)
        receivedCommentData?.let {
            loadReCommentCount(it, tvCountRe)
        }


        binding.imageButton.setOnClickListener {
            val reCommentText = binding.editTextText.text.toString()
            receivedCommentData?.let {
                if (reCommentText.isNotEmpty()) {
                    /*addReComment(it, reCommentText)*/
                } else {
                    showToast("대댓글 내용을 입력하세요", Toast.LENGTH_SHORT)
                }
            } ?: showToast("댓글 데이터를 불러오지 못했습니다", Toast.LENGTH_SHORT)
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
        loadComments()
    }

    /* private fun addReComment(receivedCommentData: CommentData, reCommentText: String) {
         val reCommentId = store.collection("Community")
             .document(receivedCommentData.postDocumentId)
             .collection("Comment")
             .document(receivedCommentData.commentId)
             .collection("ReComment")
             .document().id

         val uid = FirebaseAuth.getInstance().currentUser?.uid

         if (uid != null) {
             val reCommentData = ReCommentData(
                 reCommentId,
                 receivedCommentData.commentId,
                 reCommentText,
                 uid,
                 System.currentTimeMillis()
             )

             store.collection("Community")
                 .document(receivedCommentData.postDocumentId)
                 .collection("Comment")
                 .document(receivedCommentData.commentId)
                 .collection("ReComment")
                 .document(reCommentId)
                 .set(reCommentData)
                 .addOnSuccessListener {
                     val commentDocumentRef = store.collection("Community")
                         .document(receivedCommentData.postDocumentId)
                         .collection("Comment")
                         .document(receivedCommentData.commentId)
                     store.runTransaction { transaction ->
                         val snapshot = transaction.get(commentDocumentRef)
                         val newReCommentCount = (snapshot.getLong("reCommentCount") ?: 0) + 1
                         transaction.update(commentDocumentRef, "reCommentCount", newReCommentCount)
                         null
                     }
                     hideKeyboard()
                     binding.editTextText.text.clear()
                     adapter.reloadReComments(receivedCommentData)
                 }
                 .addOnFailureListener {
                     showToast("대댓글 추가에 실패했습니다", Toast.LENGTH_SHORT)
                 }
         } else {
             showToast("사용자 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
         }
     }*/
    private fun showBottomSheet(commentData: CommentData) {
        val layoutId = if (commentData.authorUid == FirebaseAuth.getInstance().currentUser?.uid) {
            R.layout.bottom_sheet_layout
        } else {
            R.layout.bottom_sheet_layout2
        }

        val view = layoutInflater.inflate(layoutId, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        if (layoutId == R.layout.bottom_sheet_layout) {
            val modifyTextView = view.findViewById<TextView>(R.id.bottom_sheet_modify)
            val deleteTextView = view.findViewById<TextView>(R.id.bottom_sheet_delete)

            modifyTextView.setOnClickListener {
                val intent = Intent(this, CommentModify::class.java)
                intent.putExtra("commentText", commentData.content)
                intent.putExtra("commentData", commentData)
                intent.putExtra("communityData", communityData)
                startActivity(intent)
                dialog.dismiss()
            }

            deleteTextView.setOnClickListener {
                val commentDocumentRef = firestore.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(commentData.commentId)
                commentDocumentRef.delete()
                    .addOnSuccessListener {
                        showToast("댓글이 삭제되었습니다", Toast.LENGTH_SHORT)
                        dialog.dismiss()
                        finish()
                    }
                    .addOnFailureListener {
                        showToast("댓글 삭제에 실패했습니다", Toast.LENGTH_SHORT)
                        dialog.dismiss()
                    }
            }
        } else {
            val reportTextView = view.findViewById<TextView>(R.id.bottom_sheet_report)

            reportTextView.setOnClickListener {
                val commentDocumentRef = firestore.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(commentData.commentId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(commentDocumentRef)
                    val newReportCount = snapshot.getLong("reportCount")?.plus(1) ?: 1
                    transaction.update(commentDocumentRef, "reportCount", newReportCount)

                    // 신고 횟수가 10회 이상이면 해당 댓글 삭제, 나중에 신고는 개인 당 한 번만 할 수 있게 바꿀 예정
                    if (newReportCount >= 10) {
                        transaction.delete(commentDocumentRef)
                    }
                    null
                }.addOnSuccessListener {
                    showToast("신고가 접수되었습니다", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }.addOnFailureListener {
                    showToast("신고 접수에 실패했습니다", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()

        val dimView = View(this)
        dimView.setBackgroundColor(Color.parseColor("#80000000"))
        val parentLayout = findViewById<ViewGroup>(android.R.id.content)
        parentLayout.addView(
            dimView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        dialog.setOnDismissListener {
            parentLayout.removeView(dimView)
        }
    }

    fun loadComments() {
        val receivedCommentData = intent.getParcelableExtra<CommentData>("commentData")

        firestore.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")
            .whereEqualTo("commentId", receivedCommentData?.commentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {

                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.documents.isNotEmpty()) {
                    val commentList =
                        snapshot.documents.mapNotNull { it.toObject(CommentData::class.java) }
                    adapter.submitList(commentList)
                } else {

                }
            }
    }

    private fun showReCommentBottomSheet(reCommentData: ReCommentData) {
        val layoutId = if (reCommentData.authorUid == FirebaseAuth.getInstance().currentUser?.uid) {
            R.layout.bottom_sheet_layout
        } else {
            R.layout.bottom_sheet_layout2
        }

        val view = layoutInflater.inflate(layoutId, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        if (layoutId == R.layout.bottom_sheet_layout) {
            val modifyTextView = view.findViewById<TextView>(R.id.bottom_sheet_modify)
            val deleteTextView = view.findViewById<TextView>(R.id.bottom_sheet_delete)

            modifyTextView.setOnClickListener {
                val intent = Intent(this, ReCommentModify::class.java)
                intent.putExtra("reCommentText", reCommentData.content)
                intent.putExtra("reCommentData", reCommentData)
                intent.putExtra("communityData", communityData)
                startActivity(intent)
                dialog.dismiss()
            }

            deleteTextView.setOnClickListener {
                val reCommentDocumentRef = firestore.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)
                    .collection("ReComment")
                    .document(reCommentData.reCommentId)

                // 댓글의 문서 참조
                val commentDocumentRef = firestore.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)

                firestore.runTransaction { transaction ->
                    // 댓글의 현재 reCommentCount를 가져옴
                    val commentSnapshot = transaction.get(commentDocumentRef)
                    val oldReCommentCount = commentSnapshot.getLong("reCommentCount") ?: 0

                    // reCommentCount가 0보다 클 때만 1 감소
                    if (oldReCommentCount > 0) {
                        transaction.update(
                            commentDocumentRef,
                            "reCommentCount",
                            oldReCommentCount - 1
                        )
                    }

                    // 대댓글 삭제
                    transaction.delete(reCommentDocumentRef)
                    null
                }.addOnSuccessListener {
                    showToast("대댓글이 삭제되었습니다", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }.addOnFailureListener {
                    showToast("대댓글 삭제에 실패했습니다", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }
            }
        } else {
            val reportTextView = view.findViewById<TextView>(R.id.bottom_sheet_report)

            reportTextView.setOnClickListener {
                val reCommentDocumentRef = firestore.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)
                    .collection("ReComment")
                    .document(reCommentData.reCommentId)


                val commentDocumentRef = firestore.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)

                firestore.runTransaction { transaction ->

                    val commentSnapshot = transaction.get(commentDocumentRef)
                    val oldReCommentCount = commentSnapshot.getLong("reCommentCount") ?: 0

                    val snapshot = transaction.get(reCommentDocumentRef)
                    val newReportCount = snapshot.getLong("reportCount")?.plus(1) ?: 1
                    transaction.update(reCommentDocumentRef, "reportCount", newReportCount)

                    // 신고 횟수가 10회 이상이면 해당 대댓글 삭제
                    if (newReportCount >= 10) {

                        if (oldReCommentCount > 0) {
                            transaction.update(
                                commentDocumentRef,
                                "reCommentCount",
                                oldReCommentCount - 1
                            )
                        }
                        transaction.delete(reCommentDocumentRef)
                    }
                    null
                }.addOnSuccessListener {
                    showToast("신고가 접수되었습니다", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }.addOnFailureListener {
                    showToast("신고 접수에 실패했습니다", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()

        val dimView = View(this)
        dimView.setBackgroundColor(Color.parseColor("#80000000"))
        val parentLayout = findViewById<ViewGroup>(android.R.id.content)
        parentLayout.addView(
            dimView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        dialog.setOnDismissListener {
            parentLayout.removeView(dimView)
        }
    }

    private fun likeClick(
        commentData: CommentData,
        imageView: ImageView,
        textView1: TextView,
        textView2: TextView
    ) {
        val likeUsers = commentData.likeUsers
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val commentDocumentRef = firestore.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")
            .document(commentData.commentId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(commentDocumentRef)
            val newLikeCount: Long
            val newLikeUsers: List<String>
            if (likeUsers.contains(uid)) {
                newLikeCount = (snapshot.getLong("likeCount") ?: 1) - 1
                newLikeUsers = likeUsers - uid
            } else {
                newLikeCount = (snapshot.getLong("likeCount") ?: 0) + 1
                newLikeUsers = likeUsers + uid
            }
            transaction.update(commentDocumentRef, "likeCount", newLikeCount)
            transaction.update(commentDocumentRef, "likeUsers", newLikeUsers)
            null
        }.addOnFailureListener {
            runOnUiThread { showToast("좋아요 실패했습니다", Toast.LENGTH_SHORT) }
        }
    }

    private fun reCommentLikeClick(
        reCommentData: ReCommentData,
        imageView: ImageView,
        textView1: TextView,
        textView2: TextView
    ) {
        Log.d("ReCommentActivity", "reCommentLikeClick called with $reCommentData")
        val likeUsers = reCommentData.likeUsers
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reCommentDocumentRef = firestore.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")
            .document(reCommentData.commentId)
            .collection("ReComment")
            .document(reCommentData.reCommentId)
        firestore.runTransaction { transaction ->
            Log.d("ReCommentActivity", "Running transaction...")
            val snapshot = transaction.get(reCommentDocumentRef)
            val newLikeCount: Long
            val newLikeUsers: List<String>
            if (likeUsers.contains(uid)) {
                newLikeCount = (snapshot.getLong("likeCount") ?: 1) - 1
                newLikeUsers = likeUsers - uid
            } else {
                newLikeCount = (snapshot.getLong("likeCount") ?: 0) + 1
                newLikeUsers = likeUsers + uid
            }
            transaction.update(reCommentDocumentRef, "likeCount", newLikeCount)
            transaction.update(reCommentDocumentRef, "likeUsers", newLikeUsers)
            null
        }.addOnFailureListener {
            runOnUiThread { showToast("좋아요 실패했습니다", Toast.LENGTH_SHORT) }
        }
    }

    private fun loadReCommentCount(commentData: CommentData, textView: TextView) {
        firestore.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")
            .document(commentData.commentId)
            .collection("ReComment")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val reCommentCount = (snapshot?.documents?.size ?: 0) + 1
                textView.text = reCommentCount.toString()
            }
    }


}
