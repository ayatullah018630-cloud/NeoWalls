package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Wallpaper
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.*
import com.example.ui.viewmodel.AIGenerationState
import com.example.ui.viewmodel.WallpaperViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICreateScreen(
    viewModel: WallpaperViewModel
) {
    val context = LocalContext.current
    val aiState by viewModel.aiGenerationState.collectAsState()
    var promptInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val suggestedPrompts = listOf(
        "A futuristic blue cyberpunk city",
        "Islamic geometric golden wallpaper",
        "Luxury black marble wallpaper",
        "Cute cat with flowers",
        "Cosmic cosmic nebula paint swirl"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "AI Create Laboratory",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Description Header
            Text(
                text = "Dream it. Synthesize it.",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Enter a concept below, and our Gemini Art Director will forge a premium custom high-resolution canvas for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Text Input Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = { Text("What would you like to synthesize?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("ai_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggested Pills Header
                    Text(
                        text = "Inspiration Quick-Chips",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestedPrompts) { sug ->
                            SuggestionChip(text = sug) {
                                promptInput = sug
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                viewModel.generateAIWallpaper(promptInput)
                            } else {
                                Toast.makeText(context, "Please enter a prompt first!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("ai_generate_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Forge")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Forge Masterpiece",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Output Laboratory Window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(BrandBlue.copy(alpha = 0.4f), BrandPurple.copy(alpha = 0.4f))),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = aiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ai_output_window"
                ) { state ->
                    when (state) {
                        is AIGenerationState.Idle -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(BrandBlue, BrandPurple)))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Idle",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Synthesis Vault Ready",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Your custom generated designs will materialize here. Tap forge above to begin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        is AIGenerationState.Generating -> {
                            val infiniteTransition = rememberInfiniteTransition(label = "generating_spin")
                            val rotationAngle by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(4000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "spin"
                            )

                            var generatingText by remember { mutableStateOf("Consulting Art Director...") }
                            LaunchedEffect(key1 = Unit) {
                                val textList = listOf(
                                    "Consulting Art Director...",
                                    "Infusing color palette properties...",
                                    "Rendering dynamic canvas geometries...",
                                    "Forging high-contrast light reflections...",
                                    "Enhancing resolutions to 4K mobile scale...",
                                    "NeoWalls AI completing synthesis..."
                                )
                                var index = 0
                                while (true) {
                                    delay(2000)
                                    index = (index + 1) % textList.size
                                    generatingText = textList[index]
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .rotate(rotationAngle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            brush = Brush.sweepGradient(listOf(BrandBlue, BrandPurple, BrandCyan, BrandBlue)),
                                            style = Stroke(width = 8.dp.toPx())
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Cyclone,
                                        contentDescription = "Spinning",
                                        tint = BrandCyan,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = generatingText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "This usually takes 3 to 6 seconds.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        is AIGenerationState.Success -> {
                            val result = state.result
                            var hasSaved by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = result.imageUrl,
                                    contentDescription = result.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Dark overlay on bottom
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                            )
                                        )
                                )

                                // Success details & controls
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(android.graphics.Color.parseColor(result.colorHex)))
                                                .size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = result.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.expandedPrompt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f),
                                        maxLines = 3,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Save/Catalog Button
                                        Button(
                                            onClick = {
                                                if (!hasSaved) {
                                                    viewModel.saveGeneratedAIWallpaper(result)
                                                    hasSaved = true
                                                    Toast.makeText(context, "Added to NeoWalls collection!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (hasSaved) MaterialTheme.colorScheme.secondary else BrandBlue
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (hasSaved) Icons.Default.Check else Icons.Default.Save,
                                                contentDescription = "Save",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                if (hasSaved) "Saved" else "Add Catalog",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Set Wallpaper Options
                                        var showSetDialog by remember { mutableStateOf(false) }
                                        Button(
                                            onClick = { showSetDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                                            modifier = Modifier.weight(1.0f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Wallpaper,
                                                contentDescription = "Set",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Set Screen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (showSetDialog) {
                                            AlertDialog(
                                                onDismissRequest = { showSetDialog = false },
                                                title = { Text("Apply AI Art Canvas") },
                                                text = { Text("Where would you like to set this AI generated canvas?") },
                                                confirmButton = {},
                                                dismissButton = {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Button(
                                                            onClick = {
                                                                viewModel.setWallpaper(result.imageUrl, result.title, 1)
                                                                showSetDialog = false
                                                                Toast.makeText(context, "Setting wallpaper...", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) { Text("Set Home Screen") }
                                                        Button(
                                                            onClick = {
                                                                viewModel.setWallpaper(result.imageUrl, result.title, 2)
                                                                showSetDialog = false
                                                                Toast.makeText(context, "Setting wallpaper...", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) { Text("Set Lock Screen") }
                                                        Button(
                                                            onClick = {
                                                                viewModel.setWallpaper(result.imageUrl, result.title, 3)
                                                                showSetDialog = false
                                                                Toast.makeText(context, "Setting wallpaper...", Toast.LENGTH_SHORT).show()
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

                                        // Share/Download
                                        IconButton(
                                            onClick = {
                                                // Log activity
                                                viewModel.addDownloadedWallpaper(
                                                    Wallpaper(
                                                        id = "ai_share_id",
                                                        title = result.title,
                                                        category = result.category,
                                                        imageUrl = result.imageUrl,
                                                        resolution = "3840x2160",
                                                        size = "2.4 MB",
                                                        author = "AI Creator",
                                                        colorHex = result.colorHex,
                                                        isFeatured = false,
                                                        isTrending = false,
                                                        isRecommended = false,
                                                        publishDate = System.currentTimeMillis(),
                                                        downloadCount = 1,
                                                        viewCount = 1
                                                    ),
                                                    "Local Cached Storage"
                                                )
                                                Toast.makeText(context, "Saved image to Gallery", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.2f))
                                        ) {
                                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        is AIGenerationState.Error -> {
                            val msg = state.message
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Synthesis Interrupted",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.resetAIGenerator() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Text("Reset & Try Again", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}


