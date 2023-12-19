package com.pet.frompet.ui.commnunity.communitydetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.pet.frompet.R
import com.pet.frompet.data.model.CommentData
import com.pet.frompet.data.model.CommunityData
import com.pet.frompet.data.model.User
import com.pet.frompet.databinding.ActivityCommunityDetailBinding

import com.pet.frompet.ui.commnunity.community.CommunityViewModel
import com.pet.frompet.util.getAddressGeocoder

import com.pet.frompet.util.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pet.frompet.data.model.ReCommentData
import com.pet.frompet.ui.chat.activity.ChatPullScreenActivity
import com.pet.frompet.ui.map.MapUserDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommunityDetailActivity : AppCompatActivity() { //댓글 작성할수 있는 화면

    private var _binding: ActivityCommunityDetailBinding? = null
    private val binding get() = _binding!!

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val communityViewModel: CommunityViewModel by viewModels()



    private val store = FirebaseFirestore.getInstance()
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }



    private var communityData: CommunityData? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommentAdapter
    private lateinit var replyCountTextView: TextView



    companion object {
        const val COMMUNITY_DATA = "communityData"
        const val DOCS_ID = "docsId"
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCommunityDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        recyclerView = binding.rvReply
        adapter = CommentAdapter({ commentData ->
            showBottomSheet(commentData)
        }, { commentData, imageView, textView1, textView2 ->
            likeClick(commentData, imageView, textView1, textView2)
        },{ commentData ->
            reReplyClick(commentData)
        }, reCommentModifyClick = { reCommentData ->
            showReCommentBottomSheet(reCommentData)
        },reCommentLikeClick = { reCommentData, imageView, textView1, textView2 ->
            reCommentLikeClick(reCommentData, imageView, textView1, textView2)
        })


        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        communityData = intent.getParcelableExtra(DOCS_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            communityData = intent.getParcelableExtra(COMMUNITY_DATA, CommunityData::class.java)
        } else {
            communityData = intent.extras?.getParcelable(COMMUNITY_DATA) as CommunityData?
        }





        communityData = intent.getParcelableExtra(DOCS_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            communityData = intent.getParcelableExtra(COMMUNITY_DATA, CommunityData::class.java)

        } else {
            communityData = intent.extras?.getParcelable(COMMUNITY_DATA) as CommunityData?

        }


        // 화면에 표시
        val title = binding.tvDetailTitle
        val contents = binding.tvDetailContents
        val tag = binding.chipTag
        val lastTime = binding.tvLastTime
        replyCountTextView = binding.tvReplyCount
        val address =binding.tvAddress
        val image = binding.ivGoGalley




        // CommunityData에서 가져오기
        communityData?.let {
            title.text = it.title
            contents.text = it.contents
            tag.text = it.tag
            lastTime.text = formatDate(it.timestamp)
            fetchUserLocation(this, communityData!!.uid) { latitude, longitude ->
                val addressText = getAddressGeocoder(this, latitude, longitude)
                address.text = addressText
            }
            if (it.imageUrl.isNullOrEmpty()) {
                image.isVisible = false

            } else {
                image.isVisible = true
                image.load(it.imageUrl)
                          }
            loadUserData(it.uid)
            setChipColor(it.tag)
            Log.d("tag","what is tag${it.tag}")
        }
        image.setOnClickListener {
            val intent = Intent(this,ChatPullScreenActivity::class.java)
            intent.putExtra(ChatPullScreenActivity.IMAGE_URL,communityData?.imageUrl)
            startActivity(intent)
        }



        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.threedots.setOnClickListener {
            showPopup(it, communityData?.docsId)
        }


        binding.btnDetailEnroll.setOnClickListener {
            addComment()
        }
        loadComments()

    }
    fun fetchUserLocation(context: Context, uid: String, onLocationFetched: (Double, Double) -> Unit) {
        val locationRef = FirebaseDatabase.getInstance().getReference("location/$uid")
        locationRef.get().addOnSuccessListener { dataSnapshot ->
            val latitude = dataSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
            val longitude = dataSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
            onLocationFetched(latitude, longitude)
        }.addOnFailureListener {
        }
    }


    private fun setChipColor(tag: String) {
        val chipColor = when (tag) {
            "나눔" -> R.color.colorTagShare
            "사랑" -> R.color.colorTagLove
            "산책" -> R.color.colorTagWalk
            "정보교환" -> R.color.colorTagExchange
            else -> R.color.dark_gray
        }
        binding.chipTag.chipBackgroundColor = getColorStateList(chipColor)
    }



    private fun loadUserData(uid: String) = with(binding) {
        store.collection("User").document(uid)
            .get()
            .addOnSuccessListener { docsSnapshot ->
                val user = docsSnapshot.toObject(User::class.java)
                user?.let {
                    ivPetProfile.load(user.petProfile) {
                        error(R.drawable.sampleiamge)
                    }
                    tvPetName.text = user.petName
                }
                ivPetProfile.setOnClickListener {
                    val intent = Intent(this@CommunityDetailActivity,MapUserDetailActivity::class.java)
                    intent.putExtra(MapUserDetailActivity.USER,user)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                showToast("사용자 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
            }
    }

    private fun formatDate(timestamp: Long?): String {
        timestamp ?: return "알 수 없음"
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            days == 1L -> "어제"
            else -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    private fun showPopup(v: View, docsId: String?) {
        val currentUseruid = currentUser?.uid?:""


        if (communityData?.uid == currentUseruid) {
            val popup = PopupMenu(this, v)
            menuInflater.inflate(R.menu.popup_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delete -> {

                        deleteCommunity(docsId)
                        true
                    }
                    R.id.cut -> {
                        updateActivity()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        } else {
            showToast(getString(R.string.commu_no_accecs), Toast.LENGTH_SHORT)
        }
    }



    private fun updateActivity() {
        val intent: Intent = Intent(this, CommunityDetailUpdateActivity::class.java)
        intent.putExtra(COMMUNITY_DATA, communityData)
        startActivity(intent)
        finish()
    }

    private fun deleteCommunity(docsId: String?) {
        docsId?.let {
            communityViewModel.deleteCommunityData(it)
            observeDeleteStatus()
        }
    }

    private fun observeDeleteStatus() {
        communityViewModel.deleteResult.observe(this) { isSuccess ->
            if (isSuccess) {
                showToast("게시물이 삭제되었습니다.", Toast.LENGTH_SHORT)
                setResult(Activity.RESULT_OK, Intent().apply { putExtra(DOCS_ID, communityData?.docsId) })
                finish()
            } else {
                showToast("게시물 삭제권한이 없습니다.", Toast.LENGTH_SHORT)
            }
        }
    }


    private fun addComment() {
        val etComments = binding.etDetailComments
        val commentText = etComments.text.toString()
        val commentId = store.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")
            .document().id

        if (commentText.isNotEmpty()) {
            val uid = currentUser?.uid
            if (uid != null) {

                store.collection("User")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val user = userSnapshot.toObject(User::class.java)
                        if (user != null) {
                            val commentData = CommentData(
                                commentId,
                                content = commentText,
                                authorUid = currentUser?.uid ?: "",
                                postDocumentId = communityData?.docsId ?: "",
                                timestamp = System.currentTimeMillis()
                            )

                            store.collection("Community")
                                .document(communityData?.docsId ?: "")
                                .collection("Comment")
                                .document(commentId)
                                .set(commentData)
                                .addOnSuccessListener {
                                    showToast("댓글이 추가되었습니다", Toast.LENGTH_SHORT)
                                    etComments.text.clear()
                                    hideKeyboard()
                                }
                                .addOnFailureListener {
                                    showToast("댓글 추가에 실패했습니다", Toast.LENGTH_SHORT)
                                }
                        } else {
                            showToast("사용자 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
                        }
                    }
                    .addOnFailureListener {
                        showToast("사용자 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
                    }
            }
        } else {
            showToast("댓글 내용을 입력하세요", Toast.LENGTH_SHORT)
        }
    }



    private fun loadComments() {
        val commentsRef = store.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")

        val query = commentsRef.orderBy("timestamp", Query.Direction.ASCENDING)

        query.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            querySnapshot?.let {
                val comments = it.toObjects(CommentData::class.java)
                adapter.submitList(comments)
                val totalCommentCount = comments.sumBy { it.reCommentCount ?: 0 } + comments.size
                replyCountTextView.text = totalCommentCount.toString()
            }
        }
    }
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
                val commentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(commentData.commentId)
                commentDocumentRef.delete()
                    .addOnSuccessListener {
                        showToast("댓글이 삭제되었습니다", Toast.LENGTH_SHORT)
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        showToast("댓글 삭제에 실패했습니다", Toast.LENGTH_SHORT)
                        dialog.dismiss()
                    }
            }
        } else {
            val reportTextView = view.findViewById<TextView>(R.id.bottom_sheet_report)

            reportTextView.setOnClickListener {
                val commentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(commentData.commentId)
                store.runTransaction { transaction ->
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
        parentLayout.addView(dimView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        dialog.setOnDismissListener {
            parentLayout.removeView(dimView)
        }
    }

    private fun likeClick(commentData: CommentData, imageView: ImageView, textView1: TextView, textView2: TextView) {
        val likeUsers = commentData.likeUsers
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val commentDocumentRef = store.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")
            .document(commentData.commentId)
        store.runTransaction { transaction ->
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
    private fun reReplyClick(commentData: CommentData) {
        val intent = Intent(this, ReCommentActivity::class.java)

        intent.putExtra("commentData", commentData)
        intent.putExtra("communityData", communityData)

        startActivity(intent)
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
                val reCommentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)
                    .collection("ReComment")
                    .document(reCommentData.reCommentId)
                val commentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)
                store.runTransaction { transaction ->
                    val commentSnapshot = transaction.get(commentDocumentRef)
                    val oldReCommentCount = commentSnapshot.getLong("reCommentCount") ?: 0
                    if (oldReCommentCount > 0) {
                        transaction.update(commentDocumentRef, "reCommentCount", oldReCommentCount - 1)
                    }
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

        }else {
            val reportTextView = view.findViewById<TextView>(R.id.bottom_sheet_report)

            reportTextView.setOnClickListener {
                val reCommentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)
                    .collection("ReComment")
                    .document(reCommentData.reCommentId)

                val commentDocumentRef = store.collection("Community")
                    .document(communityData?.docsId ?: "")
                    .collection("Comment")
                    .document(reCommentData.commentId)

                store.runTransaction { transaction ->
                    val commentSnapshot = transaction.get(commentDocumentRef)
                    val oldReCommentCount = commentSnapshot.getLong("reCommentCount") ?: 0

                    val snapshot = transaction.get(reCommentDocumentRef)
                    val newReportCount = snapshot.getLong("reportCount")?.plus(1) ?: 1
                    transaction.update(reCommentDocumentRef, "reportCount", newReportCount)

                    // 신고 횟수가 10회 이상이면 해당 대댓글 삭제
                    if (newReportCount >= 10) {
                        if (oldReCommentCount > 0) {
                            transaction.update(commentDocumentRef, "reCommentCount", oldReCommentCount - 1)
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
        parentLayout.addView(dimView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        dialog.setOnDismissListener {
            parentLayout.removeView(dimView)
        }
    }
    private fun reCommentLikeClick(reCommentData: ReCommentData, imageView: ImageView, textView1: TextView, textView2: TextView) {
        val likeUsers = reCommentData.likeUsers
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reCommentDocumentRef = store.collection("Community")
            .document(communityData?.docsId ?: "")
            .collection("Comment")
            .document(reCommentData.commentId)
            .collection("ReComment")
            .document(reCommentData.reCommentId)
        store.runTransaction { transaction ->
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



}