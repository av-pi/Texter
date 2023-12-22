package com.example.texter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.texter.TexterDivider
import com.example.texter.TexterProfileImage
import com.example.texter.TexterViewModel

@Composable
fun SingleChatScreen(
    chatId: String,
    navController: NavController,
    viewModel: TexterViewModel
) {

    val currentChat = viewModel.chats.value.first { it.chatId == chatId }
    val myUser = viewModel.userData.value
    val chatPartner =
        if (myUser?.userId == currentChat.userOne.userId) currentChat.userTwo else currentChat.userOne

    var reply by rememberSaveable {
        mutableStateOf("")
    }

    val onSendReply = {
        viewModel.onSendMessage(chatId, reply)
        reply = ""
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ChatHeader(name = chatPartner.name ?: "", imageUrl = chatPartner.imageUrl ?: "") {
            navController.popBackStack()
            // TODO: Remove chat messages when leaving chat
        }

        Messages(modifier = Modifier.weight(1f))

        ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = onSendReply)
    }
}

@Composable
fun ChatHeader(
    name: String,
    imageUrl: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .clickable { onBackClick.invoke() })

        TexterProfileImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )

        Text(
            text = name,
            modifier = Modifier
                .padding(start = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Messages(
    modifier: Modifier
) {
    LazyColumn(modifier = modifier) {

    }
}

@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TexterDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = reply,
                onValueChange = onReplyChange,
                maxLines = 3
            )

            IconButton(onClick = onSendReply) {
                Icon(imageVector = Icons.Outlined.Send, contentDescription = "Button to send reply")
            }
        }
    }
}









































