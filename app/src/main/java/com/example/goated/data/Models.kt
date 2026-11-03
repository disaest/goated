package com.example.goated.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- Таблицы Supabase ----------

@Serializable
data class Game(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val developer: String? = null,
    @SerialName("average_rating") val averageRating: Float? = null,
    @SerialName("rating_count") val ratingCount: Int = 0
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
    @SerialName("avatar_color") val avatarColor: String? = null,
    @SerialName("banner_style") val bannerStyle: String? = null
)

/** Используется только при создании профиля: email читать из profiles нельзя (RLS). */
@Serializable
data class NewProfile(
    val id: String,
    val username: String,
    val email: String
)

@Serializable
data class Favorite(
    @SerialName("user_id") val userId: String,
    @SerialName("game_id") val gameId: String
)

@Serializable
data class Screenshot(
    val id: String,
    @SerialName("game_id") val gameId: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("order_index") val orderIndex: Int = 0
)

/** Одна запись на пару «пользователь — игра»: оценка, рецензия и отметка «пройдено». */
@Serializable
data class Review(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("game_id") val gameId: String,
    val rating: Float? = null,
    val comment: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

/** Рецензия вместе с ником автора (join reviews -> profiles). */
@Serializable
data class ReviewWithAuthor(
    val id: String,
    @SerialName("user_id") val userId: String,
    val rating: Float? = null,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val profiles: AuthorName? = null
) {
    val authorName: String get() = profiles?.username ?: "пользователь"
}

@Serializable
data class AuthorName(val username: String? = null)

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

// ---------- Модели для экранов ----------

data class CollectionWithGames(val collection: Collection, val games: List<Game>)

data class GenreWithGames(val genre: Genre, val games: List<Game>)

/** Игра вместе с оценкой текущего пользователя (для профиля и списка пройденных). */
data class RatedGame(val game: Game, val userRating: Float?)
