package com.travelai.ui.navigation

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelai.ui.chat.ChatScreen
import com.travelai.ui.history.HistoryScreen
import com.travelai.ui.itinerary.ItineraryScreen
import com.travelai.ui.landmark.LandmarkHistoryDetailScreen
import com.travelai.ui.landmark.LandmarkHistoryScreen
import com.travelai.ui.landmark.LandmarkScannerScreen
import com.travelai.ui.map.TripMapScreen
import com.travelai.ui.planner.TripPlannerScreen
import com.travelai.ui.profile.ProfileScreen
import com.travelai.ui.settings.SettingsScreen
import com.travelai.ui.weather.WeatherScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TravelAiRoutes.PLANNER_ROUTE,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { it / 4 }
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { -it / 4 }
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { -it / 4 }
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { it / 4 }
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(route = TravelAiRoutes.PLANNER_ROUTE) {
            TripPlannerScreen(
                onOpenChat = {
                    navController.navigate(TravelAiRoutes.CHAT_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenMap = {
                    navController.navigate(TravelAiRoutes.MAP_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenLandmarkScanner = {
                    navController.navigate(TravelAiRoutes.LANDMARK_SCANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenWeather = { destination ->
                    navController.navigate(TravelAiRoutes.weatherRoute(destination)) {
                        launchSingleTop = true
                    }
                },
                onOpenSettings = {
                    navController.navigate(TravelAiRoutes.SETTINGS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(TravelAiRoutes.PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenItinerary = { sessionId ->
                    navController.navigate(TravelAiRoutes.itineraryRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onCreateItinerary = { sessionId ->
                    navController.navigate(
                        TravelAiRoutes.chatRoute(
                            sessionId = sessionId,
                            autoStart = true
                        )
                    )
                }
            )
        }

        composable(route = TravelAiRoutes.LANDMARK_SCANNER_ROUTE) {
            LandmarkScannerScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.PLANNER_ROUTE)
                    }
                },
                onAskInChat = { draftPrompt ->
                    navController.navigate(
                        TravelAiRoutes.chatRoute(draftPrompt = draftPrompt)
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.LANDMARK_HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.LANDMARK_HISTORY_ROUTE) {
            LandmarkHistoryScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.LANDMARK_SCANNER_ROUTE)
                    }
                },
                onOpenDetail = { scanId ->
                    navController.navigate(TravelAiRoutes.landmarkHistoryDetailRoute(scanId))
                }
            )
        }

        composable(
            route = TravelAiRoutes.LANDMARK_HISTORY_DETAIL_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(TravelAiRoutes.SCAN_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            LandmarkHistoryDetailScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.LANDMARK_HISTORY_ROUTE)
                    }
                },
                onAskInChat = { draftPrompt ->
                    navController.navigate(
                        TravelAiRoutes.chatRoute(draftPrompt = draftPrompt)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.CHAT_ROUTE) {
            ChatScreen(
                onOpenPlanner = {
                    navController.navigate(TravelAiRoutes.PLANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenItinerary = { sessionId ->
                    navController.navigate(TravelAiRoutes.itineraryRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(TravelAiRoutes.PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = TravelAiRoutes.CHAT_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(TravelAiRoutes.SESSION_ID_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(TravelAiRoutes.DRAFT_PROMPT_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(TravelAiRoutes.AUTO_START_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            ChatScreen(
                onOpenPlanner = {
                    navController.navigate(TravelAiRoutes.PLANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenItinerary = { sessionId ->
                    navController.navigate(TravelAiRoutes.itineraryRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(TravelAiRoutes.PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.HISTORY_ROUTE) {
            HistoryScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.PLANNER_ROUTE)
                    }
                },
                onSessionClick = { sessionId ->
                    navController.navigate(TravelAiRoutes.chatRoute(sessionId))
                },
                onOpenItinerary = { sessionId ->
                    navController.navigate(TravelAiRoutes.itineraryRoute(sessionId))
                },
                onOpenMap = { sessionId ->
                    navController.navigate(TravelAiRoutes.mapRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(TravelAiRoutes.PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = TravelAiRoutes.ITINERARY_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(TravelAiRoutes.SESSION_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            ItineraryScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.HISTORY_ROUTE)
                    }
                },
                onOpenChat = { sessionId ->
                    navController.navigate(TravelAiRoutes.chatRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenPlanner = {
                    navController.navigate(TravelAiRoutes.PLANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenMap = { sessionId ->
                    navController.navigate(TravelAiRoutes.mapRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(TravelAiRoutes.PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.PROFILE_ROUTE) {
            ProfileScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.PLANNER_ROUTE)
                    }
                },
                onOpenSettings = {
                    navController.navigate(TravelAiRoutes.SETTINGS_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.SETTINGS_ROUTE) {
            SettingsScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.PROFILE_ROUTE)
                    }
                }
            )
        }

        composable(
            route = TravelAiRoutes.WEATHER_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(TravelAiRoutes.DESTINATION_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            WeatherScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.PLANNER_ROUTE)
                    }
                }
            )
        }

        composable(route = TravelAiRoutes.MAP_ROUTE) {
            TripMapScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.PLANNER_ROUTE)
                    }
                },
                onOpenPlanner = {
                    navController.navigate(TravelAiRoutes.PLANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenChat = { sessionId ->
                    navController.navigate(TravelAiRoutes.chatRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(TravelAiRoutes.PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = TravelAiRoutes.MAP_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(TravelAiRoutes.SESSION_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            TripMapScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(TravelAiRoutes.HISTORY_ROUTE)
                    }
                },
                onOpenPlanner = {
                    navController.navigate(TravelAiRoutes.PLANNER_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenChat = { sessionId ->
                    navController.navigate(TravelAiRoutes.chatRoute(sessionId)) {
                        launchSingleTop = true
                    }
                },
                onOpenHistory = {
                    navController.navigate(TravelAiRoutes.HISTORY_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(TravelAiRoutes.PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

object TravelAiRoutes {
    const val PLANNER_ROUTE = "planner"
    const val CHAT_ROUTE = "chat"
    const val HISTORY_ROUTE = "history"
    const val ITINERARY_ROUTE = "itinerary"
    const val MAP_ROUTE = "map"
    const val PROFILE_ROUTE = "profile"
    const val SETTINGS_ROUTE = "settings"
    const val WEATHER_ROUTE = "weather"
    const val LANDMARK_SCANNER_ROUTE = "landmark_scanner"
    const val LANDMARK_HISTORY_ROUTE = "landmark_history"
    const val LANDMARK_HISTORY_DETAIL_ROUTE = "landmark_history_detail"
    const val SESSION_ID_ARG = "sessionId"
    const val DRAFT_PROMPT_ARG = "draftPrompt"
    const val AUTO_START_ARG = "autoStart"
    const val SCAN_ID_ARG = "scanId"
    const val DESTINATION_ARG = "destination"
    const val CHAT_ROUTE_WITH_ARGS =
        "$CHAT_ROUTE?$SESSION_ID_ARG={$SESSION_ID_ARG}&$DRAFT_PROMPT_ARG={$DRAFT_PROMPT_ARG}&$AUTO_START_ARG={$AUTO_START_ARG}"
    const val ITINERARY_ROUTE_WITH_ARGS = "$ITINERARY_ROUTE/{$SESSION_ID_ARG}"
    const val MAP_ROUTE_WITH_ARGS = "$MAP_ROUTE/{$SESSION_ID_ARG}"
    const val LANDMARK_HISTORY_DETAIL_ROUTE_WITH_ARGS =
        "$LANDMARK_HISTORY_DETAIL_ROUTE/{$SCAN_ID_ARG}"
    const val WEATHER_ROUTE_WITH_ARGS = "$WEATHER_ROUTE?$DESTINATION_ARG={$DESTINATION_ARG}"

    fun chatRoute(
        sessionId: Long? = null,
        draftPrompt: String? = null,
        autoStart: Boolean = false
    ): String {
        val arguments = buildList {
            sessionId?.let { add("$SESSION_ID_ARG=$it") }
            draftPrompt
                ?.takeIf { it.isNotBlank() }
                ?.let { add("$DRAFT_PROMPT_ARG=${Uri.encode(it)}") }
            if (autoStart) {
                add("$AUTO_START_ARG=true")
            }
        }
        return if (arguments.isEmpty()) {
            CHAT_ROUTE
        } else {
            "$CHAT_ROUTE?${arguments.joinToString("&")}"
        }
    }

    fun itineraryRoute(sessionId: Long): String = "$ITINERARY_ROUTE/$sessionId"

    fun mapRoute(sessionId: Long): String = "$MAP_ROUTE/$sessionId"

    fun landmarkHistoryDetailRoute(scanId: Long): String =
        "$LANDMARK_HISTORY_DETAIL_ROUTE/$scanId"

    fun weatherRoute(destination: String? = null): String {
        val encodedDestination = destination
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::encode)
        return if (encodedDestination == null) {
            WEATHER_ROUTE
        } else {
            "$WEATHER_ROUTE?$DESTINATION_ARG=$encodedDestination"
        }
    }
}
