package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Wallpaper
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandPurple
import com.example.ui.viewmodel.WallpaperViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperEditorScreen(
    viewModel: WallpaperViewModel,
    wallpaper: Wallpaper,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current

    // Transformation States
    var rotationAngle by remember { mutableStateOf(0f) }
    var flipX by remember { mutableStateOf(1f) }
    var flipY by remember { mutableStateOf(1f) }
    var blurValue by remember { mutableStateOf(0f) }

    // Color Tuning States
    var brightness by remember { mutableStateOf(0f) } // -0.5f to 0.5f
    var contrast by remember { mutableStateOf(1f) }   // 0.5f to 1.5f
    var saturation by remember { mutableStateOf(1f) } // 0f to 2f

    // Filters Preset State: 0 = None, 1 = Grayscale, 2 = Sepia, 3 = Cyberpunk, 4 = Cold Cyan
    var selectedFilterPreset by remember { mutableStateOf(0) }

    // Text Overlay State
    var textOverlayInput by remember { mutableStateOf("") }
    var textColorState by remember { mutableStateOf(Color.White) }
    var textScaleState by remember { mutableStateOf(20f) }

    // Sticker Overlay State: list of active sticker names
    var activeStickers by remember { mutableStateOf<List<String>>(emptyList()) }

    // Active Editor Tab: 0 = Transform, 1 = Filters, 2 = Adjust, 3 = Overlays, 4 = Stickers
    var activeTab by remember { mutableStateOf(0) }

    // Construct final Compose Color Matrix combining sliders and filters
    val finalColorMatrix = remember(brightness, contrast, saturation, selectedFilterPreset) {
        val matrix = ColorMatrix()

        // Apply dynamic brightness, contrast, and saturation
        val brightnessScale = brightness * 255f
        floatArrayOf(
            contrast, 0f, 0f, 0f, brightnessScale,
            0f, contrast, 0f, 0f, brightnessScale,
            0f, 0f, contrast, 0f, brightnessScale,
            0f, 0f, 0f, 1f, 0f
        ).copyInto(matrix.values)

        // Apply saturation
        val saturationMatrix = ColorMatrix().apply { setToSaturation(saturation) }
        matrix.timesAssign(saturationMatrix)

        // Apply preset matrix overrides
        when (selectedFilterPreset) {
            1 -> { // Grayscale
                val gray = ColorMatrix().apply { setToSaturation(0f) }
                matrix.timesAssign(gray)
            }
            2 -> { // Warm Sepia
                val sepia = ColorMatrix()
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ).copyInto(sepia.values)
                matrix.timesAssign(sepia)
            }
            3 -> { // Cyberpunk Pink/Blue Hue
                val cyberpunk = ColorMatrix()
                floatArrayOf(
                    1.2f, 0f, 0.5f, 0f, 0f,
                    0f, 0.8f, 0.8f, 0f, 0f,
                    0.5f, 0f, 1.5f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ).copyInto(cyberpunk.values)
                matrix.timesAssign(cyberpunk)
            }
            4 -> { // Cold Cyan
                val coldCyan = ColorMatrix()
                floatArrayOf(
                    0.8f, 0f, 0f, 0f, 0f,
                    0f, 1.1f, 0f, 0f, 0f,
                    0f, 0f, 1.4f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ).copyInto(coldCyan.values)
                matrix.timesAssign(coldCyan)
            }
        }
        matrix
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NeoWalls Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save Button
                    TextButton(
                        onClick = {
                            val id = "edited_" + UUID.randomUUID().toString().take(8)
                            val customWallpaper = Wallpaper(
                                id = id,
                                title = "Edited: ${wallpaper.title}",
                                category = "Downloads",
                                imageUrl = wallpaper.imageUrl,
                                resolution = wallpaper.resolution,
                                size = "Custom Render (2.1 MB)",
                                author = "Me (NeoWalls Creator)",
                                colorHex = wallpaper.colorHex,
                                isFeatured = false,
                                isTrending = false,
                                isRecommended = false,
                                publishDate = System.currentTimeMillis(),
                                downloadCount = 1,
                                viewCount = 1
                            )
                            viewModel.addDownloadedWallpaper(customWallpaper, "Custom Edited Gallery")
                            Toast.makeText(context, "Saved changes to Downloads catalog!", Toast.LENGTH_LONG).show()
                            onSaveSuccess()
                        },
                        modifier = Modifier.testTag("editor_save_button")
                    ) {
                        Text("Save & Export", color = BrandBlue, fontWeight = FontWeight.Bold)
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Interactive Canvas Area
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            rotationZ = rotationAngle,
                            scaleX = flipX,
                            scaleY = flipY
                        )
                ) {
                    AsyncImage(
                        model = wallpaper.imageUrl,
                        contentDescription = "Edit Target",
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(finalColorMatrix),
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(blurValue.dp)
                    )
                }

                // Overlay Text Compositor
                if (textOverlayInput.isNotBlank()) {
                    Text(
                        text = textOverlayInput,
                        color = textColorState,
                        fontSize = textScaleState.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.Center)
                    )
                }

                // Overlay Stickers Layer
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeStickers.forEach { sticker ->
                        val icon = when (sticker) {
                            "Crown" -> Icons.Default.AdminPanelSettings
                            "Heart" -> Icons.Default.Favorite
                            "Sparkle" -> Icons.Default.AutoAwesome
                            "Star" -> Icons.Default.Star
                            else -> Icons.Default.Lightbulb
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(8.dp)
                        ) {
                            Icon(imageVector = icon, contentDescription = sticker, tint = BrandBlue, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            // Controls Tabs Panel (frosted card overlay)
            Card(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Control values sliders or configurations based on active Tab
                    when (activeTab) {
                        0 -> { // Transform Tab
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EditorControlBtn(icon = Icons.Default.RotateRight, label = "Rotate") {
                                    rotationAngle = (rotationAngle + 90f) % 360f
                                }
                                EditorControlBtn(icon = Icons.Default.Flip, label = "Flip H") {
                                    flipX *= -1f
                                }
                                EditorControlBtn(icon = Icons.Default.FlipCameraAndroid, label = "Flip V") {
                                    flipY *= -1f
                                }
                            }

                            // Blur slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Dynamic Soft Blur", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text("${blurValue.toInt()}dp", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                                }
                                Slider(
                                    value = blurValue,
                                    onValueChange = { blurValue = it },
                                    valueRange = 0f..25f,
                                    colors = SliderDefaults.colors(thumbColor = BrandBlue, activeTrackColor = BrandBlue)
                                )
                            }
                        }

                        1 -> { // Filters Preset Tab
                            Text("Artistic Color Presets", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val filterNames = listOf("Original", "Monochrome", "Warm Sepia", "Cyberpunk Violet", "Cold Cyan")
                                items(filterNames.size) { idx ->
                                    val isSelected = selectedFilterPreset == idx
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) BrandPurple else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedFilterPreset = idx }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = filterNames[idx],
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        2 -> { // Adjust Values Tab (Sliders)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Brightness
                                EditorSlider(label = "Brightness", value = brightness, range = -0.5f..0.5f) { brightness = it }
                                // Contrast
                                EditorSlider(label = "Contrast", value = contrast, range = 0.5f..1.5f) { contrast = it }
                                // Saturation
                                EditorSlider(label = "Saturation", value = saturation, range = 0f..2f) { saturation = it }
                            }
                        }

                        3 -> { // Overlays Tab
                            Text("Text Compositor Overlay", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = textOverlayInput,
                                onValueChange = { textOverlayInput = it },
                                placeholder = { Text("Add custom quotes or typography...") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Title, contentDescription = "Text") },
                                trailingIcon = {
                                    if (textOverlayInput.isNotEmpty()) {
                                        IconButton(onClick = { textOverlayInput = "" }) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Quick Text Settings: Size and Color
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(Color.White, Color.Red, Color.Yellow, BrandBlue, BrandPurple).forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (textColorState == color) 2.dp else 0.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                )
                                                .clickable { textColorState = color }
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (textScaleState > 12) textScaleState -= 4f }) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Smaller")
                                    }
                                    Text("${textScaleState.toInt()}sp", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { if (textScaleState < 48) textScaleState += 4f }) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Larger")
                                    }
                                }
                            }
                        }

                        4 -> { // Stickers Tab
                            Text("Add Custom Decorative Stickers", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val stickers = listOf("Crown", "Heart", "Sparkle", "Star")
                                stickers.forEach { sticker ->
                                    val isAdded = activeStickers.contains(sticker)
                                    FilterChip(
                                        selected = isAdded,
                                        onClick = {
                                            activeStickers = if (isAdded) activeStickers - sticker else activeStickers + sticker
                                        },
                                        label = { Text(sticker) },
                                        leadingIcon = {
                                            val icon = when (sticker) {
                                                "Crown" -> Icons.Default.AdminPanelSettings
                                                "Heart" -> Icons.Default.Favorite
                                                "Sparkle" -> Icons.Default.AutoAwesome
                                                else -> Icons.Default.Star
                                            }
                                            Icon(imageVector = icon, contentDescription = sticker, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Editor Navigation Tabs (frosted selection row)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val tabs = listOf(
                            Icons.Outlined.CropFree to "Transform",
                            Icons.Outlined.Palette to "Filters",
                            Icons.Outlined.Tune to "Adjust",
                            Icons.Outlined.TextFormat to "Text",
                            Icons.Outlined.StarBorder to "Stickers"
                        )
                        tabs.forEachIndexed { index, pair ->
                            val isSelected = activeTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BrandBlue.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { activeTab = index }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = pair.first,
                                        contentDescription = pair.second,
                                        tint = if (isSelected) BrandBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = pair.second,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) BrandBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

@Composable
fun EditorControlBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun EditorSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = String.format("%.2f", value), style = MaterialTheme.typography.bodySmall, color = BrandBlue)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = BrandBlue, activeTrackColor = BrandBlue)
        )
    }
}
