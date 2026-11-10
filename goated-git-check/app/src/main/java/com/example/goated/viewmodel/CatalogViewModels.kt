package com.example.goated.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goated.data.CollectionWithGames
import com.example.goated.data.Game
import com.example.goated.data.GameRepository
import com.example.goated.data.Genre
import com.example.goated.data.GenreWithGames
import com.example.goated.data.toUserMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListUiState<T>(
    val loading: Boolean = true,
    val error: String? = null,
    val items: List<T> = emptyList()
)

abstract class ListViewModel<T> : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<T>())
    val state: StateFlow<ListUiState<T>> = _state.asStateFlow()

    protected abstract suspend fun loadItems(): List<T>

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            _state.value = try {
                ListUiState(loading = false, items = loadItems())
            } catch (e: Exception) {
                ListUiState(loading = false, error = e.toUserMessage())
            }
        }
    }
}

class MainViewModel : ListViewModel<CollectionWithGames>() {
    override suspend fun loadItems() = GameRepository.getCollectionsWithGames()
}

class GenresViewModel : ListViewModel<GenreWithGames>() {
    override suspend fun loadItems() =
        GameRepository.getGenresWithGames().map { it.copy(games = it.games.take(3)) }
}

class GenreGamesViewModel(private val genreId: Int) : ViewModel() {
    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val genreName: String = "",
        val games: List<Game> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            _state.value = try {
                val result = GameRepository.getGenreWithGames(genreId)
                if (result == null) UiState(loading = false, error = "Жанр не найден")
                else UiState(loading = false, genreName = result.genre.name, games = result.games)
            } catch (e: Exception) {
                UiState(loading = false, error = e.toUserMessage())
            }
        }
    }
}

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {
    data class UiState(
        val query: String = "",
        val searching: Boolean = false,
        val results: List<Game> = emptyList(),
        val error: String? = null,
        val popular: List<Game> = emptyList(),
        val genres: List<Genre> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private val queryFlow = MutableStateFlow("")

    init {
        loadSuggestions()
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query -> search(query) }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value, searching = value.isNotBlank()) }
        queryFlow.value = value.trim()
    }

    fun retry() {
        loadSuggestions()
        viewModelScope.launch { search(queryFlow.value) }
    }

    private suspend fun search(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), searching = false, error = null) }
            return
        }
        _state.update { it.copy(searching = true, error = null) }
        try {
            val games = GameRepository.searchGames(query)
            _state.update { it.copy(results = games, searching = false) }
        } catch (e: Exception) {
            _state.update { it.copy(results = emptyList(), searching = false, error = e.toUserMessage()) }
        }
    }
    private fun loadSuggestions() {
        viewModelScope.launch {
            try {
                val popular = GameRepository.getPopularGames()
                val genres = GameRepository.getGenres()
                _state.update { it.copy(popular = popular, genres = genres) }
            } catch (_: Exception) {
            }
        }
    }
}
