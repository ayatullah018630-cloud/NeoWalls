package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class Wallpaper(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val imageUrl: String,
    val resolution: String,
    val size: String,
    val author: String,
    val colorHex: String,
    val isFeatured: Boolean,
    val isTrending: Boolean,
    val isRecommended: Boolean,
    val publishDate: Long,
    val downloadCount: Int,
    val viewCount: Int
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String
)

@Entity(tableName = "favorites")
data class FavoriteWallpaper(
    @PrimaryKey val id: String, // Typically wallpaperId or wallpaperId + userId
    val wallpaperId: String,
    val addedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadedWallpaper(
    @PrimaryKey val id: String, // wallpaperId
    val wallpaperId: String,
    val downloadedPath: String,
    val downloadedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_activities")
data class UserActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityType: String, // "Set Home", "Set Lock", "Set Both", "Download", "Favorite", "Report", "Generate AI"
    val wallpaperId: String,
    val wallpaperTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
