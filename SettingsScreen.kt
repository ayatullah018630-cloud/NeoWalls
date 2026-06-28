package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandPurple
import com.example.ui.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WallpaperViewModel,
    onNavigateToAdmin: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    isDarkThemeActive: Boolean
) {
    val context = LocalContext.current
    var downloadQuality by remember { mutableStateOf("Ultra HD (4K)") }
    var pushEnabled by remember { mutableStateOf(true) }
    var adFreePremium by remember { mutableStateOf(false) }

    var showCacheDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var activeLanguage by remember { mutableStateOf("English") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings & Profiles",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Premium Developer Control / Admin Panel Entry Card (M3 styled with Neon Brand Gradients)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandBlue, BrandPurple)
                        )
                    )
                    .clickable(onClick = onNavigateToAdmin)
                    .padding(20.dp)
                    .testTag("admin_panel_entry")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Control Center",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Configure publishing schedules, view download activity, manage simulated ads, and batch upload wallpapers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = Color.White
                        )
                    }
                }
            }

            // Group: Visual Experience
            SettingsGroup(title = "Visual Experience") {
                SettingsSwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Aesthetic Mode",
                    subtitle = "Reduce eye strain with deep midnight shades",
                    checked = isDarkThemeActive,
                    onCheckedChange = onToggleTheme
                )

                SettingsClickableRow(
                    icon = Icons.Outlined.Translate,
                    title = "Language Selection",
                    subtitle = "Current: $activeLanguage",
                    onClick = { showLanguageDialog = true }
                )
            }

            // Group: Downloads & Cache
            SettingsGroup(title = "Downloads & Local Caching") {
                SettingsClickableRow(
                    icon = Icons.Outlined.HighQuality,
                    title = "Wallpaper Quality",
                    subtitle = "Preferred: $downloadQuality",
                    onClick = {
                        val qualities = listOf("Standard HD", "Full HD (1080p)", "2K Quad HD", "Ultra HD (4K)")
                        val currentIdx = qualities.indexOf(downloadQuality)
                        downloadQuality = qualities[(currentIdx + 1) % qualities.size]
                        Toast.makeText(context, "Preference set to $downloadQuality", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsSwitchRow(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Push Notifications",
                    subtitle = "Alert on newly featured category additions",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )

                SettingsClickableRow(
                    icon = Icons.Outlined.DeleteSweep,
                    title = "Clear Cached Wallpapers",
                    subtitle = "Reclaims storage space by clearing thumbnail cache",
                    onClick = { showCacheDialog = true }
                )
            }

            // Group: Support & Feedback
            SettingsGroup(title = "Information & Support") {
                SettingsClickableRow(
                    icon = Icons.Outlined.Info,
                    title = "About NeoWalls",
                    subtitle = "Version 1.0.0 (Release Build v1)",
                    onClick = {
                        Toast.makeText(context, "NeoWalls Premium App, built with Jetpack Compose.", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsClickableRow(
                    icon = Icons.Outlined.Mail,
                    title = "Contact Support & Feedback",
                    subtitle = "Submit bug reports or suggest feature ideas",
                    onClick = {
                        Toast.makeText(context, "Launching system mail client to support@neowalls.io...", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsClickableRow(
                    icon = Icons.Outlined.Policy,
                    title = "Privacy Policy & Terms",
                    subtitle = "Read our official GDPR compliance terms",
                    onClick = {
                        Toast.makeText(context, "Displaying browser privacy terms...", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsClickableRow(
                    icon = Icons.Outlined.ThumbUp,
                    title = "Rate App & Share",
                    subtitle = "Share NeoWalls with friends and rate us 5 stars",
                    onClick = {
                        Toast.makeText(context, "Sharing NeoWalls download link...", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirmation Dialogs
            if (showCacheDialog) {
                AlertDialog(
                    onDismissRequest = { showCacheDialog = false },
                    title = { Text("Clear Wallpaper Cache?") },
                    text = { Text("This will clear all temporary cached thumbnail files. Saved or favorited items will not be affected.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCacheDialog = false
                                Toast.makeText(context, "Cache successfully cleared!", Toast.LENGTH_SHORT).show()
                            }
                        ) { Text("Clear", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCacheDialog = false }) { Text("Cancel") }
                    }
                )
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text("Choose Language") },
                    text = {
                        Column {
                            val languages = listOf("English", "Spanish (Español)", "French (Français)", "German (Deutsch)", "Arabic (العربية)")
                            languages.forEach { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeLanguage = lang
                                            showLanguageDialog = false
                                            Toast.makeText(context, "Language switched to $lang", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = activeLanguage == lang, onClick = null)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(lang)
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
