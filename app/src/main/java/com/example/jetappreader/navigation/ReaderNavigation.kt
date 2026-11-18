package com.example.jetappreader.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jetappreader.screens.details.BookDetailsScreen
import com.example.jetappreader.screens.home.ReaderHomeScreen
import com.example.jetappreader.screens.login.LoginScreen
import com.example.jetappreader.screens.search.ReaderBookSearchScreen
import com.example.jetappreader.screens.splash.ReaderSplashScreen
import com.example.jetappreader.screens.stats.ReaderStatsScreen
import com.example.jetappreader.screens.update.BookUpdateScreen

@Composable
fun ReaderNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ReaderScreens.SplashScreen.name
    ) {
        composable(ReaderScreens.SplashScreen.name) {
            ReaderSplashScreen(
                onNavigateToLogin = {
                    navController.navigate(ReaderScreens.LoginScreen.name) {
                        popUpTo(ReaderScreens.SplashScreen.name) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(ReaderScreens.ReaderHomeScreen.name) {
                        popUpTo(ReaderScreens.SplashScreen.name) { inclusive = true }
                    }
                }
            )
        }

        composable(ReaderScreens.LoginScreen.name) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(ReaderScreens.ReaderHomeScreen.name) {
                        popUpTo(ReaderScreens.LoginScreen.name) { inclusive = true }
                    }
                }
            )
        }

        composable(ReaderScreens.CreateAccountScreen.name) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(ReaderScreens.ReaderHomeScreen.name) {
                        popUpTo(ReaderScreens.CreateAccountScreen.name) { inclusive = true }
                    }
                }
            )
        }

        composable(ReaderScreens.ReaderHomeScreen.name) {
            ReaderHomeScreen(
                onSearchClick = {
                    navController.navigate(ReaderScreens.SearchScreen.name)
                },
                onStatsClick = {
                    navController.navigate(ReaderScreens.ReaderStatsScreen.name)
                },
                onLogoutClick = {
                    navController.navigate(ReaderScreens.LoginScreen.name) {
                        popUpTo(ReaderScreens.ReaderHomeScreen.name) { inclusive = true }
                    }
                },
                onBookClick = { bookId ->
                    navController.navigate("${ReaderScreens.UpdateScreen.name}/$bookId")
                }
            )
        }

        composable(ReaderScreens.SearchScreen.name) {
            ReaderBookSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onBookClick = { bookId ->
                    navController.navigate("${ReaderScreens.DetailScreen.name}/$bookId")
                }
            )
        }

        val detailRoute = "${ReaderScreens.DetailScreen.name}/{bookId}"
        composable(
            route = detailRoute,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.popBackStack(ReaderScreens.ReaderHomeScreen.name, inclusive = false)
                },
                bookId = bookId
            )
        }

        val updateRoute = "${ReaderScreens.UpdateScreen.name}/{bookId}"
        composable(
            route = updateRoute,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookUpdateScreen(
                onBackClick = { navController.popBackStack() },
                bookId = bookId
            )
        }

        composable(ReaderScreens.ReaderStatsScreen.name) {
            ReaderStatsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
