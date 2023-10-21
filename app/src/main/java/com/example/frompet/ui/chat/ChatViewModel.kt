package com.example.frompet.ui.chat

import androidx.lifecycle.LiveData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.frompet.data.model.User
import com.example.frompet.data.repository.chat.ChatRepository
import com.example.frompet.data.repository.chat.ChatRepositoryImpl
import kotlinx.coroutines.launch


class ChatViewModel : ViewModel() {

        private val repository: ChatRepository = ChatRepositoryImpl()
        fun chatRoom(uid1: String, uid2: String) =   repository.chatRoom(uid1, uid2)

        fun lastChatLiveData(chatRoomId: String) = repository.getLastChatLiveData(chatRoomId)

        fun loadLastChats(currentUserId: String, otherUserId: String) = viewModelScope.launch { repository.loadLastChats(currentUserId, otherUserId)}

        fun goneNewMessages(chatRoomId: String) = viewModelScope.launch {  repository.goneNewMessages(chatRoomId)}

        val newChats: LiveData<HashMap<String, Boolean>> get() = repository.loadNewChats()

        fun getLastTimeSorted(user: List<User>, onUpdate: (List<User>) -> Unit) = viewModelScope.launch {  repository.getLastTimeSorted(user, onUpdate)}
    }



