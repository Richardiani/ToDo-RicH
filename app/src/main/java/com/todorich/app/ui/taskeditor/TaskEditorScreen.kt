package com.todorich.app.ui.taskeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.todorich.app.data.entity.TaskEntity
import com.todorich.app.domain.TaskPriority
import com.todorich.app.ui.viewmodel.ToDoViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    navController: NavController,
    viewModel: ToDoViewModel,
    taskId: Long
) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val task = allTasks.firstOrNull { it.id == taskId }

    var title by remember(task?.id) { mutableStateOf(task?.title ?: "") }
    var description by remember(task?.id) { mutableStateOf(task?.description ?: "") }
    var priority by remember(task?.id) { mutableStateOf(TaskPriority.fromValue(task?.priority ?: TaskPriority.MEDIUM.value)) }
    var selectedCategoryId by remember(task?.id) { mutableStateOf(task?.categoryId) }
    var dueDate by remember(task?.id) { mutableStateOf(task?.dueDate ?: "") }
    var dueTime by remember(task?.id) { mutableStateOf(task?.dueTime ?: "") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = task?.dueDate?.let {
            runCatching {
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }.getOrNull()
        } ?: System.currentTimeMillis()
    )

    val timePickerState = rememberTimePickerState(
        initialHour = task?.dueTime?.let {
            runCatching { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")).hour }.getOrNull() ?: 9
        } ?: 9,
        initialMinute = task?.dueTime?.let {
            runCatching { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")).minute }.getOrNull() ?: 0
        } ?: 0,
        is24Hour = true
    )

    LaunchedEffect(task) {
        if (task != null) {
            title = task.title
            description = task.description
            priority = TaskPriority.fromValue(task.priority)
            selectedCategoryId = task.categoryId
            dueDate = task.dueDate ?: ""
            dueTime = task.dueTime ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == -1L) "New task" else "Edit task") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = { if (titleError != null) Text(titleError ?: "") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Priority")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.entries.forEach { taskPriority ->
                    OutlinedButton(
                        onClick = { priority = taskPriority },
                        modifier = Modifier.weight(1f),
                        enabled = true
                    ) {
                        Text(taskPriority.label)
                    }
                }
            }

            Box {
                OutlinedButton(
                    onClick = { categoryMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Select category")
                }
                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("No category") },
                        onClick = {
                            selectedCategoryId = null
                            categoryMenuExpanded = false
                        }
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.id
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Text(if (dueDate.isBlank()) "Due date" else dueDate)
                    }
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Text(if (dueTime.isBlank()) "Due time" else dueTime)
                    }
                }
            }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = "Title is required."
                        return@Button
                    }

                    val existingTask = task ?: TaskEntity(
                        title = title.trim(),
                        description = description.trim(),
                        isCompleted = false,
                        priority = priority.value,
                        categoryId = selectedCategoryId,
                        dueDate = dueDate.ifBlank { null },
                        dueTime = dueTime.ifBlank { null },
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    val taskToSave = existingTask.copy(
                        title = title.trim(),
                        description = description.trim(),
                        priority = priority.value,
                        categoryId = selectedCategoryId,
                        dueDate = dueDate.ifBlank { null },
                        dueTime = dueTime.ifBlank { null },
                        updatedAt = System.currentTimeMillis()
                    )

                    viewModel.saveTask(taskToSave)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (taskId == -1L) "Create task" else "Save changes")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        dueDate = date?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: dueDate
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
