package com.parentalcare.child.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.parentalcare.child.R
import com.parentalcare.child.ui.screen.AboutScreen
import com.parentalcare.child.ui.screen.CaptureScreen
import com.parentalcare.child.ui.screen.MonitoringStatusScreen
import com.parentalcare.child.ui.screen.PairingSuccessScreen
import com.parentalcare.child.ui.screen.PairingScreen
import com.parentalcare.child.ui.screen.PermissionInfoScreen
import com.parentalcare.child.ui.screen.RequestHistoryScreen
import com.parentalcare.child.ui.screen.ScreenCapturePermissionScreen
import com.parentalcare.child.ui.screen.ScreenshotSentScreen
import com.parentalcare.child.ui.screen.SettingsScreen
import com.parentalcare.child.ui.screen.WelcomeScreen
import com.parentalcare.child.ui.screen.RequestScreen
import com.parentalcare.child.pipeline.IncomingRequestHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.parentalcare.core.design.theme.SharedColors

@Composable
fun ChildRootNav() {
    val context = LocalContext.current
    val incomingRequestHandler = EntryPointAccessors.fromApplication(
        context,
        ChildNavEntryPoint::class.java
    ).incomingRequestHandler()
    val activeRequest by incomingRequestHandler.active.collectAsState()
    
    val nav = rememberNavController()

    LaunchedEffect(activeRequest) {
        if (activeRequest != null) {
            nav.navigate(ChildRoute.REQUEST_INCOMING)
        }
    }
    
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val rootRoutes = listOf(
        RootRoute.HOME,
        RootRoute.REQUESTS,
        RootRoute.SETTINGS,
    )
    val isInRootFlow = rootRoutes.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F6FA),
        bottomBar = {
            if (isInRootFlow) {
                NavigationBar(
                    containerColor = Color(0xFFFFFFFF),
                    tonalElevation = 1.dp,
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
                            icon = {
                                Icon(imageVector = item.icon, contentDescription = null)
                            },
                            label = { Text(stringResource(item.labelRes)) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = SharedColors.ChildPrimary,
                                selectedTextColor = SharedColors.ChildPrimary,
                                unselectedIconColor = SharedColors.LightTextSecondary,
                                unselectedTextColor = SharedColors.LightTextSecondary,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = ChildRoute.WELCOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ChildRoute.WELCOME) {
                WelcomeScreen(onGetStarted = { nav.navigate(ChildRoute.PERMISSION_INFO) })
            }
            composable(ChildRoute.PERMISSION_INFO) {
                PermissionInfoScreen(
                    onContinue = { nav.navigate(ChildRoute.PAIRING) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(ChildRoute.PAIRING) {
                PairingScreen(
                    onPaired = { nav.navigate(ChildRoute.PAIRING_SUCCESS) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(ChildRoute.PAIRING_SUCCESS) {
                PairingSuccessScreen(
                    onDone = {
                        nav.navigate(ChildRoute.SCREEN_CAPTURE_PERM) {
                            popUpTo(ChildRoute.WELCOME) { inclusive = false }
                        }
                    },
                )
            }
            composable(ChildRoute.SCREEN_CAPTURE_PERM) {
                ScreenCapturePermissionScreen(
                    onContinue = {
                        nav.navigate(ChildRoute.MONITORING_HOME) {
                            popUpTo(ChildRoute.WELCOME) { inclusive = true }
                        }
                    },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(ChildRoute.MONITORING_HOME) {
                MonitoringStatusScreen(
                    onStopMonitoring = { /* TODO: Stop monitoring flow */ },
                    onSeeHistory = { nav.navigate(ChildRoute.HISTORY) },
                    onSeeAbout = { nav.navigate(ChildRoute.ABOUT) },
                )
            }
            composable(ChildRoute.HISTORY) {
                RequestHistoryScreen(onBack = { nav.popBackStack() })
            }
            composable(ChildRoute.REQUEST_INCOMING) {
                RequestScreen(
                    onTake = { nav.navigate(ChildRoute.CAPTURE) },
                    onCancel = { nav.popBackStack() },
                )
            }
            composable(ChildRoute.CAPTURE) {
                CaptureScreen(onCaptured = { nav.navigate(ChildRoute.SENT) { popUpTo(ChildRoute.CAPTURE) { inclusive = true } } })
            }
            composable(ChildRoute.SENT) {
                ScreenshotSentScreen(onOk = {
                    nav.navigate(ChildRoute.MONITORING_HOME) { popUpTo(ChildRoute.MONITORING_HOME) { inclusive = true } }
                })
            }
            composable(ChildRoute.SETTINGS) {
                SettingsScreen(
                    onAbout = { nav.navigate(ChildRoute.ABOUT) },
                    onClearData = { nav.navigate(ChildRoute.WELCOME) { popUpTo(0) } }
                )
            }
            composable(ChildRoute.ABOUT) {
                AboutScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

object ChildRoute {
    const val WELCOME = "welcome"
    const val PERMISSION_INFO = "perm_info"
    const val PAIRING = "pairing"
    const val PAIRING_SUCCESS = "pairing_success"
    const val SCREEN_CAPTURE_PERM = "screen_capture_perm"
    const val MONITORING_HOME = "monitoring_home"
    const val HISTORY = "history"
    const val REQUEST_INCOMING = "request_incoming"
    const val CAPTURE = "capture"
    const val SENT = "sent"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

private data class RootRoute(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    companion object {
        val HOME = RootRoute(ChildRoute.MONITORING_HOME, R.string.tab_home, Icons.Outlined.House)
        val REQUESTS = RootRoute(ChildRoute.HISTORY, R.string.tab_requests, Icons.Outlined.Image)
        val SETTINGS = RootRoute(ChildRoute.SETTINGS, R.string.tab_settings, Icons.Outlined.Settings)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChildNavEntryPoint {
    fun incomingRequestHandler(): IncomingRequestHandler
}
