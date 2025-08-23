package com.kanhaji.basics.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFABTemplate(
    title: String,
    showFab: Boolean = true,
    showBackIcon: Boolean = LocalNavigator.current?.canPop ?: false,
    showSettingsIcon: Boolean = true,
    content: @Composable (PaddingValues) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    MySnackBarObject.snackbarHostState = snackbarHostState

    val navigator = LocalNavigator.currentOrThrow

    // Detect scroll direction
    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
    var lastScrollOffset by remember { mutableIntStateOf(0) }
    val currentOffset = remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset + listState.firstVisibleItemIndex * 1000
        }
    }
    LaunchedEffect(currentOffset.value) {
        if (listState.isScrollInProgress) {
            fabVisible = currentOffset.value < lastScrollOffset
            lastScrollOffset = currentOffset.value
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                ),
                navigationIcon = {

                    if (showBackIcon)
                        IconButton(onClick = {
                            scope.launch {
                                navigator.pop()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                },
                actions = {
                    if (showSettingsIcon)
                        IconButton(
                            onClick = {
                                scope.launch {
                                    navigator.push(SettingsScreen)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                            )
                        }
                }
            )
        },
        snackbarHost = {
            MySnackbarHost()
        },
        floatingActionButton = {
            if (showFab) DynamicFab(fabVisible)
        },
        modifier = Modifier.fillMaxSize(),
    ) { contentPadding ->
        content(contentPadding)
    }
}

@Composable
fun DynamicFab(
    visibility: Boolean,
    icon: ImageVector = Icons.Filled.Person,
    contentDescription : String? = null,
    text: String = "Action",
    shape: Shape = FloatingActionButtonDefaults.shape,
    onClick: () -> Unit = { }
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Show text when originally visible OR when hovered and originally hidden
    val shouldShowText = visibility || isHovered

    FloatingActionButton(
        onClick = { onClick() },
        shape = shape,
        interactionSource = interactionSource,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.padding(8.dp)
            )
            AnimatedVisibility(
                visible = shouldShowText,
            ) {
                Spacer(Modifier.width(2.dp))
                Text(text = text, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}