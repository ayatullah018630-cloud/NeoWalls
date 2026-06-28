package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Wallpaper
import com.example.data.repository.WallpaperRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WallpaperViewModel
import com.example.ui.viewmodel.WallpaperViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Dependency Injection Instantiation
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = WallpaperRepository(database.wallpaperDao())
        val factory = WallpaperViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[WallpaperViewModel::class.java]

        setContent {
            // Track dynamic themes
            var isDarkTheme by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                // Main Multi-Screen Routing State Engine
                // Routes: "splash", "onboarding", "main", "detail", "editor", "admin"
                var currentRoute by remember { mutableStateOf("splash") }
                var selectedWallpaperForDetail by remember { mutableStateOf<Wallpaper?>(null) }
                var initialCategoryFilter by remember { mutableStateOf<String?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = currentRoute,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "root_navigation"
                    ) { route ->
                        when (route) {
                            "splash" -> {
                                SplashScreen {
                                    // Transition to onboarding after splash
                                    currentRoute = "onboarding"
                                }
                            }

                            "onboarding" -> {
                                OnboardingScreen {
                                    currentRoute = "main"
                                }
                            }

                            "main" -> {
                                MainTabsFrame(
                                    viewModel = viewModel,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = it },
                                    initialCategory = initialCategoryFilter,
                                    onClearCategoryFilter = { initialCategoryFilter = null },
                                    onWallpaperClick = { wp ->
                                        selectedWallpaperForDetail = wp
                                        currentRoute = "detail"
                                    },
                                    onNavigateToAdmin = {
                                        currentRoute = "admin"
                                    }
                                )
                            }

                            "detail" -> {
                                selectedWallpaperForDetail?.let { wp ->
                                    WallpaperDetailScreen(
                                        viewModel = viewModel,
                                        wallpaper = wp,
                                        onBackClick = {
                                            currentRoute = "main"
                                        },
                                        onEditClick = { targetWp ->
                                            selectedWallpaperForDetail = targetWp
                                            currentRoute = "editor"
                                        },
                                        onSimilarWallpaperClick = { simWp ->
                                            selectedWallpaperForDetail = simWp
                                        }
                                    )
                                } ?: run {
                                    currentRoute = "main"
                                }
                            }

                            "editor" -> {
                                selectedWallpaperForDetail?.let { wp ->
                                    WallpaperEditorScreen(
                                        viewModel = viewModel,
                                        wallpaper = wp,
                                        onBackClick = {
                                            currentRoute = "detail"
                                        },
                                        onSaveSuccess = {
                                            currentRoute = "main"
                                        }
                                    )
                                } ?: run {
                                    currentRoute = "main"
                                }
                            }

                            "admin" -> {
                                AdminPanelScreen(
                                    viewModel = viewModel,
                                    onBackClick = {
                                        currentRoute = "main"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
        delay(1200) // 2.2 seconds total immersive display
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A)), // Deep premium slate black
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            // Elegant pulsing neon gradient logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandBlue, BrandPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Logo Sparkle",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NeoWalls",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(listOf(BrandBlue, BrandPurple))
                )
            )

            Text(
                text = "Creative Art & AI Synthesis",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = BrandBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val onboardingPages = listOf(
        OnboardingData(
            title = "Premium Catalog",
            description = "Browse thousands of stunning hand-crafted ultra-high-definition wallpapers customized for AMOLED and modern screens.",
            icon = Icons.Outlined.Collections,
            color = BrandBlue
        ),
        OnboardingData(
            title = "AI Creative Laboratory",
            description = "Type any aesthetic description and watch the Gemini Art Engine forge custom wallpaper canvases dynamically.",
            icon = Icons.Outlined.AutoAwesome,
            color = BrandPurple
        ),
        OnboardingData(
            title = "Canvas Customization",
            description = "Apply soft blurs, 90-degree rotations, color filter presets, stickers, or typography quotes with our integrated Editor suite.",
            icon = Icons.Outlined.Brush,
            color = BrandCyan
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C14)) // Cyberpunk dark aesthetic
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIdx ->
            val page = onboardingPages[pageIdx]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(page.color.copy(alpha = 0.15f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = page.title,
                        tint = page.color,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Onboarding Indicators and Next Controls Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slide indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onboardingPages.forEachIndexed { idx, _ ->
                    val isSelected = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .size(height = 6.dp, width = if (isSelected) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) BrandBlue else Color.White.copy(alpha = 0.3f))
                    )
                }
            }

            // Next button
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class OnboardingData(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun MainTabsFrame(
    viewModel: WallpaperViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    initialCategory: String?,
    onClearCategoryFilter: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    // Tab indices: 0 = Home, 1 = Categories, 2 = AI Create, 3 = Favorites, 4 = Downloads, 5 = Settings
    var activeTabIdx by remember { mutableStateOf(0) }
    var categoryFilterState by remember { mutableStateOf<String?>(null) }

    // Synchronize initial category filter passed from Home screen clicks
    LaunchedEffect(key1 = initialCategory) {
        initialCategory?.let {
            categoryFilterState = it
            activeTabIdx = 1 // Switch to Categories Tab
            onClearCategoryFilter()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                val menuItems = listOf(
                    NavigationBarItemData("Home", Icons.Default.Home, Icons.Outlined.Home),
                    NavigationBarItemData("Categories", Icons.Default.Category, Icons.Outlined.Category),
                    NavigationBarItemData("AI Create", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
                    NavigationBarItemData("Favorites", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder),
                    NavigationBarItemData("Downloads", Icons.Default.DownloadDone, Icons.Outlined.Download),
                    NavigationBarItemData("Settings", Icons.Default.Settings, Icons.Outlined.Settings)
                )

                menuItems.forEachIndexed { index, item ->
                    val isSelected = activeTabIdx == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (index == 1) {
                                categoryFilterState = null // Reset category selection on clicking main tab
                            }
                            activeTabIdx = index
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = if (isSelected) BrandBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BrandBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTabIdx) {
                0 -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onWallpaperClick = onWallpaperClick,
                        onCategoryClick = { selectedCat ->
                            categoryFilterState = selectedCat
                            activeTabIdx = 1 // Switch tab
                        },
                        onProfileClick = {
                            activeTabIdx = 5 // Switch to Settings tab
                        },
                        onNotificationClick = {
                            activeTabIdx = 5 // Go to Settings to view Developers options
                        }
                    )
                }

                1 -> {
                    CategoriesScreen(
                        viewModel = viewModel,
                        onWallpaperClick = onWallpaperClick,
                        initialSelectedCategory = categoryFilterState
                    )
                }

                2 -> {
                    AICreateScreen(viewModel = viewModel)
                }

                3 -> {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onWallpaperClick = onWallpaperClick
                    )
                }

                4 -> {
                    DownloadsScreen(
                        viewModel = viewModel,
                        onWallpaperClick = onWallpaperClick
                    )
                }

                5 -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAdmin = onNavigateToAdmin,
                        onToggleTheme = onToggleTheme,
                        isDarkThemeActive = isDarkTheme
                    )
                }
            }
        }
    }
}

data class NavigationBarItemData(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
