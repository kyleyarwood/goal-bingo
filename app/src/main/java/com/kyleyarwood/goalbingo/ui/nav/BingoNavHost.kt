package com.kyleyarwood.goalbingo.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.ThemeMode
import com.kyleyarwood.goalbingo.ui.card.CardScreen
import com.kyleyarwood.goalbingo.ui.card.CardViewModel
import com.kyleyarwood.goalbingo.ui.detail.SquareDetailScreen
import com.kyleyarwood.goalbingo.ui.detail.SquareDetailViewModel
import com.kyleyarwood.goalbingo.ui.setup.SetupScreen
import com.kyleyarwood.goalbingo.ui.setup.SetupViewModel

private object Route {
    const val CARD = "card"
    const val SETUP = "setup"
    const val SQUARE = "square/{position}"
    fun square(position: Int) = "square/$position"
}

@Composable
fun BingoNavHost(
    repository: BingoRepository,
    year: Int,
    themeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.CARD) {
        composable(Route.CARD) {
            CardScreen(
                factory = CardViewModel.Factory(repository, year),
                onSquareClick = { navController.navigate(Route.square(it)) },
                onEditCardClick = { navController.navigate(Route.SETUP) },
                themeMode = themeMode,
                onSelectThemeMode = onSelectThemeMode,
            )
        }
        composable(Route.SETUP) {
            SetupScreen(
                factory = SetupViewModel.Factory(repository, year),
                onBack = { navController.popBackStack() },
                onSquareClick = { navController.navigate(Route.square(it)) },
            )
        }
        composable(
            route = Route.SQUARE,
            arguments = listOf(navArgument("position") { type = NavType.IntType }),
        ) { entry ->
            val position = entry.arguments?.getInt("position") ?: 0
            SquareDetailScreen(
                factory = SquareDetailViewModel.Factory(repository, year, position),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
