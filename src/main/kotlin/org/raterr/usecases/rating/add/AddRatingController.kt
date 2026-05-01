package org.raterr.usecases.rating.add

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.raterr.TmdbClient
import org.raterr.TmdbMovie
import org.raterr.usecases.movie.Movie
import org.raterr.usecases.movie.MovieRepository
import org.raterr.usecases.rating.Rating
import org.raterr.usecases.rating.RatingRepository
import org.raterr.usecases.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
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
    private val ratingRepository: RatingRepository,
    private val userRepository: UserRepository
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
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        val user = userRepository.findById(username)
            .orElseThrow { IllegalArgumentException("User not found") }

        val movie = movieRepository.findByTmdbId(request.tmdbId).orElse(null)
            ?: upsertMovie(tmdbClient.movieDetails(request.tmdbId))

        val existingRating = ratingRepository.findByMovieAndUser(movie, user).firstOrNull()
        require(existingRating == null) {
            "Rating already exists for this movie. Delete it from Tops before creating another."
        }

        val newRating = Rating(
            movie = movie,
            user = user,
            directing = request.directing,
            cinematography = request.cinematography,
            acting = request.acting,
            soundtrack = request.soundtrack,
            screenplay = request.screenplay,
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
            "directing" to request.directing,
            "cinematography" to request.cinematography,
            "acting" to request.acting,
            "soundtrack" to request.soundtrack,
            "screenplay" to request.screenplay
        ).forEach { (field, value) ->
            require(value >= 1.0 && value <= 10.0) { "Field $field must be between 1 and 10" }
        }
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

data class AddRatingRequest(
    @field:NotNull(message = "tmdbId is required")
    val tmdbId: Int,
    
    @field:NotNull(message = "directing is required")
    @field:Min(value = 1, message = "directing must be between 1 and 10")
    @field:Max(value = 10, message = "directing must be between 1 and 10")
    val directing: Double,
    
    @field:NotNull(message = "cinematography is required")
    @field:Min(value = 1, message = "cinematography must be between 1 and 10")
    @field:Max(value = 10, message = "cinematography must be between 1 and 10")
    val cinematography: Double,
    
    @field:NotNull(message = "acting is required")
    @field:Min(value = 1, message = "acting must be between 1 and 10")
    @field:Max(value = 10, message = "acting must be between 1 and 10")
    val acting: Double,
    
    @field:NotNull(message = "soundtrack is required")
    @field:Min(value = 1, message = "soundtrack must be between 1 and 10")
    @field:Max(value = 10, message = "soundtrack must be between 1 and 10")
    val soundtrack: Double,
    
    @field:NotNull(message = "screenplay is required")
    @field:Min(value = 1, message = "screenplay must be between 1 and 10")
    @field:Max(value = 10, message = "screenplay must be between 1 and 10")
    val screenplay: Double
)

data class AddRatingResponse(
    val tmdbId: Int,
    val averageScore: Double,
    val ratingsCount: Int
)
