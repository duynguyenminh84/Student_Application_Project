package com.duy842.student_application_project

import android.R.attr.password
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duy842.student_application_project.ui.theme.Student_Application_ProjectTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            Student_Application_ProjectTheme(dynamicColor = false) {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                } else {
                    var currentScreen by remember { mutableStateOf(Screen.Home) }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Home,
                                    onClick = { currentScreen = Screen.Home },
                                    icon = {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.TaskManager,
                                    onClick = { currentScreen = Screen.TaskManager },
                                    icon = {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Task"
                                        )
                                    },
                                    label = { Text("Add Task") }

                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.WeeklyPlanner,
                                    onClick = { currentScreen = Screen.WeeklyPlanner },
                                     icon = { Icon(Icons.Default.DateRange, contentDescription = "Planner") }
                                    ,
                                    label = { Text("Planner") }
                                )

                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                Screen.Home -> HomeScreen()
                                Screen.TaskManager -> TaskManagerScreen()
                                Screen.WeeklyPlanner -> WeeklyPlannerScreen()
                            }
                        }
                    }
                }
            }
        }


    }
}





enum class Screen {
    Home, TaskManager,WeeklyPlanner
}


data class Task(
    val name: String,
    val category: String = "General",
    val priority: String = "Medium",
    val isDone: Boolean = false,
    val scheduledDay: String? = null,  // e.g. "Mon"
    val scheduledHour: Int? = null     // e.g. 10
)

data class TimeBlock(
    val hour: Int,
    var task: String = ""
)

data class DaySchedule(
    val day: String,
    val blocks: List<TimeBlock>
)

////////////////////////////////////////////////////////////

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔐 Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onLoginSuccess() // ✅ No validation — logs in with any input
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}




//////////////////////////////////////////////////////////////////////////////////////////////////////////////////







@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPriority by remember { mutableStateOf("All") }

    // 🔧 Edit Dialog State
    var editingTaskIndex by remember { mutableStateOf(-1) }
    var editedPriority by remember { mutableStateOf("Medium") }
    var editedCategory by remember { mutableStateOf("Assignment") }
    var showEditDialog by remember { mutableStateOf(false) }

    val motivationalQuotes = listOf(
        "Dream big. Start small. But most of all, start. – Simon Sinek",
        "The way to get started is to quit talking and begin doing. – Walt Disney",
        "Chase the vision, not the money. The money will end up following you. – Tony Hsieh",
        "Done is better than perfect. – Sheryl Sandberg",
        "If you are not embarrassed by the first version of your product, you’ve launched too late. – Reid Hoffman",
        "The most effective way to do it is to do it. – Amelia Earhart",
        "Your time is limited, so don’t waste it living someone else’s life. – Steve Jobs",
        "Vision without execution is hallucination. – Thomas Edison",
        "When everything seems to be going against you, remember that the airplane takes off against the wind. – Henry Ford"
    )


    LaunchedEffect(Unit) {
        TaskPrefs.getTasks(context).collect { loaded ->
            tasks = loaded
        }
    }

    val filteredTasks = tasks.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (selectedPriority == "All" || it.priority == selectedPriority)
    }

    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    )
    {
        // 🔹 App Title
        Text(
            text = "📋 Task Reminder",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        // 🔹 Motivational Quotes

        val quoteOfTheDay = remember { motivationalQuotes.random() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Motivation",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = quoteOfTheDay,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 24.sp
                    ),
                    color = Color.White
                )
            }
        }




        // 🔹 Filter Section
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Filter your tasks", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterDropdown("Category", listOf("All", "Assignment", "Exam", "Personal"), selectedCategory) {
                        selectedCategory = it
                    }
                    FilterDropdown("Priority", listOf("All", "High", "Medium", "Low"), selectedPriority) {
                        selectedPriority = it
                    }
                }
            }
        }

        // 🔹 Task List Section
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎯 Today's Tasks", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(8.dp))

                if (filteredTasks.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Text("No tasks match your filters.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filteredTasks.forEachIndexed { index, task ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = task.isDone,
                                            onCheckedChange = {
                                                tasks = tasks.mapIndexed { i, t ->
                                                    if (i == index) t.copy(isDone = !t.isDone) else t
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(task.name, style = MaterialTheme.typography.bodyMedium)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("📂 ${task.category}", style = MaterialTheme.typography.labelSmall)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                AssistChip(
                                                    onClick = {},
                                                    label = { Text(task.priority) },
                                                    colors = AssistChipDefaults.assistChipColors(
                                                        containerColor = when (task.priority) {
                                                            "High" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                                            "Medium" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                                            "Low" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            editingTaskIndex = index
                                            editedPriority = task.priority
                                            editedCategory = task.category
                                            showEditDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                                        }
                                        IconButton(onClick = {
                                            tasks = tasks.filterIndexed { i, _ -> i != index }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔹 Save Button
        Button(
            onClick = {
                scope.launch {
                    TaskPrefs.saveTasks(context, tasks)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💾 Save Tasks", style = MaterialTheme.typography.titleMedium)
        }
    }

    // 🔧 Edit Dialog
    if (showEditDialog && editingTaskIndex != -1) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    tasks = tasks.mapIndexed { i, t ->
                        if (i == editingTaskIndex) t.copy(
                            priority = editedPriority,
                            category = editedCategory
                        ) else t
                    }
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Edit Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Category", style = MaterialTheme.typography.labelMedium)
                    listOf("Assignment", "Exam", "Personal").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = editedCategory == option,
                                onClick = { editedCategory = option }
                            )
                            Text(option)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("Select Priority", style = MaterialTheme.typography.labelMedium)
                    listOf("High", "Medium", "Low").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = editedPriority == option,
                                onClick = { editedPriority = option }
                            )
                            Text(option)
                        }
                    }
                }
            }
        )
    }
}






/////////////////////////////////////////////////////////////////////////////////////////








@Composable
fun TaskManagerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var taskName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Assignment") }
    var selectedPriority by remember { mutableStateOf("Medium") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    )
    {
        Text("📝 Add New Task", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )

        CategorySelector(selectedCategory) { selectedCategory = it }
        PrioritySelector(selectedPriority) { selectedPriority = it }

        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    scope.launch {
                        val current = TaskPrefs.getTasks(context).first()
                        val newTask = Task(taskName, selectedCategory, selectedPriority)
                        TaskPrefs.saveTasks(context, current + newTask)
                        taskName = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
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
        Text(label, style = MaterialTheme.typography.labelMedium)
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



/////////////////////////////////////////////////////



@Composable
fun WeeklyPlannerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val hours = (8..18).toList()

    val savedTasks by TaskPrefs.getTasks(context).collectAsState(initial = emptyList())

    // Keep local editable state separate from saved data
    var editableTasks by remember { mutableStateOf(savedTasks.toMutableList()) }

    // Sync local state with stored data when it changes
    LaunchedEffect(savedTasks) {
        editableTasks = savedTasks.toMutableList()
    }

    val horizontalScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // --- Save button ---
        Button(
            onClick = {
                scope.launch {
                    TaskPrefs.saveTasks(context, editableTasks)
                    Toast
                        .makeText(context, "Tasks saved!", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }

        // --- Planner grid ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScroll)
                .padding(top = 8.dp)
        ) {
            daysOfWeek.forEach { day ->
                val verticalScroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .padding(8.dp)
                        .verticalScroll(verticalScroll)
                        .background(Color(0xFFF9F9F9), shape = MaterialTheme.shapes.medium)
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    hours.forEach { hour ->
                        val existingTask =
                            editableTasks.find { it.scheduledDay == day && it.scheduledHour == hour }
                        var taskText by remember { mutableStateOf(existingTask?.name ?: "") }

                        OutlinedTextField(
                            value = taskText,
                            onValueChange = { newValue ->
                                taskText = newValue

                                // Update local editable list only (not saving yet)
                                editableTasks = editableTasks
                                    .filterNot { it.scheduledDay == day && it.scheduledHour == hour }
                                    .toMutableList()
                                if (newValue.isNotBlank()) {
                                    editableTasks.add(
                                        Task(
                                            name = newValue,
                                            scheduledDay = day,
                                            scheduledHour = hour
                                        )
                                    )
                                }
                            },
                            label = { Text("$hour:00") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}
