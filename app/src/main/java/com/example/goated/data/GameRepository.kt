package com.example.goated.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

/**
 * Каталог игр: подборки, жанры, поиск, карточка игры.
 * Функции бросают исключения, а ViewModel превращает их в состояние ошибки.
 */
object GameRepository {

    /** Порядок подборок на главном экране. */
    private val collectionOrder = listOf("Popular", "New", "Recommended")

    suspend fun getGame(gameId: String): Game? =
        supabase.from("games")
            .select { filter { eq("id", gameId) } }
            .decodeSingleOrNull<Game>()

    /** Игры по списку id в том же порядке, в каком переданы id. */
    suspend fun getGamesByIds(ids: List<String>): List<Game> {
        if (ids.isEmpty()) return emptyList()
        val games = supabase.from("games")
            .select { filter { isIn("id", ids.distinct()) } }
            .decodeList<Game>()
            .associateBy { it.id }
        return ids.distinct().mapNotNull { games[it] }
    }

    /** Подборки главного экрана — три запроса вместо отдельного запроса на каждую подборку. */
    suspend fun getCollectionsWithGames(): List<CollectionWithGames> {
        val collections = supabase.from("collections").select().decodeList<Collection>()
            .sortedBy { c -> collectionOrder.indexOf(c.title).let { if (it < 0) Int.MAX_VALUE else it } }
        val links = supabase.from("collection_games").select().decodeList<CollectionGame>()
        val games = getGamesByIds(links.map { it.gameId }).associateBy { it.id }

        return collections.map { collection ->
            val collectionGames = links
                .filter { it.collectionId == collection.id }
                .sortedBy { it.orderIndex }
                .mapNotNull { games[it.gameId] }
            CollectionWithGames(collection, collectionGames)
        }.filter { it.games.isNotEmpty() }
    }

    suspend fun getPopularGames(): List<Game> =
        getCollectionsWithGames().firstOrNull { it.collection.title == "Popular" }?.games.orEmpty()

    suspend fun getGenres(): List<Genre> =
        supabase.from("genres").select { order("id", Order.ASCENDING) }.decodeList<Genre>()

    /** Все жанры, у каждого — игры, отсортированные по популярности. */
    suspend fun getGenresWithGames(): List<GenreWithGames> {
        val genres = getGenres()
        val links = supabase.from("game_genres").select().decodeList<GameGenre>()
        val games = getGamesByIds(links.map { it.gameId }).associateBy { it.id }

        return genres.map { genre ->
            val genreGames = links.filter { it.genreId == genre.id }
                .mapNotNull { games[it.gameId] }
                .sortedWith(popularity)
            GenreWithGames(genre, genreGames)
        }.filter { it.games.isNotEmpty() }
    }

    suspend fun getGenreWithGames(genreId: Int): GenreWithGames? {
        val genre = supabase.from("genres")
            .select { filter { eq("id", genreId) } }
            .decodeSingleOrNull<Genre>() ?: return null
        val ids = supabase.from("game_genres")
            .select { filter { eq("genre_id", genreId) } }
            .decodeList<GameGenre>()
            .map { it.gameId }
        return GenreWithGames(genre, getGamesByIds(ids).sortedWith(popularity))
    }

    /** Поиск по названию без учёта регистра. */
    suspend fun searchGames(query: String): List<Game> {
        val text = query.trim().replace("%", "\\%").replace("_", "\\_")
        if (text.isEmpty()) return emptyList()
        return supabase.from("games")
            .select {
                filter { ilike("title", "%$text%") }
                order("title", Order.ASCENDING)
            }
            .decodeList<Game>()
    }

    suspend fun getScreenshots(gameId: String): List<String> =
        supabase.from("screenshots")
            .select {
                filter { eq("game_id", gameId) }
                order("order_index", Order.ASCENDING)
            }
            .decodeList<Screenshot>()
            .map { it.imageUrl }

    /** Рецензии с текстом к игре вместе с ником автора, новые сверху. */
    suspend fun getGameReviews(gameId: String): List<ReviewWithAuthor> =
        supabase.from("reviews")
            .select(Columns.raw("id, user_id, rating, comment, created_at, profiles(username)")) {
                filter {
                    eq("game_id", gameId)
                    neq("comment", "") // NULL тоже отбрасывается этим условием
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ReviewWithAuthor>()
            .filter { !it.comment.isNullOrBlank() }

    private val popularity = compareByDescending<Game> { it.ratingCount }
        .thenByDescending { it.averageRating ?: 0f }
        .thenBy { it.title }
}
