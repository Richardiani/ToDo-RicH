package com.todorich.app.domain

enum class TaskPriority(val value: Int, val label: String) {
    LOW(0, "Low"),
    MEDIUM(1, "Medium"),
    HIGH(2, "High");

    companion object {
        fun fromValue(value: Int): TaskPriority {
            return entries.firstOrNull { it.value == value } ?: MEDIUM
        }
    }
}
