package com.kyleyarwood.goalbingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyleyarwood.goalbingo.data.ThemeMode
import com.kyleyarwood.goalbingo.ui.nav.BingoNavHost
import com.kyleyarwood.goalbingo.ui.theme.GoalBingoTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as GoalBingoApplication
        val year = Calendar.getInstance().get(Calendar.YEAR)

        setContent {
            val themeMode by app.services.settings.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val scope = rememberCoroutineScope()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            GoalBingoTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BingoNavHost(
                        repository = app.services.repository,
                        year = year,
                        themeMode = themeMode,
                        onSelectThemeMode = { mode ->
                            scope.launch { app.services.settings.setThemeMode(mode) }
                        },
                    )
                }
            }
        }
    }
}
