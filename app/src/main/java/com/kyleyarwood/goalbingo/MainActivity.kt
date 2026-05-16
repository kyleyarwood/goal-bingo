package com.kyleyarwood.goalbingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kyleyarwood.goalbingo.ui.nav.BingoNavHost
import com.kyleyarwood.goalbingo.ui.theme.GoalBingoTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as GoalBingoApplication
        val year = Calendar.getInstance().get(Calendar.YEAR)

        setContent {
            GoalBingoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BingoNavHost(repository = app.services.repository, year = year)
                }
            }
        }
    }
}
