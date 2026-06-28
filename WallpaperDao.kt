package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    // Wallpapers
    @Query("SELECT * FROM wallpapers ORDER BY publishDate DESC")
    fun getAllWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE category = :category ORDER BY publishDate DESC")
    fun getWallpapersByCategory(category: String): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isFeatured = 1 ORDER BY publishDate DESC")
    fun getFeaturedWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isTrending = 1 ORDER BY publishDate DESC")
    fun getTrendingWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isRecommended = 1 ORDER BY publishDate DESC")
    fun getRecommendedWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchWallpapers(query: String): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE id = :wallpaperId LIMIT 1")
    fun getWallpaperById(wallpaperId: String): Flow<Wallpaper?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: Wallpaper)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<Wallpaper>)

    @Query("DELETE FROM wallpapers WHERE id = :wallpaperId")
    suspend fun deleteWallpaperById(wallpaperId: String)

    @Query("UPDATE wallpapers SET downloadCount = downloadCount + 1 WHERE id = :wallpaperId")
    suspend fun incrementDownloadCount(wallpaperId: String)

    @Query("UPDATE wallpapers SET viewCount = viewCount + 1 WHERE id = :wallpaperId")
    suspend fun incrementViewCount(wallpaperId: String)

    // Categories
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    // Favorites
    @Query("SELECT wallpapers.* FROM wallpapers INNER JOIN favorites ON wallpapers.id = favorites.wallpaperId ORDER BY favorites.addedDate DESC")
    fun getFavoriteWallpapers(): Flow<List<Wallpaper>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteWallpaper)

    @Query("DELETE FROM favorites WHERE wallpaperId = :wallpaperId")
    suspend fun deleteFavoriteByWallpaperId(wallpaperId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE wallpaperId = :wallpaperId)")
    fun isFavorite(wallpaperId: String): Flow<Boolean>

    // Downloads
    @Query("SELECT wallpapers.* FROM wallpapers INNER JOIN downloads ON wallpapers.id = downloads.wallpaperId ORDER BY downloads.downloadedDate DESC")
    fun getDownloadedWallpapers(): Flow<List<Wallpaper>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadedWallpaper)

    @Query("DELETE FROM downloads WHERE wallpaperId = :wallpaperId")
    suspend fun deleteDownloadByWallpaperId(wallpaperId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE wallpaperId = :wallpaperId)")
    fun isDownloaded(wallpaperId: String): Flow<Boolean>

    // Activity Log
    @Query("SELECT * FROM user_activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<UserActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: UserActivity)

    @Query("DELETE FROM user_activities")
    suspend fun clearActivities()
}
