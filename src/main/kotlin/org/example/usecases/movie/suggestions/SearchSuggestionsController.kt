package org.example.usecases.movie.suggestions

import org.example.TmdbClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchSuggestionsController(
    private val tmdbClient: TmdbClient
) {

    @GetMapping("/api/search/suggestions")
    fun searchSuggestions(
        @RequestParam("q") query: String,
        @RequestParam("limit", defaultValue = "5") limit: Int
    ): ResponseEntity<List<SearchSuggestionsResponse>> {
        if (query.length < 2) {
            return ResponseEntity.ok(emptyList())
        }

        return try {
            val suggestions = fetchSuggestions(query, limit)
            ResponseEntity.ok(suggestions)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(emptyList())
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(emptyList())
        }
    }

    private fun fetchSuggestions(query: String, limit: Int): List<SearchSuggestionsResponse> {
        if (query.isBlank()) return emptyList()

        val safeLimit = limit.coerceIn(1, 10)
        return tmdbClient.searchMovies(query)
            .asSequence()
            .take(safeLimit)
            .map { movie ->
                SearchSuggestionsResponse(
                    tmdbId = movie.id,
                    title = movie.title,
                    releaseYear = movie.releaseDate?.take(4)?.toIntOrNull()
                )
            }
            .toList()
    }
}

data class SearchSuggestionsResponse(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?
)
