package org.raterr.usecases.tvshow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("tv_shows")
data class TvShow(
    @Id
    val tmdbId: Int,
    val name: String,
    val originalName: String? = null,
    val overview: String? = null,
    val firstAirDate: String? = null,
    val firstAirYear: Int? = null,
    val posterPath: String? = null,
    val tmdbVoteAverage: Double? = null,
    val genres: String? = null
)
