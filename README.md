# Student Application Project 

A Kotlin & Jetpack Compose Android application that helps university students manage their study workload with tasks, weekly planning, and a simple progress dashboard.

This project is developed as part of **CP3406 – Mobile App Development** at James Cook University.

---

## 🎯 App Overview

The **Student Application** is designed for students who want a simple but structured way to:

- Create and manage study tasks (assignments, readings, exams, etc.)
- See what’s coming up this week in a planner view
- Track progress with a visual dashboard (pending, completed, overdue)

The app is built entirely in Kotlin using **Jetpack Compose** and **modern Android APIs**, following the requirements of CP3406 Assignment

---

## ✨ Core Features

### 1. Task Management (Home Screen)
- Add new tasks with:
  - Title / description  
  - Subject / category  
  - Due date  
  - Status (e.g. Pending / Completed)
- Edit existing tasks
- Delete tasks
- Tasks are grouped logically (e.g. by status or due date) so students can quickly see what to focus on.

### 2. Weekly Planner Screen
- Displays tasks distributed across days of the week.
- If multiple tasks fall on the same weekday (different dates), they appear under the same day column.
- Focuses on which tasks belong to each day, not on exact time slots.
- Uses the tasks created on the Home screen as the single source of truth.

### 3. Dashboard Screen
- Summary view of overall study workload, for example:
  - 📌 Number of tasks waiting to be finished
  - ✅ Number of completed tasks
  - ⏰ Number of tasks past the deadline
- Simple visual indicators (e.g. percentages / progress-style display) to give a quick sense of progress and workload balance.
- Encourages streaks and consistency by showing how many tasks the student has been completing.

### 4. Persistent Storage
- Tasks are saved locally on the device using a `TaskPrefs` helper (based on persistent storage such as SharedPreferences/DataStore).
- Data is automatically loaded when the app starts, so users don’t lose their task list between sessions.

### 5. Modern UI with Jetpack Compose
- Built entirely with **Jetpack Compose** and **Material 3** components.
- Uses:
  - `Scaffold` (top-level layout)
  - `NavigationBar` (bottom navigation between main screens)
  - `SnackbarHost` (feedback for user actions)
- Consistent styling and colors via a shared app background (`AppBackground`) for a clean, student-friendly look.

---

## 🧭 Navigation

The app uses a bottom navigation bar to move between core screens:

- 🏠 **Home** – create, view, update, delete study tasks  
- 📅 **Weekly Planner** – see tasks mapped to days of the week  
- 📊 **Dashboard** – view stats and completion progress  

Navigation is implemented with modern Compose patterns, keeping each screen as a separate composable for better cohesion and maintainability.

---

## 🧱 Architecture & Code Structure

The app follows a modular and maintainable structure:

- **Language:** Kotlin  
- **UI:** Jetpack Compose + Material 3  
- **State Management:** Compose `remember` / `mutableStateOf` and flows from persistent storage  
- **Persistence:** `TaskPrefs` helper for storing and loading tasks  
- **Package name:** `com.duy842.student_application_project`

> _Note:_ Room, API networking, WorkManager, and runtime permissions can be extended on top of this structure if required for additional features.

Example high-level structure:

```text
com.duy842.student_application_project
├─ MainActivity.kt          // Sets up AppBackground, Scaffold, bottom navigation
├─ ui/
│  ├─ HomeScreen.kt        // Task list, add/edit/delete tasks
│  ├─ WeeklyPlannerScreen.kt // Weekly view of tasks grouped by day
│  ├─ DashboardScreen.kt   // Summary, counts, and progress visuals
├─ data/
│  ├─ Task.kt              // Data model for tasks
│  ├─ TaskPrefs.kt         // Persistence helper for saving/loading tasks
└─ theme/
   ├─ Color.kt, Theme.kt   // App theming and styling
