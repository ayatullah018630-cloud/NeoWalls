package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.UserActivity
import com.example.data.model.Wallpaper
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandPurple
import com.example.ui.viewmodel.WallpaperViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: WallpaperViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val wallpapers by viewModel.wallpapers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activities by viewModel.activities.collectAsState()

    // Form States
    var wpTitle by remember { mutableStateOf("") }
    var wpCategory by remember { mutableStateOf("") }
    var wpImageUrl by remember { mutableStateOf("") }
    var wpResolution by remember { mutableStateOf("3840x2160") }
    var wpSize by remember { mutableStateOf("2.4 MB") }
    var wpColor by remember { mutableStateOf("#B000FF") }
    var wpFeatured by remember { mutableStateOf(true) }
    var wpTrending by remember { mutableStateOf(false) }
    var wpRecommended by remember { mutableStateOf(true) }

    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryImgUrl by remember { mutableStateOf("") }

    // Navigation sub-tab inside panel: 0 = Stats & Logs, 1 = Upload, 2 = Catalog & Delete, 3 = Ads & Push
    var activeSubTab by remember { mutableStateOf(0) }

    // Ad Config States
    val adFrequency by viewModel.selectedAdFrequency.collectAsState()
    val pushTitle by viewModel.pushNotificationTitle.collectAsState()
    val pushBody by viewModel.pushNotificationBody.collectAsState()

    // Dynamic stats computations based on current DB entries
    val totalDownloads = remember(wallpapers) { wallpapers.sumOf { it.downloadCount } }
    val totalViews = remember(wallpapers) { wallpapers.sumOf { it.viewCount } }
    val averageViews = remember(wallpapers) { if (wallpapers.isNotEmpty()) totalViews / wallpapers.size else 0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NeoWalls Control Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
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
        ) {
            // Dashboard Selector Sub-Tabs
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = activeSubTab == 0, onClick = { activeSubTab = 0 }) {
                    Text("Telemetry & Logs", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = activeSubTab == 1, onClick = { activeSubTab = 1 }) {
                    Text("Publish Console", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = activeSubTab == 2, onClick = { activeSubTab = 2 }) {
                    Text("Catalog Deletion", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = activeSubTab == 3, onClick = { activeSubTab = 3 }) {
                    Text("Ads & Alerts", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeSubTab) {
                    0 -> { // Stats and Activity Telemetry Logs Tab
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Statistics Cards Grid
                            item {
                                Text("System Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatsMetricCard(
                                        title = "Catalog Total",
                                        value = "${wallpapers.size}",
                                        icon = Icons.Outlined.Collections,
                                        color = BrandBlue,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatsMetricCard(
                                        title = "Global Downloads",
                                        value = "$totalDownloads",
                                        icon = Icons.Outlined.CloudDownload,
                                        color = BrandPurple,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatsMetricCard(
                                        title = "Global Impressions",
                                        value = "$totalViews",
                                        icon = Icons.Outlined.Visibility,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatsMetricCard(
                                        title = "Avg Performance",
                                        value = "$averageViews clicks",
                                        icon = Icons.Outlined.QueryStats,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Interactive Activity Log Header
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Real-Time Activity Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { viewModel.clearLogs() }) {
                                        Text("Clear Logs", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (activities.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No recorded user logs yet. Download, set, or favorite wallpapers to generate telemetry stream data.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(activities) { log ->
                                    UserActivityRow(activity = log)
                                }
                            }
                        }
                    }

                    1 -> { // Upload & Category Creation Forms Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Section: Create Category
                            Text("Category Management Console", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newCategoryName,
                                        onValueChange = { newCategoryName = it },
                                        label = { Text("Category Title (e.g. Minimalist)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = newCategoryImgUrl,
                                        onValueChange = { newCategoryImgUrl = it },
                                        label = { Text("Banner Image URL") },
                                        placeholder = { Text("Unsplash landscape URL...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            if (newCategoryName.isNotBlank()) {
                                                viewModel.addAdminCategory(newCategoryName, newCategoryImgUrl)
                                                Toast.makeText(context, "Category '$newCategoryName' added successfully!", Toast.LENGTH_SHORT).show()
                                                newCategoryName = ""
                                                newCategoryImgUrl = ""
                                            } else {
                                                Toast.makeText(context, "Title is required!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Register Category")
                                    }
                                }
                            }

                            // Section: Upload Wallpaper
                            Text("Publish New Premium Wallpaper", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = wpTitle,
                                        onValueChange = { wpTitle = it },
                                        label = { Text("Wallpaper Title") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    // Category Select
                                    OutlinedTextField(
                                        value = wpCategory,
                                        onValueChange = { wpCategory = it },
                                        label = { Text("Category (e.g. AMOLED, Minimal, Nature)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = wpImageUrl,
                                        onValueChange = { wpImageUrl = it },
                                        label = { Text("Source Image URL (Unsplash/Direct Link)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = wpResolution,
                                            onValueChange = { wpResolution = it },
                                            label = { Text("Resolution") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = wpSize,
                                            onValueChange = { wpSize = it },
                                            label = { Text("File Size") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    OutlinedTextField(
                                        value = wpColor,
                                        onValueChange = { wpColor = it },
                                        label = { Text("Dominant Hex Tint Color") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    // Toggles
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = wpFeatured,
                                            onClick = { wpFeatured = !wpFeatured },
                                            label = { Text("Featured Slider") }
                                        )
                                        FilterChip(
                                            selected = wpTrending,
                                            onClick = { wpTrending = !wpTrending },
                                            label = { Text("Trending") }
                                        )
                                        FilterChip(
                                            selected = wpRecommended,
                                            onClick = { wpRecommended = !wpRecommended },
                                            label = { Text("Recommended") }
                                        )
                                    }

                                    var showScheduleDialog by remember { mutableStateOf(false) }
                                    Button(
                                        onClick = {
                                            if (wpTitle.isNotBlank() && wpCategory.isNotBlank() && wpImageUrl.isNotBlank()) {
                                                showScheduleDialog = true
                                            } else {
                                                Toast.makeText(context, "Please fill in title, category, and Image URL first!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Schedule")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Schedule / Publish Wallpaper")
                                    }

                                    if (showScheduleDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showScheduleDialog = false },
                                            title = { Text("Select Publishing Mode") },
                                            text = { Text("Choose whether to publish instantly or schedule publishing for tomorrow.") },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.addAdminWallpaper(
                                                            wpTitle, wpCategory, wpImageUrl, wpResolution, wpSize, wpColor,
                                                            wpFeatured, wpTrending, wpRecommended
                                                        )
                                                        Toast.makeText(context, "Wallpaper published instantly!", Toast.LENGTH_LONG).show()
                                                        showScheduleDialog = false
                                                        // Reset form
                                                        wpTitle = ""
                                                        wpImageUrl = ""
                                                    }
                                                ) { Text("Publish Instantly") }
                                            },
                                            dismissButton = {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.addAdminWallpaper(
                                                            "[SCHEDULED] $wpTitle", wpCategory, wpImageUrl, wpResolution, wpSize, wpColor,
                                                            wpFeatured, wpTrending, wpRecommended
                                                        )
                                                        Toast.makeText(context, "Wallpaper publication scheduled for tomorrow!", Toast.LENGTH_LONG).show()
                                                        showScheduleDialog = false
                                                        wpTitle = ""
                                                        wpImageUrl = ""
                                                    }
                                                ) { Text("Schedule (Tomorrow)") }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Catalog Management & Deletion Grid Tab
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text("Active Wallpaper Index", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("A list of all wallpapers currently stored in the catalog. Removing wallpapers will erase them from all user views.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            items(wallpapers) { wp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = wp.imageUrl,
                                        contentDescription = wp.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = wp.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(text = "Category: ${wp.category} • Size: ${wp.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteWallpaper(wp.id, wp.title)
                                            Toast.makeText(context, "Successfully deleted '${wp.title}' from catalog!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Push Notifications and AdMob settings Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Section: Push Notification Simulation
                            Text("FCM Push Notification Broadcaster", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = pushTitle,
                                        onValueChange = { viewModel.pushNotificationTitle.value = it },
                                        label = { Text("Notification Header Title") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = pushBody,
                                        onValueChange = { viewModel.pushNotificationBody.value = it },
                                        label = { Text("Notification Description Body") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.addAdminWallpaper(
                                                title = "[PUSH LOG] Title: $pushTitle",
                                                category = "Broadcasting Alerts",
                                                imageUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=1080",
                                                resolution = "Broadcast",
                                                size = "FCM Notification",
                                                colorHex = "#00E5FF",
                                                isFeatured = false,
                                                isTrending = false,
                                                isRecommended = false
                                            )
                                            Toast.makeText(context, "Broadcast success! Sending notification: '$pushTitle' to 12.5k devices...", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "Broadcast")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Broadcast FCM Alert")
                                    }
                                }
                            }

                            // Section: AdMob Integration settings
                            Text("Google AdMob Frequency Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Simulated Advertisement Display Rates:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    val frequencies = listOf("Ad-Free VIP", "Light Ads (Low Intrusion)", "Standard (Balanced)", "High Exposure (Aggressive)")
                                    frequencies.forEach { freq ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.selectedAdFrequency.value = freq
                                                    Toast.makeText(context, "Ad display rate set to $freq", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = adFrequency == freq, onClick = null)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(freq)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .padding(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun UserActivityRow(activity: UserActivity) {
    val dateStr = remember(activity.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault())
        sdf.format(Date(activity.timestamp))
    }

    val icon = when (activity.activityType) {
        "Favorite" -> Icons.Default.Favorite
        "Unfavorite" -> Icons.Outlined.FavoriteBorder
        "Download" -> Icons.Default.Download
        "Set Home Screen" -> Icons.Default.Wallpaper
        "Set Lock Screen" -> Icons.Default.ScreenLockPortrait
        "Set Both Screens" -> Icons.Default.Devices
        "Generate AI" -> Icons.Default.AutoAwesome
        "Save Generated Wallpaper" -> Icons.Default.Save
        "Admin Upload" -> Icons.Default.CloudUpload
        "Admin Delete" -> Icons.Default.Delete
        else -> Icons.Default.Info
    }

    val iconColor = when (activity.activityType) {
        "Favorite", "Unfavorite" -> Color.Red
        "Download" -> BrandBlue
        "Set Home Screen", "Set Lock Screen", "Set Both Screens" -> BrandPurple
        "Generate AI", "Save Generated Wallpaper" -> Color.Cyan
        "Admin Upload", "Admin Delete" -> Color.Yellow
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f))
                .padding(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = activity.activityType, tint = iconColor, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${activity.activityType}: ${activity.wallpaperTitle}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            if (activity.details.isNotBlank()) {
                Text(
                    text = activity.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 2
                )
            }
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
