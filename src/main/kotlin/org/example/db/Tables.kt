package org.example.db

import org.jetbrains.exposed.dao.id.IntIdTable

object Movies : IntIdTable("movie") {
    val tmdbId = integer("tmdb_id").uniqueIndex()
    val title = varchar("title", 255)
    val originalTitle = varchar("original_title", 255).nullable()
    val overview = text("overview").nullable()
    val releaseDate = varchar("release_date", 20).nullable()
    val releaseYear = integer("release_year").nullable()
    val posterPath = varchar("poster_path", 255).nullable()
    val tmdbVoteAverage = double("tmdb_vote_average").nullable()
}

object Ratings : IntIdTable("rating") {
    val movieId = reference("movie_id", Movies)
    val direccion = double("direccion")
    val fotografia = double("fotografia")
    val actuacion = double("actuacion")
    val bandaSonora = double("banda_sonora")
    val guion = double("guion")
    val createdAtEpochMs = long("created_at_epoch_ms")
}

