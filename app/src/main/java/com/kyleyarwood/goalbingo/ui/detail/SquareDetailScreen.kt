package com.kyleyarwood.goalbingo.ui.detail

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyleyarwood.goalbingo.R
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.ReminderConfig
import com.kyleyarwood.goalbingo.data.Square
import java.time.LocalDate

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
                onSetReminder = viewModel::setReminder,
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
    onSetReminder: (ReminderConfig?) -> Unit,
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
                onSetReminder = onSetReminder,
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
    onSetReminder: (ReminderConfig?) -> Unit,
) {
    var editingProgress by remember { mutableStateOf(false) }
    var confirmExtra by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    Text(goal.title, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))

    when (goal) {
        is Goal.Counter -> {
            val countedToday = goal.wasIncrementedOn(today)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.progress_format, goal.progress, goal.target),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { editingProgress = true },
                )
                if (countedToday) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(stringResource(R.string.counted_today)) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onIncrement(-1) }) { Text(stringResource(R.string.minus_one)) }
                Button(onClick = {
                    if (goal.reminder != null && countedToday) {
                        confirmExtra = true
                    } else {
                        onIncrement(1)
                    }
                }) { Text(stringResource(R.string.add_one)) }
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
            if (confirmExtra) {
                AlertDialog(
                    onDismissRequest = { confirmExtra = false },
                    title = { Text(stringResource(R.string.confirm_extra_count_title)) },
                    text = { Text(stringResource(R.string.confirm_extra_count_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            onIncrement(1)
                            confirmExtra = false
                        }) { Text(stringResource(R.string.add_anyway)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmExtra = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
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
    ReminderSection(
        reminder = goal.reminder,
        onChange = onSetReminder,
    )

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
    val valid = parsed != null

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
                        reminder = initial?.reminder,
                    )
                    EditableType.Counter -> Goal.Counter(
                        title = title.trim(),
                        target = targetText.toInt().coerceAtLeast(1),
                        progress = (initial as? Goal.Counter)?.progress ?: 0,
                        reminder = initial?.reminder,
                        lastIncrementedDate = (initial as? Goal.Counter)?.lastIncrementedDate,
                    )
                }
                onSave(saved)
            },
        ) { Text(stringResource(R.string.save)) }
    }
}

@Composable
private fun ReminderSection(
    reminder: ReminderConfig?,
    onChange: (ReminderConfig?) -> Unit,
) {
    val context = LocalContext.current
    var pickerOpen by remember { mutableStateOf(false) }
    var pendingRequest by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* Outcome is reflected by hasNotificationPermission(context) below. */ }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.reminder_label), style = MaterialTheme.typography.titleLarge)

        if (reminder == null) {
            OutlinedButton(onClick = { pickerOpen = true }) {
                Text(stringResource(R.string.reminder_set))
            }
        } else {
            Text(
                text = stringResource(R.string.reminder_time_format, reminder.hour, reminder.minute),
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { pickerOpen = true }) {
                    Text(stringResource(R.string.reminder_change))
                }
                TextButton(onClick = { onChange(null) }) {
                    Text(stringResource(R.string.reminder_remove))
                }
            }
        }

        // Surface a warning + jump-to-settings if the OS won't actually deliver these.
        if (reminder != null) {
            if (!hasNotificationPermission(context)) {
                PermissionWarning(
                    message = stringResource(R.string.notification_permission_needed),
                    actionLabel = stringResource(R.string.open_settings),
                    onAction = { openAppNotificationSettings(context) },
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms(context)) {
                PermissionWarning(
                    message = stringResource(R.string.exact_alarm_permission_needed),
                    actionLabel = stringResource(R.string.open_settings),
                    onAction = { openExactAlarmSettings(context) },
                )
            }
        }
    }

    if (pickerOpen) {
        TimePickerDialog(
            initialHour = reminder?.hour ?: 9,
            initialMinute = reminder?.minute ?: 0,
            onConfirm = { hour, minute ->
                onChange(ReminderConfig(hour = hour, minute = minute))
                pickerOpen = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !hasNotificationPermission(context) && !pendingRequest
                ) {
                    pendingRequest = true
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDismiss = { pickerOpen = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        text = { TimePicker(state = state) },
    )
}

@Composable
private fun PermissionWarning(message: String, actionLabel: String, onAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
        AssistChip(onClick = onAction, label = { Text(actionLabel) })
    }
}

private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        android.content.pm.PackageManager.PERMISSION_GRANTED
}

private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val am = context.getSystemService(AlarmManager::class.java) ?: return false
    return am.canScheduleExactAlarms()
}

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        .setData("package:${context.packageName}".toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
