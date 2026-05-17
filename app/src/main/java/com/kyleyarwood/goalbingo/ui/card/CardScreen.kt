package com.kyleyarwood.goalbingo.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyleyarwood.goalbingo.R
import com.kyleyarwood.goalbingo.data.BingoCard
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.Square
import com.kyleyarwood.goalbingo.data.StreakCadence
import com.kyleyarwood.goalbingo.data.StreakStatus
import com.kyleyarwood.goalbingo.data.ThemeMode
import com.kyleyarwood.goalbingo.data.statusOn
import java.time.LocalDate
import java.time.temporal.WeekFields
import com.kyleyarwood.goalbingo.ui.theme.BingoOrangeDark
import com.kyleyarwood.goalbingo.ui.theme.BingoOrangeLight
import com.kyleyarwood.goalbingo.ui.theme.CompletedGreenDark
import com.kyleyarwood.goalbingo.ui.theme.CompletedGreenLight
import com.kyleyarwood.goalbingo.ui.theme.LocalIsDarkTheme
import com.kyleyarwood.goalbingo.ui.theme.OnBingoOrangeDark
import com.kyleyarwood.goalbingo.ui.theme.OnBingoOrangeLight
import com.kyleyarwood.goalbingo.ui.theme.OnCompletedGreenDark
import com.kyleyarwood.goalbingo.ui.theme.OnCompletedGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardScreen(
    factory: CardViewModel.Factory,
    onSquareClick: (position: Int) -> Unit,
    onEditCardClick: () -> Unit,
    themeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
) {
    val viewModel: CardViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.card_title, viewModel.year)) },
                actions = {
                    ThemeMenu(current = themeMode, onSelect = onSelectThemeMode)
                    IconButton(onClick = onEditCardClick) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_card))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.hasBingo) {
                Text(
                    text = stringResource(R.string.bingo),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            BingoGrid(
                card = state.card,
                highlighted = state.completedPositions,
                onSquareClick = onSquareClick,
            )
        }
    }
}

@Composable
private fun ThemeMenu(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Default.Brightness6, contentDescription = stringResource(R.string.theme))
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        ThemeMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(stringResource(mode.labelRes())) },
                onClick = {
                    onSelect(mode)
                    open = false
                },
                leadingIcon = {
                    RadioButton(selected = mode == current, onClick = null)
                },
            )
        }
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

@Composable
private fun BingoGrid(
    card: BingoCard,
    highlighted: Set<Int>,
    onSquareClick: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        for (row in 0 until BingoCard.SIZE) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (col in 0 until BingoCard.SIZE) {
                    val position = row * BingoCard.SIZE + col
                    SquareCell(
                        square = card.square(position),
                        isHighlighted = position in highlighted,
                        onClick = { onSquareClick(position) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareCell(
    square: Square,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalIsDarkTheme.current
    val background = when {
        isHighlighted -> if (dark) BingoOrangeDark else BingoOrangeLight
        square.isComplete -> if (dark) CompletedGreenDark else CompletedGreenLight
        else -> scheme.surfaceVariant
    }
    val contentColor = when {
        isHighlighted -> if (dark) OnBingoOrangeDark else OnBingoOrangeLight
        square.isComplete -> if (dark) OnCompletedGreenDark else OnCompletedGreenLight
        else -> scheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        val goal = square.goal
        if (goal == null) {
            Text(
                text = "+",
                color = contentColor.copy(alpha = 0.5f),
                style = MaterialTheme.typography.titleLarge,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = goal.title,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = if (square.isComplete) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                when (goal) {
                    is Goal.Counter -> {
                        Text(
                            text = stringResource(R.string.progress_format, goal.progress, goal.target),
                            color = contentColor,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        LinearProgressIndicator(
                            progress = { (goal.progress.toFloat() / goal.target).coerceIn(0f, 1f) },
                            color = scheme.primary,
                            trackColor = Color.Transparent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    is Goal.Streak -> {
                        val today = java.time.LocalDate.now()
                        val status = goal.statusOn(today)
                        val cardFormat = when (goal.cadence) {
                            StreakCadence.MonthlyAllDays -> R.string.streak_monthly_card
                            StreakCadence.WeeklyAllDays -> R.string.streak_weekly_card
                            StreakCadence.YearlyOncePerWeek -> R.string.streak_yearly_card
                        }
                        val label = when (status) {
                            StreakStatus.Achieved -> stringResource(R.string.streak_achieved)
                            StreakStatus.NotStarted -> stringResource(cardFormat, 0, totalUnitsFor(goal.cadence, today))
                            is StreakStatus.Active -> stringResource(cardFormat, status.confirmed, status.total)
                            is StreakStatus.Broken -> stringResource(cardFormat, status.confirmed, status.total)
                        }
                        Text(
                            text = label,
                            color = contentColor,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    is Goal.Checkbox -> Unit
                }
            }
        }
    }
}

private fun totalUnitsFor(cadence: StreakCadence, today: LocalDate): Int = when (cadence) {
    StreakCadence.MonthlyAllDays -> today.lengthOfMonth()
    StreakCadence.WeeklyAllDays -> 7
    StreakCadence.YearlyOncePerWeek ->
        today.range(WeekFields.ISO.weekOfWeekBasedYear()).maximum.toInt()
}
