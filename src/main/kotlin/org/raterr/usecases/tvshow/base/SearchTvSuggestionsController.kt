package org.raterr.usecases.tvshow.base

import org.raterr.TmdbClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchTvSuggestionsController(
    private val tmdbClient: TmdbClient
) {

    @GetMapping("/api/tv/search/suggestions")
    fun searchSuggestions(
        @RequestParam("q") query: String,
        @RequestParam("limit", defaultValue = "5") limit: Int
    ): ResponseEntity<List<SearchTvSuggestionsResponse>> {
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

    private fun fetchSuggestions(query: String, limit: Int): List<SearchTvSuggestionsResponse> {
        if (query.isBlank()) return emptyList()

        val safeLimit = limit.coerceIn(1, 10)
        return tmdbClient.searchTvShows(query)
            .asSequence()
            .take(safeLimit)
            .map { show ->
                SearchTvSuggestionsResponse(
                    tmdbId = show.id,
                    name = show.name,
                    firstAirYear = show.firstAirDate?.take(4)?.toIntOrNull()
                )
            }
            .toList()
    }
}

data class SearchTvSuggestionsResponse(
    val tmdbId: Int,
    val name: String,
    val firstAirYear: Int?
)
