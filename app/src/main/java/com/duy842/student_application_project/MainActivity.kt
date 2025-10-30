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
import java.util.Calendar
import android.app.DatePickerDialog
import androidx.compose.ui.unit.sp
import com.duy842.student_application_project.ui.theme.Student_Application_ProjectTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
///Set Up
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
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Dashboard,
                                    onClick = { currentScreen = Screen.Dashboard },
                                    icon = { Icon(Icons.Default.Info, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard") }
                                )


                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                Screen.Home -> HomeScreen()
                                Screen.TaskManager -> TaskManagerScreen()
                                Screen.WeeklyPlanner -> WeeklyPlannerScreen()
                                Screen.Dashboard -> DashboardScreen()

                            }
                        }
                    }
                }
            }
        }


    }
}





enum class Screen {
    Home, TaskManager, WeeklyPlanner, Dashboard
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
        "Your time is limited, so don’t waste it living someone else’s life. – Steve Jobs",
        "Vision without execution is hallucination. – Thomas Edison",
        "When everything seems to be going against you, remember that the airplane takes off against the wind. – Henry Ford"
    )

    LaunchedEffect(Unit) {
        TaskPrefs.getTasks(context).collect { loaded ->
            tasks = loaded
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 🔹 App Title
        Text(
            text = "📋 Task Reminder",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        // 🔹 Motivational Quote
        val quoteOfTheDay = remember { motivationalQuotes.random() }
        Box(
            modifier = Modifier
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
                    FilterDropdown(
                        "Category",
                        listOf("All", "Assignment", "Exam", "Personal"),
                        selectedCategory
                    ) { selectedCategory = it }
                    FilterDropdown(
                        "Priority",
                        listOf("All", "High", "Medium", "Low"),
                        selectedPriority
                    ) { selectedPriority = it }
                }
            }
        }

        // 🔹 Task List Section
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "🎯 All Tasks",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(8.dp))

                val displayedTasks = tasks.filter { task ->
                    (selectedCategory == "All" || task.category == selectedCategory) &&
                            (selectedPriority == "All" || task.priority == selectedPriority)
                }

                if (displayedTasks.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Text("No tasks match your filters.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        displayedTasks.forEach { task ->
                            val taskIndex = tasks.indexOf(task)
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
                                            onCheckedChange = { isChecked ->
                                                tasks = tasks.mapIndexed { i, t ->
                                                    if (i == taskIndex) t.copy(isDone = isChecked) else t
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(task.name, style = MaterialTheme.typography.bodyMedium)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("📂 ${task.category}", style = MaterialTheme.typography.labelSmall)
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
                                                val deadlineText = when {
                                                    !task.scheduledDay.isNullOrBlank() && task.scheduledHour != null ->
                                                        "🗓 ${task.scheduledDay} at ${task.scheduledHour}:00"
                                                    !task.scheduledDay.isNullOrBlank() ->
                                                        "🗓 ${task.scheduledDay}"
                                                    task.scheduledHour != null ->
                                                        "🗓 Today at ${task.scheduledHour}:00"
                                                    else -> "🗓 No deadline"
                                                }
                                                Text(
                                                    text = deadlineText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // 🔹 Edit & Delete Buttons
                                    Row {
                                        IconButton(onClick = {
                                            editingTaskIndex = taskIndex
                                            editedPriority = task.priority
                                            editedCategory = task.category
                                            showEditDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                                        }
                                        IconButton(onClick = {
                                            tasks = tasks.filterIndexed { i, _ -> i != taskIndex }
                                            scope.launch { TaskPrefs.saveTasks(context, tasks) }
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
                    // ✅ Save all tasks with current isDone status
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
                    scope.launch { TaskPrefs.saveTasks(context, tasks) }
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            },
            title = { Text("Edit Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Category", style = MaterialTheme.typography.labelMedium)
                    listOf("Assignment", "Exam", "Personal").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = editedCategory == option, onClick = { editedCategory = option })
                            Text(option)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("Select Priority", style = MaterialTheme.typography.labelMedium)
                    listOf("High", "Medium", "Low").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = editedPriority == option, onClick = { editedPriority = option })
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
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Date Picker Dialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "${year}-${month + 1}-${dayOfMonth}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        // Date Selector
        OutlinedButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedDate?.let { "🗓 Deadline: $it" } ?: "Select Deadline"
            )
        }

        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    scope.launch {
                        val current = TaskPrefs.getTasks(context).first()
                        val newTask = Task(
                            name = taskName,
                            category = selectedCategory,
                            priority = selectedPriority,
                            scheduledDay = selectedDate
                        )
                        TaskPrefs.saveTasks(context, current + newTask)
                        taskName = ""
                        selectedDate = null
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

    val savedTasks by TaskPrefs.getTasks(context).collectAsState(initial = emptyList())
    var editableTasks by remember { mutableStateOf(savedTasks.toMutableList()) }
    LaunchedEffect(savedTasks) { editableTasks = savedTasks.toMutableList() }

    val horizontalScroll = rememberScrollState()
    var editingTask by remember { mutableStateOf<Task?>(null) }

    // Helper: convert "yyyy-MM-dd" string to day of week
    fun getDayOfWeek(dateStr: String?): String? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val parts = dateStr.split("-").map { it.toInt() }
            val calendar = Calendar.getInstance()
            calendar.set(parts[0], parts[1] - 1, parts[2])
            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> null
            }
        } catch (e: Exception) { null }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Button(
            onClick = { scope.launch { TaskPrefs.saveTasks(context, editableTasks) } },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScroll)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysOfWeek.forEach { day ->
                val verticalScroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .padding(4.dp)
                        .verticalScroll(verticalScroll)
                        .background(Color(0xFFF9F9F9), shape = MaterialTheme.shapes.medium)
                        .padding(8.dp)
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- Filter tasks for this day ---
                    val tasksForDay = editableTasks.filter { getDayOfWeek(it.scheduledDay) == day }

                    if (tasksForDay.isEmpty()) {
                        Text("No tasks", style = MaterialTheme.typography.bodySmall)
                    } else {
                        tasksForDay.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(task.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Text("${task.category} | ${task.priority}", style = MaterialTheme.typography.labelSmall)
                                }

                                IconButton(onClick = { editingTask = task }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Edit Dialog ---
    editingTask?.let { task ->
        var editedCategory by remember { mutableStateOf(task.category) }
        var editedPriority by remember { mutableStateOf(task.priority) }

        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = { Text("Edit Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category")
                    listOf("Assignment", "Exam", "Personal").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = editedCategory == option, onClick = { editedCategory = option })
                            Text(option)
                        }
                    }

                    Text("Priority")
                    listOf("High", "Medium", "Low").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = editedPriority == option, onClick = { editedPriority = option })
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editableTasks = editableTasks.map {
                        if (it.name == task.name && it.scheduledDay == task.scheduledDay)
                            it.copy(category = editedCategory, priority = editedPriority)
                        else it
                    }.toMutableList()
                    editingTask = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingTask = null }) { Text("Cancel") }
            }
        )
    }
}


@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val tasks by TaskPrefs.getTasks(context).collectAsState(initial = emptyList())

    // Helper to convert date string to Calendar
    fun dateToCalendar(dateStr: String?): Calendar? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val parts = dateStr.split("-").map { it.toInt() }
            Calendar.getInstance().apply {
                set(parts[0], parts[1] - 1, parts[2], 23, 59) // end of day
            }
        } catch (e: Exception) { null }
    }

    val now = Calendar.getInstance()

    val finishedTasks = tasks.count { it.isDone }
    val pastDeadlineTasks = tasks.count {
        !it.isDone && dateToCalendar(it.scheduledDay)?.before(now) == true
    }
    val pendingTasks = tasks.count {
        !it.isDone && dateToCalendar(it.scheduledDay)?.after(now) != false
    }

    val totalTasks = tasks.size.coerceAtLeast(1) // avoid division by 0

    val finishedPercent = (finishedTasks * 100 / totalTasks)
    val pastDeadlinePercent = (pastDeadlineTasks * 100 / totalTasks)
    val pendingPercent = (pendingTasks * 100 / totalTasks)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊 Dashboard", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DashboardCard("Pending", pendingPercent, Color(0xFFFFC107)) // amber
            DashboardCard("Finished", finishedPercent, Color(0xFF4CAF50)) // green
            DashboardCard("Past Deadline", pastDeadlinePercent, Color(0xFFF44336)) // red
        }
    }
}

@Composable
fun DashboardCard(title: String, percent: Int, color: Color) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular progress bar
        CircularProgressIndicator(
            progress = percent / 100f,
            strokeWidth = 8.dp,
            color = color,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("$percent%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
    }
}

