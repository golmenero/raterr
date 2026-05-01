package org.example.service

import org.example.model.dto.TopMovieDto
import org.example.repository.MovieRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TopService(
    private val movieRepository: MovieRepository
) {

    @Transactional(readOnly = true)
    fun getTop(limit: Int?, year: Int?): List<TopMovieDto> {
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
                    (rating.direccion + rating.fotografia + rating.actuacion + rating.bandaSonora + rating.guion) / 5.0
                }.average()

                val avgDireccion = ratings.map { it.direccion }.average()
                val avgFotografia = ratings.map { it.fotografia }.average()
                val avgActuacion = ratings.map { it.actuacion }.average()
                val avgBandaSonora = ratings.map { it.bandaSonora }.average()
                val avgGuion = ratings.map { it.guion }.average()

                TopMovieDto(
                    tmdbId = movie.tmdbId,
                    title = movie.title,
                    releaseYear = movie.releaseYear,
                    posterPath = movie.posterPath,
                    averageScore = avgScore,
                    ratingsCount = count,
                    direccion = avgDireccion,
                    fotografia = avgFotografia,
                    actuacion = avgActuacion,
                    bandaSonora = avgBandaSonora,
                    guion = avgGuion
                )
            }
            .sortedByDescending { it.averageScore }

        return if (safeLimit != null) results.take(safeLimit) else results
    }
}
