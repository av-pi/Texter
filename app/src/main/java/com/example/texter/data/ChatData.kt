package com.example.texter.data

/**
 * Data class for storing chat data
 * @param chatId unique id of the chat
 * @param userOne first user in the chat
 * @param userTwo second user in the chat
 */
data class ChatData(
    val chatId: String? = "",
    val userOne: ChatUser = ChatUser(),
    val userTwo: ChatUser = ChatUser()
)

/**
 * Data class for storing user information in a chat
 * @param userId unique id of the user
 * @param name name of the user
 * @param number phone number of the user
 * @param imageUrl url of the user's profile picture
 */
data class ChatUser(
    val userId: String? = "",
    val name: String? = "",
    val number: String? = "",
    val imageUrl: String? = ""
)

