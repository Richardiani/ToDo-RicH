package com.todorich.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.todorich.app.data.entity.TaskEntity
import com.todorich.app.domain.TaskFilter
import com.todorich.app.domain.TaskPriority
import com.todorich.app.domain.TaskSort
import com.todorich.app.ui.viewmodel.TaskListItem
import com.todorich.app.ui.viewmodel.ToDoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ToDoViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var priorityMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ToDo-RicH") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("task_editor/-1") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create task")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChanged,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search tasks") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { navController.navigate("categories") },
                    label = { Text("Categories") }
                )
                AssistChip(
                    onClick = { viewModel.clearFilters() },
                    label = { Text("Clear filters") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.values().take(5).forEach { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.name.replace('_', ' ')) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    OutlinedButton(onClick = { categoryMenuExpanded = true }) {
                        Text(uiState.selectedCategoryName ?: "Category")
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All categories") },
                            onClick = {
                                viewModel.setCategoryFilter(null)
                                categoryMenuExpanded = false
                            }
                        )
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.setCategoryFilter(category.id)
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Box {
                    OutlinedButton(onClick = { priorityMenuExpanded = true }) {
                        Text(uiState.selectedPriority?.label ?: "Priority")
                    }
                    DropdownMenu(
                        expanded = priorityMenuExpanded,
                        onDismissRequest = { priorityMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All priorities") },
                            onClick = {
                                viewModel.setPriorityFilter(null)
                                priorityMenuExpanded = false
                            }
                        )
                        TaskPriority.entries.forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.label) },
                                onClick = {
                                    viewModel.setPriorityFilter(priority)
                                    priorityMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sorting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = sortMenuExpanded,
                    onExpandedChange = { sortMenuExpanded = !sortMenuExpanded }
                ) {
                    OutlinedButton(
                        modifier = Modifier.menuAnchor(),
                        onClick = { sortMenuExpanded = true }
                    ) {
                        Text(uiState.selectedSort.name.replace('_', ' '))
                    }
                    ExposedDropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        TaskSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.name.replace('_', ' ')) },
                                onClick = {
                                    viewModel.setSort(sort)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FilterList, modifier = Modifier.size(46.dp), contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No tasks match your current filters")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.tasks) { taskItem ->
                        TaskCard(
                            taskItem = taskItem,
                            onEdit = { navController.navigate("task_editor/${taskItem.task.id}") },
                            onDelete = { viewModel.deleteTask(taskItem.task.id) },
                            onToggle = { viewModel.toggleCompleted(taskItem.task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    taskItem: TaskListItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val item = taskItem.task
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit task",
                        modifier = Modifier.clickable { onEdit() }
                    )
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete task",
                        modifier = Modifier.clickable { onDelete() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val priorityText = TaskPriority.fromValue(item.priority).label
                val priorityColor = when (TaskPriority.fromValue(item.priority)) {
                    TaskPriority.LOW -> MaterialTheme.colorScheme.primary
                    TaskPriority.MEDIUM -> WarningAmber
                    TaskPriority.HIGH -> MaterialTheme.colorScheme.error
                }
                Box(
                    modifier = Modifier
                        .background(priorityColor.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(priorityText, style = MaterialTheme.typography.labelMedium)
                }
                if (taskItem.categoryName != null) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(taskItem.categoryName)
                    }
                }
                if (taskItem.isDueToday) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Due today") }
                    )
                }
                if (taskItem.isOverdue) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Overdue") }
                    )
                }
            }

            if (item.dueDate != null || item.dueTime != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Text(
                        text = listOfNotNull(item.dueDate, item.dueTime).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
