package com.nourgad.sahtein.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nourgad.sahtein.data.MealSlot
import com.nourgad.sahtein.domain.LangCtx
import com.nourgad.sahtein.domain.Muscle
import com.nourgad.sahtein.domain.Place
import com.nourgad.sahtein.domain.S
import java.time.LocalTime

private val tabs = listOf("today", "history", "log", "workouts", "progress")

/** The meal closest to now: the last one whose time has come (up to 1 h early), else the first. */
fun currentSlot(slots: List<MealSlot>): MealSlot? {
    val now = LocalTime.now().let { it.hour * 60 + it.minute }
    return slots.lastOrNull { it.minuteOfDay <= now + 60 } ?: slots.firstOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: AppVM, openRequest: String?, onOpenHandled: () -> Unit, requestNotifications: () -> Unit) {
    val state by vm.profile.collectAsStateWithLifecycle()
    if (!state.loaded) { Box(Modifier.fillMaxSize()); return }
    val profile = state.profile
    if (profile == null) {
        OnboardingScreen(vm, requestNotifications)
        return
    }
    val lang = LangCtx(LangCtx.deviceIsArabic(), profile.female)
    val nav = rememberNavController()
    val slots by vm.slots.collectAsStateWithLifecycle()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "today"
    val topRoute = route.substringBefore('/')
    var place by rememberSaveable { mutableStateOf(Place.GYM) }

    LaunchedEffect(openRequest, slots) {
        val req = openRequest ?: return@LaunchedEffect
        if (slots.isEmpty()) return@LaunchedEffect
        when {
            req.startsWith("log:") -> nav.navigate("log/${req.removePrefix("log:")}/${vm.today()}/1")
            req == "summary" -> nav.navigate("summary/${vm.today()}")
        }
        onOpenHandled()
    }

    CompositionLocalProvider(LocalLang provides lang) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(titleFor(topRoute, lang)) },
                    navigationIcon = {
                        if (topRoute !in tabs) IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, lang.t(S.back))
                        }
                    },
                    actions = {
                        if (topRoute != "settings") IconButton(onClick = { nav.navigate("settings") }) {
                            Icon(Icons.Default.Settings, lang.t(S.settings))
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    navItem(nav, topRoute, "today", Icons.Default.Home, lang.t(S.today))
                    navItem(nav, topRoute, "history", Icons.Default.CalendarMonth, lang.t(S.history))
                    NavigationBarItem(
                        selected = topRoute == "log",
                        onClick = { currentSlot(slots)?.let { nav.navigate("log/${it.id}/${vm.today()}/0") } },
                        icon = { Icon(Icons.Default.AddCircle, null) },
                        label = { Text(lang.t(S.log)) },
                    )
                    navItem(nav, topRoute, "workouts", Icons.Default.FitnessCenter, lang.t(S.workouts))
                    navItem(nav, topRoute, "progress", Icons.Default.ShowChart, lang.t(S.progress))
                }
            },
        ) { padding ->
            NavHost(nav, startDestination = "today") {
                composable("today") {
                    TodayScreen(vm, profile, padding,
                        onLog = { nav.navigate("log/$it/${vm.today()}/0") },
                        onSummary = { nav.navigate("summary/${vm.today()}") })
                }
                composable("log/{slot}/{day}/{voice}") { e ->
                    val a = e.arguments
                    LogScreen(vm, profile, a?.getString("slot")?.toLongOrNull() ?: 0, a?.getString("day")?.toLongOrNull() ?: vm.today(),
                        padding, startWithVoice = a?.getString("voice") == "1")
                }
                composable("summary/{day}") { e ->
                    SummaryScreen(vm, profile, e.arguments?.getString("day")?.toLongOrNull() ?: vm.today(), padding)
                }
                composable("history") {
                    HistoryScreen(vm, profile, padding, onDay = { nav.navigate("day/$it") }, onWeek = { nav.navigate("week/$it") })
                }
                composable("day/{day}") { e ->
                    val day = e.arguments?.getString("day")?.toLongOrNull() ?: vm.today()
                    DayScreen(vm, profile, day, padding, onLog = { slot -> nav.navigate("log/$slot/$day/0") })
                }
                composable("week/{start}") { e ->
                    WeekScreen(vm, profile, e.arguments?.getString("start")?.toLongOrNull() ?: vm.today(), padding)
                }
                composable("workouts") {
                    WorkoutsScreen(profile, padding, place, { place = it },
                        onMuscle = { nav.navigate("muscle/${it.name}/${place.name}") },
                        onPlan = { nav.navigate("plan/$it") },
                        hasActive = vm.activeSets.isNotEmpty(),
                        onResume = { nav.navigate("active") })
                }
                composable("muscle/{m}/{p}") { e ->
                    val m = Muscle.valueOf(e.arguments?.getString("m") ?: Muscle.CHEST.name)
                    val p = Place.valueOf(e.arguments?.getString("p") ?: Place.GYM.name)
                    MuscleScreen(m, p, padding, onExercise = { nav.navigate("exercise/$it") })
                }
                composable("exercise/{id}") { e ->
                    ExerciseScreen(e.arguments?.getString("id") ?: "", padding, onLogThis = { ex ->
                        vm.startWorkout(lang.t(ex.name), listOf(ex.id)) { ex.sets }
                        nav.navigate("active")
                    })
                }
                composable("plan/{id}") { e ->
                    PlanScreen(e.arguments?.getString("id") ?: "", padding,
                        onStart = { title, ids ->
                            vm.startWorkout(title, ids) { id -> com.nourgad.sahtein.domain.ExerciseDb.byId[id]?.sets ?: 3 }
                            nav.navigate("active")
                        },
                        onExercise = { nav.navigate("exercise/$it") })
                }
                composable("active") {
                    ActiveWorkoutScreen(vm, padding, onDone = { nav.popBackStack("workouts", inclusive = false) })
                }
                composable("progress") { ProgressScreen(vm, profile, padding) }
                composable("settings") { SettingsScreen(vm, profile, padding) }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.navItem(
    nav: NavHostController, current: String, route: String, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String,
) {
    NavigationBarItem(
        selected = current == route,
        onClick = {
            nav.navigate(route) {
                popUpTo("today") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        icon = { Icon(icon, null) },
        label = { Text(label) },
    )
}

private fun titleFor(route: String, lang: LangCtx): String = when (route) {
    "today" -> lang.t(S.appName)
    "history", "day" -> lang.t(S.history)
    "week" -> lang.t(S.weeklyReport)
    "log" -> lang.t(S.log)
    "summary" -> lang.t(S.dailySummary)
    "workouts", "muscle", "exercise", "plan", "active" -> lang.t(S.workouts)
    "progress" -> lang.t(S.progress)
    "settings" -> lang.t(S.settings)
    else -> lang.t(S.appName)
}
