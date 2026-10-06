package com.todorich.app.data

import com.todorich.app.data.entity.CategoryEntity
import com.todorich.app.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeTasks()

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeCategories()

    suspend fun saveTask(task: TaskEntity) {
        val updated = task.copy(updatedAt = System.currentTimeMillis())
        if (updated.id == 0L) {
            taskDao.insert(updated.copy(createdAt = System.currentTimeMillis()))
        } else {
            taskDao.update(updated)
        }
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }

    suspend fun toggleTaskCompleted(taskId: Long) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.update(task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis()))
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(categoryId: Long) {
        taskDao.clearCategoryAssignments(categoryId)
        categoryDao.delete(categoryId)
    }

    suspend fun ensureDefaultCategory() {
        if (categoryDao.countCategories() == 0) {
            categoryDao.insert(CategoryEntity(id = 1L, name = "General"))
        }
    }
}
