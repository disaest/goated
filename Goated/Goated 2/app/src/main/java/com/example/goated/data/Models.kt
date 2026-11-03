package com.example.goated.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val developer: String? = null,
    @SerialName("average_rating") val averageRating: Double? = null,
    @SerialName("rating_1_count") val rating1Count: Int = 0,
    @SerialName("rating_2_count") val rating2Count: Int = 0,
    @SerialName("rating_3_count") val rating3Count: Int = 0,
    @SerialName("rating_4_count") val rating4Count: Int = 0,
    @SerialName("rating_5_count") val rating5Count: Int = 0
)

@Serializable
data class Genre(
    val id: Int,
    val name: String,
    val slug: String? = null
)

@Serializable
data class GameGenre(
    @SerialName("game_id") val gameId: String,
    @SerialName("genre_id") val genreId: Int
)

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    val email: String? = null
)

@Serializable
data class Favorite(
    @SerialName("user_id") val userId: String,
    @SerialName("game_id") val gameId: String
)

@Serializable
data class Review(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("game_id") val gameId: String,
    val rating: Int? = null,
    val comment: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean = false
)

@Serializable
data class ActivityItem(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("action_type") val actionType: String,
    @SerialName("target_game_id") val targetGameId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Collection(
    val id: String,
    val title: String,
    val type: String? = null
)

@Serializable
data class CollectionGame(
    @SerialName("collection_id") val collectionId: String,
    @SerialName("game_id") val gameId: String,
    @SerialName("order_index") val orderIndex: Int = 0
)