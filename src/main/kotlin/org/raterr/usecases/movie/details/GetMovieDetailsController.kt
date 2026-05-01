package org.raterr.usecases.movie.details

import org.raterr.TmdbClient
import org.raterr.TmdbMovie
import org.raterr.usecases.movie.Movie
import org.raterr.usecases.movie.MovieRepository
import org.raterr.usecases.rating.Rating
import org.raterr.usecases.rating.RatingRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class GetMovieDetailsController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @GetMapping("/api/movie/{id}")
    fun getMovie(@PathVariable("id") tmdbId: Int): ResponseEntity<GetMovieDetailsResponse> {
        return try {
            val movie = getMovieByTmdbId(tmdbId)
            ResponseEntity.ok(movie)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    private fun getMovieByTmdbId(tmdbId: Int): GetMovieDetailsResponse {
        val localMovie = movieRepository.findByTmdbId(tmdbId).orElse(null)

        val movie = localMovie ?: run {
            val tmdbMovie = tmdbClient.movieDetails(tmdbId)
            upsertMovie(tmdbMovie)
        }

        return buildResponse(movie)
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

    private fun buildResponse(movie: Movie): GetMovieDetailsResponse {
        val ratings = ratingRepository.findByMovie(movie)
        val stats = calculateStats(ratings)

        return GetMovieDetailsResponse(
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
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
        }.average()

        return ScoreStats(averageScore = avg, ratingsCount = ratings.size)
    }
}

private data class ScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

data class GetMovieDetailsResponse(
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
