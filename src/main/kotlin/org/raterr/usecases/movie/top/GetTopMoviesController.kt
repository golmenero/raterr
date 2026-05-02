package org.raterr.usecases.movie.top

import org.raterr.usecases.movie.rating.Rating
import org.raterr.usecases.movie.rating.RatingRepository
import org.raterr.usecases.user.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class GetTopMoviesController(
    private val ratingRepository: RatingRepository,
    private val userRepository: UserRepository
) {

    @GetMapping("/top")
    fun topsPage(
        @RequestParam("limit", required = false) limit: Int?,
        @RequestParam("year", required = false) year: Int?,
        @RequestParam("category", required = false) category: String?,
        model: Model
    ): String {
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            val username = authentication.name
            val user = userRepository.findById(username).orElse(null)
            
            if (user == null) {
                model.addAttribute("tops", emptyList<GetTopMoviesResponse>())
                model.addAttribute("selectedYear", year)
                model.addAttribute("selectedCategory", category)
                model.addAttribute("availableCategories", emptyList<String>())
                return "top"
            }
            
            val ratings = ratingRepository.findByUser(user)
            val tops = getTopMovies(ratings, limit, year, category)
            
            model.addAttribute("tops", tops)
            model.addAttribute("selectedYear", year)
            model.addAttribute("selectedCategory", category)
            return "top"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the tops.")
            return "top"
        }
    }

    private fun getTopMovies(ratings: List<Rating>, limit: Int?, year: Int?, category: String?): List<GetTopMoviesResponse> {
        val safeLimit = limit?.coerceIn(1, 100)

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