package com.example.goated.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goated.data.AuthRepository
import com.example.goated.data.Game
import com.example.goated.data.GameRepository
import com.example.goated.data.Review
import com.example.goated.data.ReviewWithAuthor
import com.example.goated.data.UserRepository
import com.example.goated.data.toUserMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameCardViewModel(private val gameId: String) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val game: Game? = null,
        val screenshots: List<String> = emptyList(),
        val reviews: List<ReviewWithAuthor> = emptyList(),
        val myReview: Review? = null,
        val isFavorite: Boolean = false,
        val isLoggedIn: Boolean = false,
        val saving: Boolean = false,
        val message: String? = null
    ) {
        val myRating: Float? get() = myReview?.rating
        val myComment: String? get() = myReview?.comment?.takeIf { it.isNotBlank() }
        val otherReviews: List<ReviewWithAuthor>
            get() = reviews.filter { it.userId != myReview?.userId }
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = it.game == null, error = null) }
            try {
                val game = async { GameRepository.getGame(gameId) }
                val screenshots = async { GameRepository.getScreenshots(gameId) }
                val reviews = async { GameRepository.getGameReviews(gameId) }
                val myReview = async { UserRepository.getMyReview(gameId) }
                val favorite = async { UserRepository.isFavorite(gameId) }
                val loaded = game.await()
                _state.update {
                    it.copy(
                        loading = false,
                        error = if (loaded == null) "Игра не найдена" else null,
                        game = loaded,
                        screenshots = screenshots.await(),
                        reviews = reviews.await(),
                        myReview = myReview.await(),
                        isFavorite = favorite.await(),
                        isLoggedIn = AuthRepository.currentUserId() != null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUserMessage()) }
            }
        }
    }
    fun setRating(rating: Float?) = runAction(
        success = if (rating == null) "Оценка удалена" else null
    ) { UserRepository.saveRating(gameId, rating) }

    fun saveComment(text: String) = runAction(success = "Рецензия сохранена") {
        UserRepository.saveComment(gameId, text)
    }

    fun deleteComment() = runAction(success = "Рецензия удалена") {
        UserRepository.saveComment(gameId, null)
    }

    fun toggleCompleted() {
        val completed = !(_state.value.myReview?.isCompleted ?: false)
        runAction(success = if (completed) "Игра отмечена как пройденная" else null) {
            UserRepository.setCompleted(gameId, completed)
        }
    }

    fun toggleFavorite() {
        if (_state.value.saving) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            try {
                val nowFavorite = UserRepository.toggleFavorite(gameId)
                _state.update {
                    it.copy(
                        saving = false,
                        isFavorite = nowFavorite,
                        message = if (nowFavorite) "Добавлено в избранное" else "Удалено из избранного"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, message = e.toUserMessage()) }
            }
        }
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    private fun runAction(success: String?, action: suspend () -> Unit) {
        if (_state.value.saving) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            try {
                action()
                val game = GameRepository.getGame(gameId)
                val reviews = GameRepository.getGameReviews(gameId)
                val myReview = UserRepository.getMyReview(gameId)
                _state.update {
                    it.copy(saving = false, game = game ?: it.game, reviews = reviews, myReview = myReview, message = success)
                }
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, message = e.toUserMessage()) }
            }
        }
    }
}
