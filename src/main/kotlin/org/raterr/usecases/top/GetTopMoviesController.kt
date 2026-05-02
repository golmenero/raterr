package org.raterr.usecases.top

import org.raterr.TmdbClient
import org.raterr.usecases.movie.MovieRepository
import org.raterr.usecases.rating.RatingRepository
import org.raterr.usecases.user.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class GetTopMoviesController(
    private val ratingRepository: RatingRepository,
    private val userRepository: UserRepository,
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository
) {

    @GetMapping("/top")
    fun topsPage(
        @RequestParam("limit", required = false) limit: Int?,
        @RequestParam("year", required = false) year: Int?,
        @RequestParam("category", required = false) category: String?,
        model: Model
    ): String {
        try {
            val tops = getTopMovies(limit, year, category)
            model.addAttribute("tops", tops)
            model.addAttribute("selectedYear", year)
            model.addAttribute("selectedCategory", category)
            model.addAttribute("availableCategories", getAvailableCategories())
            return "top"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the tops.")
            return "top"
        }
    }

    private fun getTopMovies(limit: Int?, year: Int?, category: String?): List<GetTopMoviesResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        val user = userRepository.findById(username).orElse(null)
            ?: return emptyList()

        val safeLimit = limit?.coerceIn(1, 100)

        val ratings = ratingRepository.findByUser(user)

        var filtered = if (year != null) {
            ratings.filter { it.movie.releaseYear == year }
        } else {
            ratings
        }

        if (category != null && category.isNotBlank()) {
            filtered = filtered.filter { rating ->
                rating.movie.genres?.contains(category, ignoreCase = true) == true
            }
        }

        val results = filtered
            .groupBy { it.movie }
            .map { (movie, ratings) ->
                val count = ratings.size

                val individualScores = ratings.map { rating ->
                    (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
                }

                val avgScore = if (individualScores.isNotEmpty()) individualScores.average() else 0.0
                val avgDirecting = if (ratings.isNotEmpty()) ratings.map { it.directing }.average() else 0.0
                val avgCinematography = if (ratings.isNotEmpty()) ratings.map { it.cinematography }.average() else 0.0
                val avgActing = if (ratings.isNotEmpty()) ratings.map { it.acting }.average() else 0.0
                val avgSoundtrack = if (ratings.isNotEmpty()) ratings.map { it.soundtrack }.average() else 0.0
                val avgScreenplay = if (ratings.isNotEmpty()) ratings.map { it.screenplay }.average() else 0.0

                GetTopMoviesResponse(
                    tmdbId = movie.tmdbId,
                    title = movie.title,
                    releaseYear = movie.releaseYear,
                    posterPath = movie.posterPath,
                    averageScore = avgScore,
                    ratingsCount = count,
                    directing = avgDirecting,
                    cinematography = avgCinematography,
                    acting = avgActing,
                    soundtrack = avgSoundtrack,
                    screenplay = avgScreenplay
                )
            }
            .sortedByDescending { it.averageScore }

        return if (safeLimit != null) results.take(safeLimit) else results
    }

    private fun getAvailableCategories(): List<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        val user = userRepository.findById(username).orElse(null)
            ?: return emptyList()

        val ratings = ratingRepository.findByUser(user)
        ratings.forEach { rating ->
            if (rating.movie.genres == null || rating.movie.genres.isNullOrBlank()) {
                fetchAndPersistGenres(rating.movie.tmdbId)
            }
        }

        val updatedRatings = ratingRepository.findByUser(user)
        return updatedRatings
            .mapNotNull { it.movie.genres }
            .flatMap { it.split(",").map { genre -> genre.trim() } }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    private fun fetchAndPersistGenres(tmdbId: Int) {
        try {
            val tmdbMovie = tmdbClient.movieDetails(tmdbId)
            val existingMovie = movieRepository.findById(tmdbId).orElse(null)
            if (existingMovie != null) {
                val genres = tmdbMovie.genres.joinToString(",") { it.name }
                val updated = existingMovie.copy(genres = genres)
                movieRepository.save(updated)
            }
        } catch (e: Exception) {
            // Silently ignore errors when fetching genres
        }
    }
}

data class GetTopMoviesResponse(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?,
    val posterPath: String?,
    val averageScore: Double,
    val ratingsCount: Int,
    val directing: Double = 0.0,
    val cinematography: Double = 0.0,
    val acting: Double = 0.0,
    val soundtrack: Double = 0.0,
    val screenplay: Double = 0.0
)