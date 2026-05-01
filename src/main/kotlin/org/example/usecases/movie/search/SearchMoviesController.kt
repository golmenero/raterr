package org.example.usecases.movie.search

import org.example.TmdbClient
import org.example.TmdbMovie
import org.example.usecases.movie.Movie
import org.example.usecases.movie.MovieRepository
import org.example.usecases.rating.Rating
import org.example.usecases.rating.RatingRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchMoviesController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @GetMapping("/api/search")
    fun search(@RequestParam("q") query: String): ResponseEntity<List<SearchMoviesResponse>> {
        return try {
            val results = searchAndCacheMovies(query)
            ResponseEntity.ok(results)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    private fun searchAndCacheMovies(query: String): List<SearchMoviesResponse> {
        val tmdbMovies = tmdbClient.searchMovies(query).take(16)
        return tmdbMovies.map { tmdbMovie ->
            val movie = upsertMovie(tmdbMovie)
            buildResponse(movie)
        }
    }

    private fun upsertMovie(tmdbMovie: TmdbMovie): Movie {
        val existing = movieRepository.findByTmdbId(tmdbMovie.id).orElse(null)

        return if (existing != null) {
            val updated = existing.copy(
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage
            )
            movieRepository.save(updated)
        } else {
            val newMovie = Movie(
                tmdbId = tmdbMovie.id,
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage
            )
            movieRepository.save(newMovie)
        }
    }

    private fun buildResponse(movie: Movie): SearchMoviesResponse {
        val ratings = ratingRepository.findByMovie(movie)
        val stats = calculateStats(ratings)

        return SearchMoviesResponse(
            tmdbId = movie.tmdbId,
            title = movie.title,
            overview = movie.overview,
            releaseDate = movie.releaseDate,
            releaseYear = movie.releaseYear,
            posterPath = movie.posterPath,
            tmdbVoteAverage = movie.tmdbVoteAverage,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    private fun calculateStats(ratings: List<Rating>): ScoreStats {
        if (ratings.isEmpty()) {
            return ScoreStats(averageScore = 0.0, ratingsCount = 0)
        }

        val avg = ratings.map { rating ->
            (rating.direccion + rating.fotografia + rating.actuacion + rating.bandaSonora + rating.guion) / 5.0
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
