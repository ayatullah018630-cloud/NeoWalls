package com.example.ui.viewmodel

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.util.UUID

sealed interface AIGenerationState {
    object Idle : AIGenerationState
    object Generating : AIGenerationState
    data class Success(val result: GeminiService.AICreationResult) : AIGenerationState
    data class Error(val message: String) : AIGenerationState
}

sealed interface SettingWallpaperState {
    object Idle : SettingWallpaperState
    object Setting : SettingWallpaperState
    object Success : SettingWallpaperState
    data class Error(val message: String) : SettingWallpaperState
}

class WallpaperViewModel(
    application: Application,
    private val repository: WallpaperRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // State flows from database
    val wallpapers: StateFlow<List<Wallpaper>> = repository.allWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredWallpapers: StateFlow<List<Wallpaper>> = repository.featuredWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingWallpapers: StateFlow<List<Wallpaper>> = repository.trendingWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedWallpapers: StateFlow<List<Wallpaper>> = repository.recommendedWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWallpapers: StateFlow<List<Wallpaper>> = repository.favoriteWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedWallpapers: StateFlow<List<Wallpaper>> = repository.downloadedWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activities: StateFlow<List<UserActivity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Interaction states
    val selectedWallpaper = MutableStateFlow<Wallpaper?>(null)
    val searchQuery = MutableStateFlow("")

    val searchResults: StateFlow<List<Wallpaper>> = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchWallpapers(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Generator State
    private val _aiGenerationState = MutableStateFlow<AIGenerationState>(AIGenerationState.Idle)
    val aiGenerationState: StateFlow<AIGenerationState> = _aiGenerationState.asStateFlow()

    // Wallpaper Setter State
    private val _settingState = MutableStateFlow<SettingWallpaperState>(SettingWallpaperState.Idle)
    val settingState: StateFlow<SettingWallpaperState> = _settingState.asStateFlow()

    // Simulation Configs for Admin Controls
    val pushNotificationTitle = MutableStateFlow("Unleash AMOLED Gold!")
    val pushNotificationBody = MutableStateFlow("Check out our brand new ultra-high-definition wallpapers added today!")
    val selectedAdFrequency = MutableStateFlow("Standard (Balanced)")
    val isWatermarkEnabled = MutableStateFlow(true)

    init {
        // Seed initial wallpapers and categories if DB is clean
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun selectWallpaper(wallpaper: Wallpaper?) {
        selectedWallpaper.value = wallpaper
        if (wallpaper != null) {
            viewModelScope.launch {
                repository.incrementViewCount(wallpaper.id)
            }
        }
    }

    fun isFavorite(wallpaperId: String): Flow<Boolean> {
        return repository.isFavorite(wallpaperId)
    }

    fun toggleFavorite(wallpaper: Wallpaper, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(wallpaper.id, isFav)
            repository.logActivity(
                activityType = if (isFav) "Favorite" else "Unfavorite",
                wallpaperId = wallpaper.id,
                wallpaperTitle = wallpaper.title,
                details = "User toggled favorite status to $isFav"
            )
        }
    }

    fun isDownloaded(wallpaperId: String): Flow<Boolean> {
        return repository.isDownloaded(wallpaperId)
    }

    fun addDownloadedWallpaper(wallpaper: Wallpaper, localPath: String) {
        viewModelScope.launch {
            repository.addDownload(wallpaper.id, localPath)
            repository.incrementDownloadCount(wallpaper.id)
            repository.logActivity(
                activityType = "Download",
                wallpaperId = wallpaper.id,
                wallpaperTitle = wallpaper.title,
                details = "Downloaded to local storage path: $localPath"
            )
        }
    }

    // AI Wallpaper Creation
    fun generateAIWallpaper(prompt: String) {
        if (prompt.isBlank()) return
        _aiGenerationState.value = AIGenerationState.Generating
        viewModelScope.launch {
            try {
                val result = GeminiService.generateWallpaperMetadata(prompt)
                _aiGenerationState.value = AIGenerationState.Success(result)
                
                repository.logActivity(
                    activityType = "Generate AI",
                    wallpaperId = "ai_generation",
                    wallpaperTitle = result.title,
                    details = "Prompt: '$prompt' | Expanded: '${result.expandedPrompt}'"
                )
            } catch (e: Exception) {
                _aiGenerationState.value = AIGenerationState.Error(e.message ?: "Unknown AI error")
            }
        }
    }

    fun saveGeneratedAIWallpaper(result: GeminiService.AICreationResult) {
        viewModelScope.launch {
            val id = "ai_" + UUID.randomUUID().toString().take(8)
            val customWallpaper = Wallpaper(
                id = id,
                title = result.title,
                category = result.category,
                imageUrl = result.imageUrl,
                resolution = "3840x2160",
                size = "2.4 MB",
                author = "NeoWalls AI Creator",
                colorHex = result.colorHex,
                isFeatured = true,
                isTrending = false,
                isRecommended = true,
                publishDate = System.currentTimeMillis(),
                downloadCount = 1,
                viewCount = 1
            )
            repository.insertWallpaper(customWallpaper)
            repository.logActivity(
                activityType = "Save Generated Wallpaper",
                wallpaperId = id,
                wallpaperTitle = result.title,
                details = "Saved AI generation to catalog."
            )
        }
    }

    fun resetAIGenerator() {
        _aiGenerationState.value = AIGenerationState.Idle
    }

    // Apply Wallpaper (Home, Lock, or Both)
    fun setWallpaper(imageUrl: String, title: String, screenType: Int) {
        // screenType: 1 = Home, 2 = Lock, 3 = Both
        _settingState.value = SettingWallpaperState.Setting
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = downloadBitmapFromUrl(imageUrl)
                if (bitmap != null) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    when (screenType) {
                        1 -> {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        }
                        2 -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                            } else {
                                wallpaperManager.setBitmap(bitmap) // Fallback for older SDKs
                            }
                        }
                        3 -> {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                            }
                        }
                    }
                    _settingState.value = SettingWallpaperState.Success
                    repository.logActivity(
                        activityType = when (screenType) {
                            1 -> "Set Home Screen"
                            2 -> "Set Lock Screen"
                            else -> "Set Both Screens"
                        },
                        wallpaperId = "apply_flow",
                        wallpaperTitle = title,
                        details = "Applied via WallpaperManager successfully"
                    )
                } else {
                    _settingState.value = SettingWallpaperState.Error("Failed to decode image bitmap.")
                }
            } catch (e: Exception) {
                Log.e("WallpaperViewModel", "Error applying wallpaper", e)
                _settingState.value = SettingWallpaperState.Error(e.message ?: "Failed to set wallpaper.")
            }
        }
    }

    fun resetSettingState() {
        _settingState.value = SettingWallpaperState.Idle
    }

    private fun downloadBitmapFromUrl(urlStr: String): Bitmap? {
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("WallpaperViewModel", "Error downloading bitmap", e)
            null
        }
    }

    // Admin Controls
    fun addAdminWallpaper(
        title: String,
        category: String,
        imageUrl: String,
        resolution: String,
        size: String,
        colorHex: String,
        isFeatured: Boolean,
        isTrending: Boolean,
        isRecommended: Boolean
    ) {
        viewModelScope.launch {
            val id = "admin_" + UUID.randomUUID().toString().take(8)
            val nw = Wallpaper(
                id = id,
                title = title.ifBlank { "Architectural Neon" },
                category = category,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=1080" },
                resolution = resolution.ifBlank { "3840x2160" },
                size = size.ifBlank { "2.5 MB" },
                author = "Administrator",
                colorHex = colorHex.ifBlank { "#B000FF" },
                isFeatured = isFeatured,
                isTrending = isTrending,
                isRecommended = isRecommended,
                publishDate = System.currentTimeMillis(),
                downloadCount = 0,
                viewCount = 0
            )
            repository.insertWallpaper(nw)
            repository.logActivity(
                activityType = "Admin Upload",
                wallpaperId = id,
                wallpaperTitle = nw.title,
                details = "Admin created new wallpaper in category: $category"
            )
        }
    }

    fun deleteWallpaper(wallpaperId: String, title: String) {
        viewModelScope.launch {
            repository.deleteWallpaperById(wallpaperId)
            repository.logActivity(
                activityType = "Admin Delete",
                wallpaperId = wallpaperId,
                wallpaperTitle = title,
                details = "Admin removed wallpaper from system"
            )
        }
    }

    fun addAdminCategory(name: String, imageUrl: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = name.lowercase().replace(" ", "_")
            val nc = Category(id = id, name = name, imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1501854140801-50d01698950b?q=80&w=600" })
            repository.insertCategory(nc)
            repository.logActivity(
                activityType = "Admin Add Category",
                wallpaperId = id,
                wallpaperTitle = name,
                details = "Admin created category: $name"
            )
        }
    }

    fun deleteCategory(categoryId: String, name: String) {
        viewModelScope.launch {
            repository.deleteCategoryById(categoryId)
            repository.logActivity(
                activityType = "Admin Delete Category",
                wallpaperId = categoryId,
                wallpaperTitle = name,
                details = "Admin removed category"
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearActivities()
        }
    }
}

class WallpaperViewModelFactory(
    private val application: Application,
    private val repository: WallpaperRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WallpaperViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WallpaperViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
