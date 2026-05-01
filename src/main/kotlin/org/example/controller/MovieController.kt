package org.example.controller

import kotlinx.coroutines.runBlocking
import org.example.model.dto.ApiError
import org.example.model.dto.MovieDto
import org.example.model.dto.MovieSuggestionDto
import org.example.model.dto.RatingRequest
import org.example.model.dto.RatingResult
import org.example.model.dto.TopMovieDto
import org.example.service.RatingService
import org.example.service.TopService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.NoSuchElementException

@RestController
@RequestMapping("/api")
class MovieController(
    private val ratingService: RatingService,
    private val topService: TopService
) {

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }

    @GetMapping("/search")
    fun search(@RequestParam("q") query: String): ResponseEntity<List<MovieDto>> {
        return try {
            val results = runBlocking { ratingService.searchAndCacheMovies(query) }
            ResponseEntity.ok(results)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/search/suggestions")
    fun searchSuggestions(
        @RequestParam("q") query: String,
        @RequestParam("limit", defaultValue = "5") limit: Int
    ): ResponseEntity<List<MovieSuggestionDto>> {
        if (query.length < 2) {
            return ResponseEntity.ok(emptyList())
        }

        return try {
            val suggestions = runBlocking { ratingService.searchSuggestions(query, limit) }
            ResponseEntity.ok(suggestions)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(listOf())
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(listOf())
        }
    }

    @GetMapping("/movie/{id}")
    fun getMovie(@PathVariable("id") tmdbId: Int): ResponseEntity<MovieDto> {
        return try {
            val movie = runBlocking { ratingService.getMovieByTmdbId(tmdbId) }
            ResponseEntity.ok(movie)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/movie/{id}/rating")
    fun deleteRating(@PathVariable("id") tmdbId: Int): ResponseEntity<RatingResult> {
        return try {
            val result = ratingService.deleteRatingByTmdbId(tmdbId)
            ResponseEntity.ok(result)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    @PostMapping("/rate")
    fun rate(@RequestBody @Valid request: RatingRequest): ResponseEntity<RatingResult> {
        return try {
            val result = runBlocking { ratingService.addRating(request) }
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(RatingResult(0, 0.0, 0))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }
    }

    @GetMapping("/tops")
    fun getTops(
        @RequestParam("limit", required = false) limit: Int?,
        @RequestParam("year", required = false) year: Int?
    ): ResponseEntity<List<TopMovieDto>> {
        return try {
            val tops = topService.getTop(limit, year)
            ResponseEntity.ok(tops)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
