package com.duy842.student_application_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duy842.student_application_project.ui.theme.Student_Application_ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Student_Application_ProjectTheme {
                var currentScreen by remember { mutableStateOf(Screen.Home) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(currentScreen) { selected ->
                            currentScreen = selected
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.Home -> HomeScreen()
                            Screen.TaskManager -> TaskManagerScreen()
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    Home, TaskManager
}

@Composable
fun BottomNavigationBar(current: Screen, onSelect: (Screen) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Screen.Home,
            onClick = { onSelect(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = current == Screen.TaskManager,
            onClick = { onSelect(Screen.TaskManager) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
            label = { Text("Add Task") }
        )
    }
}


@Composable
fun HomeScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "💡 Keep pushing forward!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Today's Tasks",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        TaskItem("Finish CP3406 planning doc")
        TaskItem("Review Jetpack Compose tutorial")
        TaskItem("Start mockup for presentation")
    }
}

@Composable
fun TaskItem(task: String) {
    Text(
        text = "• $task",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun TaskManagerScreen() {
    var taskName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Assignment") }
    var selectedPriority by remember { mutableStateOf("Medium") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Add New Task", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Category: $selectedCategory")
        Row {
            listOf("Assignment", "Exam", "Personal").forEach { category ->
                Button(onClick = { selectedCategory = category }, modifier = Modifier.padding(4.dp)) {
                    Text(category)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Priority: $selectedPriority")
        Row {
            listOf("High", "Medium", "Low").forEach { priority ->
                Button(onClick = { selectedPriority = priority }, modifier = Modifier.padding(4.dp)) {
                    Text(priority)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { /* Save logic later */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Task")
        }
    }
}
