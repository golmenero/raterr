package org.example.service

import org.example.model.dto.MovieDto
import org.example.model.dto.MovieSuggestionDto
import org.example.model.dto.RatingRequest
import org.example.model.dto.RatingResult
import org.example.model.entity.Movie
import org.example.model.entity.Rating
import org.example.repository.MovieRepository
import org.example.repository.RatingRepository
import org.example.tmdb.TmdbClient
import org.example.tmdb.TmdbMovie
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@Service
class RatingService(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {
    private val maxSearchResults = 16

    suspend fun searchSuggestions(query: String, limit: Int = 5): List<MovieSuggestionDto> {
        if (query.isBlank()) return emptyList()

        val safeLimit = limit.coerceIn(1, 10)
        return tmdbClient.searchMovies(query)
            .asSequence()
            .take(safeLimit)
            .map { movie ->
                MovieSuggestionDto(
                    tmdbId = movie.id,
                    title = movie.title,
                    releaseYear = movie.releaseDate?.take(4)?.toIntOrNull()
                )
            }
            .toList()
    }

    suspend fun searchAndCacheMovies(query: String): List<MovieDto> {
        val tmdbMovies = tmdbClient.searchMovies(query).take(maxSearchResults)
        return tmdbMovies.map { tmdbMovie ->
            val movie = upsertMovie(tmdbMovie)
            buildMovieDto(movie)
        }
    }

    suspend fun getMovieByTmdbId(tmdbId: Int): MovieDto {
        val localMovie = movieRepository.findByTmdbId(tmdbId).orElse(null)

        val movie = localMovie ?: run {
            val tmdbMovie = tmdbClient.movieDetails(tmdbId)
            upsertMovie(tmdbMovie)
        }

        return buildMovieDto(movie)
    }

    @Transactional
    suspend fun addRating(request: RatingRequest): RatingResult {
        validateRating(request)

        val movie = movieRepository.findByTmdbId(request.tmdbId).orElse(null)
            ?: upsertMovie(tmdbClient.movieDetails(request.tmdbId))

        val existingRating = ratingRepository.findByMovie(movie).firstOrNull()
        require(existingRating == null) {
            "Ya existe una valoracion para esta pelicula. Eliminala desde Tops antes de crear otra."
        }

        val newRating = Rating(
            movie = movie,
            direccion = request.direccion,
            fotografia = request.fotografia,
            actuacion = request.actuacion,
            bandaSonora = request.bandaSonora,
            guion = request.guion,
            createdAtEpochMs = System.currentTimeMillis()
        )
        ratingRepository.save(newRating)

        val stats = calculateStats(movie)
        return RatingResult(
            tmdbId = request.tmdbId,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    @Transactional
    fun deleteRatingByTmdbId(tmdbId: Int): RatingResult {
        val movie = movieRepository.findByTmdbId(tmdbId)
            .orElseThrow { NoSuchElementException("Pelicula no encontrada") }

        val deletedCount = ratingRepository.deleteByMovie(movie)

        if (deletedCount == 0) {
            throw NoSuchElementException("Valoracion no encontrada")
        }

        val stats = calculateStats(movie)
        return RatingResult(
            tmdbId = tmdbId,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    private fun validateRating(request: RatingRequest) {
        listOf(
            "direccion" to request.direccion,
            "fotografia" to request.fotografia,
            "actuacion" to request.actuacion,
            "bandaSonora" to request.bandaSonora,
            "guion" to request.guion
        ).forEach { (field, value) ->
            require(value >= 1.0 && value <= 10.0) { "El campo $field debe estar entre 1 y 10" }
        }
    }

    @Transactional
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

    private fun buildMovieDto(movie: Movie): MovieDto {
        val stats = calculateStats(movie)

        return MovieDto(
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

    private fun calculateStats(movie: Movie): ScoreStats {
        val ratings = ratingRepository.findByMovie(movie)
        if (ratings.isEmpty()) {
            return ScoreStats(averageScore = 0.0, ratingsCount = 0)
        }

        val avg = ratings.map { rating ->
            (
                rating.direccion +
                    rating.fotografia +
                    rating.actuacion +
                    rating.bandaSonora +
                    rating.guion
                ) / 5.0
        }.average()

        return ScoreStats(averageScore = avg, ratingsCount = ratings.size)
    }
}

private data class ScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)
