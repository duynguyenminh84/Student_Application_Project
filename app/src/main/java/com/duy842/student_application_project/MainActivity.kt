package com.duy842.student_application_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.duy842.student_application_project.ui.auth.AuthViewModel
import com.duy842.student_application_project.ui.auth.AuthViewModelFactory
import com.duy842.student_application_project.ui.theme.Student_Application_ProjectTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ---------- Simple Task model (shared across screens) ----------
data class Task(
    val name: String,
    val category: String = "General",
    val priority: String = "Medium",
    val isDone: Boolean = false,
    val scheduledDay: String? = null,  // e.g. "2025-11-03"
    val scheduledHour: Int? = null
)

// ====== Room debug mirror so tasks appear in App Inspection ======
@Entity(tableName = "tasks_debug")
data class TaskRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: Long,
    val name: String,
    val category: String,
    val priority: String,
    val isDone: Boolean,
    val scheduledDay: String?,
    val scheduledHour: Int?
)

@Dao
interface TaskRowDao {
    @Query("DELETE FROM tasks_debug WHERE uid = :uid")
    suspend fun deleteForUid(uid: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<TaskRow>)
}

@Database(entities = [TaskRow::class], version = 1, exportSchema = false)
abstract class DebugTasksDb : RoomDatabase() {
    abstract fun dao(): TaskRowDao
}

object DebugTasksMirror {
    @Volatile
    private var _db: DebugTasksDb? = null

    private fun db(ctx: android.content.Context): DebugTasksDb {
        return _db ?: Room.databaseBuilder(
            ctx.applicationContext,
            DebugTasksDb::class.java,
            "debug_tasks.db"
        ).fallbackToDestructiveMigration().build().also { _db = it }
    }

    suspend fun replaceForUid(ctx: android.content.Context, uid: Long, tasks: List<Task>) {
        val dao = db(ctx).dao()
        dao.deleteForUid(uid)
        if (tasks.isNotEmpty()) {
            val rows = tasks.map {
                TaskRow(
                    uid = uid,
                    name = it.name,
                    category = it.category,
                    priority = it.priority,
                    isDone = it.isDone,
                    scheduledDay = it.scheduledDay,
                    scheduledHour = it.scheduledHour
                )
            }
            dao.insertAll(rows)
        }
    }
}

/** Collects DataStore tasks for a uid and mirrors them into Room (for App Inspection). */
@Composable
fun TaskMirrorEffect(uid: Long) {
    val ctx = LocalContext.current
    LaunchedEffect(uid) {
        if (uid > 0) {
            TaskPrefs.getTasks(ctx, uid).collect { list ->
                DebugTasksMirror.replaceForUid(ctx, uid, list)
            }
        }
    }
}

// ---------- Reusable confirm dialog ----------
@Composable
fun ConfirmRemoveDialog(
    title: String = "Remove task?",
    message: String = "This action cannot be undone.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ---------- Tiny helper: show Undo snackbar ----------
suspend fun showUndoDelete(
    snackbarHost: SnackbarHostState,
    message: String,
    onUndo: () -> Unit
) {
    val res = snackbarHost.showSnackbar(
        message = message,
        actionLabel = "Undo",
        withDismissAction = true,
        duration = SnackbarDuration.Short
    )
    if (res == SnackbarResult.ActionPerformed) onUndo()
}

// ---------- App-wide premium background ----------
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        // soft radial blobs
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            cs.primary.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(120f, 220f),
                        radius = 600f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            cs.tertiary.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(900f, 400f),
                        radius = 700f
                    )
                )
        )
        // gentle vertical fade to keep content readable
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            cs.surface.copy(alpha = 0.55f),
                            Color.Transparent,
                            cs.surface.copy(alpha = 0.55f)
                        )
                    )
                )
        )
        content()
    }
}

// ---------- Navigation ----------
enum class Screen { Home, TaskManager, WeeklyPlanner, Dashboard }

// ---------- App entry ----------
class MainActivity : ComponentActivity() {

    private var cloudInitForUid: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Student_Application_ProjectTheme(dynamicColor = false) {

                val authVm: AuthViewModel =
                    viewModel(factory = AuthViewModelFactory(application))

                val isLoggedIn by authVm.isLoggedIn.collectAsState()
                val error by authVm.error.collectAsState()
                val uid by authVm.currentUserId.collectAsState()

                val appCtx = LocalContext.current.applicationContext
                val snackbarHost = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Anonymous Firebase auth (for Firestore)
                LaunchedEffect(Unit) {
                    try {
                        Firebase.auth.signInAnonymously().await()
                    } catch (_: Exception) {
                    }
                }

                // One-time cloud init per uid
                LaunchedEffect(isLoggedIn, uid) {
                    if (isLoggedIn && uid > 0 && cloudInitForUid != uid) {
                        cloudInitForUid = uid
                        try {
                            val local = TaskPrefs.getTasks(appCtx, uid).first()
                            if (local.isEmpty()) {
                                val remote = CloudTasks.pullAll(uid.toString()) ?: emptyList()
                                if (remote.isNotEmpty()) {
                                    TaskPrefs.saveTasks(appCtx, uid, remote)
                                }
                            }
                            helloFirestore(uid)
                        } catch (_: Exception) {
                        }
                    }
                }

                if (!isLoggedIn) {
                    // Auth screen (separate file)
                    LoginScreen(
                        onLogin = { e, p -> authVm.login(e, p) },
                        onRegister = { e, p -> authVm.register(e, p) },
                        error = error
                    )
                } else {
                    // Mirror DataStore tasks into Room for debugging
                    TaskMirrorEffect(uid = uid)

                    var currentScreen by rememberSaveable { mutableStateOf(Screen.Home) }

                    AppBackground {
                        Scaffold(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            snackbarHost = { SnackbarHost(snackbarHost) },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Home,
                                        onClick = { currentScreen = Screen.Home },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                        label = { Text("Home") },
                                        alwaysShowLabel = false,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.TaskManager,
                                        onClick = { currentScreen = Screen.TaskManager },
                                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                                        label = { Text("Add Task") },
                                        alwaysShowLabel = false,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.WeeklyPlanner,
                                        onClick = { currentScreen = Screen.WeeklyPlanner },
                                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Planner") },
                                        label = { Text("Planner") },
                                        alwaysShowLabel = false,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Dashboard,
                                        onClick = { currentScreen = Screen.Dashboard },
                                        icon = { Icon(Icons.Default.Info, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard") },
                                        alwaysShowLabel = false,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(Modifier.padding(innerPadding)) {
                                when (currentScreen) {
                                    Screen.Home -> HomeScreen(
                                        uid = uid,
                                        snackbarHost = snackbarHost,
                                        onLogout = {
                                            authVm.logout()
                                            scope.launch {
                                                snackbarHost.showSnackbar("Logged In")
                                            }
                                        }
                                    )

                                    Screen.TaskManager -> TaskManagerScreen(
                                        uid = uid,
                                        snackbarHost = snackbarHost
                                    )

                                    Screen.WeeklyPlanner -> WeeklyPlannerScreen(
                                        uid = uid,
                                        snackbarHost = snackbarHost
                                    )

                                    Screen.Dashboard -> DashboardScreen(
                                        uid = uid,
                                        snackbarHost = snackbarHost
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Firestore hello ----------
private suspend fun helloFirestore(uid: Long) {
    val db = Firebase.firestore
    val doc = db.collection("users")
        .document(uid.toString())
        .collection("meta")
        .document("hello")

    doc.set(
        mapOf(
            "message" to "Hi from the app!",
            "ts" to System.currentTimeMillis()
        )
    ).await()
}
