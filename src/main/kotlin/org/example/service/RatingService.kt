package org.example.service

import org.example.db.Movies
import org.example.db.Ratings
import org.example.model.MovieDto
import org.example.model.MovieSuggestionDto
import org.example.model.RatingRequest
import org.example.model.RatingResult
import org.example.tmdb.TmdbClient
import org.example.tmdb.TmdbMovie
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class RatingService(private val tmdbClient: TmdbClient) {

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
        val tmdbMovies = tmdbClient.searchMovies(query)
        return tmdbMovies.map { tmdbMovie ->
            val movieRow = upsertMovie(tmdbMovie)
            buildMovieDto(movieRow)
        }
    }

    suspend fun getMovieByTmdbId(tmdbId: Int): MovieDto {
        val localMovie = transaction {
            Movies.selectAll().where { Movies.tmdbId eq tmdbId }.singleOrNull()
        }

        val movieRow = localMovie ?: run {
            val tmdbMovie = tmdbClient.movieDetails(tmdbId)
            upsertMovie(tmdbMovie)
        }

        return buildMovieDto(movieRow)
    }

    suspend fun addRating(request: RatingRequest): RatingResult {
        validateRating(request)

        val movieRow = transaction {
            Movies.selectAll().where { Movies.tmdbId eq request.tmdbId }.singleOrNull()
        } ?: upsertMovie(tmdbClient.movieDetails(request.tmdbId))

        transaction {
            val existingRating = Ratings.selectAll().where { Ratings.movieId eq movieRow[Movies.id] }.singleOrNull()
            require(existingRating == null) {
                "Ya existe una valoracion para esta pelicula. Eliminala desde Tops antes de crear otra."
            }

            Ratings.insert {
                it[movieId] = movieRow[Movies.id]
                it[direccion] = request.direccion
                it[fotografia] = request.fotografia
                it[actuacion] = request.actuacion
                it[bandaSonora] = request.bandaSonora
                it[guion] = request.guion
                it[createdAtEpochMs] = System.currentTimeMillis()
            }
        }

        val stats = transaction { calculateStats(movieRow[Movies.id]) }
        return RatingResult(
            tmdbId = request.tmdbId,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    fun deleteRatingByTmdbId(tmdbId: Int): RatingResult {
        return transaction {
            val movieRow = Movies.selectAll().where { Movies.tmdbId eq tmdbId }.singleOrNull()
                ?: throw NoSuchElementException("Pelicula no encontrada")

            val movieInternalId = movieRow[Movies.id]
            val deletedCount = Ratings.deleteWhere { Ratings.movieId eq movieInternalId }

            if (deletedCount == 0) {
                throw NoSuchElementException("Valoracion no encontrada")
            }

            val stats = calculateStats(movieInternalId)
            RatingResult(
                tmdbId = tmdbId,
                averageScore = stats.averageScore,
                ratingsCount = stats.ratingsCount
            )
        }
    }

    private fun validateRating(request: RatingRequest) {
        listOf(
            "direccion" to request.direccion,
            "fotografia" to request.fotografia,
            "actuacion" to request.actuacion,
            "bandaSonora" to request.bandaSonora,
            "guion" to request.guion
        ).forEach { (field, value) ->
            require(value in 1..10) { "El campo $field debe estar entre 1 y 10" }
        }
    }

    private fun upsertMovie(tmdbMovie: TmdbMovie): ResultRow {
        val existing = transaction {
            Movies.selectAll().where { Movies.tmdbId eq tmdbMovie.id }.singleOrNull()
        }

        return if (existing != null) {
            transaction {
                Movies.update({ Movies.id eq existing[Movies.id] }) {
                    it[title] = tmdbMovie.title
                    it[originalTitle] = tmdbMovie.originalTitle
                    it[overview] = tmdbMovie.overview
                    it[releaseDate] = tmdbMovie.releaseDate
                    it[releaseYear] = tmdbMovie.releaseDate?.take(4)?.toIntOrNull()
                    it[posterPath] = tmdbMovie.posterPath
                    it[tmdbVoteAverage] = tmdbMovie.voteAverage
                }
                Movies.selectAll().where { Movies.id eq existing[Movies.id] }.single()
            }
        } else {
            val insertedId = transaction {
                Movies.insertAndGetId {
                    it[tmdbId] = tmdbMovie.id
                    it[title] = tmdbMovie.title
                    it[originalTitle] = tmdbMovie.originalTitle
                    it[overview] = tmdbMovie.overview
                    it[releaseDate] = tmdbMovie.releaseDate
                    it[releaseYear] = tmdbMovie.releaseDate?.take(4)?.toIntOrNull()
                    it[posterPath] = tmdbMovie.posterPath
                    it[tmdbVoteAverage] = tmdbMovie.voteAverage
                }
            }
            transaction {
                Movies.selectAll().where { Movies.id eq insertedId }.single()
            }
        }
    }

    private fun buildMovieDto(movieRow: ResultRow): MovieDto {
        val stats = transaction { calculateStats(movieRow[Movies.id]) }

        return MovieDto(
            tmdbId = movieRow[Movies.tmdbId],
            title = movieRow[Movies.title],
            overview = movieRow[Movies.overview],
            releaseDate = movieRow[Movies.releaseDate],
            releaseYear = movieRow[Movies.releaseYear],
            posterPath = movieRow[Movies.posterPath],
            tmdbVoteAverage = movieRow[Movies.tmdbVoteAverage],
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    private fun calculateStats(movieInternalId: EntityID<Int>): ScoreStats {
        val rows = Ratings.selectAll().where { Ratings.movieId eq movieInternalId }.toList()
        if (rows.isEmpty()) {
            return ScoreStats(averageScore = 0.0, ratingsCount = 0)
        }

        val avg = rows.map { row ->
            (
                row[Ratings.direccion] +
                    row[Ratings.fotografia] +
                    row[Ratings.actuacion] +
                    row[Ratings.bandaSonora] +
                    row[Ratings.guion]
                ) / 5.0
        }.average()

        return ScoreStats(averageScore = round2(avg), ratingsCount = rows.size)
    }

    private fun round2(value: Double): Double = kotlin.math.round(value * 100.0) / 100.0
}

private data class ScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

