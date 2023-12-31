package com.pet.frompet.data.repository.chat

import androidx.lifecycle.LiveData
import com.pet.frompet.data.model.ChatMessage
import com.pet.frompet.data.model.User

interface ChatRepository {
    fun chatRoom(uid1: String, uid2: String): String

    fun getLastChatLiveData(chatRoomId: String): LiveData<ChatMessage?>

    fun loadLastChats(currentUserId: String, otherUserId: String)

    fun goneNewMessages(chatRoomId: String)

    fun loadNewChats(): LiveData<HashMap<String, Boolean>>

    fun getLastTimeSorted(user: List<User>, onUpdate: (List<User>) -> Unit)

    fun removeChatRoomData(chatRoomId: String)

}
