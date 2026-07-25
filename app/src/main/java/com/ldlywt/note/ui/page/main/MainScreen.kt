package com.ldlywt.note.ui.page.main

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ldlywt.note.ui.page.home.AllNotePage
import com.ldlywt.note.ui.page.home.CalenderPage
import com.ldlywt.note.ui.page.settings.HeatContent
import com.ldlywt.note.ui.page.settings.SettingsHeadLayout
import com.ldlywt.note.ui.page.settings.SettingsPage
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val destinations = NavigationBarPath.entries
    var currentDestination by rememberSaveable { mutableStateOf(destinations[0].route) }
    val pagerState = rememberPagerState(initialPage = 0) { destinations.size }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val view = LocalView.current

    var showInputDialog by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(340.dp),
                drawerContainerColor = SaltTheme.colors.background,
                drawerContentColor = SaltTheme.colors.text
            ) {
                Spacer(Modifier.height(24.dp))
                HeatContent()
                Spacer(Modifier.height(12.dp))
                SettingsHeadLayout()
                Spacer(Modifier.height(12.dp))
                destinations.forEachIndexed { index, destination ->
                    val selected = destination.route == currentDestination
                    NavigationDrawerItem(
                        label = { Text(destination.route) },
                        selected = selected,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            currentDestination = destinations[index].route
                            scope.launch {
                                pagerState.scrollToPage(index)
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = SaltTheme.colors.highlight.copy(alpha = 0.1f),
                            selectedIconColor = SaltTheme.colors.highlight,
                            selectedTextColor = SaltTheme.colors.highlight,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = SaltTheme.colors.subText,
                            unselectedTextColor = SaltTheme.colors.subText
                        )
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SaltTheme.colors.background)
                .displayCutoutPadding()
        ) {
            MainPager(
                pagerState = pagerState,
                navController = navController,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                showInputDialog = showInputDialog,
                onShowInputDialogChange = { showInputDialog = it },
                modifier = Modifier.fillMaxSize()
            )

            // 加号按钮常驻
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
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 32.dp)
                    .size(64.dp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainPager(
    pagerState: PagerState,
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
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
            0 -> AllNotePage(
                navController = navController,
                onOpenDrawer = onOpenDrawer,
                externalShowInputDialog = showInputDialog,
                onExternalShowInputDialogChange = onShowInputDialogChange
            )

            1 -> CalenderPage(navController = navController, onOpenDrawer = onOpenDrawer)
            2 -> SettingsPage(navController = navController, onOpenDrawer = onOpenDrawer)
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
