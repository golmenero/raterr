package org.raterr.usecases.top

import org.raterr.usecases.movie.MovieRepository
import org.raterr.usecases.rating.RatingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class GetTopMoviesController(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @GetMapping("/top")
    fun topsPage(
        @RequestParam("limit", required = false) limit: Int?,
        @RequestParam("year", required = false) year: Int?,
        model: Model
    ): String {
        try {
            val tops = getTopMovies(limit, year)
            model.addAttribute("tops", tops)
            model.addAttribute("selectedYear", year)
            return "top"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the tops.")
            return "top"
        }
    }

    private fun getTopMovies(limit: Int?, year: Int?): List<GetTopMoviesResponse> {
        val safeLimit = limit?.coerceIn(1, 100)

        val movies = movieRepository.findAllWithRatings()

        val filtered = if (year != null) {
            movies.filter { it.releaseYear == year }
        } else {
            movies
        }

        val results = filtered
            .filter { it.ratings.isNotEmpty() }
            .map { movie ->
                val ratings = movie.ratings
                val count = ratings.size

                val avgScore = ratings.map { rating ->
                    (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
                }.average()

                val avgDirecting = ratings.map { it.directing }.average()
                val avgCinematography = ratings.map { it.cinematography }.average()
                val avgActing = ratings.map { it.acting }.average()
                val avgSoundtrack = ratings.map { it.soundtrack }.average()
                val avgScreenplay = ratings.map { it.screenplay }.average()

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