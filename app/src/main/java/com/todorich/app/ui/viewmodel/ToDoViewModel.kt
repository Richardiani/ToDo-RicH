package com.todorich.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.todorich.app.data.TaskRepository
import com.todorich.app.data.entity.CategoryEntity
import com.todorich.app.data.entity.TaskEntity
import com.todorich.app.domain.TaskFilter
import com.todorich.app.domain.TaskPriority
import com.todorich.app.domain.TaskSort
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskListItem(
    val task: TaskEntity,
    val categoryName: String?,
    val isOverdue: Boolean,
    val isDueToday: Boolean
)

data class ToDoUiState(
    val tasks: List<TaskListItem> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val selectedSort: TaskSort = TaskSort.CREATED_AT,
    val selectedCategoryId: Long? = null,
    val selectedPriority: TaskPriority? = null,
    val errorMessage: String? = null
) {
    val selectedCategoryName: String?
        get() = categories.firstOrNull { it.id == selectedCategoryId }?.name
}

class ToDoViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter

    private val _selectedSort = MutableStateFlow(TaskSort.CREATED_AT)
    val selectedSort: StateFlow<TaskSort> = _selectedSort

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId

    private val _selectedPriority = MutableStateFlow<TaskPriority?>(null)
    val selectedPriority: StateFlow<TaskPriority?> = _selectedPriority

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val allTasks: StateFlow<List<TaskEntity>> = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ToDoUiState> = combine(
        allTasks,
        categories,
        _searchQuery,
        _selectedFilter,
        _selectedSort,
        _selectedCategoryId,
        _selectedPriority,
        _errorMessage
    ) { tasks, categoriesList, query, filter, sort, categoryId, priority, error ->
        val categoryMap = categoriesList.associateBy { it.id }
        val filtered = tasks.filter { task ->
            val queryMatch = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)

            val filterMatch = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.ACTIVE -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
                TaskFilter.OVERDUE -> !task.isCompleted && task.dueDate != null &&
                    runCatching {
                        LocalDate.parse(task.dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    }.getOrNull()?.isBefore(LocalDate.now()) == true
                TaskFilter.DUE_TODAY -> !task.isCompleted && task.dueDate != null &&
                    runCatching {
                        LocalDate.parse(task.dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    }.getOrNull()?.isEqual(LocalDate.now()) == true
                TaskFilter.CATEGORY -> categoryId == null || task.categoryId == categoryId
                TaskFilter.PRIORITY -> priority == null || TaskPriority.fromValue(task.priority) == priority
            }

            queryMatch && filterMatch && (priority == null || TaskPriority.fromValue(task.priority) == priority) &&
                (categoryId == null || task.categoryId == categoryId)
        }

        val sorted = filtered.sortedWith { left, right ->
            when (sort) {
                TaskSort.CREATED_AT -> right.createdAt.compareTo(left.createdAt)
                TaskSort.DUE_DATE -> {
                    val leftDate = left.dueDate?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }
                    val rightDate = right.dueDate?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }
                    when {
                        leftDate == null && rightDate == null -> 0
                        leftDate == null -> 1
                        rightDate == null -> -1
                        else -> leftDate.compareTo(rightDate)
                    }
                }
                TaskSort.PRIORITY -> {
                    val leftPriority = TaskPriority.fromValue(left.priority).value
                    val rightPriority = TaskPriority.fromValue(right.priority).value
                    rightPriority.compareTo(leftPriority)
                }
                TaskSort.ALPHABETICAL -> left.title.lowercase().compareTo(right.title.lowercase())
            }
        }

        val items = sorted.map { task ->
            val category = categoryMap[task.categoryId]
            val dueDate = task.dueDate?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }
            val isOverdue = !task.isCompleted && dueDate != null && dueDate.isBefore(LocalDate.now())
            val isDueToday = !task.isCompleted && dueDate != null && dueDate.isEqual(LocalDate.now())
            TaskListItem(
                task = task,
                categoryName = category?.name,
                isOverdue = isOverdue,
                isDueToday = isDueToday
            )
        }

        ToDoUiState(
            tasks = items,
            categories = categoriesList,
            searchQuery = query,
            selectedFilter = filter,
            selectedSort = sort,
            selectedCategoryId = categoryId,
            selectedPriority = priority,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToDoUiState())

    fun onSearchChanged(search: String) {
        _searchQuery.value = search
    }

    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
        if (filter != TaskFilter.CATEGORY) {
            _selectedCategoryId.value = null
        }
        if (filter != TaskFilter.PRIORITY) {
            _selectedPriority.value = null
        }
    }

    fun setSort(sort: TaskSort) {
        _selectedSort.value = sort
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
        _selectedFilter.value = if (categoryId == null) TaskFilter.ALL else TaskFilter.CATEGORY
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _selectedPriority.value = priority
        _selectedFilter.value = if (priority == null) TaskFilter.ALL else TaskFilter.PRIORITY
    }

    fun clearFilters() {
        _selectedFilter.value = TaskFilter.ALL
        _selectedCategoryId.value = null
        _selectedPriority.value = null
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.saveTask(task)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Could not save task."
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Could not delete task."
            }
        }
    }

    fun toggleCompleted(taskId: Long) {
        viewModelScope.launch {
            try {
                repository.toggleTaskCompleted(taskId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Could not update task."
            }
        }
    }

    fun createCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _errorMessage.value = "Category name cannot be empty."
            return
        }
        viewModelScope.launch {
            try {
                repository.insertCategory(CategoryEntity(name = trimmed))
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Could not create category."
            }
        }
    }

    fun renameCategory(categoryId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _errorMessage.value = "Category name cannot be empty."
            return
        }
        viewModelScope.launch {
            try {
                val category = categories.value.firstOrNull { it.id == categoryId } ?: return@launch
                repository.updateCategory(category.copy(name = trimmed))
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Could not rename category."
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Could not delete category."
            }
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
                return ToDoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
