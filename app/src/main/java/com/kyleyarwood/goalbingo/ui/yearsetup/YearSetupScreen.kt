package com.kyleyarwood.goalbingo.ui.yearsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSetupScreen(
    factory: YearSetupViewModel.Factory,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: YearSetupViewModel = viewModel(factory = factory)
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val loaded by viewModel.loaded.collectAsStateWithLifecycle()
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.clear_all_title)) },
            text = { Text(stringResource(R.string.clear_all_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearConfirm = false
                }) { Text(stringResource(R.string.clear)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.year_setup_title, viewModel.year)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.clear_all))
                    }
                    IconButton(onClick = viewModel::shuffle) {
                        Icon(Icons.Default.Casino, contentDescription = stringResource(R.string.shuffle))
                    }
                },
            )
        },
    ) { padding ->
        if (!loaded) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(rows, key = { index, _ -> index }) { index, row ->
                GoalRowEditor(
                    index = index,
                    row = row,
                    onTitleChange = { viewModel.updateTitle(index, it) },
                    onTargetChange = { viewModel.updateTarget(index, it) },
                )
            }
            item {
                Button(
                    onClick = { viewModel.save(onSaved) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) { Text(stringResource(R.string.save_card)) }
            }
        }
    }
}

@Composable
private fun GoalRowEditor(
    index: Int,
    row: GoalRow,
    onTitleChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "#${index + 1}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
        )
        OutlinedTextField(
            value = row.title,
            onValueChange = onTitleChange,
            placeholder = { Text(stringResource(R.string.goal_hint, index + 1)) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = row.target,
            onValueChange = onTargetChange,
            placeholder = { Text(stringResource(R.string.target_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(90.dp),
        )
    }
}
