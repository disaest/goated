package com.example.goated.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

object GameRepository {

    suspend fun getGameById(gameId: String): Game? {
        return try {
            supabase.from("games")
                .select { filter { eq("id", gameId) } }
                .decodeSingleOrNull<Game>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllGames(): List<Game> {
        return try {
            supabase.from("games").select().decodeList<Game>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCollections(): List<Collection> {
        return try {
            supabase.from("collections").select().decodeList<Collection>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getGamesByCollection(collectionId: String): List<Game> {
        return try {
            val links = supabase.from("collection_games")
                .select { filter { eq("collection_id", collectionId) } }
                .decodeList<CollectionGame>()
            if (links.isEmpty()) {
                return emptyList()
            }
            val ids = links.map { it.gameId }
            supabase.from("games")
                .select { filter { isIn("id", ids) } }
                .decodeList<Game>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getGenres(): List<Genre> {
        return try {
            supabase.from("genres").select().decodeList<Genre>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getGenreByName(name: String): Genre? {
        return try {
            supabase.from("genres")
                .select { filter { eq("name", name) } }
                .decodeSingleOrNull<Genre>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGamesByGenre(genreId: Int, limit: Int = 3): List<Game> {
        return try {
            val links = supabase.from("game_genres")
                .select { filter { eq("genre_id", genreId) } }
                .decodeList<GameGenre>()
            if (links.isEmpty()) {
                return emptyList()
            }
            val ids = links.map { it.gameId }.take(limit)
            supabase.from("games")
                .select { filter { isIn("id", ids) } }
                .decodeList<Game>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllGamesByGenre(genreId: Int): List<Game> {
        return try {
            val links = supabase.from("game_genres")
                .select { filter { eq("genre_id", genreId) } }
                .decodeList<GameGenre>()
            if (links.isEmpty()) {
                return emptyList()
            }
            val ids = links.map { it.gameId }
            supabase.from("games")
                .select { filter { isIn("id", ids) } }
                .decodeList<Game>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProfile(userId: String = AuthRepository.currentUserId() ?: ""): Profile? {
        return try {
            supabase.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFavorites(userId: String = AuthRepository.currentUserId() ?: ""): List<Game> {
        return try {
            val links = supabase.from("favorites")
                .select { filter { eq("user_id", userId) } }
                .decodeList<Favorite>()
            if (links.isEmpty()) {
                return emptyList()
            }
            val ids = links.map { it.gameId }
            supabase.from("games")
                .select { filter { isIn("id", ids) } }
                .decodeList<Game>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun isFavorite(gameId: String): Boolean {
        val userId = AuthRepository.currentUserId() ?: return false
        return try {
            val fav = supabase.from("favorites")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("game_id", gameId)
                    }
                }
                .decodeSingleOrNull<Favorite>()
            fav != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(gameId: String): Boolean {
        val userId = AuthRepository.currentUserId() ?: return false
        return try {
            val already = isFavorite(gameId)
            if (already) {
                supabase.from("favorites").delete {
                    filter {
                        eq("user_id", userId)
                        eq("game_id", gameId)
                    }
                }
            } else {
                val count = supabase.from("favorites")
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Favorite>()
                    .size
                if (count >= 4) return false
                supabase.from("favorites").insert(
                    Favorite(userId = userId, gameId = gameId)
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRecentActivity(userId: String = AuthRepository.currentUserId() ?: "", limit: Int = 4): List<Game> {
        return try {
            val items = supabase.from("activity_feed")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ActivityItem>()
            val ids = items.mapNotNull { it.targetGameId }
            if (ids.isEmpty()) {
                return emptyList()
            }
            supabase.from("games")
                .select { filter { isIn("id", ids) } }
                .decodeList<Game>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserReviews(userId: String = AuthRepository.currentUserId() ?: ""): List<Review> {
        return try {
            supabase.from("reviews")
                .select { filter { eq("user_id", userId) } }
                .decodeList<Review>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPlayedGames(userId: String = AuthRepository.currentUserId() ?: ""): List<Game> {
        return try {
            val items = supabase.from("reviews")
                .select {
                    filter { eq("user_id", userId) }
                    filter { eq("is_completed", true) }
                }
                .decodeList<Review>()
            if (items.isEmpty()) {
                return emptyList()
            }
            val ids = items.map { it.gameId }
            supabase.from("games")
                .select { filter { isIn("id", ids) } }
                .decodeList<Game>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserRatingForGame(gameId: String, userId: String = AuthRepository.currentUserId() ?: ""): Int? {
        return try {
            supabase.from("reviews")
                .select {
                    filter { eq("user_id", userId) }
                    filter { eq("game_id", gameId) }
                }
                .decodeSingleOrNull<Review>()?.rating
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitRating(gameId: String, rating: Int): Boolean {
        val userId = AuthRepository.currentUserId() ?: return false
        return try {
            val existing = supabase.from("reviews")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("game_id", gameId)
                    }
                }
                .decodeSingleOrNull<Review>()

            if (rating == 0 || (existing != null && existing.rating == rating)) {
                supabase.from("reviews").delete {
                    filter {
                        eq("user_id", userId)
                        eq("game_id", gameId)
                    }
                }
                supabase.from("activity_feed").delete {
                    filter {
                        eq("user_id", userId)
                        eq("target_game_id", gameId)
                        eq("action_type", "review")
                    }
                }
            } else {
                if (existing != null) {
                    supabase.from("reviews").delete {
                        filter {
                            eq("user_id", userId)
                            eq("game_id", gameId)
                        }
                    }
                }
                supabase.from("reviews").insert(
                    Review(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = userId,
                        gameId = gameId,
                        rating = rating,
                        comment = null,
                        isCompleted = true
                    )
                )
                supabase.from("activity_feed").delete {
                    filter {
                        eq("user_id", userId)
                        eq("target_game_id", gameId)
                        eq("action_type", "review")
                    }
                }
                supabase.from("activity_feed").insert(
                    ActivityItem(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = userId,
                        actionType = "review",
                        targetGameId = gameId,
                        createdAt = java.time.OffsetDateTime.now().toString()
                    )
                )
            }

            val ratings = supabase.from("reviews")
                .select { filter { eq("game_id", gameId) } }
                .decodeList<Review>()
            val counts = IntArray(6)
            var sum = 0
            ratings.forEach { r ->
                val v = r.rating ?: return@forEach
                counts[v]++
                sum += v
            }
            val avg = if (ratings.isEmpty()) 0.0 else sum.toDouble() / ratings.size

            supabase.from("games").update(
                {
                    set("rating_1_count", counts[1])
                    set("rating_2_count", counts[2])
                    set("rating_3_count", counts[3])
                    set("rating_4_count", counts[4])
                    set("rating_5_count", counts[5])
                    set("average_rating", avg)
                }
            ) {
                filter { eq("id", gameId) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}