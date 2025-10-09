package com.duy842.student_application_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.duy842.student_application_project.ui.theme.Student_Application_ProjectTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Student_Application_ProjectTheme {
                var currentScreen by remember { mutableStateOf(Screen.Home) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == Screen.Home,
                                onClick = { currentScreen = Screen.Home },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.TaskManager,
                                onClick = { currentScreen = Screen.TaskManager },
                                icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                                label = { Text("Add Task") }
                            )
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

data class Task(
    val name: String,
    val category: String,
    val priority: String,
    val isDone: Boolean = false
)

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPriority by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        TaskPrefs.getTasks(context).collect { loaded ->
            tasks = loaded
        }
    }

    val filtered = tasks.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (selectedPriority == "All" || it.priority == selectedPriority)
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("💡 Keep pushing forward!", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterDropdown("Category", listOf("All", "Assignment", "Exam", "Personal"), selectedCategory) {
                selectedCategory = it
            }
            FilterDropdown("Priority", listOf("All", "High", "Medium", "Low"), selectedPriority) {
                selectedPriority = it
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Today's Tasks", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        filtered.forEachIndexed { index, task ->
            TaskItem(task,
                onToggle = {
                    tasks = tasks.mapIndexed { i, t ->
                        if (i == index) t.copy(isDone = !t.isDone) else t
                    }
                },
                onRemove = {
                    tasks = tasks.filterIndexed { i, _ -> i != index }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                TaskPrefs.saveTasks(context, tasks)
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Tasks")
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(task.name)
                Text("📂 ${task.category}   🔥 ${task.priority}", style = MaterialTheme.typography.labelSmall)
            }
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Task")
        }
    }
}

@Composable
fun TaskManagerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var taskName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Assignment") }
    var selectedPriority by remember { mutableStateOf("Medium") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("📝 Add New Task", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )

        CategorySelector(selectedCategory) { selectedCategory = it }
        PrioritySelector(selectedPriority) { selectedPriority = it }

        Button(onClick = {
            if (taskName.isNotBlank()) {
                scope.launch {
                    val current = TaskPrefs.getTasks(context).first()
                    val newTask = Task(taskName, selectedCategory, selectedPriority)
                    TaskPrefs.saveTasks(context, current + newTask)
                    taskName = ""
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Task to Home")
        }
    }
}

@Composable
fun CategorySelector(selected: String, onSelect: (String) -> Unit) {
    Column {
        Text("Category", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Assignment", "Exam", "Personal").forEach {
                FilterChip(selected = selected == it, onClick = { onSelect(it) }, label = { Text(it) })
            }
        }
    }
}

@Composable
fun PrioritySelector(selected: String, onSelect: (String) -> Unit) {
    Column {
        Text("Priority", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("High", "Medium", "Low").forEach {
                FilterChip(selected = selected == it, onClick = { onSelect(it) }, label = { Text(it) })
            }
        }
    }
}

@Composable
fun FilterDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selected)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            onSelect(it)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
