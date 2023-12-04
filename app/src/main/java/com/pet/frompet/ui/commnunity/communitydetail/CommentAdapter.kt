package com.pet.frompet.ui.commnunity.communitydetail

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.pet.frompet.R
import com.pet.frompet.data.model.CommentData
import com.pet.frompet.data.model.CommunityData
import com.pet.frompet.data.model.User
import com.pet.frompet.databinding.ItemReplyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.data.model.ReCommentData
import com.pet.frompet.ui.map.MapUserDetailActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class CommentAdapter(
    private val modifyClick: (CommentData) -> Unit,
    private val likeClick: (CommentData, ImageView, TextView, TextView) -> Unit,
    private val reReplyClick: (CommentData) -> Unit,
    private val reCommentModifyClick: (ReCommentData) -> Unit,
    private val reCommentLikeClick: (ReCommentData, ImageView, TextView, TextView) -> Unit
) : ListAdapter<CommentData, CommentAdapter.ViewHolder>(CommentDiffCallback) {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val commentData = getItem(position)
        holder.bindItems(commentData)


    }


    inner class ViewHolder( val binding: ItemReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(commentData: CommentData) {
            with(binding) {
                val authorUid = commentData.authorUid
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val likeUsers = commentData.likeUsers
                val likeCount = commentData.likeCount

                if (itemView.context is ReCommentActivity) {
                    textView10.visibility = View.GONE
                    btReReply.visibility = View.GONE
                    ivAddReply.visibility = View.GONE
                }

                if (likeUsers.contains(uid)) {
                    textView11.text = likeCount.toString()
                    textView11.setTextColor(Color.parseColor("#FF88C1"))
                    textView9.setTextColor(Color.parseColor("#FF88C1"))
                    ivThumbsUp.setImageResource(R.drawable.icon_sel_thumb)
                } else {
                    textView11.text = if (likeCount > 0) likeCount.toString() else ""
                    textView11.setTextColor(Color.parseColor("#000000"))
                    textView9.setTextColor(Color.parseColor("#000000"))
                    ivThumbsUp.setImageResource(R.drawable.icon_unsel_thumb)                }

                if (authorUid.isNotEmpty()) {
                    val userRef = FirebaseFirestore.getInstance().collection("User").document(authorUid)
                    userRef.get()
                        .addOnSuccessListener { userSnapshot ->
                            val user = userSnapshot.toObject(User::class.java)
                            user?.let {

                                ivPetProfile.load(user.petProfile) {
                                    error(R.drawable.sampleiamge)
                                }

                                tvPetName.text = user.petName
                                ivPetProfile.setOnClickListener {
                                    val context = it.context
                                    val intent = Intent(context, MapUserDetailActivity::class.java)
                                    intent.putExtra(MapUserDetailActivity.USER, user)
                                    context.startActivity(intent)
                                }
                            }
                        }

                }

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
                sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                val formattedDate = sdf.format(Date(commentData.timestamp))
                tvLastTime.text = formattedDate
                tvDetailContents.text = commentData.content

                threedots3.setOnClickListener {
                    modifyClick(commentData)
                }
                btThumbsUp.setOnClickListener {
                    likeClick.invoke(commentData, ivThumbsUp, textView11, textView9)
                }
                btReReply.setOnClickListener {
                    reReplyClick(commentData)
                }
                val reCommentAdapter = ReCommentAdapter(
                    modifyClick = { reCommentData ->
                        reCommentModifyClick(reCommentData)
                    },
                    likeClick = { reCommentData, imageView, textView1, textView2 ->
                        reCommentLikeClick.invoke(reCommentData, imageView, textView1, textView2)
                    }
                )
                binding.rvReReply.layoutManager = LinearLayoutManager(itemView.context)
                binding.rvReReply.adapter = reCommentAdapter

                loadReComments(commentData, reCommentAdapter, textView10)

            }
        }
    }
    fun loadReComments(commentData: CommentData, reCommentAdapter: ReCommentAdapter, textView: TextView) {
        FirebaseFirestore.getInstance()
            .collection("Community")
            .document(commentData.postDocumentId)
            .collection("Comment")
            .document(commentData.commentId)
            .collection("ReComment")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("CommentAdapter", "Failed to load reComments", error)
                    return@addSnapshotListener
                }

                val reComments = snapshot?.documents?.mapNotNull { it.toObject(ReCommentData::class.java) }
                Log.d("CommentAdapter", "ReComments for comment ${commentData.commentId}: $reComments")

                val reCommentCount = reComments?.size ?: 0
                textView.text = if (reCommentCount > 0) "답글$reCommentCount" else "답글쓰기"
                reCommentAdapter.submitList(reComments)
            }
    }

    fun reloadReComments(commentData: CommentData) {
        val position = currentList.indexOfFirst { it.commentId == commentData.commentId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }
}


    object CommentDiffCallback : DiffUtil.ItemCallback<CommentData>() {
        override fun areItemsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
            return oldItem.postDocumentId == newItem.postDocumentId
        }

        override fun areContentsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
            return oldItem == newItem
        }
    }


