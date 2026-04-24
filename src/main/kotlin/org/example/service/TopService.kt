package org.example.service

import org.example.db.Movies
import org.example.db.Ratings
import org.example.model.TopMovieDto
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class TopService {

    fun getTop(limit: Int, year: Int?): List<TopMovieDto> {
        val safeLimit = limit.coerceIn(1, 100)

        return transaction {
            val query = (Movies leftJoin Ratings).selectAll()
            if (year != null) {
                query.andWhere { Movies.releaseYear eq year }
            }

            val grouped = linkedMapOf<Int, TopAccumulator>()

            query.forEach { row ->
                val key = row[Movies.id].value
                val acc = grouped.getOrPut(key) {
                    TopAccumulator(
                        tmdbId = row[Movies.tmdbId],
                        title = row[Movies.title],
                        releaseYear = row[Movies.releaseYear]
                    )
                }

                try {
                    val ratingId = row[Ratings.id]
                    if (ratingId != null) {
                        val perRatingScore = (
                            row[Ratings.direccion] +
                                row[Ratings.fotografia] +
                                row[Ratings.actuacion] +
                                row[Ratings.bandaSonora] +
                                row[Ratings.guion]
                            ) / 5.0
                        acc.scoreSum += perRatingScore
                        acc.count += 1
                    }
                } catch (e: Exception) {
                    // row viene de left join, si no hay rating esta fila es null
                }
            }

            grouped.values
                .filter { it.count > 0 }
                .map {
                    TopMovieDto(
                        tmdbId = it.tmdbId,
                        title = it.title,
                        releaseYear = it.releaseYear,
                        averageScore = round2(it.scoreSum / it.count),
                        ratingsCount = it.count
                    )
                }
                .sortedByDescending { it.averageScore }
                .take(safeLimit)
        }
    }


    private fun round2(value: Double): Double = kotlin.math.round(value * 100.0) / 100.0
}

private data class TopAccumulator(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?
) {
    var scoreSum: Double = 0.0
    var count: Int = 0
}

