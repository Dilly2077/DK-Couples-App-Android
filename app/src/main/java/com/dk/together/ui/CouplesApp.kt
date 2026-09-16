package com.dk.together.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dk.together.ui.screens.ExploreScreen
import com.dk.together.ui.screens.HomeScreen
import com.dk.together.ui.screens.OnboardingScreen
import com.dk.together.ui.screens.TimelineScreen
import com.dk.together.ui.screens.TogetherScreen
import com.dk.together.ui.screens.UsScreen

private data class NavItem(val label: String, val symbol: String)

@Composable
fun CouplesApp(viewModel: AppViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.prefs.profile.onboarded) {
        OnboardingScreen(onSave = viewModel::saveProfile)
        return
    }

    val items = remember {
        listOf(
            NavItem("Home", "⌂"),
            NavItem("Explore", "✦"),
            NavItem("Discuss", "◌"),
            NavItem("Dates", "◷"),
            NavItem("Us", "♡")
        )
    }
    var selected by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = {
                            Text(
                                item.symbol,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (selected) {
            0 -> HomeScreen(state, viewModel, Modifier.padding(padding))
            1 -> ExploreScreen(state, viewModel, Modifier.padding(padding))
            2 -> TogetherScreen(state, viewModel, Modifier.padding(padding))
            3 -> TimelineScreen(state, viewModel, Modifier.padding(padding))
            else -> UsScreen(state, viewModel, Modifier.padding(padding))
        }
    }
}
