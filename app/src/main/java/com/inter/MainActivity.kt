package com.inter

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inter.ui.editor.EditorScreen
import com.inter.ui.main.HomeScreen
import com.inter.ui.main.NoteViewModel
import com.inter.ui.main.ViewModelFactory
import com.inter.ui.settings.SettingsScreen
import com.inter.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Setup Nougat-compliant launcher rich widgets dynamic shortcuts
        setupDynamicShortcuts()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val activeNavController = rememberNavController()
                    navController = activeNavController

                    val factory = ViewModelFactory(application)
                    val viewModel: NoteViewModel = viewModel(factory = factory)

                    // Navigation Host Definition
                    NavHost(navController = activeNavController, startDestination = "home") {
                        composable("home") {
                            // Check for direct textual search shortcut triggers
                            val queryTrigger = intent?.getBooleanExtra("action_trigger_search", false) ?: false
                            if (queryTrigger) {
                                viewModel.setSearchQuery("")
                                intent?.removeExtra("action_trigger_search")
                            }

                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToEditor = { noteId ->
                                    activeNavController.navigate("editor/$noteId")
                                },
                                onNavigateToSettings = {
                                    activeNavController.navigate("settings")
                                }
                            )
                        }

                        composable("editor/{noteId}") { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: -1
                            EditorScreen(
                                viewModel = viewModel,
                                noteId = noteId,
                                onNavigateBack = {
                                    activeNavController.popBackStack()
                                }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    activeNavController.popBackStack()
                                }
                            )
                        }
                    }

                    // Process incoming deep link signals and navigate accordingly
                    LaunchedEffect(intent) {
                        processIncomingIntent(intent, activeNavController)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::navController.isInitialized) {
            processIncomingIntent(intent, navController)
        }
    }

    // Dynamic Nougat+ (API 25) long-press Launcher Shortcuts setup
    private fun setupDynamicShortcuts() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return

                // 1. New note creation shortcut
                val createShortcut = ShortcutInfo.Builder(this, "shortcut_create_new")
                    .setShortLabel("New Note")
                    .setLongLabel("Create a fresh secure note")
                    .setIcon(Icon.createWithResource(this, android.R.drawable.ic_input_add))
                    .setIntent(Intent(this, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("action_trigger_new_note", true)
                    })
                    .build()

                // 2. Clear vault exploration search shortcut
                val searchShortcut = ShortcutInfo.Builder(this, "shortcut_search_vault")
                    .setShortLabel("Search Notes")
                    .setLongLabel("Search your dynamic local vaults")
                    .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_search))
                    .setIntent(Intent(this, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("action_trigger_search", true)
                    })
                    .build()

                shortcutManager.dynamicShortcuts = listOf(createShortcut, searchShortcut)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Deep routing coordinator parsing triggers from Home widgets or background services
    private fun processIncomingIntent(incomingIntent: Intent?, controller: NavController) {
        if (incomingIntent == null) return

        if (incomingIntent.getBooleanExtra("action_trigger_new_note", false)) {
            incomingIntent.removeExtra("action_trigger_new_note")
            controller.navigate("editor/-1") {
                popUpTo("home") { saveState = true }
            }
        } else if (incomingIntent.getBooleanExtra("action_trigger_open_note", false)) {
            val id = incomingIntent.getIntExtra("EXTRA_NOTE_ID", -1)
            incomingIntent.removeExtra("action_trigger_open_note")
            if (id != -1) {
                controller.navigate("editor/$id") {
                    popUpTo("home") { saveState = true }
                }
            }
        }
    }
}
