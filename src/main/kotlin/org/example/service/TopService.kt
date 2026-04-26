package org.example.service

import org.example.db.Movies
import org.example.db.Ratings
import org.example.model.TopMovieDto
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class TopService {

    fun getTop(limit: Int?, year: Int?): List<TopMovieDto> {
        val safeLimit = limit?.coerceIn(1, 100)

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
                        releaseYear = row[Movies.releaseYear],
                        posterPath = row[Movies.posterPath]
                    )
                }

                try {
                    row[Ratings.id]
                    val perRatingScore = (
                        row[Ratings.direccion] +
                            row[Ratings.fotografia] +
                            row[Ratings.actuacion] +
                            row[Ratings.bandaSonora] +
                            row[Ratings.guion]
                        ) / 5.0
                    acc.scoreSum += perRatingScore
                    acc.direccionSum += row[Ratings.direccion]
                    acc.fotografiaSum += row[Ratings.fotografia]
                    acc.actuacionSum += row[Ratings.actuacion]
                    acc.bandaSonoraSum += row[Ratings.bandaSonora]
                    acc.guionSum += row[Ratings.guion]
                    acc.count += 1
                } catch (_: Exception) {
                    // row viene de left join, si no hay rating esta fila es null
                }
            }

            val sorted = grouped.values
                .filter { it.count > 0 }
                .map {
                    TopMovieDto(
                        tmdbId = it.tmdbId,
                        title = it.title,
                        releaseYear = it.releaseYear,
                        posterPath = it.posterPath,
                        averageScore = it.scoreSum / it.count,
                        ratingsCount = it.count,
                        direccion = it.direccionSum / it.count,
                        fotografia = it.fotografiaSum / it.count,
                        actuacion = it.actuacionSum / it.count,
                        bandaSonora = it.bandaSonoraSum / it.count,
                        guion = it.guionSum / it.count
                    )
                }
                .sortedByDescending { it.averageScore }

            if (safeLimit != null) sorted.take(safeLimit) else sorted
        }
    }
}

private data class TopAccumulator(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?,
    val posterPath: String?
) {
    var scoreSum: Double = 0.0
    var direccionSum: Double = 0.0
    var fotografiaSum: Double = 0.0
    var actuacionSum: Double = 0.0
    var bandaSonoraSum: Double = 0.0
    var guionSum: Double = 0.0
    var count: Int = 0
}
