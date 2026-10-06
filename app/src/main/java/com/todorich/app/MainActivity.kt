package com.todorich.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.todorich.app.ui.categories.CategoriesScreen
import com.todorich.app.ui.home.HomeScreen
import com.todorich.app.ui.taskeditor.TaskEditorScreen
import com.todorich.app.ui.theme.ToDoRicHTheme
import com.todorich.app.ui.viewmodel.ToDoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoRicHTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as ToDoApplication
    val viewModel: ToDoViewModel = viewModel(factory = ToDoViewModel.Factory(app.repository))

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
        }

        composable("categories") {
            CategoriesScreen(navController = navController, viewModel = viewModel)
        }

        composable(
            route = "task_editor/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
            TaskEditorScreen(
                navController = navController,
                viewModel = viewModel,
                taskId = taskId
            )
        }
    }
}
