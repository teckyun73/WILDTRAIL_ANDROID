package com.wildtrail.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

internal enum class AppTab(
    val label: String,
    val marker: String,
    val subtitle: String,
) {
    Identify("식별", "ID", "Image identification"),
    Status("상태", "ST", "Backend readiness"),
    Species("도감", "SP", "Species encyclopedia"),
    Records("기록", "RC", "Observation records"),
    Trips("여행", "TR", "Trip planner"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WildTrailTopBar(selectedTab: AppTab) {
    TopAppBar(
        title = {
            Column {
                Text("WildTrail", fontWeight = FontWeight.Bold)
                Text(
                    selectedTab.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
    )
}

@Composable
internal fun WildTrailBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Text(tab.marker, style = MaterialTheme.typography.labelMedium) },
                label = { Text(tab.label) },
            )
        }
    }
}
