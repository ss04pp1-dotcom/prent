package com.parentalcare.parent.nav
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.parentalcare.parent.R
import com.parentalcare.parent.ui.screen.AboutScreen
import com.parentalcare.parent.ui.screen.ActivityLogScreen
import com.parentalcare.parent.ui.screen.BiometricSettingsScreen
import com.parentalcare.parent.ui.screen.CalendarScreen
import com.parentalcare.parent.ui.screen.ChildProfileScreen
import com.parentalcare.parent.ui.screen.ConnectedDevicesScreen
import com.parentalcare.parent.ui.screen.DashboardScreen
import com.parentalcare.parent.ui.screen.DeleteConfirmScreen
import com.parentalcare.parent.ui.screen.LoginScreen
import com.parentalcare.parent.ui.screen.NotificationsSettingsScreen
import com.parentalcare.parent.ui.screen.PairingQRScreen
import com.parentalcare.parent.ui.screen.ProfileScreen
import com.parentalcare.parent.ui.screen.RequestScreenshotScreen
import com.parentalcare.parent.ui.screen.RequestsListScreen
import com.parentalcare.parent.ui.screen.ScreenshotHistoryScreen
import com.parentalcare.parent.ui.screen.ScreenshotInboxScreen
import com.parentalcare.parent.ui.screen.ScreenshotViewerScreen
import com.parentalcare.parent.ui.screen.SettingsScreen
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun ParentRootNav() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val rootRoutes = listOf(
        RootRoute.DASHBOARD,
        RootRoute.INBOX,
        RootRoute.REQUESTS,
        RootRoute.SETTINGS,
    )
    val isInRootFlow = rootRoutes.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SharedColors.DarkBg,
        bottomBar = {
            if (isInRootFlow) {
                NavigationBar(
                    containerColor = SharedColors.DarkSurfaceVariant,
                    tonalElevation = 0.dp,
                ) {
                    rootRoutes.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                nav.navigate(item.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelRes)) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = SharedColors.ParentPrimary,
                                selectedTextColor = SharedColors.ParentPrimary,
                                unselectedIconColor = SharedColors.DarkTextSecondary,
                                unselectedTextColor = SharedColors.DarkTextSecondary,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = ParentRoute.LOGIN,
            modifier = Modifier.padding(inner),
        ) {
            composable(ParentRoute.LOGIN) {
                LoginScreen(onLoginSuccess = {
                    nav.navigate(ParentRoute.DASHBOARD) {
                        popUpTo(ParentRoute.LOGIN) { inclusive = true }
                    }
                })
            }
            composable(ParentRoute.DASHBOARD) {
                DashboardScreen(
                    onChildClick = { deviceId -> nav.navigate("child_profile/$deviceId") },
                    onActivityLog = { nav.navigate(ParentRoute.ACTIVITY_LOG) },
                    onSettings = { nav.navigate(ParentRoute.SETTINGS) },
                    onMenuClick = { nav.navigate(ParentRoute.SETTINGS) },
                    onNotificationsClick = { nav.navigate(ParentRoute.NOTIF_SETTINGS) },
                )
            }
            composable(ParentRoute.CHILD_PROFILE) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
                ChildProfileScreen(
                    onBack = { nav.popBackStack() },
                    onRequestScreenshot = { nav.navigate("request_screenshot/$deviceId") },
                    onSeeAll = { nav.navigate(ParentRoute.INBOX) },
                )
            }
            composable(ParentRoute.INBOX) {
                ScreenshotInboxScreen(
                    onBack = { nav.popBackStack() },
                    onScreenshotClick = { screenshotId -> nav.navigate("viewer/$screenshotId") },
                )
            }
            composable(ParentRoute.VIEWER) { backStackEntry ->
                val screenshotId = backStackEntry.arguments?.getString("screenshotId") ?: return@composable
                ScreenshotViewerScreen(
                    onBack = { nav.popBackStack() },
                    onDelete = { nav.navigate(ParentRoute.DELETE_CONFIRM) },
                )
            }
            composable(ParentRoute.REQUEST_SCREENSHOT) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
                RequestScreenshotScreen(
                    onBack = { nav.popBackStack() },
                    onSent = { nav.popBackStack() },
                )
            }
            composable(ParentRoute.HISTORY) {
                ScreenshotHistoryScreen(
                    onBack = { nav.popBackStack() },
                    onCalendar = { nav.navigate(ParentRoute.CALENDAR) },
                )
            }
            composable(ParentRoute.CALENDAR) {
                CalendarScreen(onBack = { nav.popBackStack() })
            }
            composable(ParentRoute.DELETE_CONFIRM) {
                DeleteConfirmScreen(
                    onBack = { nav.popBackStack() },
                    onDeleted = { nav.popBackStack() },
                )
            }
            composable(ParentRoute.ACTIVITY_LOG) {
                ActivityLogScreen(onBack = { nav.popBackStack() })
            }
            composable(ParentRoute.REQUESTS) {
                RequestsListScreen(onBack = { nav.popBackStack() })
            }
            composable(ParentRoute.SETTINGS) {
                SettingsScreen(
                    onLogout = { nav.navigate(ParentRoute.LOGIN) { popUpTo(0) } },
                    onNotifications = { nav.navigate(ParentRoute.NOTIF_SETTINGS) },
                    onConnectedDevices = { nav.navigate(ParentRoute.DEVICES) },
                    onProfile = { nav.navigate(ParentRoute.PROFILE) },
                    onAbout = { nav.navigate(ParentRoute.ABOUT) },
                    onPairingQR = { nav.navigate(ParentRoute.PAIRING_QR) },
                    onBiometric = { nav.navigate(ParentRoute.BIOMETRIC) },
                )
            }
            composable(ParentRoute.NOTIF_SETTINGS) {
                NotificationsSettingsScreen(onBack = { nav.popBackStack() })
            }
            composable(ParentRoute.DEVICES) {
                ConnectedDevicesScreen(
                    onBack = { nav.popBackStack() },
                    onAdd = { nav.navigate(ParentRoute.PAIRING_QR) },
                )
            }
            composable(ParentRoute.PROFILE) {
                ProfileScreen(onBack = { nav.popBackStack() })
            }
            composable(ParentRoute.ABOUT) {
                AboutScreen(onBack = { nav.popBackStack() })
            }
            composable(ParentRoute.PAIRING_QR) {
                PairingQRScreen(onBack = { nav.popBackStack() })
            }
            composable(ParentRoute.BIOMETRIC) {
                BiometricSettingsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

object ParentRoute {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CHILD_PROFILE = "child_profile/{deviceId}"
    const val INBOX = "inbox"
    const val VIEWER = "viewer/{screenshotId}"
    const val REQUEST_SCREENSHOT = "request_screenshot/{deviceId}"
    const val HISTORY = "history"
    const val CALENDAR = "calendar"
    const val DELETE_CONFIRM = "delete_confirm"
    const val ACTIVITY_LOG = "activity_log"
    const val REQUESTS = "requests"
    const val SETTINGS = "settings"
    const val NOTIF_SETTINGS = "notif_settings"
    const val DEVICES = "devices"
    const val PROFILE = "profile"
    const val ABOUT = "about"
    const val PAIRING_QR = "pairing_qr"
    const val BIOMETRIC = "biometric"
}

private data class RootRoute(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    companion object {
        val DASHBOARD = RootRoute(ParentRoute.DASHBOARD, R.string.tab_dashboard, Icons.Outlined.Dashboard)
        val INBOX = RootRoute(ParentRoute.INBOX, R.string.tab_children, Icons.Outlined.Inbox)
        val REQUESTS = RootRoute(ParentRoute.REQUESTS, R.string.tab_requests, Icons.Outlined.Email)
        val SETTINGS = RootRoute(ParentRoute.SETTINGS, R.string.tab_settings, Icons.Outlined.Settings)
    }
}
