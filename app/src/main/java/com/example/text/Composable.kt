package com.example.text

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

var drawerState = DrawerState(DrawerValue.Open)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarUi() {
    val scope = rememberCoroutineScope()
    CenterAlignedTopAppBar(title = {
        Text(
            text = "Gem AI",
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )
    },
        navigationIcon = {
            Icon(
                modifier = Modifier.clickable {
                    Log.d("Button clicked", "icon button")
                },
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
        },
        actions = {
            IconButton(onClick = {

                scope.launch {
                    drawerState.open()
                    Log.d("Button clicked", "icon button")
                }
            }) {
                Icon(modifier = Modifier.clickable {
                    Log.d("Button clicked", "icon button")
                    scope.launch {
                        drawerState.open()
                        Log.d("Button clicked", "icon button")
                    }
                }, imageVector = Icons.Default.MoreVert, contentDescription = "More vert")
            }

        }
    )
}

@Composable
fun NavigationDrawer() {
    ModalNavigationDrawer(drawerContent = {
        ModalDrawerSheet {
            NavigationDrawerItem(
                label = { Text(text = "Under Development") },
                selected = false,
                onClick = { /*TODO*/ })
        }
    }, drawerState = drawerState) {

    }
}

@Composable
fun BottomAppBarUI() {
    BottomAppBar() {
        TextField(value = "Nothing", onValueChange = {})
    }
}

