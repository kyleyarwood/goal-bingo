package com.kyleyarwood.goalbingo.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyleyarwood.goalbingo.R
import com.kyleyarwood.goalbingo.data.Goal

private enum class EditableType { Checkbox, Counter }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquareDetailScreen(
    factory: SquareDetailViewModel.Factory,
    onBack: () -> Unit,
) {
    val viewModel: SquareDetailViewModel = viewModel(factory = factory)
    val square by viewModel.square.collectAsStateWithLifecycle()
    val goal = square.goal

    var editing by remember { mutableStateOf(goal == null) }

    LaunchedEffect(goal) {
        if (goal == null) editing = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.square_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    if (goal != null && !editing) {
                        IconButton(onClick = { viewModel.clear() }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (editing || goal == null) {
                GoalEditor(
                    initial = goal,
                    onSave = {
                        viewModel.save(it)
                        editing = false
                    },
                    onCancel = if (goal != null) { { editing = false } } else null,
                )
            } else {
                GoalReadView(
                    goal = goal,
                    onIncrement = viewModel::increment,
                    onToggle = viewModel::toggleDone,
                    onEdit = { editing = true },
                )
            }
        }
    }
}

@Composable
private fun GoalReadView(
    goal: Goal,
    onIncrement: (Int) -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    Text(goal.title, style = MaterialTheme.typography.titleLarge)
    if (goal.description.isNotBlank()) {
        Text(goal.description, style = MaterialTheme.typography.bodyLarge)
    }
    Spacer(Modifier.height(8.dp))

    when (goal) {
        is Goal.Counter -> {
            Text(
                stringResource(R.string.progress_format, goal.progress, goal.target),
                style = MaterialTheme.typography.titleLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onIncrement(-1) }) { Text(stringResource(R.string.minus_one)) }
                Button(onClick = { onIncrement(1) }) { Text(stringResource(R.string.add_one)) }
            }
        }
        is Goal.Checkbox -> {
            Button(onClick = onToggle) {
                Text(
                    stringResource(
                        if (goal.done) R.string.mark_undone else R.string.mark_done,
                    ),
                )
            }
        }
    }

    OutlinedButton(onClick = onEdit) { Text("Edit goal") }
}

@Composable
private fun GoalEditor(
    initial: Goal?,
    onSave: (Goal) -> Unit,
    onCancel: (() -> Unit)?,
) {
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var type by remember(initial) {
        mutableStateOf(
            when (initial) {
                is Goal.Counter -> EditableType.Counter
                else -> EditableType.Checkbox
            },
        )
    }
    var targetText by remember(initial) {
        mutableStateOf(((initial as? Goal.Counter)?.target ?: 1).toString())
    }

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text(stringResource(R.string.title_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text(stringResource(R.string.description_label)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.goal_type_label), modifier = Modifier.padding(top = 8.dp))
        FilterChip(
            selected = type == EditableType.Checkbox,
            onClick = { type = EditableType.Checkbox },
            label = { Text(stringResource(R.string.goal_type_checkbox)) },
        )
        FilterChip(
            selected = type == EditableType.Counter,
            onClick = { type = EditableType.Counter },
            label = { Text(stringResource(R.string.goal_type_counter)) },
        )
    }

    if (type == EditableType.Counter) {
        OutlinedTextField(
            value = targetText,
            onValueChange = { targetText = it.filter(Char::isDigit).take(5) },
            label = { Text(stringResource(R.string.target_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (onCancel != null) {
            OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        }
        Button(
            enabled = title.isNotBlank() &&
                (type == EditableType.Checkbox || (targetText.toIntOrNull() ?: 0) > 0),
            onClick = {
                val saved: Goal = when (type) {
                    EditableType.Checkbox -> Goal.Checkbox(
                        title = title.trim(),
                        description = description.trim(),
                        done = (initial as? Goal.Checkbox)?.done ?: false,
                    )
                    EditableType.Counter -> Goal.Counter(
                        title = title.trim(),
                        description = description.trim(),
                        target = targetText.toInt().coerceAtLeast(1),
                        progress = (initial as? Goal.Counter)?.progress ?: 0,
                    )
                }
                onSave(saved)
            },
        ) { Text(stringResource(R.string.save)) }
    }
}
