package org.example.usecases.rating.add

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.example.TmdbClient
import org.example.TmdbMovie
import org.example.usecases.movie.Movie
import org.example.usecases.movie.MovieRepository
import org.example.usecases.rating.Rating
import org.example.usecases.rating.RatingRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api")
class AddRatingController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @PostMapping("/rate")
    @Transactional
    fun rate(@RequestBody @Valid request: AddRatingRequest): ResponseEntity<AddRatingResponse> {
        return try {
            validateRating(request)
            val result = addRating(request)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(null)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }
    }

    private fun addRating(request: AddRatingRequest): AddRatingResponse {
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

        val ratings = ratingRepository.findByMovie(movie)
        val stats = calculateStats(ratings)

        return AddRatingResponse(
            tmdbId = request.tmdbId,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
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

    private fun validateRating(request: AddRatingRequest) {
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

data class AddRatingRequest(
    @field:NotNull(message = "tmdbId es requerido")
    val tmdbId: Int,
    
    @field:NotNull(message = "direccion es requerido")
    @field:Min(value = 1, message = "direccion debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "direccion debe ser menor o igual a 10")
    val direccion: Double,
    
    @field:NotNull(message = "fotografia es requerido")
    @field:Min(value = 1, message = "fotografia debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "fotografia debe ser menor o igual a 10")
    val fotografia: Double,
    
    @field:NotNull(message = "actuacion es requerido")
    @field:Min(value = 1, message = "actuacion debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "actuacion debe ser menor o igual a 10")
    val actuacion: Double,
    
    @field:NotNull(message = "bandaSonora es requerido")
    @field:Min(value = 1, message = "bandaSonora debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "bandaSonora debe ser menor o igual a 10")
    val bandaSonora: Double,
    
    @field:NotNull(message = "guion es requerido")
    @field:Min(value = 1, message = "guion debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "guion debe ser menor o igual a 10")
    val guion: Double
)

data class AddRatingResponse(
    val tmdbId: Int,
    val averageScore: Double,
    val ratingsCount: Int
)
