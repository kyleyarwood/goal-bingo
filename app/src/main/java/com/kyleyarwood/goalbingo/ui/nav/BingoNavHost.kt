package com.kyleyarwood.goalbingo.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kyleyarwood.goalbingo.ui.yearsetup.YearSetupScreen
import com.kyleyarwood.goalbingo.ui.yearsetup.YearSetupViewModel
import kotlinx.coroutines.flow.first

private object Route {
    const val CARD = "card"
    const val YEAR_SETUP = "year_setup"
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
    // One-shot: on first launch this composition, jump to year setup if the year is empty.
    val initialRouteChecked = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialRouteChecked.value) {
            initialRouteChecked.value = true
            val card = repository.observeCard(year).first()
            if (card.squares.all { it.goal == null }) {
                navController.navigate(Route.YEAR_SETUP)
            }
        }
    }

    NavHost(navController = navController, startDestination = Route.CARD) {
        composable(Route.CARD) {
            CardScreen(
                factory = CardViewModel.Factory(repository, year),
                onSquareClick = { navController.navigate(Route.square(it)) },
                onEditCardClick = { navController.navigate(Route.YEAR_SETUP) },
                themeMode = themeMode,
                onSelectThemeMode = onSelectThemeMode,
            )
        }
        composable(Route.YEAR_SETUP) {
            YearSetupScreen(
                factory = YearSetupViewModel.Factory(repository, year),
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack(Route.CARD, inclusive = false)
                },
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
