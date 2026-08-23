package com.example.mytvplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.example.mytvplayer.ui.theme.AccentBlue
import com.example.mytvplayer.ui.theme.SidebarDark
import com.example.mytvplayer.ui.theme.TextSecondary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SideBar(
    modifier: Modifier = Modifier,
    onNavItemClick: (String) -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf("Home") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(90.dp)
            .background(SidebarDark)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Logo",
                tint = AccentBlue,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "MYTV",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Navigation Items
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NavItem(Icons.Default.Home, "Home", selectedItem == "Home") { 
                selectedItem = "Home"
                onNavItemClick("Home")
            }
            NavItem(Icons.Default.Movie, "Movies", selectedItem == "Movies") {
                selectedItem = "Movies"
                onNavItemClick("Movies")
            }
            NavItem(Icons.Default.Tv, "TV", selectedItem == "TV Shows") {
                selectedItem = "TV Shows"
                onNavItemClick("TV Shows")
            }
            NavItem(Icons.Default.List, "List", selectedItem == "My List") {
                selectedItem = "My List"
                onNavItemClick("My List")
            }
        }

        // Bottom Items
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NavItem(Icons.Default.Settings, "Settings", selectedItem == "Settings") {
                selectedItem = "Settings"
                onNavItemClick("Settings")
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = AccentBlue.copy(alpha = 0.2f),
            pressedContainerColor = AccentBlue.copy(alpha = 0.3f)
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        modifier = Modifier.size(56.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) AccentBlue else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) AccentBlue else TextSecondary,
                fontSize = 8.sp
            )
        }
    }
}
