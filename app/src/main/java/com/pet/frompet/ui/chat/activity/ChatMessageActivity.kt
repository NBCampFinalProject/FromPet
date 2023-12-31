package com.pet.frompet.ui.chat.activity

import com.pet.frompet.ui.chat.dialog.ChatExitDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.frompet.MatchSharedViewModel
import com.pet.frompet.data.model.ChatItem
import com.pet.frompet.ui.chat.adapter.ChatMessageAdapter
import com.pet.frompet.databinding.ActivityChatMessageBinding
import com.pet.frompet.data.model.User
import com.pet.frompet.ui.chat.utils.ChatItemConverter
import com.pet.frompet.ui.chat.viewmodel.ChatViewModel
import com.pet.frompet.ui.chat.viewmodel.MessageViewModel
import com.pet.frompet.ui.setting.fcm.FCMNotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class ChatMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatMessageBinding
    private val messageViewModel: MessageViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val matchSharedViewModel: MatchSharedViewModel by viewModels()
    private val fcmViewModel: FCMNotificationViewModel by viewModels()
    private lateinit var user: User


    private lateinit var receiverId: String
    private lateinit var adapter: ChatMessageAdapter
    private val chatItemConverter = ChatItemConverter()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val database = FirebaseDatabase.getInstance().reference

    private val typingTimeoutHandler = Handler(Looper.getMainLooper())
    private val typingTimeoutRunnable = Runnable {
        messageViewModel.setTypingStatus(receiverId, false)
    }

    companion object {
        const val USER = "user"
        const val PICK_IMAGE_FROM_ALBUM = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = intent.getParcelableExtra(USER) ?: User()
        binding = ActivityChatMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        setupRecyclerView()
        initViewModels()
        handleUserIntent()
    }

    private fun initView() {
        with(binding) {
            backBtn.setOnClickListener { handleBackAction() }
            ivExit.setOnClickListener { showExitDialog() }
            ivSendImage.setOnClickListener { goGallery() }
        }
    }

    private fun handleBackAction() {
        goneNewMessage()
        finish()
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(this@ChatMessageActivity,fcmViewModel,messageViewModel,user)
        with(binding) {
            rvMessage.adapter = adapter
            val layoutManager = LinearLayoutManager(this@ChatMessageActivity)
            layoutManager.stackFromEnd = true
            rvMessage.layoutManager = layoutManager
        }
    }

    private fun initViewModels() {
        messageViewModel.chatMessages.observe(this) { messages ->
            val chatItems = chatItemConverter.convertToChatItems(messages)
            updateChatList(chatItems)
        }

        messageViewModel.isTyping.observe(this, Observer { isTyping ->
            displayTypingStatus(isTyping)
        })
    }

    private fun handleUserIntent() {
        val user: User? = intent.getParcelableExtra(USER)
        user?.let {
            receiverId = it.uid
            handleChatActions(it)
        }
    }

    private fun displayTypingStatus(isTyping: Boolean) {
        binding.tvTyping.text = if (isTyping) "입력중..." else ""
    }

    private fun updateChatList(chatItems: List<ChatItem>) {
        adapter.submitList(chatItems) {
            binding.rvMessage.post {
                binding.rvMessage.scrollToPosition(chatItems.size - 1)
            }
        }
    }

    private fun showExitDialog() {
        ChatExitDialog(this).showExitDialog {
            val user: User? = intent.getParcelableExtra(USER)
            user?.let { handleExitAction(it) }
        }
    }

    private fun handleExitAction(user: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatRoomId = messageViewModel.chatRoom(currentUserId, user.uid)
        matchSharedViewModel.removeMatchedUser(user.uid)
        chatViewModel.removeChatRoom(chatRoomId)
        finish()
    }

    private fun handleChatActions(user: User) {
        displayUserInfo(user)
        messageViewModel.checkTypingStatus(user.uid)

        val currentUserId = auth.currentUser?.uid ?: return
        val chatRoomId = messageViewModel.chatRoom(currentUserId, user.uid)
        messageViewModel.observeChatMessages(chatRoomId)
        messageViewModel.observeTypingStatus(user.uid)
        messageViewModel.observeUserProfile(user.uid)

        with(binding) {
            fun sendMessage() {
                var message = etMessage.text.toString()
                if (message.isNotEmpty()) {
                    messageViewModel.sendMessage(user.uid, message)
                    etMessage.text.clear()
                    // 메시지 길이 제한
                    if (message.length > 40) {
                        message = message.substring(0, 40) + "..."
                    }
                    firestore.collection("User").document(currentUserId).get()
                        .addOnSuccessListener { docs ->
                            val currentUserName = docs.getString("petName") ?: "알 수 없음"
                            val title = "${currentUserName}님에게 새로운 메시지"
                            fcmViewModel.sendFCMChatNotification(
                                chatRoomId,
                                auth.currentUser?.uid ?: "",
                                user.uid,
                                title,
                                message
                            )
                        }
                }
            }
            etMessage.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                    sendMessage()
                    return@setOnKeyListener true
                }
                false
            }
            ivSendBtn.setOnClickListener { sendMessage() }

            etMessage.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    handleTypingStatus(s, user)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        messageViewModel.loadPreviousMessages(chatRoomId)
    }

    private fun handleTypingStatus(s: Editable?, user: User) {
        if (s.isNullOrEmpty()) {
            messageViewModel.setTypingStatus(user.uid, false)
        } else {
            messageViewModel.setTypingStatus(user.uid, true)
            typingTimeoutHandler.removeCallbacks(typingTimeoutRunnable)
            typingTimeoutHandler.postDelayed(typingTimeoutRunnable, 5000)
        }
    }

    private fun goneNewMessage() {
        val user: User? = intent.getParcelableExtra(USER)
        user?.let {
            val currentUserId = auth.currentUser?.uid ?: return
            val chatRoomId = messageViewModel.chatRoom(currentUserId, user.uid)
            messageViewModel.goneNewMessages(chatRoomId)
        }
    }

    private fun goGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, PICK_IMAGE_FROM_ALBUM)
    }

    private fun displayUserInfo(user: User) {
        binding.tvFriendName.text = user.petName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            handleImageSelection(data)
        }
    }

    private fun handleImageSelection(data: Intent?) {
        val photoUri = data?.data
        photoUri?.let {
            val user: User? = intent.getParcelableExtra(USER)
            user?.let { selectedUser ->
                messageViewModel.uploadImage(it, selectedUser)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val user: User? = intent.getParcelableExtra(USER)
        user?.let {
            val currentUserId = auth.currentUser?.uid ?: return
            val chatRoomId = messageViewModel.chatRoom(currentUserId, it.uid)
            messageViewModel.updateUserChatStatus(chatRoomId, true)
        }
    }

    override fun onPause() {
        super.onPause()
        val user: User? = intent.getParcelableExtra(USER)
        user?.let {
            val currentUserId = auth.currentUser?.uid ?: return
            val chatRoomId = messageViewModel.chatRoom(currentUserId, it.uid)
            messageViewModel.updateUserChatStatus(chatRoomId, false)
        }
    }

    override fun onBackPressed() {
        handleBackAction()
    }

    override fun onDestroy() {
        goneNewMessage()
        super.onDestroy()
    }

}

