package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Wallpaper
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandPurple
import com.example.ui.viewmodel.SettingWallpaperState
import com.example.ui.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    viewModel: WallpaperViewModel,
    wallpaper: Wallpaper,
    onBackClick: () -> Unit,
    onEditClick: (Wallpaper) -> Unit,
    onSimilarWallpaperClick: (Wallpaper) -> Unit
) {
    val context = LocalContext.current
    val isFavorite by viewModel.isFavorite(wallpaper.id).collectAsState(initial = false)
    val isDownloaded by viewModel.isDownloaded(wallpaper.id).collectAsState(initial = false)
    val settingState by viewModel.settingState.collectAsState()
    val allWallpapers by viewModel.wallpapers.collectAsState()

    // Query similar wallpapers of same category
    val similarWallpapers = remember(wallpaper, allWallpapers) {
        allWallpapers.filter { it.category == wallpaper.category && it.id != wallpaper.id }
    }

    var showSetDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }

    // Register active wallpaper detail analytics
    LaunchedEffect(key1 = wallpaper.id) {
        viewModel.selectWallpaper(wallpaper)
    }

    // Handle wallpaper setting success/error
    LaunchedEffect(key1 = settingState) {
        when (settingState) {
            is SettingWallpaperState.Success -> {
                Toast.makeText(context, "Wallpaper successfully set!", Toast.LENGTH_LONG).show()
                viewModel.resetSettingState()
            }
            is SettingWallpaperState.Error -> {
                Toast.makeText(context, "Error: ${(settingState as SettingWallpaperState.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetSettingState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen Background Image with high-performance aspect ratio cropping
        AsyncImage(
            model = wallpaper.imageUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Vignette Shadow Mask Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Top Navigation and Actions Bar (Status notch safe)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Favorite Toggle Button
                IconButton(
                    onClick = { viewModel.toggleFavorite(wallpaper, !isFavorite) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("favorite_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Look at this incredible wallpaper: '${wallpaper.title}' in ${wallpaper.category}! Download NeoWalls to see more!")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Details and Controls Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wallpaper Basic Card with frosted glass style
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title and Author
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wallpaper.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "By ${wallpaper.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Info toggle button
                        IconButton(onClick = { showInfoSheet = !showInfoSheet }) {
                            Icon(
                                imageVector = if (showInfoSheet) Icons.Default.Close else Icons.Default.Info,
                                contentDescription = "Toggle Info",
                                tint = Color.White
                            )
                        }
                    }

                    // Frosted expand info details (Moshi metadata mapping)
                    AnimatedVisibility(
                        visible = showInfoSheet,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            InfoBadgeRow(label = "Category", value = wallpaper.category)
                            InfoBadgeRow(label = "Resolution", value = wallpaper.resolution)
                            InfoBadgeRow(label = "File Size", value = wallpaper.size)
                            InfoBadgeRow(label = "Dominant Tint", value = wallpaper.colorHex)
                            InfoBadgeRow(label = "Downloads", value = "${wallpaper.downloadCount}")
                            InfoBadgeRow(label = "Views", value = "${wallpaper.viewCount}")
                        }
                    }

                    // Main Actions Bar: Set Screen, Download, Edit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Apply Wallpaper Button
                        Button(
                            onClick = { showSetDialog = true },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(52.dp)
                                .testTag("set_wallpaper_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                        ) {
                            if (settingState is SettingWallpaperState.Setting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Wallpaper, contentDescription = "Apply")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Apply Screen", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Edit Button (Custom Canvas Editor)
                        IconButton(
                            onClick = { onEditClick(wallpaper) },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Canvas",
                                tint = Color.White
                            )
                        }

                        // Download Offline Button
                        IconButton(
                            onClick = {
                                viewModel.addDownloadedWallpaper(wallpaper, "neowalls_vault/${wallpaper.title}.jpg")
                                Toast.makeText(context, "Successfully downloaded to local gallery!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("download_wallpaper_button")
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                contentDescription = "Download",
                                tint = if (isDownloaded) BrandBlue else Color.White
                            )
                        }
                    }
                }
            }

            // Similar Wallpapers Recommendation Row
            if (similarWallpapers.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Similar Recommendations",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(similarWallpapers) { simWp ->
                            SimilarWallpaperPill(
                                wallpaper = simWp,
                                onClick = { onSimilarWallpaperClick(simWp) }
                            )
                        }
                    }
                }
            }

            // Report content link
            Text(
                text = "Inappropriate content? Report Wallpaper",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier
                    .clickable { showReportDialog = true }
                    .padding(8.dp)
            )
        }

        // Apply Screens Selector Dialog
        if (showSetDialog) {
            AlertDialog(
                onDismissRequest = { showSetDialog = false },
                title = { Text("Set Wallpaper Destination") },
                text = { Text("Where would you like to apply the background?") },
                confirmButton = {},
                dismissButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setWallpaper(wallpaper.imageUrl, wallpaper.title, 1)
                                showSetDialog = false
                                Toast.makeText(context, "Setting Home Screen background...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Home Screen") }
                        Button(
                            onClick = {
                                viewModel.setWallpaper(wallpaper.imageUrl, wallpaper.title, 2)
                                showSetDialog = false
                                Toast.makeText(context, "Setting Lock Screen background...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Lock Screen") }
                        Button(
                            onClick = {
                                viewModel.setWallpaper(wallpaper.imageUrl, wallpaper.title, 3)
                                showSetDialog = false
                                Toast.makeText(context, "Setting both screen background...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Set Both Screens") }
                        TextButton(
                            onClick = { showSetDialog = false },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Cancel") }
                    }
                }
            )
        }

        // Report Dialog
        if (showReportDialog) {
            var reportReason by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("Report Content") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Please describe why this wallpaper violates policies (e.g. copyrighted material, inappropriate graphics).")
                        OutlinedTextField(
                            value = reportReason,
                            onValueChange = { reportReason = it },
                            placeholder = { Text("Details...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (reportReason.isNotBlank()) {
                                viewModel.addDownloadedWallpaper(wallpaper, "Reported Content flag")
                                viewModel.addAdminWallpaper(
                                    title = "[FLAGGED] ${wallpaper.title}",
                                    category = "Reported Logs",
                                    imageUrl = wallpaper.imageUrl,
                                    resolution = wallpaper.resolution,
                                    size = wallpaper.size,
                                    colorHex = "#FF1744",
                                    isFeatured = false,
                                    isTrending = false,
                                    isRecommended = false
                                )
                                viewModel.deleteWallpaper(wallpaper.id, wallpaper.title)
                                Toast.makeText(context, "Thank you. Wallpaper reported and removed from local indexing.", Toast.LENGTH_LONG).show()
                                showReportDialog = false
                                onBackClick()
                            } else {
                                Toast.makeText(context, "Reason is required!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) { Text("Submit Report", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun InfoBadgeRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SimilarWallpaperPill(
    wallpaper: Wallpaper,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = wallpaper.imageUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        Text(
            text = wallpaper.title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
        )
    }
}
