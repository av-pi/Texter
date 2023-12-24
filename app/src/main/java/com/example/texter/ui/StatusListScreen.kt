package com.example.texter.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.texter.DestinationScreen
import com.example.texter.TexterDivider
import com.example.texter.TexterListItem
import com.example.texter.TexterProgressSpinner
import com.example.texter.TexterViewModel
import com.example.texter.TitleText
import com.example.texter.navigateTo

@Composable
fun StatusListScreen(
    navController: NavController,
    viewModel: TexterViewModel
) {
    val inProgress = viewModel.inProgressStatus.value

    if (inProgress) {
        TexterProgressSpinner()
    } else {
        val userData = viewModel.userData.value
        val statuses = viewModel.status.value

        val myStatuses = statuses.filter {
            it.user.userId == userData?.userId
        }

        val otherStatuses = statuses.filter {
            it.user.userId != userData?.userId
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                viewModel.uploadStatus(uri)
            }

        }

        Scaffold(
            floatingActionButton = {
                FAB {
                    launcher.launch("image/*")
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TitleText(title = "Statuses")

                    if (statuses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No statuses available")
                        }
                    } else {
                        if (myStatuses.isNotEmpty()) {
                            TexterListItem(
                                imageUrl = myStatuses[0].user.imageUrl,
                                name = myStatuses[0].user.name
                            ) {
                                navigateTo(
                                    navController,
                                    DestinationScreen.SingleStatus.createRoute(myStatuses[0].user.userId)
                                )
                            }

                            TexterDivider()
                        }

                        // Multiple users may have multiple statuses so retrieve all distinct users
                        val uniqueUsers = otherStatuses.map { it.user }.toSet().toList()

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(uniqueUsers) { user ->
                                TexterListItem(imageUrl = user.imageUrl, name = user.name) {
                                    navigateTo(
                                        navController,
                                        DestinationScreen.SingleStatus.createRoute(user.userId )
                                    )
                                }
                            }
                        }


                    }

                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.STATUSLIST,
                        navController = navController
                    )
                }
            }
        )
    }


}

@Composable
fun FAB(onFabClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    )
    {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Edit",
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}