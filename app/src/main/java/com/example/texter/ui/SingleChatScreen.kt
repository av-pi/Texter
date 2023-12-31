package com.example.texter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.texter.TexterImage
import com.example.texter.TexterViewModel
import com.example.texter.data.Message

@Composable
fun SingleChatScreen(
    chatId: String,
    navController: NavController,
    viewModel: TexterViewModel
) {

    /**
     * Launches an effect that populates the chat messages when the component is mounted.
     */
    LaunchedEffect(key1 = Unit) {
        viewModel.populateChatMessages(chatId)
    }

    /**
     * Adds a back handler to the component that depopulates the chat messages when the back button is pressed.
     */
    BackHandler {
        viewModel.depopulateChatMessages()
    }

    /**
     * Retrieves the current chat from the list of chats and selects the one with the specified chat ID.
     * The current user's data is also retrieved.
     */
    val currentChat = viewModel.chats.value.first { it.chatId == chatId }
    val myUser = viewModel.userData.value
    val chatPartner = if (myUser?.userId == currentChat.userOne.userId) currentChat.userTwo else currentChat.userOne

    /**
     * A mutable state variable to hold the reply text.
     */
    var reply by rememberSaveable {
        mutableStateOf("")
    }

    /**
     * A function to be called when the reply is sent. It sends the reply text to the view model and resets the reply text.
     */
    val onSendReply = {
        viewModel.onSendMessage(chatId, reply)
        reply = ""
    }

    /**
     * Retrieves the list of chat messages from the view model.
     */
    val chatMessages = viewModel.chatMessages.value

    /**
     * A column that fills the entire screen and contains the chat header and messages.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        /**
         * A function that is called when the back button is clicked. It pops the back stack and depopulates the chat messages.
         */
        ChatHeader(name = chatPartner.name ?: "", imageUrl = chatPartner.imageUrl ?: "") {
            navController.popBackStack()
            viewModel.depopulateChatMessages()
        }

        /**
         * A column that occupies the majority of the screen and contains the messages.
         */
        Messages(
            modifier = Modifier.weight(1f),
            chatMessages = chatMessages,
            currentUserId = myUser?.userId ?: ""
        )

        /**
         * A column that contains the reply text field and the send reply button.
         */
        ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = onSendReply)
    }
}

@Composable
fun ChatHeader(
    name: String,
    imageUrl: String,
    onBackClick: () -> Unit
) {
    /**
     * Renders a chat header with the given name, image URL, and a back button that invokes the given onBackClick function
     *
     * @param name the name of the chat partner
     * @param imageUrl the URL of the image to display for the chat partner
     * @param onBackClick a function to invoke when the back button is clicked
     */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.primary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .clickable { onBackClick.invoke() },
            tint = MaterialTheme.colorScheme.onPrimary
        )

        TexterImage(
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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun Messages(
    modifier: Modifier,
    chatMessages: List<Message>,
    currentUserId: String
) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) { msg ->
            val alignment = if (msg.sentBy == currentUserId) Alignment.End
            else Alignment.Start

            val colourContainer = if (msg.sentBy == currentUserId) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary

            val colourText = if (msg.sentBy == currentUserId) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSecondary

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colourContainer)
                        .padding(12.dp),
                    color = colourText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit
) {
    /**
     * Renders a reply box with a text field for the reply, an icon button to send the reply, and a divider.
     *
     * @param reply the current reply text
     * @param onReplyChange a function to be called when the reply text is changed
     * @param onSendReply a function to be called when the reply is sent
     */
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        TexterDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = reply,
                onValueChange = onReplyChange,
                maxLines = 3,
                modifier = Modifier.weight(4f),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary
                )

            )

//            TextField(
//                value = reply,
//                onValueChange = onReplyChange,
//                maxLines = 3,
//                modifier = Modifier.weight(4f),
//                colors = TextFieldDefaults.colors(
//                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
//                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
//                    focusedContainerColor = MaterialTheme.colorScheme.primary,
//                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
//                    cursorColor = MaterialTheme.colorScheme.onPrimary
//                )
//            )

            IconButton(onClick = onSendReply, modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Outlined.Send, contentDescription = "Button to send reply", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}









































