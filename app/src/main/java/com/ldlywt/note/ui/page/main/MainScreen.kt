package com.ldlywt.note.ui.page.main

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ldlywt.note.ui.page.home.AllNotesPage
import com.ldlywt.note.ui.page.home.CalenderPage
import com.ldlywt.note.ui.page.settings.SettingsPage
import com.ldlywt.note.utils.isWideScreen
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val destinations = NavigationBarPath.entries
    var currentDestination by rememberSaveable { mutableStateOf(destinations[0].route) }
    val pagerState = rememberPagerState(initialPage = 0) { destinations.size }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    var hideNavBar by rememberSaveable { mutableStateOf(false) }
    val isWideScreen = remember(configuration.orientation) { isWideScreen(context) }
    
    var showInputDialog by rememberSaveable { mutableStateOf(false) }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutPadding()
        ) {
            if (!hideNavBar) {
                AdaptiveNavigationBar(
                    destinations = destinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = { index ->
                        currentDestination = destinations[index].route
                        scope.launch { pagerState.scrollToPage(index) }
                    },
                    isWideScreen = true
                )
            }
            MainPager(
                pagerState = pagerState,
                navController = navController,
                onHideNavBar = { hideNavBar = it },
                showInputDialog = showInputDialog,
                onShowInputDialogChange = { showInputDialog = it },
                modifier = Modifier.fillMaxHeight().weight(1f)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SaltTheme.colors.background)
        ) {
            MainPager(
                pagerState = pagerState,
                navController = navController,
                onHideNavBar = { hideNavBar = it },
                showInputDialog = showInputDialog,
                onShowInputDialogChange = { showInputDialog = it },
                modifier = Modifier.fillMaxSize()
            )
            if (!hideNavBar) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .navigationBarsPadding()
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.widthIn(max = 400.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdaptiveNavigationBar(
                            modifier = Modifier.weight(1f, fill = false),
                            destinations = destinations,
                            currentDestination = currentDestination,
                            onNavigateToDestination = { index ->
                                currentDestination = destinations[index].route
                                scope.launch { pagerState.scrollToPage(index) }
                            },
                            isWideScreen = false
                        )

                        // 加号按钮常驻，不隐藏
                        Surface(
                            onClick = {
                                if (currentDestination == NavigationBarPath.AllNote.route) {
                                    showInputDialog = true
                                } else {
                                    currentDestination = NavigationBarPath.AllNote.route
                                    scope.launch {
                                        pagerState.scrollToPage(0)
                                        showInputDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = SaltTheme.colors.subBackground.copy(alpha = 0.95f),
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainPager(
    pagerState: PagerState,
    navController: NavHostController,
    onHideNavBar: (Boolean) -> Unit,
    showInputDialog: Boolean,
    onShowInputDialogChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false,
        modifier = modifier
    ) { page ->
        when (page) {
            0 -> AllNotesPage(
                navController = navController, 
                hideBottomNavBar = onHideNavBar,
                externalShowInputDialog = showInputDialog,
                onExternalShowInputDialogChange = onShowInputDialogChange
            )
            1 -> CalenderPage(navController = navController)
            2 -> SettingsPage(navController = navController)
        }
    }
}

@Composable
private fun AdaptiveNavigationBar(
    destinations: List<NavigationBarPath>,
    currentDestination: String,
    onNavigateToDestination: (Int) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    if (isWideScreen) {
        NavigationRail(modifier, containerColor = SaltTheme.colors.subBackground) {
            destinations.forEachIndexed { index, destination ->
                val selected = destination.route == currentDestination
                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onNavigateToDestination(index)
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Black.copy(alpha = 0.4f),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    } else {
        Surface(
            modifier = modifier.height(64.dp).widthIn(min = 200.dp, max = 280.dp), 
            shape = RoundedCornerShape(32.dp),
            color = SaltTheme.colors.subBackground.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEachIndexed { index, destination ->
                    val selected = destination.route == currentDestination
                    val backgroundColor by animateColorAsState(
                        targetValue = if (selected) Color.Black.copy(alpha = 0.1f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 300),
                        label = "nav_bg_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onNavigateToDestination(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .widthIn(max = 76.dp)
                                .fillMaxWidth(0.8f)
                                .clip(RoundedCornerShape(21.dp))
                                .background(backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            val tint = if (selected) Color.Black else Color.Black.copy(alpha = 0.35f)
                            val icon = if (selected) destination.selectedIcon else destination.unselectedIcon
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class NavigationBarPath(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    AllNote(
        route = "Home",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    Calendar(
        route = "Calendar",
        selectedIcon = Icons.Rounded.Event,
        unselectedIcon = Icons.Outlined.Event
    ),
    Settings(
        route = "Settings",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}
