package com.example.texter.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.texter.DestinationScreen
import com.example.texter.TexterDivider
import com.example.texter.TexterImage
import com.example.texter.TexterProgressSpinner
import com.example.texter.TexterViewModel
import com.example.texter.navigateTo

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: TexterViewModel
) {
    val inProgress = viewModel.inProgress.value
    if (inProgress) {
        TexterProgressSpinner()
    } else {
        val userData = viewModel.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var number by rememberSaveable { mutableStateOf(userData?.number ?: "") }

        val scrollState = rememberScrollState()
        val focus = LocalFocusManager.current

        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {

            ProfileContent(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(8.dp),
                viewModel = viewModel,
                name = name,
                number = number,
                onNameChange = { name = it },
                onNumberChange = { number = it },
                onSave = {
                    focus.clearFocus(force = true)
                    viewModel.updateProfile(name, number)
                },
                onBack = {
                    focus.clearFocus(force = true)
                    navigateTo(navController, DestinationScreen.ChatList.route)
                },
                onLogout = {
                    viewModel.onLogout()
                    navigateTo(navController, DestinationScreen.Login.route)
                })


            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier,
    viewModel: TexterViewModel,
    name: String,
    number: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {

    val imageUrl = viewModel.userData.value?.imageUrl

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Back",
                modifier = Modifier.clickable { onBack.invoke() },
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Save",
                modifier = Modifier.clickable { onSave.invoke() },
                color = MaterialTheme.colorScheme.onPrimary
            )

        }

        TexterDivider()

        ProfilePicture(imageUrl = imageUrl, viewModel = viewModel)

        TexterDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                modifier = Modifier.width(100.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            TextField(
                value = name,
                onValueChange = { onNameChange(it) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Number", modifier = Modifier.width(100.dp))
            TextField(
                value = number,
                onValueChange = { onNumberChange(it) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        TexterDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onLogout.invoke() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            ) {
                Text(text = "Logout", color = MaterialTheme.colorScheme.onPrimary)
            }

        }

    }
}

@Composable
fun ProfilePicture(
    imageUrl: String?,
    viewModel: TexterViewModel
) {

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.uploadProfilePicture(uri)
            }

        }

    // Box has the minimum size of the profile picture
    Box(modifier = Modifier.height(IntrinsicSize.Min)) {

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                TexterImage(data = imageUrl)
            }

            Text(text = "Change profile picture", color = MaterialTheme.colorScheme.onPrimary)


        }
        val isLoading = viewModel.inProgress.value

        if (isLoading) TexterProgressSpinner()
    }
}