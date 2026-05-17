package com.kyleyarwood.goalbingo.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyleyarwood.goalbingo.R
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.Square

private enum class EditableType { Checkbox, Counter }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquareDetailScreen(
    factory: SquareDetailViewModel.Factory,
    onBack: () -> Unit,
) {
    val viewModel: SquareDetailViewModel = viewModel(factory = factory)
    val square by viewModel.square.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.square_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        val loaded = square
        if (loaded == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
        } else {
            SquareDetailContent(
                square = loaded,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                onIncrement = viewModel::increment,
                onSetProgress = viewModel::setProgress,
                onToggle = viewModel::toggleDone,
                onSave = viewModel::save,
                onDelete = viewModel::clear,
            )
        }
    }
}

@Composable
private fun SquareDetailContent(
    square: Square,
    modifier: Modifier,
    onIncrement: (Int) -> Unit,
    onSetProgress: (Int) -> Unit,
    onToggle: () -> Unit,
    onSave: (Goal) -> Unit,
    onDelete: () -> Unit,
) {
    val goal = square.goal
    var editing by remember(square.position) { mutableStateOf(goal == null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (editing || goal == null) {
            GoalEditor(
                initial = goal,
                onSave = {
                    onSave(it)
                    editing = false
                },
                onCancel = if (goal != null) { { editing = false } } else null,
            )
        } else {
            GoalReadView(
                goal = goal,
                onIncrement = onIncrement,
                onSetProgress = onSetProgress,
                onToggle = onToggle,
                onEdit = { editing = true },
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun GoalReadView(
    goal: Goal,
    onIncrement: (Int) -> Unit,
    onSetProgress: (Int) -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var editingProgress by remember { mutableStateOf(false) }

    Text(goal.title, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))

    when (goal) {
        is Goal.Counter -> {
            Text(
                text = stringResource(R.string.progress_format, goal.progress, goal.target),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { editingProgress = true },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onIncrement(-1) }) { Text(stringResource(R.string.minus_one)) }
                Button(onClick = { onIncrement(1) }) { Text(stringResource(R.string.add_one)) }
            }

            if (editingProgress) {
                SetProgressDialog(
                    current = goal.progress,
                    target = goal.target,
                    onConfirm = {
                        onSetProgress(it)
                        editingProgress = false
                    },
                    onDismiss = { editingProgress = false },
                )
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

    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onEdit) { Text("Edit goal") }
        TextButton(onClick = onDelete) { Text("Delete") }
    }
}

@Composable
private fun SetProgressDialog(
    current: Int,
    target: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(current.toString()) }
    val parsed = text.toIntOrNull()
    val valid = parsed != null && parsed in 0..target

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_progress)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter(Char::isDigit).take(6) },
                label = { Text(stringResource(R.string.progress_label, target)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = !valid,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(parsed!!) },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun GoalEditor(
    initial: Goal?,
    onSave: (Goal) -> Unit,
    onCancel: (() -> Unit)?,
) {
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
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
                        done = (initial as? Goal.Checkbox)?.done ?: false,
                    )
                    EditableType.Counter -> Goal.Counter(
                        title = title.trim(),
                        target = targetText.toInt().coerceAtLeast(1),
                        progress = (initial as? Goal.Counter)?.progress ?: 0,
                    )
                }
                onSave(saved)
            },
        ) { Text(stringResource(R.string.save)) }
    }
}
