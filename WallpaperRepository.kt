package com.example.data.repository

import com.example.data.PredefinedData
import com.example.data.db.WallpaperDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class WallpaperRepository(private val dao: WallpaperDao) {

    val allWallpapers: Flow<List<Wallpaper>> = dao.getAllWallpapers()
    val featuredWallpapers: Flow<List<Wallpaper>> = dao.getFeaturedWallpapers()
    val trendingWallpapers: Flow<List<Wallpaper>> = dao.getTrendingWallpapers()
    val recommendedWallpapers: Flow<List<Wallpaper>> = dao.getRecommendedWallpapers()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()
    val favoriteWallpapers: Flow<List<Wallpaper>> = dao.getFavoriteWallpapers()
    val downloadedWallpapers: Flow<List<Wallpaper>> = dao.getDownloadedWallpapers()
    val allActivities: Flow<List<UserActivity>> = dao.getAllActivities()

    fun getWallpapersByCategory(category: String): Flow<List<Wallpaper>> {
        return dao.getWallpapersByCategory(category)
    }

    fun searchWallpapers(query: String): Flow<List<Wallpaper>> {
        return dao.searchWallpapers(query)
    }

    fun getWallpaperById(id: String): Flow<Wallpaper?> {
        return dao.getWallpaperById(id)
    }

    suspend fun insertWallpaper(wallpaper: Wallpaper) {
        dao.insertWallpaper(wallpaper)
    }

    suspend fun deleteWallpaperById(id: String) {
        dao.deleteWallpaperById(id)
    }

    suspend fun incrementDownloadCount(id: String) {
        dao.incrementDownloadCount(id)
    }

    suspend fun incrementViewCount(id: String) {
        dao.incrementViewCount(id)
    }

    suspend fun insertCategory(category: Category) {
        dao.insertCategory(category)
    }

    suspend fun deleteCategoryById(id: String) {
        dao.deleteCategoryById(id)
    }

    suspend fun toggleFavorite(wallpaperId: String, shouldFavorite: Boolean) {
        if (shouldFavorite) {
            dao.insertFavorite(FavoriteWallpaper(id = wallpaperId, wallpaperId = wallpaperId))
        } else {
            dao.deleteFavoriteByWallpaperId(wallpaperId)
        }
    }

    fun isFavorite(wallpaperId: String): Flow<Boolean> {
        return dao.isFavorite(wallpaperId)
    }

    suspend fun addDownload(wallpaperId: String, path: String) {
        dao.insertDownload(DownloadedWallpaper(id = wallpaperId, wallpaperId = wallpaperId, downloadedPath = path))
    }

    fun isDownloaded(wallpaperId: String): Flow<Boolean> {
        return dao.isDownloaded(wallpaperId)
    }

    suspend fun logActivity(activityType: String, wallpaperId: String, wallpaperTitle: String, details: String = "") {
        dao.insertActivity(
            UserActivity(
                activityType = activityType,
                wallpaperId = wallpaperId,
                wallpaperTitle = wallpaperTitle,
                details = details
            )
        )
    }

    suspend fun clearActivities() {
        dao.clearActivities()
    }

    suspend fun seedIfNeeded() {
        val existingCategories = dao.getAllCategories().first()
        if (existingCategories.isEmpty()) {
            dao.insertCategories(PredefinedData.categories)
        }

        val existingWallpapers = dao.getAllWallpapers().first()
        if (existingWallpapers.isEmpty()) {
            dao.insertWallpapers(PredefinedData.wallpapers)
        }
    }
}
