package org.example.usecases.rating.delete

import org.example.usecases.movie.MovieRepository
import org.example.usecases.rating.Rating
import org.example.usecases.rating.RatingRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.NoSuchElementException

@RestController
class DeleteRatingController(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @DeleteMapping("/api/movie/{id}/rating")
    @Transactional
    fun deleteRating(@PathVariable("id") tmdbId: Int): ResponseEntity<DeleteRatingResponse> {
        return try {
            val movie = movieRepository.findByTmdbId(tmdbId)
                .orElseThrow { NoSuchElementException("Pelicula no encontrada") }

            val deletedCount = ratingRepository.deleteByMovie(movie)

            if (deletedCount == 0) {
                throw NoSuchElementException("Valoracion no encontrada")
            }

            val ratings = ratingRepository.findByMovie(movie)
            val stats = calculateStats(ratings)

            val response = DeleteRatingResponse(
                tmdbId = tmdbId,
                averageScore = stats.averageScore,
                ratingsCount = stats.ratingsCount
            )
            ResponseEntity.ok(response)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
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

data class DeleteRatingResponse(
    val tmdbId: Int,
    val averageScore: Double,
    val ratingsCount: Int
)
