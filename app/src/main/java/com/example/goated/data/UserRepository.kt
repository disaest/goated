package com.example.goated.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Данные текущего пользователя: профиль, избранное, оценки, рецензии,
 * пройденные игры и история активности.
 */
object UserRepository {

    const val FAVORITES_LIMIT = 4

    /** Столбцы профиля, которые разрешено читать (email скрыт политиками). */
    private val profileColumns = Columns.list("id", "username", "avatar_url", "bio", "avatar_color", "banner_style")

    private fun requireUserId(): String =
        AuthRepository.currentUserId() ?: throw AppException("Войдите в аккаунт")

    // ---------- Профиль ----------

    suspend fun getProfile(userId: String): Profile? =
        supabase.from("profiles")
            .select(profileColumns) { filter { eq("id", userId) } }
            .decodeSingleOrNull<Profile>()

    suspend fun updateProfileStyle(avatarColor: String, bannerStyle: String) {
        val userId = requireUserId()
        supabase.from("profiles").update({
            set("avatar_color", avatarColor)
            set("banner_style", bannerStyle)
        }) { filter { eq("id", userId) } }
    }

    // ---------- Избранное ----------

    private suspend fun getFavoriteIds(userId: String): List<String> =
        supabase.from("favorites")
            .select { filter { eq("user_id", userId) } }
            .decodeList<Favorite>()
            .map { it.gameId }

    suspend fun getFavorites(userId: String): List<Game> =
        GameRepository.getGamesByIds(getFavoriteIds(userId))

    suspend fun isFavorite(gameId: String): Boolean {
        val userId = AuthRepository.currentUserId() ?: return false
        return gameId in getFavoriteIds(userId)
    }

    /** Добавляет или убирает игру из избранного. Возвращает новое состояние. */
    suspend fun toggleFavorite(gameId: String): Boolean {
        val userId = requireUserId()
        val ids = getFavoriteIds(userId)
        if (gameId in ids) {
            supabase.from("favorites").delete {
                filter { eq("user_id", userId); eq("game_id", gameId) }
            }
            return false
        }
        if (ids.size >= FAVORITES_LIMIT) {
            throw AppException("В избранном может быть не более $FAVORITES_LIMIT игр. Удалите одну, чтобы добавить новую")
        }
        supabase.from("favorites").insert(Favorite(userId = userId, gameId = gameId))
        return true
    }

    // ---------- Оценки, рецензии, «пройдено» ----------

    suspend fun getMyReview(gameId: String): Review? {
        val userId = AuthRepository.currentUserId() ?: return null
        return supabase.from("reviews")
            .select { filter { eq("user_id", userId); eq("game_id", gameId) } }
            .decodeSingleOrNull<Review>()
    }

    suspend fun getMyReviews(userId: String): List<Review> =
        supabase.from("reviews")
            .select { filter { eq("user_id", userId) } }
            .decodeList<Review>()

    /** Оценка 0.5..5.0 с шагом 0.5 или null — удалить оценку. */
    suspend fun saveRating(gameId: String, rating: Float?) {
        if (rating != null) require(isValidRating(rating)) { "Недопустимая оценка: $rating" }
        updateMyReview(gameId) { current ->
            // при первой оценке игра считается пройденной (как и раньше в приложении)
            current.copy(rating = rating, isCompleted = current.isCompleted || (rating != null && current.rating == null))
        }
    }

    /** Текст рецензии или null — удалить рецензию (оценка при этом остаётся). */
    suspend fun saveComment(gameId: String, comment: String?) {
        updateMyReview(gameId) { it.copy(comment = comment?.trim()?.takeIf(String::isNotEmpty)) }
    }

    suspend fun setCompleted(gameId: String, completed: Boolean) {
        updateMyReview(gameId) { it.copy(isCompleted = completed) }
    }

    /**
     * Общая логика изменения записи reviews: читаем свою запись, меняем,
     * затем обновляем/создаём её. Если в записи ничего не осталось — удаляем.
     */
    private suspend fun updateMyReview(gameId: String, change: (Review) -> Review) {
        val userId = requireUserId()
        val existing = getMyReview(gameId)
        val base = existing ?: Review(id = UUID.randomUUID().toString(), userId = userId, gameId = gameId)
        val updated = change(base)
        val isEmpty = updated.rating == null && updated.comment.isNullOrBlank() && !updated.isCompleted

        when {
            isEmpty && existing != null -> supabase.from("reviews").delete {
                filter { eq("id", existing.id) }
            }
            isEmpty -> Unit
            existing != null -> supabase.from("reviews").update({
                set("rating", updated.rating)
                set("comment", updated.comment)
                set("is_completed", updated.isCompleted)
            }) { filter { eq("id", existing.id) } }
            else -> supabase.from("reviews").insert(
                NewReview(updated.id, userId, gameId, updated.rating, updated.comment, updated.isCompleted)
            )
        }
        logActivity(userId, gameId, hasReview = !isEmpty && (updated.rating != null || !updated.comment.isNullOrBlank()))
    }

    private fun isValidRating(value: Float): Boolean =
        value in 0.5f..5f && (value * 2f) % 1f == 0f

    suspend fun getCompletedGames(userId: String): List<RatedGame> {
        val reviews = getMyReviews(userId).filter { it.isCompleted }
            .sortedByDescending { it.createdAt }
        val ratings = reviews.associate { it.gameId to it.rating }
        return GameRepository.getGamesByIds(reviews.map { it.gameId })
            .map { RatedGame(it, ratings[it.id]) }
    }

    // ---------- Активность ----------

    /** Для каждой игры хранится одна последняя запись активности «review». */
    private suspend fun logActivity(userId: String, gameId: String, hasReview: Boolean) {
        supabase.from("activity_feed").delete {
            filter { eq("user_id", userId); eq("target_game_id", gameId); eq("action_type", "review") }
        }
        if (hasReview) {
            supabase.from("activity_feed").insert(
                ActivityItem(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    actionType = "review",
                    targetGameId = gameId,
                    createdAt = OffsetDateTime.now().toString()
                )
            )
        }
    }

    suspend fun getRecentActivity(userId: String, limit: Int = 4): List<Game> {
        val ids = supabase.from("activity_feed")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<ActivityItem>()
            .mapNotNull { it.targetGameId }
        return GameRepository.getGamesByIds(ids)
    }

    /** Новая запись reviews без created_at — дату ставит база. */
    @Serializable
    private data class NewReview(
        val id: String,
        @SerialName("user_id") val userId: String,
        @SerialName("game_id") val gameId: String,
        val rating: Float?,
        val comment: String?,
        @SerialName("is_completed") val isCompleted: Boolean
    )
}
