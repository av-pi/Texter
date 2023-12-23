package com.example.texter.data

data class Status(
    val user: ChatUser = ChatUser(),
    val imageUrl: String? = "",
    val timestamp: Long? = null,
)