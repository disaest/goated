package com.example.goated.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goated.data.AppException
import com.example.goated.data.AuthRepository
import com.example.goated.data.Game
import com.example.goated.data.Profile
import com.example.goated.data.RatedGame
import com.example.goated.data.UserRepository
import com.example.goated.data.toUserMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(private val userId: String) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val profile: Profile? = null,
        val favorites: List<Game> = emptyList(),
        val recent: List<RatedGame> = emptyList(),
        val completedCount: Int = 0,
        val savingStyle: Boolean = false,
        val message: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = it.profile == null, error = null) }
            try {
                val profile = async { UserRepository.getProfile(userId) }
                val favorites = async { UserRepository.getFavorites(userId) }
                val recent = async { UserRepository.getRecentActivity(userId) }
                val reviews = async { UserRepository.getMyReviews(userId) }
                val ratings = reviews.await().associate { it.gameId to it.rating }
                _state.update {
                    it.copy(
                        loading = false,
                        profile = profile.await()
                            ?: throw AppException("Профиль не найден. Выполните SQL-миграцию Supabase"),
                        favorites = favorites.await(),
                        recent = recent.await().map { game -> RatedGame(game, ratings[game.id]) },
                        completedCount = reviews.await().count { r -> r.isCompleted }
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUserMessage()) }
            }
        }
    }

    fun saveStyle(avatarColor: String, bannerStyle: String) {
        viewModelScope.launch {
            _state.update { it.copy(savingStyle = true) }
            try {
                UserRepository.updateProfileStyle(avatarColor, bannerStyle)
                _state.update {
                    it.copy(
                        savingStyle = false,
                        profile = it.profile?.copy(avatarColor = avatarColor, bannerStyle = bannerStyle),
                        message = "Оформление сохранено"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(savingStyle = false, message = e.toUserMessage()) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                AuthRepository.signOut()
            } catch (e: Exception) {
                _state.update { it.copy(message = e.toUserMessage()) }
            }
        }
    }

    fun messageShown() = _state.update { it.copy(message = null) }
}
class CompletedGamesViewModel(private val userId: String) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val profile: Profile? = null,
        val games: List<RatedGame> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = it.games.isEmpty() && it.profile == null, error = null) }
            try {
                val profile = async { UserRepository.getProfile(userId) }
                val games = async { UserRepository.getCompletedGames(userId) }
                _state.update { it.copy(loading = false, profile = profile.await(), games = games.await()) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUserMessage()) }
            }
        }
    }
}
