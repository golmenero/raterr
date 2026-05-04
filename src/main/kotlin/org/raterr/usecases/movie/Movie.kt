package org.raterr.usecases.movie

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("movies")
data class Movie(
    @Id
    val tmdbId: Int,
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val releaseDate: String? = null,
    val releaseYear: Int? = null,
    val posterPath: String? = null,
    val tmdbVoteAverage: Double? = null,
    val genres: String? = null
)
