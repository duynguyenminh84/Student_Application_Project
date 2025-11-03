package com.duy842.student_application_project

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duy842.student_application_project.ui.auth.AuthViewModel
import com.duy842.student_application_project.ui.theme.Student_Application_ProjectTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Student_Application_ProjectTheme(dynamicColor = false) {

                // ViewModel-based offline auth
                val authVm: AuthViewModel = viewModel()
                val isLoggedIn by authVm.isLoggedIn.collectAsState()
                val error by authVm.error.collectAsState()
                val uid by authVm.currentUserId.collectAsState() // current user id

                // Always require login on app launch (remove this if you want auto-login)
                LaunchedEffect(Unit) { authVm.logout() }

                if (!isLoggedIn) {
                    LoginScreen(
                        onLogin = { email, pass -> authVm.login(email, pass) },
                        onRegister = { email, pass -> authVm.register(email, pass) },
                        error = error
                    )
                } else {
                    var currentScreen by rememberSaveable { mutableStateOf(Screen.Home) }

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
                                NavigationBarItem(
                                    selected = currentScreen == Screen.WeeklyPlanner,
                                    onClick = { currentScreen = Screen.WeeklyPlanner },
                                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Planner") },
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
                                Screen.Home          -> HomeScreen(uid = uid)
                                Screen.TaskManager   -> TaskManagerScreen(uid = uid)
                                Screen.WeeklyPlanner -> WeeklyPlannerScreen(uid = uid)
                                Screen.Dashboard     -> DashboardScreen(uid = uid)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Navigation ---------- */

enum class Screen { Home, TaskManager, WeeklyPlanner, Dashboard }

/* ---------- Data models ---------- */

data class Task(
    val name: String,
    val category: String = "General",
    val priority: String = "Medium",
    val isDone: Boolean = false,
    val scheduledDay: String? = null,  // e.g. "2025-11-03"
    val scheduledHour: Int? = null     // optional hour
)

data class TimeBlock(val hour: Int, var task: String = "")
data class DaySchedule(val day: String, val blocks: List<TimeBlock>)

/* ---------- Auth: Login screen (offline) ---------- */

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    error: String?
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
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Login") }

        TextButton(
            onClick = { onRegister(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create Account") }
    }
}

/* ---------- Home ---------- */

@Composable
fun HomeScreen(uid: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPriority by remember { mutableStateOf("All") }

    // Edit Dialog state
    var editingTaskIndex by remember { mutableStateOf(-1) }
    var editedPriority by remember { mutableStateOf("Medium") }
    var editedCategory by remember { mutableStateOf("Assignment") }
    var showEditDialog by remember { mutableStateOf(false) }

    // Load tasks for this user whenever uid changes
    LaunchedEffect(uid) {
        TaskPrefs.getTasks(context, uid).collect { loaded -> tasks = loaded }
    }

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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "📋 Task Reminder",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

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
                    shape = MaterialTheme.shapes.medium
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

        // Filters
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Filter your tasks", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterDropdown(
                        label = "Category",
                        options = listOf("All", "Assignment", "Exam", "Personal"),
                        selected = selectedCategory
                    ) { selectedCategory = it }
                    FilterDropdown(
                        label = "Priority",
                        options = listOf("All", "High", "Medium", "Low"),
                        selected = selectedPriority
                    ) { selectedPriority = it }
                }
            }
        }

        // Task list
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = task.isDone,
                                            onCheckedChange = { isChecked ->
                                                tasks = tasks.mapIndexed { i, t ->
                                                    if (i == taskIndex) t.copy(isDone = isChecked) else t
                                                }
                                                scope.launch { TaskPrefs.saveTasks(context, uid, tasks) }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(task.name, style = MaterialTheme.typography.bodyMedium)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("📂 ${task.category}", style = MaterialTheme.typography.labelSmall)
                                                AssistChip(onClick = {}, label = { Text(task.priority) })
                                                val deadlineText = when {
                                                    !task.scheduledDay.isNullOrBlank() && task.scheduledHour != null ->
                                                        "🗓 ${task.scheduledDay} at ${task.scheduledHour}:00"
                                                    !task.scheduledDay.isNullOrBlank() -> "🗓 ${task.scheduledDay}"
                                                    task.scheduledHour != null -> "🗓 Today at ${task.scheduledHour}:00"
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

                                    Divider(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            editingTaskIndex = taskIndex
                                            editedPriority = task.priority
                                            editedCategory = task.category
                                            showEditDialog = true
                                        }) { Icon(Icons.Default.Edit, contentDescription = "Edit Task") }

                                        IconButton(onClick = {
                                            tasks = tasks.filterIndexed { i, _ -> i != taskIndex }
                                            scope.launch { TaskPrefs.saveTasks(context, uid, tasks) }
                                        }) { Icon(Icons.Default.Delete, contentDescription = "Delete Task") }

                                        TextButton(onClick = {
                                            tasks = tasks.filterIndexed { i, _ -> i != taskIndex }
                                            scope.launch { TaskPrefs.saveTasks(context, uid, tasks) }
                                        }) { Text("Remove") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
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
                    scope.launch { TaskPrefs.saveTasks(context, uid, tasks) }
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } },
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

/* ---------- Task Manager ---------- */

@Composable
fun TaskManagerScreen(uid: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var taskName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Assignment") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d -> selectedDate = "$y-${m + 1}-$d" },
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

        OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = selectedDate?.let { "🗓 Deadline: $it" } ?: "Select Deadline")
        }

        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    scope.launch {
                        val current = TaskPrefs.getTasks(context, uid).first()
                        val newTask = Task(
                            name = taskName,
                            category = selectedCategory,
                            priority = selectedPriority,
                            scheduledDay = selectedDate
                        )
                        TaskPrefs.saveTasks(context, uid, current + newTask)
                        taskName = ""
                        selectedDate = null
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add Task to Home") }
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
            OutlinedButton(onClick = { expanded = true }) { Text(selected) }
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

/* ---------- Weekly Planner ---------- */

@Composable
fun WeeklyPlannerScreen(uid: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val savedTasks by TaskPrefs.getTasks(context, uid).collectAsState(initial = emptyList())
    var editableTasks by remember { mutableStateOf(savedTasks.toMutableList()) }
    LaunchedEffect(savedTasks) { editableTasks = savedTasks.toMutableList() }

    fun persist(list: List<Task>) {
        scope.launch { TaskPrefs.saveTasks(context, uid, list) }
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val horizontalScroll = rememberScrollState()
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    fun getDayOfWeek(dateStr: String?): String? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val parts = dateStr.split("-").map { it.toInt() }
            val cal = Calendar.getInstance().apply { set(parts[0], parts[1] - 1, parts[2]) }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> null
            }
        } catch (_: Exception) { null }
    }

    fun prettyDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "No date"
        return try {
            val inFmt = java.text.SimpleDateFormat("yyyy-M-d", java.util.Locale.getDefault())
            val date = inFmt.parse(dateStr)
            val outFmt = java.text.SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale.getDefault())
            outFmt.format(date!!)
        } catch (_: Exception) { dateStr }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Button(
            onClick = { persist(editableTasks) },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Save") }

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
                        .width(180.dp)
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

                    val indicesForDay = editableTasks.mapIndexedNotNull { idx, t ->
                        if (getDayOfWeek(t.scheduledDay) == day) idx else null
                    }

                    if (indicesForDay.isEmpty()) {
                        Text("No tasks", style = MaterialTheme.typography.bodySmall)
                    } else {
                        indicesForDay.forEach { globalIdx ->
                            val task = editableTasks[globalIdx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(task.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Text(
                                        prettyDate(task.scheduledDay),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("${task.category} | ${task.priority}", style = MaterialTheme.typography.labelSmall)
                                }
                                Row {
                                    IconButton(onClick = { editingIndex = globalIdx }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                                    }
                                    IconButton(onClick = {
                                        editableTasks.removeAt(globalIdx)
                                        persist(editableTasks)
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

    // Edit dialog with bounds check
    editingIndex?.let { idx ->
        if (idx in 0 until editableTasks.size) {
            val task = editableTasks[idx]
            var editedCategory by remember { mutableStateOf(task.category) }
            var editedPriority by remember { mutableStateOf(task.priority) }

            AlertDialog(
                onDismissRequest = { editingIndex = null },
                title = { Text("Edit Task") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Category", style = MaterialTheme.typography.labelMedium)
                        listOf("Assignment", "Exam", "Personal").forEach { option ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = editedCategory == option, onClick = { editedCategory = option })
                                Text(option)
                            }
                        }

                        Text("Priority", style = MaterialTheme.typography.labelMedium)
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
                        editableTasks[idx] = editableTasks[idx].copy(
                            category = editedCategory,
                            priority = editedPriority
                        )
                        persist(editableTasks)
                        editingIndex = null
                    }) { Text("Save") }
                },
                dismissButton = { TextButton(onClick = { editingIndex = null }) { Text("Cancel") } }
            )
        } else {
            editingIndex = null
        }
    }
}

/* ---------- Dashboard ---------- */

@Composable
fun DashboardScreen(uid: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tasks by TaskPrefs.getTasks(context, uid).collectAsState(initial = emptyList())

    fun setDone(task: Task) {
        val idx = tasks.indexOf(task)
        if (idx >= 0) {
            val newList = tasks.toMutableList()
            newList[idx] = newList[idx].copy(isDone = true)
            scope.launch { TaskPrefs.saveTasks(context, uid, newList) }
        }
    }
    fun removeTask(task: Task) {
        val idx = tasks.indexOf(task)
        if (idx >= 0) {
            val newList = tasks.toMutableList()
            newList.removeAt(idx)
            scope.launch { TaskPrefs.saveTasks(context, uid, newList) }
        }
    }

    fun parseDate(dateStr: String?): Calendar? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val parts = dateStr.split("-").map { it.toInt() } // yyyy-M-d
            Calendar.getInstance().apply {
                set(parts[0], parts[1] - 1, parts[2], 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (_: Exception) { null }
    }
    fun fmt(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "—"
        return try {
            val inf = java.text.SimpleDateFormat("yyyy-M-d", java.util.Locale.getDefault())
            val d = inf.parse(dateStr) ?: return "—"
            val outf = java.text.SimpleDateFormat("EEE, d MMM", java.util.Locale.getDefault())
            outf.format(d)
        } catch (_: Exception) { "—" }
    }
    fun daysBetween(a: Calendar, b: Calendar): Int {
        val aa = a.clone() as Calendar; aa.set(Calendar.HOUR_OF_DAY,0); aa.set(Calendar.MINUTE,0); aa.set(Calendar.SECOND,0); aa.set(Calendar.MILLISECOND,0)
        val bb = b.clone() as Calendar; bb.set(Calendar.HOUR_OF_DAY,0); bb.set(Calendar.MINUTE,0); bb.set(Calendar.SECOND,0); bb.set(Calendar.MILLISECOND,0)
        val diff = aa.timeInMillis - bb.timeInMillis
        return (diff / (24*60*60*1000L)).toInt()
    }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    val total = tasks.size.coerceAtLeast(1)
    val finished = tasks.count { it.isDone }
    val overdue = tasks.count { !it.isDone && (parseDate(it.scheduledDay)?.before(today) == true) }
    val pending = tasks.count {
        !it.isDone && when (val c = parseDate(it.scheduledDay)) {
            null -> true
            else -> !c.before(today)
        }
    }

    val finishedPct = (finished * 100f / total)
    val pendingPct = (pending * 100f / total)
    val overduePct = (overdue * 100f / total)

    val upcomingList = tasks
        .filter { !it.isDone && parseDate(it.scheduledDay)?.after(today) == true }
        .sortedBy { parseDate(it.scheduledDay)?.timeInMillis ?: Long.MAX_VALUE }
        .take(5)

    val overdueList = tasks
        .filter { !it.isDone && parseDate(it.scheduledDay)?.before(today) == true }
        .sortedBy { parseDate(it.scheduledDay)?.timeInMillis ?: 0L }
        .take(5)

    val nextDue = upcomingList.firstOrNull()

    fun completionStreakDays(): Int {
        var streak = 0
        var day = (today.clone() as Calendar)
        while (true) {
            val hadDone = tasks.any { it.isDone && parseDate(it.scheduledDay)?.let { d -> daysBetween(d, day) == 0 } == true }
            if (hadDone) { streak += 1; day.add(Calendar.DAY_OF_YEAR, -1) } else break
        }
        return streak
    }
    val streakDays = completionStreakDays()

    val ringProgress by animateFloatAsState(targetValue = finishedPct / 100f, label = "progressAnim")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📊 Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(
                progress = ringProgress,
                label = "${finishedPct.toInt()}%",
                caption = "Finished",
                ringColor = Color(0xFF4CAF50)
            )
            StatChip(title = "Pending", value = "${pendingPct.toInt()}%", sub = "($pending)")
            StatChip(title = "Past Due", value = "${overduePct.toInt()}%", sub = "($overdue)")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard(
                title = "Total Tasks",
                content = { Text("$total", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f)
            )
            SectionCard(
                title = "Streak",
                content = { Text("$streakDays day${if (streakDays == 1) "" else "s"}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f)
            )
            SectionCard(
                title = "Next Due",
                content = { Text(nextDue?.scheduledDay?.let { fmt(it) } ?: "—", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f)
            )
        }

        SectionCard(title = "Overdue") {
            if (overdueList.isEmpty()) {
                Text("No overdue tasks. 🎉", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    overdueList.forEach { t ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(t.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
                                Text("${fmt(t.scheduledDay)} • ${t.category} • ${t.priority}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row {
                                IconButton(onClick = { setDone(t) }) { Icon(Icons.Default.Check, contentDescription = "Mark Done") }
                                IconButton(onClick = { removeTask(t) }) { Icon(Icons.Default.Delete, contentDescription = "Remove") }
                            }
                        }
                    }
                }
            }
        }

        SectionCard(title = "Upcoming") {
            if (upcomingList.isEmpty()) {
                Text("Nothing coming up.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upcomingList.forEach { t ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(t.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
                                Text("${fmt(t.scheduledDay)} • ${t.category} • ${t.priority}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row {
                                IconButton(onClick = { setDone(t) }) { Icon(Icons.Default.Check, contentDescription = "Mark Done") }
                                IconButton(onClick = { removeTask(t) }) { Icon(Icons.Default.Delete, contentDescription = "Remove") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, label: String, caption: String, ringColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                strokeWidth = 10.dp,
                modifier = Modifier.size(110.dp),
                color = ringColor
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(caption, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatChip(title: String, value: String, sub: String) {
    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 110.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
