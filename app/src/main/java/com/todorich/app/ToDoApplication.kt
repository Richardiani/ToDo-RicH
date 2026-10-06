package com.todorich.app

import android.app.Application

class ToDoApplication : Application() {
    val database by lazy { com.todorich.app.data.TaskDatabase.getDatabase(this@ToDoApplication) }
    val repository by lazy {
        com.todorich.app.data.TaskRepository(
            database.taskDao(),
            database.categoryDao()
        )
    }
}
