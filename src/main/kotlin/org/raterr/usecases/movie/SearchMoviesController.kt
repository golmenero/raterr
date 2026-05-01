package org.raterr.usecases.movie

import org.raterr.TmdbClient
import org.raterr.TmdbMovie
import org.raterr.usecases.movie.MovieRepository
import org.raterr.usecases.rating.Rating
import org.raterr.usecases.rating.RatingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SearchMoviesController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @GetMapping("/")
    fun searchPage(@RequestParam("q", required = false) query: String?, model: Model): String {
        if (!query.isNullOrBlank()) {
            val results = searchAndCacheMovies(query)
            model.addAttribute("query", query)
            model.addAttribute("movies", results)
        }
        return "index"
    }

    private fun searchAndCacheMovies(query: String): List<SearchMoviesResponse> {
        val tmdbMovies = tmdbClient.searchMovies(query).take(16)
        return tmdbMovies.map { tmdbMovie ->
            buildResponseFromTmdb(tmdbMovie)
        }
    }

    private fun buildResponseFromTmdb(tmdbMovie: TmdbMovie): SearchMoviesResponse {
        val movie = movieRepository.findByTmdbId(tmdbMovie.id).orElse(null)
        val ratings = if (movie != null) {
            ratingRepository.findByMovie(movie)
        } else {
            emptyList()
        }
        val stats = calculateStats(ratings)

        return SearchMoviesResponse(
            tmdbId = tmdbMovie.id,
            title = tmdbMovie.title,
            overview = tmdbMovie.overview,
            releaseDate = tmdbMovie.releaseDate,
            releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
            posterPath = tmdbMovie.posterPath,
            tmdbVoteAverage = tmdbMovie.voteAverage,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    private fun calculateStats(ratings: List<Rating>): ScoreStats {
        if (ratings.isEmpty()) {
            return ScoreStats(averageScore = 0.0, ratingsCount = 0)
        }

        val avg = ratings.map { rating ->
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
        }.average()

        return ScoreStats(averageScore = avg, ratingsCount = ratings.size)
    }
}

private data class ScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

data class SearchMoviesResponse(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val releaseYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val averageScore: Double,
    val ratingsCount: Int
)
